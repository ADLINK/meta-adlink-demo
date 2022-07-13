DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "gitsm://github.com/po-cheng-adlink/lava-lab.git"
SRCBRANCH = "ci-box-builder"
SRCOPTIONS = ";protocol=https"
SRCOPTIONS_append_private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "0c6b04fde18d86a1a26ea94b26eca18d056f18d9"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

include docker-build.inc

DOCKER_COMPOSE_IMAGE = "lava-worker"

DEPENDS_append = " ipxe-bin"
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
    if tgtplatform in ('linux/arm64', 'linux/amd64'):
      with open("%s/ci-box-conf.yaml" % srcdir, "r+") as fd:
        import yaml
        ciboxconf = yaml.safe_load(fd)
        for slave in ciboxconf["slaves"]:
          if "rpi3" in slave['name'] or len(ciboxconf["slaves"]) == 1:
            slave['arch'] = tgtplatform
            slave['default_slave'] = True
            default = slave['name']
          else:
            if 'default_slave' in slave and slave['default_slave'] and default is not None:
              slave['default_slave'] = False
        fd.seek(0)
        fd.truncate()
        yaml.safe_dump(ciboxconf, fd, default_flow_style=False)
    else:
      bb.warn("TARGET_PLATFORM: %s not recognized" % tgtplatform)
}

do_configure_prepend () {
	cd ${S}
	./ci-box-gen.sh slaves
}

do_install_append () {
	# copy the ci-box-lava-worker/ to /home/adlink/ci-box-lava-worker
	install -d ${D}/home/adlink/
	cp -rf ${S}/ci-box-lava-worker ${D}/home/adlink/
	if [ -f ${S}/docker-compose.yml ]; then
		install -m 0644 ${S}/docker-compose.yml ${D}/home/adlink/docker-compose.yml
	fi
}

inherit deploy

do_deploy () {
	install -d ${DEPLOY_DIR_IMAGE}
	if which ${COMPRESSCMD} ; then
		if [ -f ${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} ]; then
			install -m 644 ${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} ${DEPLOY_DIR_IMAGE}/${PN}.${IMAGE_COMPRESS_TYPE}
		else
			bbfatal "${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} not found."
		 fi
	else
		if [ -f ${B}/${DOCKER_COMPOSE_IMAGE}.tar ]; then
			install -m 644 ${B}/${DOCKER_COMPOSE_IMAGE}.tar ${DEPLOY_DIR_IMAGE}/${PN}.tar
		else
			bbfatal "${B}/${DOCKER_COMPOSE_IMAGE}.tar not found."
		fi
	fi
}
addtask deploy before do_package after do_compile

FILES_${PN} += "/home/adlink/ci-box-lava-worker /home/adlink/docker-compose.yml"
