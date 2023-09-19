SUMMARY  = "PassMark BurnInTest"
DESCRIPTION = "PC Reliability and Load Testing"
HOMEPAGE = "https://www.passmark.com/products/burnintest/index.php"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://BurnInTest_Linux_CLI_EULA.txt;md5=bb18a822cfcbfafffabc6b46b0302ee7"

SRC_TAR:arm = "bitlinux_arm.tar.gz;md5=01d56681a42c12fb8bf6edba730aaa83"
SRC_TAR:aarch64 = "bitlinux_arm.tar.gz;md5=01d56681a42c12fb8bf6edba730aaa83"
SRC_TAR:x86_64 = "bitlinux.tar.gz;md5=f9a62676f7788cff075bb4a1188cf87e"
SRC_URI = "https://www.passmark.com/downloads/${SRC_TAR}"
SRC_URI[sha256sum] = "23652af3bb70e1411449b66365c2b0645641a610f81517ec71b7363e202643ac"

SRC_DIR:arm = "burnintest_arm"
SRC_DIR:aarch64 = "burnintest_arm"
SRC_DIR:x86-64 = "burnintest"
S = "${WORKDIR}/${SRC_DIR}"

INSANE_SKIP:${PN} += " already-stripped ldflags arch file-rdeps"
do_package_qa[noexec] = "1"

RDEPS_LIBS:arm = " \
        glibc (>=2.17) \
        libusb1 (>=1.0) \
        alsa-oss (>=1.0.20) \
        ncurses (>=5.0) \
"
#       linux-imx (>=3.10)
RDEPS_LIBS:aarch64 = " \
        glibc (>=2.17) \
        libusb1 (>=1.0) \
        alsa-oss (>=1.0.20) \
        ncurses (>=5.0) \
"
#        linux-imx (>=3.10)
RDEPS_LIBS:x86-64 = " \
        glibc (>=2.25) \
        qtbase (>=5.9.4) \
        opengl-es-cts \
        libusb1 (>=1.0) \
        alsa-oss (>=1.1.4) \
        curl (>=7.54.1) \
"
#        linux-intel (>=4.16)
RDEPENDS:${PN} += "${RDEPS_LIBS}"

do_install:append() {
	install -d ${D}${sbindir}

	# burnintest
	cp -R --no-dereference --preserve=mode,links -v ${S} ${D}${sbindir}/burnintest
}

FILES:${PN} += " \
	${sbindir}/burnintest \
"

