SUMMARY = "Manipulation and analysis of geometric objects"
DESCRIPTION = "Manipulation and analysis of geometric objects in the Cartesian plane."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

inherit python3-dir setuptools3

WHL_PN="${@ d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
#
# https://files.pythonhosted.org/packages/fd/dd/b35d7891d25cc11066a70fb8d8169a6a7fca0735dd9b4d563a84684969a3/shapely-2.0.7-cp312-cp312-manylinux_2_17_aarch64.manylinux2014_aarch64.whl
#
SRC_URI = "https://files.pythonhosted.org/packages/fd/dd/b35d7891d25cc11066a70fb8d8169a6a7fca0735dd9b4d563a84684969a3/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_17_${TARGET_ARCH}.manylinux2014_${TARGET_ARCH}.whl;downloadfilename=${WORKDIR}/dist/${WHL_PN}-${PV}-cp312-cp312-manylinux_2_17_${TARGET_ARCH}.manylinux2014_${TARGET_ARCH}.whl;subdir=dist"
SRC_URI[sha256sum] = "73c9ae8cf443187d784d57202199bf9fd2d4bb7d5521fe8926ba40db1bc33e8e"

S = "${WORKDIR}"

do_compile[noexec] = "1"

RDEPENDS:${PN} = "\
    python3-numpy \
"


