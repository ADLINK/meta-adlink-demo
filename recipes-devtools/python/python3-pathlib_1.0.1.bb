SUMMARY = "This backport module isn’t maintained anymore. If you want to report issues or contribute patches, please consider the pathlib2 project instead"
HOMEPAGE = "https://pathlib.readthedocs.io/en/pep428/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit pypi setuptools3

SRC_URI[sha256sum] = "6940718dfc3eff4258203ad5021090933e5c04707d5ca8cc9e73c94a7894ea9f"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-patch-update-setup-for-setuptools3-build.patch"

do_install () {
        python_pep517_do_bootstrap_install
}
