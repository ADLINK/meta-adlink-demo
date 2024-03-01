UBUNTU_INSTALLED_PACKAGES = "${@bb.utils.contains_any('UBUNTU_TARGET_VERSION', '22.04.1 20.04.3 18.04.3 18.04.3 16.04.5', 'stress-ng', '', d)}"
RDEPENDS:${PN}:remove = "${UBUNTU_INSTALLED_PACKAGES}"

