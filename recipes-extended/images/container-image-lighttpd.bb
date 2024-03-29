SUMMARY = "A small docker container image with lighttpd based on container-base.bb from meta-virtualization"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# reuse container-base.bb from meta-virtualization
require recipes-extended/images/container-base.bb

# Enough free space for a full image update
IMAGE_OVERHEAD_FACTOR = "2.3"

#
PACKAGE_ARCH = "${MACHINE_ARCH}"
ROOTFS_BOOTSTRAP_INSTALL = ""

#
IMAGE_COMPRESS_TYPE = "tar.gz"
IMAGE_TYPEDEP_container += "${IMAGE_COMPRESS_TYPE}"

#
# Default IMAGE_INSTALL from container-base.bb = base-files base-passwd netbase and CONTAINER_SHELL
# where CONTAINER_SHELL ?= "${@bb.utils.contains('PACKAGE_EXTRA_ARCHS', 'container-dummy-provides', 'container-dummy-provides', 'busybox', d)}"
#
# Note that busybox is required to satisfy /bin/sh requirement of lighttpd,
# and the access* modules need to be explicitly specified since RECOMMENDATIONS
# are disabled.
IMAGE_INSTALL += " \
	lighttpd \
	lighttpd-module-access \
	lighttpd-module-accesslog \
"

IMAGE_LINK_NAME = "${IMAGE_BASENAME}"
