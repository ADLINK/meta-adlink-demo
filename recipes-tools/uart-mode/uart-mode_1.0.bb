SUMMARY = "UART-Mode script to set uart to RS232 mode"
DESCRIPTION = "UART-Mode script"
SECTION = "app"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = " \
    file://uart-mode.sh \
    file://uart-mode.service \
    file://README.md \
    file://LICENSE \
"

S = "${WORKDIR}"

do_compile[noexec] = "1"

do_install () {
	# add the service to systemd
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
	install -m 0644 ${S}/uart-mode.service ${D}${systemd_unitdir}/system/
	# enable the service
	ln -sf ${systemd_unitdir}/system/uart-mode.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/uart-mode.service

	# install UART-Mode script
	install -d ${D}${bindir}
	install -m 0755 ${S}/uart-mode.sh ${D}${bindir}/
	if [ "${DISTRO_CODENAME}" = "scarthgap" ]; then
		bbnote "modify gpioset command to specify -c gpio_num"
		sed -e 's,gpioset,gpioset -t0 -c,g' -i ${D}${bindir}/uart-mode.sh
	fi
}

FILES:${PN} += "${bindir}"
FILES:${PN} += "${systemd_unitdir}/system/"
FILES:${PN} += "${sysconfdir}/systemd/system/multi-user.target.wants/"

RDEPENDS_EXTRA = "${@bb.utils.contains('DISTRO', 'imx-desktop-xwayland', '', 'libgpiod-tools', d)}"

RDEPENDS:${PN} += "bash ${RDEPENDS_EXTRA}"

