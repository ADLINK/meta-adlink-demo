SUMMARY = "Script to set spread specturm for video signal on CCM_ANALOG_VIDEO_PLL1_SSCG_CTRL"
DESCRIPTION = "Service script to set VIDEO PLL1 PLL SSCG Control"
SECTION = "app"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://lvds-sscg.service \
"

S = "${WORKDIR}"

do_compile[noexec] = "1"

do_install () {
	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${S}/lvds-sscg.service ${D}${systemd_unitdir}/system/
	# enable the service
	ln -sf ${systemd_unitdir}/system/lvds-sscg.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/lvds-sscg.service
}

FILES:${PN} += "${systemd_unitdir}/system/"
FILES:${PN} += "${sysconfdir}/systemd/system/multi-user.target.wants/"

# memtool is in imx-test
RDEPENDS:${PN} += "bash imx-test"

