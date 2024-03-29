SUMMARY = "AMI scelnx - under NDA"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://SceLnx/64/AMI_Aptio_5.x_AMISCE_Release_Notes_NDA.pdf;md5=ab289f4a90f0f799134fb2890321e78e"

SRCSERVER = "git://GitLab.Adlinktech.com/EV/amitool-sce.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=http"
SRCOPTIONS:append:private = ";user=${ADLINK_USER}:${ADLINK_TOKEN}"
SRCREV = "bdb34111dfaa4e65d0a2d58f47e3b77e0aa6d17e"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

SRC_URI[sha256sum] = "2ba20f58aefb506e686a02f20088a8dbaf741bb6767f296ebfe8ebd59fdf8259"

S = "${WORKDIR}/git"

do_install:append() {
    chmod a+x ${S}/SceLnx/64/SCELNX_64
    install -d ${D}${sbindir}
    install -m 0755 ${S}/SceLnx/64/SCELNX_64 ${D}${sbindir}/scelnx_64
}

INSANE_SKIP:${PN} += "already-stripped"
SKIP_FILEDEPS = "1"

FILES:${PN} += "${sbindir}/"

RDEPENDS:${PN} += " kernel-module-amifldrv-mod"

COMPATIBLE_MACHINE = "(x86-64|intel-corei7-64)"
