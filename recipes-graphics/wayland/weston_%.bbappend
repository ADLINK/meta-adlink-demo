FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

WESTON_BACKGROUND_IMAGE ?= "adlink.jpg"

SRC_URI += "file://${WESTON_BACKGROUND_IMAGE}"

do_install:append() {
   install ${WORKDIR}/${WESTON_BACKGROUND_IMAGE} ${D}${datadir}/weston
}

FILES:${PN} += "${datadir/weston}"
