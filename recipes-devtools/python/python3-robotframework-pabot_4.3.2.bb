SUMMARY = "Parallel test runner for Robot Framework."
HOMEPAGE = "https://pabot.org/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit pypi setuptools3

PYPI_PACKAGE = "${@d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1).replace('-', '_', 1)}"
#
# https://files.pythonhosted.org/packages/cd/3f/783d744e0d5a65d6aaa322440b09d53a24946abdedef936aa59b7334647f/robotframework_pabot-4.3.2.tar.gz
#
SRC_URI[sha256sum] = "7ff5036448457d9d92ccf7e724fdf71bea9607691e0381f141e19790402b8f2b"

