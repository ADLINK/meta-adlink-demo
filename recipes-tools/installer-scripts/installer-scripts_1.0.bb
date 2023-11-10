SUMMARY  = "Adlink Installer Scripts"
DESCRIPTION = "Bash scripts to run on target board to flash image to eMMC"
SECTION = "console/utils"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://installer.sh"

S = "${WORKDIR}"

GUI_DIALOG_CMD ?= "yad"

# required configurations: INSTALLER_BASE_IMAGE/INSTALLER_BASE_IMAGE_INCLUDE_PATH
python () {
    import os
    # check INSTALLER_BASE_IMAGE
    baseimg = d.getVar("INSTALLER_BASE_IMAGE", expand=True)
    if not baseimg:
        bb.fatal("Please specify INSTALLER_BASE_IMAGE to determine dependency...\n")
    else:
        if baseimg == "imx-image-desktop":
            d.setVar("GUI_DIALOG_CMD", "")
}

RDEPENDS:${PN} += " \
	bmap-tools \
	bash \
	${GUI_DIALOG_CMD} \
	"

do_install:append() {
	install -d ${D}${sbindir}

	# installer.sh
	install -m 755 ${S}/installer.sh ${D}${sbindir}/installer.sh
}

FILES:${PN} += "${sbindir}/"

