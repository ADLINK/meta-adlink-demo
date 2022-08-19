SRCREV = "2acdc92994e7aca397b0d24b112e4973e82e0f91"

SRC_URI:remove = " \
    file://ipxe-intel-Avoid-spurious-compiler-warning-on-GCC-10.patch \
    file://ipxe-golan-Add-explicit-type-casts-for-nodnic_queue_pair_.patch \
    file://build-be-explicit-about-fcommon-compiler-directive.patch \
"

DEPENDS += "coreutils-native"

do_post_patch () {
    sed -e "s|//#define.*CONSOLE_SERIAL|#define\tCONSOLE_SERIAL|g" -i ${S}/config/console.h
}
addtask post_patch after do_patch before do_configure

IPXE_PLATFORM = '${@bb.utils.contains("BBMULTICONFIG", "genericx86", "bin-i386-efi/", \
bb.utils.contains("BBMULTICONFIG", "genericx86-64", "bin-x86_64-efi/", "", d), d)}'
EXTRA_OEMAKE:append = " ${IPXE_PLATFORM}intel.efi"

do_install() {
    install -d ${D}/tftproot
    install -m 0644 ${S}/${IPXE_PLATFORM}*.efi ${D}/tftproot
}

FILES:${PN} = "/tftproot/*.efi"


# image depending on ipxe binaries please add the following line
# do_image[mcdepends] = "mc:${MACHINE}:${BBMULTICONFIG}:ipxe:do_deploy"
inherit deploy
do_deploy () {
    install -d ${TOPDIR}/tmp/deploy/share
    install -m 0644 ${S}/${IPXE_PLATFORM}*.efi ${TOPDIR}/tmp/deploy/share/
}
addtask deploy before do_package after do_install
