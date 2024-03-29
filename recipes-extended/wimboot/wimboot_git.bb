DESCRIPTION = "Open source wim boot firmware"
HOMEPAGE = "https://ipxe.org/wimboot"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

# syslinux has this restriction
COMPATIBLE_HOST:class-target = '(x86_64|i.86).*-(linux|freebsd.*)'

DEPENDS = "binutils-native perl-native syslinux mtools-native cdrtools-native xz gcab-native wimboot-native"

SRCREV = "28bfd5fec851777e08452fa6e335d9ed30a6fc04"
PV = "2.7.5+git${SRCPV}"
PR = "r0"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = " \
    git://github.com/ipxe/wimboot.git;protocol=https;branch=master \
    "

SECURITY_CFLAGS = ""
DEBUG_FLAGS = ""

EXTRA_OEMAKE = ' \
    ISOLINUX_BIN="${STAGING_DIR_TARGET}/usr/share/syslinux/isolinux.bin" \
    CROSS_COMPILE="${TARGET_PREFIX}" \
    EXTRA_HOST_CFLAGS="${BUILD_CFLAGS}" \
    EXTRA_HOST_LDFLAGS="${BUILD_LDFLAGS}" \
'

S = "${WORKDIR}/git/src"

do_compile() {
    # remove efireloc build and call efireloc directly from /usr/sbin in path from wimboot-native
    sed -e 's,efireloc Makefile,Makefile,g' -i ${S}/Makefile
    sed -e 's,\./efireloc,efireloc,g' -i ${S}/Makefile
    oe_runmake
}

do_install() {
    install -d ${D}/tftproot
    bbnote "Copy wimboot.{cab,i386*,x86_64*} to /tftproot"
    install -m 0644 ${S}/wimboot.cab ${D}/tftproot
    install -m 0755 ${S}/wimboot ${D}/tftproot
    install -m 0755 ${S}/wimboot.i386* ${D}/tftproot
    install -m 0755 ${S}/wimboot.x86_64* ${D}/tftproot
}

# image depending on wimboot binaries please add the following line
# do_image[mcdepends] = "mc:${MACHINE}:${BBMULTICONFIG}:wimboot:do_deploy"
inherit deploy
do_deploy () {
    install -d ${TOPDIR}/tmp/deploy/share
    if [ -f ${S}/wimboot.cab ]; then
        install -m 0644 ${S}/wimboot.cab ${TOPDIR}/tmp/deploy/share/
    fi
    if [ -f ${S}/wimboot ]; then
        install -m 0755 ${S}/wimboot ${TOPDIR}/tmp/deploy/share/
    fi
    if [ -f ${S}/wimboot.i386 -a -f ${S}/wimboot.i386.efi ]; then
        install -m 0755 ${S}/wimboot.i386 ${TOPDIR}/tmp/deploy/share/
        install -m 0755 ${S}/wimboot.i386.efi ${TOPDIR}/tmp/deploy/share/
    fi
    if [ -f ${S}/wimboot.x86_64 -a -f ${S}/wimboot.x86_64.efi ]; then
        install -m 0755 ${S}/wimboot.x86_64 ${TOPDIR}/tmp/deploy/share/
        install -m 0755 ${S}/wimboot.x86_64.efi ${TOPDIR}/tmp/deploy/share/
    fi
}
addtask deploy before do_package after do_install

FILES:${PN} = "/tftproot/"
TOOLCHAIN = "gcc"



DEPENDS:class-native += "binutils-native"

do_compile:class-native () {
    # BINUTILS_DIR     := /usr ---> BINUTILS_DIR := ${RECIPE_SYSROOT_NATIVE}/usr
    sed -e "s,BINUTILS_DIR.*:=.*,BINUTILS_DIR := ${RECIPE_SYSROOT_NATIVE}/usr,g" -i ${S}/Makefile
    oe_runmake efireloc
}

do_install:class-native () {
    # Install the wimboot efireloc binary
    rm -rf ${D}${sbindir}
    mkdir -p ${D}${sbindir}
    install ${S}/efireloc ${D}${sbindir}
}

FILES:${PN}:append:class-native = " ${sbindir}"
BBCLASSEXTEND = "native"

