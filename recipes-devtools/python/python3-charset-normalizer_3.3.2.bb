SUMMARY = "A library that helps you read text from an unknown charset encoding."
HOMEPAGE = "https://github.com/jawah/charset_normalizer"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI[sha256sum] = "f30c3cb33b24454a82faecaf01b19c18562b1e89558fb6c56de4d9118a032fd5"

inherit pypi setuptools3

RDEPENDS:${PN} += " \
        ${PYTHON_PN}-core \
        ${PYTHON_PN}-logging \
        ${PYTHON_PN}-codecs \
        ${PYTHON_PN}-json \
"

