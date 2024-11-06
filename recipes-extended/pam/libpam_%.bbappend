FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://samba"

do_install:append () {
	install -d ${D}/${sysconfdir}/pam.d/
	install -m 0644 ${WORKDIR}/samba ${D}/${sysconfdir}/pam.d/samba
}

FILES:${PN} += "${sysconfdir}/pam.d/"

