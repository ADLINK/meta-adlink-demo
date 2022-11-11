DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "gitsm://github.com/po-cheng-adlink/lava-lab.git"
SRCBRANCH = "ci-box-2022.03"
SRCOPTIONS = ";protocol=https"
SRCOPTIONS:append:private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "6a6a12812d7677d8fda1e9d6bfe226aecf56c3a3"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

include docker-build.inc

DOCKER_COMPOSE_IMAGE = "lava-worker"

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
    tgtimage =  d.getVar("DOCKER_COMPOSE_IMAGE")
    if tgtplatform in ('linux/arm64', 'linux/amd64'):
      with open("%s/ci-box-conf.yaml" % srcdir, "r+") as fd:
        import yaml
        ciboxconf = yaml.safe_load(fd)
        for slave in ciboxconf["slaves"]:
          # clear all default slave from ci-box-conf.yaml file
          if "default_slave" in slave:
            slave['default_slave'] = False
          # set defaul slave if DOCKER_COMPOSE_IMAGE match slave name or if only one slave
          if tgtimage == slave['name'] or len(ciboxconf["slaves"]) == 1:
            slave['arch'] = tgtplatform
            slave['default_slave'] = True
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

RDEPENDS:${PN} += "python3-pyudev"

FILES:${PN} += "/home/adlink/ ${sysconfdir}/systemd/system/multi-user.target.wants/ ${systemd_unitdir}/system/"

