FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

WESTON_BACKGROUND_IMAGE ?= "adlink.jpg"

SRC_URI += "file://${WESTON_BACKGROUND_IMAGE}"

do_install:append() {
   install ${WORKDIR}/${WESTON_BACKGROUND_IMAGE} ${D}${datadir}/weston
}

SRC_URI += " \
	file://0001-ci-backend-vnc-update-to-Neat-VNC-0.7.0.patch \
"

PACKAGECONFIG:append = " vnc rdp"

FILES:${PN} += "${datadir/weston} ${sysconfdir}/pam.d/weston-remote-access"
