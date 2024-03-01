SUMMARY = "Power Button Counter setup script that utilises acpi event to count number of power button being pressed"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://powerbtn-counter.sh;md5=bb8d3df3a3d80b6d32ba729def6fb57d"

SRC_URI = "\
    file://powerbtn-counter.sh \
"

S = "${WORKDIR}"

do_install() {
    install -d -m 0755 ${D}/usr${base_bindir}
    install -m 0755 ${WORKDIR}/powerbtn-counter.sh ${D}/usr${base_bindir}/
}

FILES_${PN} += " /usr${base_bindir}/powerbtn-counter.sh"

RDEPENDS_${PN} += "acpid"

