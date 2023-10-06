SUMMARY = "Infineon Embedded Linux TPM Toolbox 2 (ELTT2) for TPM 2.0"
HOMEPAGE = "https://github.com/Infineon/eltt2"
SECTION = "console/utils"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

SRCSERVER = "git://github.com/Infineon/eltt2.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=https"
SRCREV = "3d55476179da9bd61c2df1ba1ef010afe27e7776"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"

EXTRA_OEMAKE = "'CC=${CC}'"
TARGET_CC_ARCH += "${LDFLAGS}"

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${B}/eltt2 ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/eltt2"

