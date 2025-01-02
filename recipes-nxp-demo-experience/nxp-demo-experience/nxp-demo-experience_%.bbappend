FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-DemoPage-Modify-board-string-parsing-to-match-compat.patch"

include ${@bb.utils.contains('DISTRO_CODENAME', 'kirkstone', 'weston-userdir.inc', '', d)}

