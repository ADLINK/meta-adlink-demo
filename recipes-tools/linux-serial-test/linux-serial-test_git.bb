SUMMARY = "Linux Serial Test Application"
HOMEPAGE = "https://github.com/cbrake/linux-serial-test"
SECTION = "console/utils"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRCSERVER = "git://github.com/cbrake/linux-serial-test.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=https"
SRCREV = "2ee61484167eab846f7b7c565284d7c350d738d3"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${B}/linux-serial-test ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/linux-serial-test"

