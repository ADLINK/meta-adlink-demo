#
# Common configuration setting for the extra data partition to be bundled into image
#

# NOTE: docker-datapart or image-datapart must be in DEPENDS before include this file
python () {
    # We need docker-datapart or image-datapart in DEPENDS
    if not any(dep in d.getVar("DEPENDS", expand=True) for dep in ['docker-datapart', 'image-datapart']):
        bb.fatal("No docker-datapart or image-datapart in DEPENDS list\n")
}

# WIC template required configurations
WKS_FILE = "image-rootfs-data.wks.in"
WKS_FILE:raspberrypi3 = "sdimage-rootfs-data.wks.in"
WKS_FILE:raspberrypi3-64 = "sdimage-rootfs-data.wks.in"
WKS_FILE:intel-corei7-64 = "efi-bootdisk-microcode-data.wks.in"

# disk/image related
IMAGE_ROOTFS_ALIGNMENT ?= "4096"
WIC_PARTITION_TABLE_TYPE ?= "msdos"
WIC_FSTAB_BLKDEV ?= "mmcblk0"
WIC_IMAGE_BOOT_LOADER = "${@bb.utils.contains('EFI_PROVIDER', 'systemd-boot', 'systemd-boot', 'grub-efi', d)}"

# data partition related
WIC_PARTITION_SIZE ?= "8196"
WIC_PARTITION_TYPE ?= "ext4"
WIC_DATA_PARTITION_MOUNT_PATH = "${@bb.utils.contains('DEPENDS', 'docker-datapart', '/var/lib/docker', '/installer', d)}"
WIC_DATA_PARTITION_IMAGE = "${@bb.utils.contains("DEPENDS", "docker-datapart", "docker-data-partition.img", "${INSTALLER_TARGET_IMAGE}_image-datapart.${WIC_PARTITION_TYPE}", d)}"
WIC_DATA_PARTITION_LABEL = "${@bb.utils.contains('DEPENDS', 'docker-datapart', 'docker', 'images', d)}"

do_calc_wic () {
	if [ -f ${DEPLOY_DIR_IMAGE}/${WIC_DATA_PARTITION_IMAGE} ]; then
		FILE_SIZE=$(du -m ${DEPLOY_DIR_IMAGE}/${WIC_DATA_PARTITION_IMAGE} | awk '{print $1}')
		bbnote "${DEPLOY_DIR_IMAGE}/${WIC_DATA_PARTITION_IMAGE} = ${FILE_SIZE} MB..."
		if [ ${FILE_SIZE} -lt 4096 ]; then
			WIC_PARTITION_SIZE=4096
		elif [ ${FILE_SIZE} -lt 8192 ]; then
			WIC_PARTITION_SIZE=8192
		else
			WIC_PARTITION_SIZE=16384
		fi
		bbnote "WIC_PARTITION_SIZE = ${WIC_PARTITION_SIZE} MB..."
	else
		bbwarn "${DEPLOY_DIR_IMAGE}/${WIC_DATA_PARTITION_IMAGE} not found!"
	fi
}
addtask calc_wic before do_image_wic

