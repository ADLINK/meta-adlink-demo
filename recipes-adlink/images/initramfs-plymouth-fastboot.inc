PACKAGE_INSTALL = "initramfs-framework-base initramfs-module-udev \
    initramfs-module-rootfs initramfs-module-debug \
    initramfs-module-plymouth ${VIRTUAL-RUNTIME_base-utils} base-passwd \
    initramfs-module-kmod"

SYSTEMD_DEFAULT_TARGET = "initrd.target"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = "splash"

# avoid circular dependencies
EXTRA_IMAGEDEPENDS = ""

inherit core-image nopackages

BAD_RECOMMENDATIONS += "busybox-syslog"

