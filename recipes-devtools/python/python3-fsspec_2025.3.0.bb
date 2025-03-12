SUMMARY = "File-system specification"
DESCRIPTION = "A specification for pythonic filesystems"
HOMEPAGE = "https://github.com/fsspec/filesystem_spec"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

inherit pypi python_poetry_core

SRC_URI[sha256sum] = "a935fd1ea872591f2b5148907d103488fc523295e6c64b835cfad8c3eca44972"

DEPENDS += "python3-hatchling-native python3-hatch-vcs-native"
