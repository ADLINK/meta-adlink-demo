DESCRIPTION = "Open source network boot firmware"
HOMEPAGE = "https://ipxe.org/wimboot"
LICENSE = "GPLv2"

# syslinux has this restriction
COMPATIBLE_HOST:class-target = '(arm|aarch64).*-(linux|freebsd.*)'

do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install[mcdepends] = "mc:${MACHINE}:${BBMULTICONFIG}:wimboot:do_deploy"
do_install() {
    install -d ${D}/tftproot
    if [ -f ${DEPLOY_DIR}/share/wimboot.cab ]; then
        bbnote "Copy wimboot.cab to /tftproot"
        install -m 0644 ${DEPLOY_DIR}/share/wimboot.cab ${D}/tftproot
    else
        bbwarn "No wimboot.cab found in ${DEPLOY_DIR}/share, wimboot binary not packaged!"
    fi
    if [ -f ${DEPLOY_DIR}/share/wimboot ]; then
        bbnote "Copy wimboot to /tftproot"
        install -m 0755 ${DEPLOY_DIR}/share/wimboot ${D}/tftproot
    else
        bbwarn "No wimboot found in ${DEPLOY_DIR}/share, wimboot binary not packaged!"
    fi
    if [ -f ${DEPLOY_DIR}/share/wimboot.i386 -a -f ${DEPLOY_DIR}/share/wimboot.i386.efi ]; then
        bbnote "Copy wimboot.i386{.efi} to /tftproot"
        install -m 0755 ${DEPLOY_DIR}/share/wimboot.i386 ${D}/tftproot
        install -m 0755 ${DEPLOY_DIR}/share/wimboot.i386.efi ${D}/tftproot
    else
        bbwarn "No wimboot.i386{.efi} found in ${DEPLOY_DIR}/share, wimboot binary not packaged!"
    fi
    if [ -f ${DEPLOY_DIR}/share/wimboot.x86_64 -a -f ${DEPLOY_DIR}/share/wimboot.x86_64.efi ]; then
        bbnote "Copy wimboot.x86_64{.efi} to /tftproot"
        install -m 0755 ${DEPLOY_DIR}/share/wimboot.x86_64 ${D}/tftproot
        install -m 0755 ${DEPLOY_DIR}/share/wimboot.x86_64.efi ${D}/tftproot
    else
        bbwarn "No wimboot.x86_64{.efi} found in ${DEPLOY_DIR}/share, wimboot binary not packaged!"
    fi
}

FILES:${PN} = "/tftproot/*"
