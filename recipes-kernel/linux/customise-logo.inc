FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://logo.ppm"

do_copy_logo () {
    cp ${WORKDIR}/logo.ppm ${S}/drivers/video/logo/logo_linux_clut224.ppm
}
addtask copy_logo before do_configure after do_patch

