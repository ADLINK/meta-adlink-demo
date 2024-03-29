FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}/sp2-imx8mp:"

SRC_URI += "\
  file://sgtl5000.state \
"

do_install:append:sp2-imx8mp() {
    install -d ${D}/${localstatedir}/lib/alsa
    install -m 0644 ${WORKDIR}/sgtl5000.state ${D}${localstatedir}/lib/alsa/asound.state
}
