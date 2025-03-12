SUMMARY = "image and video datasets and models for torch deep learning"
DESCRIPTION = "The torchvision package consists of popular datasets, model architectures, and common image transformations for computer vision."
HOMEPAGE = "https://pytorch.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"


inherit python3-dir setuptools3

WHL_PN="${@ d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
#
# https://files.pythonhosted.org/packages/bb/ea/03541ed901cdc30b934f897060d09bbf7a98466a08ad1680320f9ce0cbe0/torchvision-0.21.0-cp312-cp312-manylinux_2_28_aarch64.whl
#
SRC_URI = "https://files.pythonhosted.org/packages/bb/ea/03541ed901cdc30b934f897060d09bbf7a98466a08ad1680320f9ce0cbe0/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;downloadfilename=${WORKDIR}/dist/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;subdir=dist"
SRC_URI[sha256sum] = "5083a5b1fec2351bf5ea9900a741d54086db75baec4b1d21e39451e00977f1b1"

S = "${WORKDIR}"

INSANE_SKIP:${PN} += "already-stripped file-rdeps ldflags"

do_compile[noexec] = "1"

RDEPENDS:${PN} = "\
    python3-numpy \
    python3-torch \
    python3-pillow \
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

