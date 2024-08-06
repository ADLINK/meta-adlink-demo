PYQT_MODULES:append = " QtDBus QtSvg ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'QtQuick QtWidgets QtQuickWidgets', '', d)}"

DEPENDS += "dbus qtsvg"

RDEPENDS:${PN}:append = " dbus qtsvg"
