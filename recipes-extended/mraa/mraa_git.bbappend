FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

EXTRA_SRC_PATCHES ?= ""
EXTRA_SRC_PATCHES:lec-imx8mp = "\
	file://0001-LEC-iMX8MP-Add-board-file-changes-for-imx8mp.patch \
	file://0002-Added-Test-applications-support.patch \
"

SRC_URI += "${EXTRA_SRC_PATCHES}"

do_install:append() {
	install -d ${D}/usr/share/mraa
	cp -r ${B}/examples/ ${D}/usr/share/mraa
}
