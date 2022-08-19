DESCRIPTION = "Open source network boot firmware"
HOMEPAGE = "http://ipxe.org"
LICENSE = "GPLv2"

# syslinux has this restriction
COMPATIBLE_HOST_class-target = '(arm|aarch64).*-(linux|freebsd.*)'

do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install[mcdepends] = "mc:${MACHINE}:${BBMULTICONFIG}:ipxe:do_deploy"
do_install() {
    install -d ${D}/tftproot
    if [ -f ${DEPLOY_DIR}/share/intel.efi ]; then
        bbnote "Copy intel.efi to /tftproot"
        install -m 0644 ${DEPLOY_DIR}/share/intel.efi ${D}/tftproot
    else
        bbwarn "No intel.efi found in ${DEPLOY_DIR}/share, ipxe binary not packaged!"
    fi
}

FILES:${PN} = "/tftproot/*"
