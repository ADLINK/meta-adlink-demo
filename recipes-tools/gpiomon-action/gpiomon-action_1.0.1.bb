SUMMARY  = "Adlink gpiomon-action Python Scripts"
DESCRIPTION = "Python scripts to run on target board to monitor gpio input and perform specified actions"
HOMEPAGE = ""
LICENSE = "LGPLv2.1-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-only;md5=1a6d268fd218675ffea8be556788b780"

SRC_URI = " \
file://gpiomon-action.py \
file://usb-otg-mux.service \
"

S = "${WORKDIR}"

RDEPENDS:${PN} += " \
	python3-gpiod \
	"

do_install:append() {
	# gpiomon-action.py
	install -d ${D}${sbindir}
	install -m 755 ${S}/gpiomon-action.py ${D}${sbindir}/gpiomon-action.py

	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${S}/usb-otg-mux.service ${D}${systemd_unitdir}/system/

	# enable the service
	ln -sf ${systemd_unitdir}/system/usb-otg-mux.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/usb-otg-mux.service
}

FILES:${PN} += " \
	${sbindir}/ \
	${systemd_unitdir}/system/ \
	${sysconfdir}/systemd/system/multi-user.target.wants/ \
"

