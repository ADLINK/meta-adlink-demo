SUMMARY = "Wi-Fi6 Driver for RealTek 8852B/8832B chipset"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_PATCHES ?= ""
SRC_PATCHES:aarch64 = "file://WIFI/0001-rtl8852be-modify-for-arm64-kernel-module-build.patch"

# For Kernel 5.4 and later
SRC_URI += "\
        file://WIFI/rtl8852BE_WiFi_linux_v1.19.17-245-gab1aab9f5.20250502_Certified_Module.tar.gz \
        ${SRC_PATCHES} \
"

S = "${WORKDIR}/rtl8852BE_WiFi_linux_v1.19.17-245-gab1aab9f5.20250502_Certified_Module"

inherit module

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile () {
    oe_runmake build
}

KERNEL_MODULE_AUTOLOAD += " 8852be"

