# Copyright 2021 ADLINK
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "\
 A small image just capable of allowing a device to boot with\
 docker container engine.\
"
DESCRIPTION = "\
 Docker OS Image. This image contains everything used\
 to start a docker container, e.g. arm64v8/ubuntu docker container.\
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

IMAGE_INSTALL = "\
	${CORE_IMAGE_EXTRA_INSTALL} \
	${CORE_IMAGE_BASE_INSTALL} \
	docker-ce \
	python3-docker-compose \
	openflow \
	kernel-modules \
	git \
	jq \
	sudo \
	dnsmasq \
	iptables \
	wget \
	"

# Select Image Features
IMAGE_FEATURES += " \
    splash \
    hwcodecs \
    ssh-server-openssh \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', '', bb.utils.contains('DISTRO_FEATURES', 'x11', 'x11-base x11-sato', '', d), d)} \
"

IMAGE_LINGUAS = " "

CORE_IMAGE_EXTRA_INSTALL += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston-init', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11 wayland', 'weston-xwayland xterm', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'wifi', 'wpa-supplicant', '', d)} \
"

CORE_IMAGE_EXTRA_INSTALL_append_raspberrypi3-64 = " raspi-gpio rpi-gpio ipxe-bin"

# raw image setting
IMAGE_ROOTFS_SIZE ?= "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "4096"
IMAGE_ROOTFS_EXTRA_SPACE_append = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' + 4096', '', d)}"

# adlink-image-docker-os WIC template required configurations
WKS_FILE ?= "image-rootfs-data.wks.in"
WKS_FILE_raspberrypi3 = "sdimage-rootfs-data.wks.in"
WIC_FSTAB_BLKDEV ?= "mmcblk0"
IMAGE_ROOTFS_ALIGNMENT ?= "4096"
WIC_DATA_PARTITION_MOUNT_PATH ?= "/var/lib/docker"
WIC_DATA_PARTITION_IMAGE ?= "docker-data-partition.img"
WIC_DATA_PARTITION_LABEL ?= "docker"
WIC_PARTITION_SIZE ?= "8196"
WIC_PARTITION_TABLE_TYPE ?= "msdos"

EXTRA_USERS_PARAMS = " \
useradd -P adlink adlink; \
usermod -a -G sudo,users,plugdev,docker adlink; \
"

inherit core-image extrausers

# NOTE: no need to include docker-datapart in IMAGE_INSTALL because
# docker-datapart only produce the extracted data partition for docker engine
# the data partition is flashed by WIC (with datafs.py modification)
DEPENDS_append = " docker-datapart"
MOUNT_PREFIX = ""

IMAGE_CMD_dataimg_prepend () {
  if [ -f ${DEPLOY_DIR_IMAGE}/${TARGET_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} ]; then
    mkdir -p ${IMAGE_ROOTFS}${MOUNT_PREFIX}${DOCKER_PARTITION_MOUNT_PATH}
    bbwarn "Extract ${TARGET_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} to ${IMAGE_ROOTFS}${MOUNT_PREFIX}${DOCKER_PARTITION_MOUNT_PATH}, please ensure docker.service start with --data-root set to ${MOUNT_PREFIX}${DOCKER_PARTITION_MOUNT_PATH} directory"
    tar zxf ${DEPLOY_DIR_IMAGE}/${TARGET_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} -C ${IMAGE_ROOTFS}${MOUNT_PREFIX}${DOCKER_PARTITION_MOUNT_PATH} .
  fi
}


ROOTFS_POSTPROCESS_COMMAND += " rpi_ipv4forward_sysctl_config ; "

rpi_ipv4forward_sysctl_config() {
  if ${@bb.utils.contains_any('MACHINE', 'raspberrypi3-64 raspberrypi3', 'true', 'false', d)} ; then
    # systemd sysctl config
    test -d ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d && \
        echo "net.ipv4.ip_forward = 1" > ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d/rpi-ipv4-forward.conf

    # sysv sysctl config
    IMAGE_SYSCTL_CONF="${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf"
    test -e ${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf && \
        sed -e "/net.ipv4.ip_forward/d" -i ${IMAGE_SYSCTL_CONF}
    echo "net.ipv4.ip_forward = 1" >> ${IMAGE_SYSCTL_CONF}
  fi
}
