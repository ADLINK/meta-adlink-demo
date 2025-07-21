FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://iptables.nat"
SRC_URI:raspberrypi3-64 += "file://iptables.rpi3"
SRC_URI:raspberrypi3 += "file://iptables.rpi3"

RULE_FILE = "iptables.nat"
RULE_FILE:raspberrypi3-64 = "iptables.rpi3"
RULE_FILE:raspberrypi3 = "iptables.rpi3"

do_replace() {
    cp -f ${WORKDIR}/${RULE_FILE} ${WORKDIR}/iptables.rules
}
addtask replace before do_configure after do_patch

