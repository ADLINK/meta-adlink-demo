SUMMARY = "Wi-Fi6 Driver for RealTek 8852B/8832B chipset"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_PATCHES ?= ""
SRC_PATCHES:aarch64 = "file://BT/0001-rtl8852be-bt-modify-for-arm64-kernel-usb-module-buil.patch"

# For Kernel 5.4 and later
SRC_URI += "\
        file://BT/20250515_LINUX_BT_DRIVER_RTL8852B_COEX_v0707.tgz \
        ${SRC_PATCHES} \
"

S = "${WORKDIR}/20250515_LINUX_BT_DRIVER_RTL8852B_COEX_v0707"

inherit module

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile () {
    cd ${S}/usb/bluetooth_usb_driver
    oe_runmake all
}

do_install:prepend () {
    cd ${S}/usb/bluetooth_usb_driver
}

do_install:append () {
	install -d ${D}${nonarch_base_libdir}/firmware
	install -m 0644 ${S}/rtkbt-firmware/lib/firmware/rtl8852bu_config ${D}${nonarch_base_libdir}/firmware/
	install -m 0644 ${S}/rtkbt-firmware/lib/firmware/rtl8852bu_fw ${D}${nonarch_base_libdir}/firmware/
}

FILES:${PN} += "${nonarch_base_libdir}/firmware/"

KERNEL_MODULE_AUTOLOAD += " rtk_btusb"

