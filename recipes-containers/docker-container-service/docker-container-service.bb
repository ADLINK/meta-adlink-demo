SUMMARY = "Package a systemd docker-container.service for docker containers in the partition image (i.e. mounted to /var/lib/docker)"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI = "file://docker-container.service.template"

DOCKERHUB_REPOSITORY ?= "debian-buster-wayland"
DOCKERHUB_TAG ?= "latest"
DOCKERHUB_IMAGE ?= "glmark2-es2-wayland"

# setup dependency to docker-datapart's do_deploy task
do_install[depends] = "docker-datapart:do_deploy"
do_compile[noexec] = "1"

do_install () {
	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${WORKDIR}/docker-container.service.template ${D}${systemd_unitdir}/system/docker-container.service
	sed -e "s|\@DOCKERHUB_REPOSITORY\@|${DOCKERHUB_REPOSITORY}|g" -i ${D}${systemd_unitdir}/system/docker-container.service
	sed -e "s|\@DOCKERHUB_TAG\@|${DOCKERHUB_TAG}|g" -i ${D}${systemd_unitdir}/system/docker-container.service
	sed -e "s|\@DOCKERHUB_IMAGE\@|${DOCKERHUB_IMAGE}|g" -i ${D}${systemd_unitdir}/system/docker-container.service
	# enable the service
	ln -sf ${systemd_unitdir}/system/docker-container.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/docker-container.service
}

FILES_${PN} += "/lib/systemd/system/"

RDEPENDS_${PN} += "systemd-container"
