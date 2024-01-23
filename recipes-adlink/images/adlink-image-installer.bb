# Copyright 2021 ADLINK
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "An image extension to package target emmc image in a separate partition"
DESCRIPTION = "This installer image extends original image with separate partition storing the original target wic image/bmap file for emmc flashing."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

INSTALLER_BASE_IMAGE ??= "core-image-minimal"
INSTALLER_BASE_IMAGE_INCLUDE_PATH ??= "../../../poky/meta/recipes-core/images/"

# required configurations: INSTALLER_TARGET_IMAGE, INSTALLER_BASE_IMAGE/INSTALLER_BASE_IMAGE_INCLUDE_PATH, and WIC_PARTITION_TYPE
python () {
    # We need INSTALLER_TARGET_IMAGE, and WIC_PARTITION_TYPE specified
    import os
    tgtimg = d.getVar("INSTALLER_TARGET_IMAGE", expand=True)
    if not tgtimg:
        bb.fatal("Please specify INSTALLER_TARGET_IMAGE to be packaged as a partition image file...\n")
    # check INSTALLER_BASE_IMAGE_INCLUDE_PATH, and INSTALLER_BASE_IMAGE
    thisdir = d.getVar("THISDIR", expand=True)
    basedir = d.getVar("INSTALLER_BASE_IMAGE_INCLUDE_PATH", expand=True)
    baseimg = d.getVar("INSTALLER_BASE_IMAGE", expand=True)
    if not os.path.exists("{}/{}{}.bb".format(thisdir, basedir, baseimg)):
        bb.fatal("Could not find specified {}/{}{}.bb...\n".format(thisdir, basedir, baseimg))
    else:
        if baseimg == "imx-image-desktop":
            d.setVar("GUI_DIALOG_CMD", "")
    tgtpart = d.getVar("WIC_PARTITION_TYPE", expand=True)
    if not tgtpart:
        bb.fatal("Please specify WIC_PARTITION_TYPE for the packaged partition image file...\n")
}

require ${INSTALLER_BASE_IMAGE_INCLUDE_PATH}${INSTALLER_BASE_IMAGE}.bb

# image related
GUI_DIALOG_CMD ?= "yad"
BOOTLOADER_TOOLS = ""
BOOTLOADER_TOOLS:arm = "u-boot-fw-utils"
BOOTLOADER_TOOLS:aarch64 = "u-boot-fw-utils"
IMAGE_INSTALL:append = " bmap-tools installer-scripts ${GUI_DIALOG_CMD} ${BOOTLOADER_TOOLS}"
IMAGE_LINGUAS = ""
IMAGE_FSTYPES:append = " wic.gz wic.bmap"
IMAGE_FSTYPES:remove = "wic wic.xz wic.zst wic.md5sum sdcard sdcard.bz2 sdcard.xz sdcard.md5sum"
IMAGE_BOOT_FILES:append = " \
	splash.bmp;splash.bmp \
	adlink-image-initrd-${MACHINE}.cpio.gz;rootfs.cpio.gz \
	adlink-image-initrd-${MACHINE}.cpio.gz.u-boot;rootfs.cpio.gz.u-boot \
"
IMAGE_ROOTFS_SIZE ?= "8192"
IMAGE_ROOTFS_EXTRA_SPACE:append = "${@bb.utils.contains("DISTRO_FEATURES", "systemd", " + 4096", "", d)}"

# partition related
WIC_BOOTLOADER_IMAGE = "${@bb.utils.contains('IMAGE_BOOTLOADER', 'imx-boot', 'imx-boot', bb.utils.contains_any("EFI_PROVIDER", "systemd-boot", "systemd-boot", "grub-efi", d), d)}"
WIC_BOOTLOADER_OFFSET = "${IMX_BOOT_SEEK}"
WKS_FILE = "image-rootfs-data.wks.in"
WIC_FSTAB_BLKDEV ?= "mmcblk0"
# IMAGE_ROOTFS_ALIGNMENT ?= "1"
WIC_DATA_PARTITION_MOUNT_PATH = "/installer"
WIC_DATA_PARTITION_IMAGE ?= "${INSTALLER_TARGET_IMAGE}_image-datapart.${WIC_PARTITION_TYPE}"
WIC_DATA_PARTITION_LABEL ?= "images"
WIC_PARTITION_SIZE ?= "4096"
WIC_PARTITION_TYPE ?= "ext4"
WIC_PARTITION_TABLE_TYPE ?= "msdos"

# NOTE: no need to include image-datapart in IMAGE_INSTALL because
# image-datapart only produce data partition image with packaged wic image.
# The data partition is flashed by datafs.py modification
DEPENDS:append = " image-datapart adlink-image-initrd"

do_calc_wic[depends] = "image-datapart:do_deploy"
do_calc_wic () {
	FILE_SIZE=$(du -m ${DEPLOY_DIR_IMAGE}/${INSTALLER_TARGET_IMAGE}_image-datapart.${WIC_PARTITION_TYPE} | awk '{print $1}')
	bbnote "${DEPLOY_DIR_IMAGE}/${INSTALLER_TARGET_IMAGE}_image-datapart.${INSTALLER_IMAGE_FSTYPE} = ${FILE_SIZE} MB..."
	if [ ${FILE_SIZE} -lt 4096 ]; then
		WIC_PARTITION_SIZE=4096
	elif [ ${FILE_SIZE} -lt 8192 ]; then
		WIC_PARTITION_SIZE=8192
	else
		WIC_PARTITION_SIZE=16384
	fi
	bbnote "WIC_PARTITION_SIZE = ${WIC_PARTITION_SIZE} MB..."
	WIC_DATA_PARTITION_IMAGE="${INSTALLER_TARGET_IMAGE}_image-datapart.${WIC_PARTITION_TYPE}"
}
addtask calc_wic before do_image_wic

