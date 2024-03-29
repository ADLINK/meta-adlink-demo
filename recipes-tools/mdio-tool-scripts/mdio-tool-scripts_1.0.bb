SUMMARY = "mdio-tool-scripts - use mdio-tool to test eth0/eth1"
SECTION = "network"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://eth0/eth0_100M_test_mode.sh \
    file://eth0/eth0_giga_test_mode_2.sh \
    file://eth0/stop_eth0_100M_test_mode.sh \
    file://eth0/eth0_giga_test_mode_1.sh \
    file://eth0/eth0_giga_test_mode_4.sh \
    file://eth0/stop_eth0_giga_test_mode_all.sh \
    file://eth1/eth1_100M_test_mode.sh \
    file://eth1/eth1_giga_test_mode_2.sh \
    file://eth1/stop_eth1_100M_test_mode.sh \
    file://eth1/eth1_giga_test_mode_1.sh \
    file://eth1/eth1_giga_test_mode_4.sh \
    file://eth1/stop_eth1_giga_test_mode_all.sh \
"

RDEPENDS:${PN} += "mdio-tool"

S = "${WORKDIR}"

do_install:append () {
    install -d ${D}${sbindir}
    install -m 0755 eth0/eth0_100M_test_mode.sh ${D}${sbindir}/
    install -m 0755 eth0/eth0_giga_test_mode_2.sh ${D}${sbindir}/
    install -m 0755 eth0/stop_eth0_100M_test_mode.sh ${D}${sbindir}/
    install -m 0755 eth0/eth0_giga_test_mode_1.sh ${D}${sbindir}/
    install -m 0755 eth0/eth0_giga_test_mode_4.sh ${D}${sbindir}/
    install -m 0755 eth0/stop_eth0_giga_test_mode_all.sh ${D}${sbindir}/
    install -m 0755 eth1/eth1_100M_test_mode.sh ${D}${sbindir}/
    install -m 0755 eth1/eth1_giga_test_mode_2.sh ${D}${sbindir}/
    install -m 0755 eth1/stop_eth1_100M_test_mode.sh ${D}${sbindir}/
    install -m 0755 eth1/eth1_giga_test_mode_1.sh ${D}${sbindir}/
    install -m 0755 eth1/eth1_giga_test_mode_4.sh ${D}${sbindir}/
    install -m 0755 eth1/stop_eth1_giga_test_mode_all.sh ${D}${sbindir}/
}

FILES:${PN} += "${sbindir}/"

