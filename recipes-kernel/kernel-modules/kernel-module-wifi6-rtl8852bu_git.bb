SUMMARY = "Wi-Fi6 Driver for RealTek 8852B/8832B chipset"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_PATCHES ?= ""
SRC_PATCHES:aarch64 = "file://0001-rtl8853bu-modify-for-arm64-kernel-module-build.patch"

# For Kernel 5.4 and later
SRC_URI += "\
	file://rtl8852BU_rtl8832BU_WiFi_linux_v1.19.14-127-gd73bd7b91.20240418.tar.gz \
	${SRC_PATCHES} \
"

S = "${WORKDIR}/rtl8852BU_rtl8832BU_WiFi_linux_v1.19.14-127-gd73bd7b91.20240418"

inherit module

EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile () {
    oe_runmake build
}

KERNEL_MODULE_AUTOLOAD += " 8852bu"

