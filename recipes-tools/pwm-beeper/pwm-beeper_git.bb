SUMMARY = "Beep util for kernel pwm-beeper driver"
SECTION = "console/utils"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://beep.c"

S = "${WORKDIR}"

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile() {
        ${CC} ${WORKDIR}/beep.c -o ${WORKDIR}/beep
}

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${S}/beep ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/beep"

