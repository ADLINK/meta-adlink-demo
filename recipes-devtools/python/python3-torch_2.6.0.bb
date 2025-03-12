SUMMARY = "Tensors and Dynamic neural networks in Python with strong GPU acceleration"
HOMEPAGE = "https://pytorch.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

inherit python3-dir setuptools3

WHL_PN="${@ d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
#
# https://files.pythonhosted.org/packages/01/d6/455ab3fbb2c61c71c8842753b566012e1ed111e7a4c82e0e1c20d0c76b62/torch-2.6.0-cp312-cp312-manylinux_2_28_aarch64.whl
#
SRC_URI = "https://files.pythonhosted.org/packages/01/d6/455ab3fbb2c61c71c8842753b566012e1ed111e7a4c82e0e1c20d0c76b62/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;downloadfilename=${WORKDIR}/dist/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;subdir=dist"
SRC_URI[sha256sum] = "b789069020c5588c70d5c2158ac0aa23fd24a028f34a8b4fcb8fcb4d7efcf5fb"

S = "${WORKDIR}"

INSANE_SKIP:${PN} += "already-stripped file-rdeps"

do_unpack[depends] += "unzip-native:do_populate_sysroot"

do_compile[noexec] = "1"

RDEPENDS:${PN} = "\
    python3-filelock \
    python3-typing-extensions (>=4.10.0) \
    python3-setuptools \
    python3-sympy \
    python3-networkx \
    python3-jinja2 \
    python3-fsspec \
    python3-mpmath \
    python3-markupsafe \
"
