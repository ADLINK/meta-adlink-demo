RY = "An audio package for PyTorch"
DESCRIPTION = "The aim of torchaudio is to apply PyTorch to the audio domain. By supporting PyTorch, torchaudio follows the same philosophy of providing strong GPU acceleration, having a focus on trainable features through the autograd system, and having consistent style (tensor names and dimension names). Therefore, it is primarily a machine learning library and not a general signal processing library. The benefits of PyTorch can be seen in torchaudio through having all the computations be through PyTorch operations which makes it easy to use and feel like a natural extension."
HOMEPAGE = "https://github.com/pytorch/audio"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

inherit python3-dir setuptools3

WHL_PN="${@ d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
#
# https://files.pythonhosted.org/packages/f2/e7/0bcb2e33f4bdec69477344eccfe25c515b90496888095e99f837ea422089/torchaudio-2.6.0-cp312-cp312-manylinux_2_28_aarch64.whl
#
SRC_URI = "https://files.pythonhosted.org/packages/f2/e7/0bcb2e33f4bdec69477344eccfe25c515b90496888095e99f837ea422089/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;downloadfilename=${WORKDIR}/dist/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_28_${TARGET_ARCH}.whl;subdir=dist"
SRC_URI[sha256sum] = "6291d9507dc1d6b4ffe8843fbfb201e6c8270dd8c42ad70bb76226c0ebdcad56"

S = "${WORKDIR}"

INSANE_SKIP:${PN} += "file-rdeps"

do_compile[noexec] = "1"

RDEPENDS:${PN} = "\
    python3-torch \
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
