SUMMARY  = "Adlink GPIO udev rules"
DESCRIPTION = "Udev rules to set /dev/gpiochipX to MODE ug+rw and sudo group"
SECTION = "tools"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://99-gpio.rules"

do_install() {
	# add the service to /etc/udev/rules.d/
	install -d ${D}${sysconfdir}/udev/rules.d/
	install -m 0644 ${WORKDIR}/99-gpio.rules ${D}${sysconfdir}/udev/rules.d/
}

FILES:${PN} += "${sysconfdir}/udev/rules.d/"

RDEPENDS:${PN}:append = " bash libgpiod"

