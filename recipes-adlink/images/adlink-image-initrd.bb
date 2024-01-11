# Copyright 2024 Adlink
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "Small image capable of booting a device. The kernel includes \
the Minimal RAM-based Initial Root Filesystem (initramfs), which finds the \
first 'init' program more efficiently. The installer-scripts are included, and \
used to program the target board with demo images."

# reassigned to RDEPNDS in image.bbclass
PACKAGE_INSTALL = " \
	${VIRTUAL-RUNTIME_base-utils} \
	packagegroup-core-boot \
	kernel-modules \
	udev \
	mmc-utils \
	bmap-tools \
	installer-scripts \
	"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = "empty-root-password"
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"
LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES} ${INITRAMFS_FSTYPES}.u-boot"
IMAGE_FSTYPES:remove = "wic wic.gz wic.xz wic.md5sum ext4 sdcard.xz sdcard.md5sum wic wic.gz wic.md5sum"

inherit core-image

IMAGE_ROOTFS_SIZE = "0"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
IMAGE_OVERHEAD_FACTOR = "1.0"

XZ_INTEGRITY_CHECK = "crc32"

# Workaround /var/volatile for now
ROOTFS_POSTPROCESS_COMMAND += "rootfs_fixup_var_volatile ; "

rootfs_fixup_var_volatile () {
	install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
	install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}

