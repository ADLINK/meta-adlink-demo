SUMMARY  = "Adlink MsgPacker Python Scripts"
DESCRIPTION = "Python scripts to run on target board to program and parse json configuration to eeprom"
HOMEPAGE = ""
LICENSE = "LGPLv2.1-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-only;md5=1a6d268fd218675ffea8be556788b780"

SRC_URI = "file://msgpacker.py"

S = "${WORKDIR}"

RDEPENDS:${PN} += " \
	python3-msgpack \
	python3-smbus2 \
	python3-json \
	"

do_install:append() {
	install -d ${D}${sbindir}

	# msgpacker.py
	install -m 755 ${S}/msgpacker.py ${D}${sbindir}/msgpacker.py
}

FILES:${PN} += " \
	${sbindir}/msgpacker.py \
"

