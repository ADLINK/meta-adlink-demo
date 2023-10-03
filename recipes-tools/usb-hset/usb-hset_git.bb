SUMMARY = "Linux example code that accompanies MCHP USB Smart hubs"
HOMEPAGE = "https://github.com/MicrochipTech/USB-Hub-Linux-Examples"
SECTION = "network"
LICENSE = "LGPLv2.1-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-only;md5=1a6d268fd218675ffea8be556788b780"

SRCSERVER = "git://github.com/MicrochipTech/USB-Hub-Linux-Examples.git"
SRCBRANCH = "master"
SRCOPTIONS = ";protocol=https"
SRCREV = "a2cb98cc7bf136b216536a8153beb8223b213024"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS} \
        file://0001-Compile-with-PRODUCT_ID_STR-for-fixed-PID-build.patch"

S = "${WORKDIR}/git"

DEPENDS += "libusb1"
RDEPENDS:${PN} += "libusb1"

EXTRA_OEMAKE = "'CC=${CC} -DPRODUCT_ID_STR=2514' 'INC=${STAGING_INCDIR}/libusb-1.0'"
TARGET_CC_ARCH += "${LDFLAGS}"

do_compile:prepend() {
        if [ -d ${S}/General\ USB\ Examples/USB\ High\ Speed\ Electrical\ Test ]; then
                cp -rf ${S}/General\ USB\ Examples/USB\ High\ Speed\ Electrical\ Test/* ${S}/
        fi
}

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 ${S}/USB_Linux_HSET ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/USB_Linux_HSET"
