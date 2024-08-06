SUMMARY = "tools required for testing"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "\
	file://eltt2 \
	file://edid-decode \
	file://mbw \
	file://mdio-tool \
	file://spidev_fdx \
	file://spidev_test \
	file://USB31_TX_COMPLIANCE \
	file://USBTest \
	file://UTest.sh \
"

SRC_URI:append:lec-imx8mp = " \
	file://set_mac_address.c \
	file://hwbom_id.sh \
"

SRC_URI:append:lec-imx8mm = " \
        file://uart_test.c \
"
S = "${WORKDIR}"

do_compile:lec-imx8mp() {
	${CC} ${WORKDIR}/set_mac_address.c -o ${WORKDIR}/set_mac_address
}

do_compile:lec-imx8mm() {
	${CC} ${WORKDIR}/uart_test.c -o ${WORKDIR}/uart_test
}
do_install() {
    install -d -m 0755 ${D}${bindir}
    install -m 0755 ${WORKDIR}/eltt2 ${D}${bindir}
    install -m 0755 ${WORKDIR}/edid-decode ${D}${bindir}
    install -m 0755 ${WORKDIR}/mbw ${D}${bindir}
    install -m 0755 ${WORKDIR}/mdio-tool ${D}${bindir}
    install -m 0755 ${WORKDIR}/spidev_fdx ${D}${bindir}
    install -m 0755 ${WORKDIR}/spidev_test ${D}${bindir}
    install -m 0755 ${WORKDIR}/USB31_TX_COMPLIANCE ${D}${bindir}
    install -m 0755 ${WORKDIR}/USBTest ${D}${bindir}
    install -m 0755 ${WORKDIR}/UTest.sh ${D}${bindir}
}
do_install:append:lec-imx8mm() {
    install -m 0755 ${WORKDIR}/uart_test ${D}${bindir}
}
 
do_install:append:lec-imx8mp() {
	install -m 0755 ${WORKDIR}/hwbom_id.sh ${D}${bindir}/
	install -m 0755 ${WORKDIR}/set_mac_address ${D}${bindir}/
}

do_package_qa[noexec] = "1"

FILES_${PN} += " ${bindir}"
INSANE_SKIP_${PN} = "already-stripped"

FILES_${PN}:append:lec-imx8mp = " ${bindir}/set_mac_address"
FILES_${PN}:append:lec-imx8mp = " ${bindir}/hwbom_id.sh"
FILES_${PN}:append:lec-imx8mm = " ${bindir}/uart_test"

RDEPENDS_${PN}:append:lec-imx8mp = "bash i2c-tools"

