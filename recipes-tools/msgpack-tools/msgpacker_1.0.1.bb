SUMMARY  = "Adlink MsgPacker Python Scripts"
DESCRIPTION = "Python scripts to run on target board to program and parse json configuration to eeprom"
HOMEPAGE = ""
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "file://msgpacker.py file://LICENSE"

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

