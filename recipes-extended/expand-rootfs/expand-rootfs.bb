#
# Utilize cloud-utils to expand rootfs partition
#
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://standby.png"

RDEPENDS:${PN} = " \
  cloud-utils \
  fbida \
  e2fsprogs \
"

do_install() {
    install -d ${D}${datadir}/adlink
    install -m 0644 ${WORKDIR}/standby.png ${D}${datadir}/adlink
}

#
# scripts to run ontarget at first boot
#
# NOTE: from psplash recipe
# SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'psplash-start.service psplash-systemd.service', '', d)}"
# so enable psplash-start.service and psplash-systemd.service to have splash screen on next reboot
#
pkg_postinst_ontarget:${PN} () {
if systemctl >/dev/null 2>/dev/null; then
	# find and prune path of root device
	ROOT_DEVICE=$(realpath $(findmnt / -o source -n))
	# get the partition number and type
	ROOT_PART_NAME=${ROOT_DEVICE##*/}
	DEVICE_NAME=${ROOT_PART_NAME%p*}
	MMC_DEV="/dev/${DEVICE_NAME}"
	PART_ENTRY_NUMBER=$(cat "/sys/block/${DEVICE_NAME}/${ROOT_PART_NAME}/partition")
	# gui notice
	NOTIFICATION="/usr/share/adlink/standby.png"
	export DISPLAY=:0
	# show standby notificationn image
	fbi -1 -noverbose ${NOTIFICATION} < /dev/zero &
	# grow rootfs partition
	echo 'Resizing rootfs partition on running storage at first boot.....'
	growpart "${MMC_DEV}" "${PART_ENTRY_NUMBER}"
	resize2fs "${ROOT_DEVICE}"
	echo 'Resizing task completes. Please reboot.....'
	systemctl enable psplash-start.service
	systemctl enable psplash-systemd.service
	systemctl reboot
fi
}

FILES:${PN} = "${datadir}/adlink/standby.png"
