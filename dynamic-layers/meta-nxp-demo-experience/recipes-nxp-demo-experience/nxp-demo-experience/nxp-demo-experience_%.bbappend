FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-DemoPage-Modify-board-string-parsing-to-match-compat.patch"

include ${@bb.utils.contains('DISTRO_CODENAME', 'kirkstone', 'weston-userdir.inc', '', d)}

require dynamic-layers/ostree-var-path.inc
OSTREE_TMPFILES_CONF:sota = "L+ /var/rootdirs/opt/gopoint-apps - - - - /var/local/opt/gopoint-apps"
