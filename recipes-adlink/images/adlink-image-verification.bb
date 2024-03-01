# Copyright 2021 ADLINK
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "\
 A image capable for hw/sw function verification.\
"
DESCRIPTION = "\
 Verification Image. This image contains everything needed to test the hw/sw system.\
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

include adlink-image-minimal.bb

IMAGE_INSTALL = "\
	${CORE_IMAGE_EXTRA_INSTALL} \
	${CORE_IMAGE_BASE_INSTALL} \
	packagegroup-adlink \
	kernel-modules \
	openflow \
	jq \
	sudo \
	"

# Select Image Features
IMAGE_FEATURES += " \
    debug-tweaks \
    tools-profile \
    tools-sdk \
    tools-debug \
    tools-testapps \
    package-management \
    splash \
    hwcodecs \
    ssh-server-openssh \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston', bb.utils.contains('DISTRO_FEATURES', 'x11', 'x11-base x11-sato', '', d), d)} \
"

IMAGE_LINGUAS = " "

CORE_IMAGE_EXTRA_INSTALL += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston-init', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11 wayland', 'weston-xwayland xterm', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'wifi', 'wpa-supplicant', '', d)} \
"

inherit extrausers
# clear password deprecated
# use 'mkpasswd -m sha-512 adlink -s 00000000'
EXTRA_USERS_PARAMS = " \
useradd -p '$6$00000000$/noRU5LR3VQ5X2EyOdcXvqd9bNRwM/PCAMxY8cvKXsjSqxezozESCRWuVphSrjGhvUjD4H9RmVBX4tcQHEQiH0' adlink; \
usermod -a -G sudo,users,plugdev adlink; \
"

# disk/partition related
IMAGE_FSTYPES:append = " wic.gz wic.bmap"
IMAGE_FSTYPES:remove = "wic wic.xz wic.zst wic.md5sum sdcard sdcard.bz2 sdcard.xz sdcard.md5sum"

