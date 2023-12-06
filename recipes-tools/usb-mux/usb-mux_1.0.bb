SUMMARY  = "Adlink Udev and Bash Scripts to Handle USB Muxing from USB ID kernel event (USB Role Switching)"
DESCRIPTION = "Udev and Bash scripts to run on target board to monitor kernel even and perform usb mux switching"
SECTION = "tools"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
	file://usb-mux.sh \
	file://10-usb-otg-mux.rules \
	file://usb-mux.service \
"

S = "${WORKDIR}"

do_install:append() {
	# gpiomon-action.py
	install -d ${D}${sbindir}
	install -m 755 ${S}/usb-mux.sh ${D}${sbindir}/usb-mux.sh

	# add the service to /etc/udev/rules.d/
	install -d ${D}${sysconfdir}/udev/rules.d/
	install -m 0644 ${S}/10-usb-otg-mux.rules ${D}${sysconfdir}/udev/rules.d/

	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${S}/usb-mux.service ${D}${systemd_unitdir}/system/
	# enable the service
	ln -sf ${systemd_unitdir}/system/usb-mux.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/usb-mux.service
}

FILES:${PN} += " \
	${sbindir}/ \
	${sysconfdir}/udev/rules.d/ \
	${systemd_unitdir}/system/ \
	${sysconfdir}/systemd/system/multi-user.target.wants/ \
"

RDEPENDS:${PN}:append = " bash libgpiod"
