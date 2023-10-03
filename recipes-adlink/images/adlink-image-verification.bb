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

EXTRA_USERS_PARAMS = " \
useradd -p adlink adlink; \
usermod -a -G sudo,users,plugdev adlink; \
"

inherit extrausers

