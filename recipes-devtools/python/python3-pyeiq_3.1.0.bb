SUMMARY = "PyeIQ provide high level classes to allow the user execute eIQ applications and demos."
HOMEPAGE = "https://github.com/nxp-imx-support/pyeiq"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

inherit pypi setuptools3

SRC_URI[sha256sum] = "9b101861016840d1183f25728feba2448c1c677fef91c4b8367fcc29f92f29b3"


FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://eiq-cache-data_3.1.0.tar.gz"

do_install:append () {
	install -d ${D}/${bindir} ${D}/home/root/.cache/
	install -m 0755 ${B}/build/lib/eiq/apps/pyeiq_launcher/pyeiq.py ${D}${bindir}/pyeiq
	cp -rf ${WORKDIR}/eiq ${D}/home/root/.cache/
	chown root:root -R ${D}/home/root/.cache/eiq/
}

RDEPENDS:${PN} += "\
	python3-requests (>= 2.19) \
	python3-cffi (>= 1.10.0) \
	python3-certifi (>= 2017.4.17) \
	python3-idna (>= 2.5) \
	python3-charset-normalizer (>= 2) \
	python3-urllib3 (>= 1.21.1) \
	python3-numpy (>= 1.22.3) \
"

FILES:${PN} += "/home/root/.cache/ ${bindir}"

