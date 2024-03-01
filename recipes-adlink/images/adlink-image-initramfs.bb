# Simple initramfs image. Mostly used for live images.

SUMMARY = "A small image wrapped in initramfs just capable of allowing a device to boot."

include adlink-image-minimal.bb

DISTRO_FEATURES:remove = "systemd"

PACKAGE_INSTALL = "${IMAGE_INSTALL}"

export IMAGE_BASENAME = "${MLPREFIX}adlink-image-initramfs"
IMAGE_NAME_SUFFIX ?= ""

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"

# Use the same restriction as initramfs-module-install
COMPATIBLE_HOST = '(x86_64.*|i.86.*|arm.*|aarch64.*)-(linux.*|freebsd.*)'

