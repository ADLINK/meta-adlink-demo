SUMMARY = "Package a systemd docker-startup.service for docker containers in the partition image (i.e. mounted to /var/lib/docker)"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI = "file://docker-startup.service.template"

#
# DOCKER_RUN_IMAGES specify which docker container needs to be started
# DOCKER_RUN_IMAGES = "${@' '.join(list(img for img in d.getVar(DOCKER_COMPOSE_IMAGES).split() if img.startswith('lava-dispatcher')))}"
#
DOCKER_RUN_IMAGES ?= "${DOCKER_COMPOSE_IMAGES}"

DOCKER_COMPOSE_CMD ?= "/usr/bin/docker-compose"
DOCKER_COMPOSE_FILE ?= "-f docker-compose.yml"
DOCKER_COMPOSE_START_ARGS ?= "up -d"
DOCKER_COMPOSE_STOP_ARGS ?= "stop"
DOCKER_EXTRA_STARTUP_SCRIPT ?= "/home/adlink/udev_reload.sh"

# setup dependency to docker-datapart's do_deploy task
do_install[depends] = "docker-datapart:do_deploy"

do_compile[noexec] = "1"

do_install () {
	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${WORKDIR}/docker-startup.service.template ${D}${systemd_unitdir}/system/docker-startup.service

	# substitude template contents
	sed -e "s,\@DOCKER_COMPOSE_CMD\@,${DOCKER_COMPOSE_CMD},g" -i ${D}${systemd_unitdir}/system/docker-startup.service
	sed -e "s,\@DOCKER_COMPOSE_FILE\@,${DOCKER_COMPOSE_FILE},g" -i ${D}${systemd_unitdir}/system/docker-startup.service
	sed -e "s,\@DOCKER_COMPOSE_START_ARGS\@,${DOCKER_COMPOSE_START_ARGS},g" -i ${D}${systemd_unitdir}/system/docker-startup.service
	sed -e "s,\@DOCKER_COMPOSE_STOP_ARGS\@,${DOCKER_COMPOSE_STOP_ARGS},g" -i ${D}${systemd_unitdir}/system/docker-startup.service
	sed -e "s,\@DOCKER_RUN_IMAGES\@,${DOCKER_RUN_IMAGES},g" -i ${D}${systemd_unitdir}/system/docker-startup.service
	sed -e "s,\@DOCKER_EXTRA_STARTUP_SCRIPT\@,${DOCKER_EXTRA_STARTUP_SCRIPT},g" -i ${D}${systemd_unitdir}/system/docker-startup.service

	# enable the service
	ln -sf ${systemd_unitdir}/system/docker-startup.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/docker-startup.service
}

FILES:${PN} += "${sysconfdir}/systemd/system/multi-user.target.wants/ ${systemd_unitdir}/system/"
