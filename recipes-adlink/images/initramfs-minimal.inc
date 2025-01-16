include adlink-image-minimal.bb

DISTRO_FEATURES:remove = "systemd"

PACKAGE_INSTALL = "${IMAGE_INSTALL}"

