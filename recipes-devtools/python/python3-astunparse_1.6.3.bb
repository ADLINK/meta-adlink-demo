SUMMARY = "An AST unparser for Python"
DESCRIPTION = "This is a factored out version of unparse found in the Python source distribution; under Demo/parser in Python 2 and under Tools/parser in Python 3."
HOMEPAGE ="https://github.com/simonpercivall/astunparse"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI[sha256sum] = "5ad93a8456f0d084c3456d059fd9a92cce667963232cbf763eac3bc5b7940872"

inherit pypi python_poetry_core


