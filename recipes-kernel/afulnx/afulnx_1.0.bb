SUMMARY = "AMI afulnx module - under NDA"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://AfuLnx/64/AMI_Aptio_5.x_AFU_Release_Notes_NDA.pdf;md5=9f0f1d33fbf602f3a2f105c417dbc8cb"

SRCSERVER = "git://GitLab.Adlinktech.com/EV/amitool-afu.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=http"
SRCOPTIONS:append:private = ";user=${ADLINK_USER}:${ADLINK_TOKEN}"
SRCREV = "8fd3893ef08f328271acd6d975161ff746835f98"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

SRC_URI[sha256sum] = "2ba20f58aefb506e686a02f20088a8dbaf741bb6767f296ebfe8ebd59fdf8259"

S = "${WORKDIR}/git"

do_unpack[depends] += "unzip-native:do_populate_sysroot"

do_unpack:append () {
    bb.build.exec_func('unzip_file', d)
}

unzip_file () {
    cd ${S}
    ${bindir}/env unzip ${S}/AfuLnx/64/AfuLnx64.zip
}

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${S}/AfuLnx64/afulnx_64 ${D}${sbindir}/
}

INSANE_SKIP:${PN} += "already-stripped"
SKIP_FILEDEPS = "1"

FILES:${PN} += "${sbindir}/"

RDEPENDS:${PN} += " kernel-module-amifldrv-mod"

COMPATIBLE_MACHINE = "(x86-64|intel-corei7-64)"
