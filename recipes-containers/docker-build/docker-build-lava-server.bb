DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "gitsm://github.com/po-cheng-adlink/lava-lab.git"
SRCBRANCH = "ci-box-valiguard"
SRCOPTIONS = ";protocol=https"
SRCOPTIONS:append:private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "c68e47e196960d786c7d3b48dc4b08cf20b13455"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

include docker-build.inc

DOCKER_COMPOSE_IMAGES ?= "lava-server"

do_configure[prefuncs] += "reconfigure_lava_server"
python reconfigure_lava_server() {
    default = None
    srcdir = d.getVar("S")
    tgtplatform = d.getVar("TARGET_PLATFORM")
    tgtimages = d.getVar("DOCKER_COMPOSE_IMAGES")
    if tgtplatform in ('linux/arm64', 'linux/amd64'):
      with open("%s/ci-box-conf.yaml" % srcdir, "r+") as fd:
        import yaml
        ciboxconf = yaml.safe_load(fd)
        for master in ciboxconf["masters"]:
          # ciboxconf['']['args']['healthcheck_url'] = ${HEALTHCHECK_URL}
          # modify build:args:healthcheck_url to point to appropriate healthcheck/file server
          for tgtimage in tgtimages.split():
            if tgtimage == master['name'] or len(ciboxconf["masters"]) == 1:
              master['arch'] = tgtplatform
              master['persistent_db'] = True
              default = master['version']
              break
        # dump back out to ci-box-conf.yaml
        fd.seek(0)
        fd.truncate()
        yaml.safe_dump(ciboxconf, fd, default_flow_style=False)
      with open("%s/configfile" % srcdir, "w") as envfd:
        envfd.seek(0)
        envfd.truncate()
        envfd.write("tgtplatform=%s/v8\n" % tgtplatform)
        envfd.write("version=%s\n" % default)
    else:
      bb.warn("TARGET_PLATFORM: %s not recognized" % tgtplatform)
}

do_configure:prepend () {
	sed -i 's/lava_master=remote_master/lava_master=remote_address/g' ${S}/ci-box-gen.py
	cd ${S}
	./ci-box-gen.sh slaves
}

DOCKER_BASE_IMAGES ?= "lavasoftware/lava-server:2025.04"
do_clear_base () {
	# We force the PATH to be the standard linux path in order to use the host's
	# docker daemon instead of the result of docker-native. This avoids version mismatches
	DOCKER=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)

	bbnote "Remove existing base images..."
	for dimg in ${DOCKER_BASE_IMAGES}; do
		if $DOCKER images --format '{{.Repository}}:{{.Tag}}' | grep -q "${dimg}"; then
			$DOCKER rmi ${dimg}
		fi
	done
}
addtask clear_base before do_configure after do_unpack

do_configure[postfuncs] += "pull_lava_server"

pull_lava_server () {
	# At this point we need Internet connectivity for pulling lava-dispatcher
	DOCKER=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)
	if [ -f ${S}/configfile ]; then
		for tgtimage in ${DOCKER_BASE_IMAGES}; do
			MASTER_PLATFORM=$(cat ${S}/configfile | grep tgtplatform | cut -d'=' -f2)
			MASTER_VERSION=$(cat ${S}/configfile | grep version | cut -d'=' -f2)
			case ${tgtimage} in
			*lava-server*)
				${DOCKER} pull --platform ${MASTER_PLATFORM} lavasoftware/lava-server:${MASTER_VERSION}
				;;
			*)
				${DOCKER} pull --platform ${MASTER_PLATFORM} ${tgtimage}
				;;
			esac
		done
	fi
}

do_install:append () {
	# copy the ci-box-lava-master/ to /home/adlink/ci-box-lava-master
	install -d ${D}/home/adlink/
	cp -rf ${S}/ci-box-lava-master ${D}/home/adlink/

	# copy docker-comppose.yml for docker-compose-service
	if [ -f ${S}/docker-compose.yml ]; then
		install -m 0644 ${S}/docker-compose.yml ${D}/home/adlink/docker-compose.yml
	fi

	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
}

RDEPENDS:${PN} += "python3-pyudev python3-pyserial python3-flask python3-psutil"

FILES:${PN} += "/home/adlink/ ${sysconfdir}/systemd/system/multi-user.target.wants/ ${systemd_unitdir}/system/"

