DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "gitsm://github.com/po-cheng-adlink/lava-lab.git"
SRCBRANCH = "ci-box-valiguard"
SRCOPTIONS = ";protocol=https"
SRCOPTIONS:append:private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "24b40e87df83f2443757bc55ce7255a09399a352"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

include docker-build.inc

DOCKER_COMPOSE_IMAGES ?= "lava-dispatcher"

DEPENDS:append = " ipxe-bin"
do_ipxe_fetch () {
    if [ -f ${DEPLOY_DIR}/share/intel.efi ]; then
        bbnote "Copy intel.efi to ci-box-lava-worker/configs/"
        install -m 0644 ${DEPLOY_DIR}/share/intel.efi ${S}/ci-box-lava-worker/configs/
    else
        bbwarn "No intel.efi found in ${DEPLOY_DIR}/share, ipxe binary not copied to lava-dispatcher!"
    fi
}
addtask ipxe_fetch before do_configure after do_unpack

do_configure[prefuncs] += "reconfigure_lava_dispatcher"
python reconfigure_lava_dispatcher() {
    default = None
    srcdir = d.getVar("S")
    tgtplatform = d.getVar("TARGET_PLATFORM")
    tgtimages =  d.getVar("DOCKER_COMPOSE_IMAGES")
    if tgtplatform in ('linux/arm64', 'linux/amd64'):
      with open("%s/ci-box-conf.yaml" % srcdir, "r+") as fd:
        import yaml
        ciboxconf = yaml.safe_load(fd)
        for slave in ciboxconf["slaves"]:
          # clear all default slave from ci-box-conf.yaml file
          if "default_slave" in slave:
            slave['default_slave'] = False
          # set defaul slave if DOCKER_COMPOSE_IMAGES match slave name or if only one slave
          for tgtimage in tgtimages.split():
            if tgtimage == slave['name'] or len(ciboxconf["slaves"]) == 1:
              slave['arch'] = tgtplatform
              slave['default_slave'] = True
              break
        # dump back out to ci-box-conf.yaml
        fd.seek(0)
        fd.truncate()
        yaml.safe_dump(ciboxconf, fd, default_flow_style=False)
    else:
      bb.warn("TARGET_PLATFORM: %s not recognized" % tgtplatform)
}

do_configure:prepend () {
	sed -i 's/lava_master=remote_master/lava_master=remote_address/g' ${S}/ci-box-gen.py
	cd ${S}
	./ci-box-gen.sh slaves
}

do_install:append () {
	# copy the ci-box-lava-worker/ to /home/adlink/ci-box-lava-worker
	install -d ${D}/home/adlink/
	cp -rf ${S}/ci-box-lava-worker ${D}/home/adlink/
	cp -rf ${S}/ci-box-avahi ${D}/home/adlink/
	# copy generated udev rules and scripts to reload udev for udev-forward-service
	sed -i 's|/home/jenkins.*/docker-udev-tools/|/home/adlink/docker-udev-tools/|g' ${S}/udev/99-lavaworker-udev.rules
	cp -rf ${S}/udev ${D}/home/adlink/
	sed -i 's|/home/jenkins.*/udev-forward|${systemd_unitdir}/system/udev-forward|g' ${S}/udev-forward.sh
	install -m 0755 ${S}/udev-forward.sh ${D}/home/adlink/
	install -m 0755 ${S}/udev_reload.sh ${D}/home/adlink/
	# copy docker-comppose.yml for docker-compose-service
	if [ -f ${S}/docker-compose.yml ]; then
		install -m 0644 ${S}/docker-compose.yml ${D}/home/adlink/docker-compose.yml
	fi
	# add/enable the udev-forward.service to systemd
	# replace syslog.LOG_DEBUG to 7 (from syslog.h) as yocto has no python3-syslog
	sed -i 's|import syslog||g' ${S}/docker-udev-tools/udev-forward.py
	sed -i 's|syslog.LOG_DEBUG|7|g' ${S}/docker-udev-tools/udev-forward.py
	cp -rf ${S}/docker-udev-tools ${D}/home/adlink/
	sed -i 's|After=multi-user.target|After=multi-user.target docker-compose.service|g' ${S}/udev-forward.service
	sed -i 's|/home/jenkins/.*udev-forward|/home/adlink/docker-udev-tools/udev-forward|g' ${S}/udev-forward.service
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${S}/udev-forward.service ${D}${systemd_unitdir}/system/udev-forward.service
	ln -sf ${systemd_unitdir}/system/udev-forward.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/udev-forward.service
	# copy generated avahi_services.sh
	if [ -f ${S}/avahi_services.sh ]; then
		install -m 0777 ${S}/avahi_services.sh ${D}/home/adlink/
	fi
}

inherit deploy

do_deploy () {
	install -d ${DEPLOY_DIR_IMAGE}
	for dimg in ${DOCKER_COMPOSE_IMAGES}; do
		if which ${COMPRESSCMD} ; then
			if [ -f ${B}/${dimg}.${IMAGE_COMPRESS_TYPE} ]; then
				install -m 644 ${B}/${dimg}.${IMAGE_COMPRESS_TYPE} ${DEPLOY_DIR_IMAGE}/docker-build-${dimg}.${IMAGE_COMPRESS_TYPE}
			else
				bbfatal "${B}/${dimg}.${IMAGE_COMPRESS_TYPE} not found."
			fi
		else
			if [ -f ${B}/${dimg}.tar ]; then
				install -m 644 ${B}/${dimg}.tar ${DEPLOY_DIR_IMAGE}/docker-build-${dimg}.tar
			else
				bbfatal "${B}/${dimg}.tar not found."
			fi
		fi
	done
}
addtask deploy before do_package after do_compile

RDEPENDS:${PN} += "python3-pyudev"

FILES:${PN} += "/home/adlink/ ${sysconfdir}/systemd/system/multi-user.target.wants/ ${systemd_unitdir}/system/"

