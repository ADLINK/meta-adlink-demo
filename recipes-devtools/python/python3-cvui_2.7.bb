SUMMARY = "A (very) simple UI lib built on top of OpenCV drawing primitives"
DESCRIPTION = "A (very) simple UI lib built on top of OpenCV drawing primitives. \
Other UI libs, such as imgui, require a graphical backend (e.g. OpenGL) to work, \
so if you want to use imgui in a OpenCV app, you must make it OpenGL enabled, \
for instance. It is not the case with cvui, which uses only OpenCV drawing primitives \
to do all the rendering (no OpenGL or Qt required)."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit python3-dir python_pep517

WHL_BPN="${@ d.getVar('BPN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
WHL_BP="${@ d.getVar('BP').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"
WHL_PN="${@ d.getVar('PN').replace(d.getVar('PYTHON_PN')+'-', '', 1)}"

#
# download URL from PYPI:
# https://files.pythonhosted.org/packages/0e/44/63ba8fb1f6b0a537f04b3085ca75371a810b3aee9b269f72e9aa3ddf61d1/cvui-2.7-py3-none-any.whl
#
PYPI_DL_OPT = ";subdir=dist;unpack=false"
SRC_URI = "https://files.pythonhosted.org/packages/0e/44/63ba8fb1f6b0a537f04b3085ca75371a810b3aee9b269f72e9aa3ddf61d1/${WHL_BP}-py3-none-any.whl${PYPI_DL_OPT}"
SRC_URI[md5sum] = "9e224242030062a613d6fe9be2c35a52"
SRC_URI[sha256sum] = "6064be24794e4a244ea5cfc467b27b930aacf3985a2041417eba3fed133f6f6f"

DEPENDS += "unzip-native"

do_compile[noexec] = "1"

do_install() {
	python_pep517_do_bootstrap_install
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"

