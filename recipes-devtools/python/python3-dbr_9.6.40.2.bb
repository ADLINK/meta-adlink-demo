SUMMARY = "Barcode scanning SDK for Python"
DESCRIPTION = "The Python Barcode SDK is a wrapper for Dynamsoft C++ Barcode SDK. \
It comes with all the general features of Dynamsoft Barcode Reader, bringing \
convenience for Python developers."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI[sha256sum] = "b7e6c4be46e310b0797eba438c6a1a18c12db2ffa54281281ef1837386042409"

inherit pypi setuptools3

INSANE_SKIP:${PN} = "already-stripped"

