SUMMARY = "mdio-tool - read and write MII registers from ethernet physicals under linux."
DESCRIPTION = "This is tool to read and write MII registers from ethernet \
physicals under linux. It has been tested with Realtek and Marvell PHY's \
connected via PCIe and should work with all drivers implementing the mdio ioctls. \
mdio-tool comes with ABSOLUTELY NO WARRANTY; Use with care!"
HOMEPAGE = "https://github.com/PieVo/mdio-tool"
SECTION = "network"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e8c1458438ead3c34974bc0be3a03ed6"

SRCSERVER = "git://github.com/PieVo/mdio-tool.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=https"
SRCREV = "72bd5a915ff046a59ce4303c8de672e77622a86c"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${B}/mdio-tool ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/mdio-tool"

