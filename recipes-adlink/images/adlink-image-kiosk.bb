SUMMARY = "A standard yocto image to run chromium browser in weston kiosk mode"

LICENSE = "MIT"

# add EXTRA_IMAGE_FEATURES += "kiosk-mode" to local.conf to enable weston.ini update and browser.service
python () {
    # We need kiosk-mode defined in IMAGE_FEATURES
    if not "kiosk-mode" in d.getVar('IMAGE_FEATURES', True):
        bb.warn("To build adlink-image-kiosk: Please add EXTRA_IMAGE_FEATURES += \"kiosk-mode\" to local.conf")
}

BASE_IMAGE ?= "imx-image-full"
BASE_IMAGE_INCLUDE_PATH ?= "../../../meta-imx/meta-sdk/dynamic-layers/qt6-layer/recipes-fsl/images/"
include ${BASE_IMAGE_INCLUDE_PATH}${BASE_IMAGE}.bb

CORE_IMAGE_EXTRA_INSTALL += "chromium-ozone-wayland"

