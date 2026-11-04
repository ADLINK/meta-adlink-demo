IMAGE_FEATURES:append = "${@bb.utils.contains('EXTRA_IMAGE_FEATURES', 'resize', ' resize', '', d)}"
PACKAGE_INSTALL:append = "${@bb.utils.contains('IMAGE_FEATURES', 'resize', ' parted e2fsprogs-resize2fs', '', d)}"
