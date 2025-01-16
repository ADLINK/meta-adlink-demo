# Simple initramfs image. Mostly used for live images.

SUMMARY = "A small image wrapped in initramfs just capable of allowing a device to boot."

export IMAGE_BASENAME = "${MLPREFIX}adlink-image-initramfs"
IMAGE_NAME_SUFFIX ?= ""
IMAGE_LINGUAS = ""

LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"

include ${@bb.utils.contains('IMAGE_FEATURES', 'fastboot', 'initramfs-plymouth-fastboot.inc', 'initramfs-minimal.inc', d)}

IMAGE_ROOTFS_SIZE = "8192"
NO_RECOMMENDATIONS = "1"
# Users will often ask for extra space in their rootfs by setting this
# globally.  Since this is a initramfs, we don't want to make it bigger
IMAGE_ROOTFS_EXTRA_SPACE = "0"
IMAGE_OVERHEAD_FACTOR = "1.0"

# Use the same restriction as initramfs-module-install
COMPATIBLE_HOST = '(x86_64.*|i.86.*|arm.*|aarch64.*)-(linux.*|freebsd.*)'

