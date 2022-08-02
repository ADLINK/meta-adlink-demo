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

EXTRA_OEMAKE += "KERNEL=${STAGING_KERNEL_DIR}"

inherit module

do_unpack[depends] += "unzip-native:do_populate_sysroot"

do_unpack:append () {
    bb.build.exec_func('unzip_file', d)
}

unzip_file () {
    cd ${S}
    ${bindir}/env unzip ${S}/AfuLnx/64/AfuLnx64.zip
    mv ${S}/AfuLnx64/* .
}

do_gendrv () {
    cd ${S}
    chmod a+x ${S}/afulnx_64
    ln -s ../amifldrv.tgz amifldrv.tgz
    bbwarn "Generating amifldrv source code to amifldrv.tgz..."
    ./afulnx_64 /GENDRV || :
    tar zxf ../amifldrv.tgz --strip-components 1
    # patch the Makefile
    chmod a+w Makefile
    sed "s,^OUTPUT.*,OUTPUT = ../amifldrv_mod.o,g" -i Makefile
    sed "s,mv amifldrv,cp amifldrv,g" -i Makefile
    sed "s,amifldrv_impl-objs.*,,g" -i Makefile
    sed "s,amifldrv_mod.o amifldrv_impl.o,amifldrv_mod.o,g" -i Makefile
}
addtask gendrv before do_configure after do_patch

do_install () {
    install -d ${D}${sbindir}
    install -m 0644 ${S}/../amifldrv_mod.o ${D}${sbindir}/
}

RPROVIDES:${PN} += "kernel-module-amifldrv-mod"

FILES:${PN} = "${sbindir}/"

COMPATIBLE_MACHINE = "(x86-64|intel-corei7-64)"
