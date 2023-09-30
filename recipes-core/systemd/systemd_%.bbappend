WPA_SUPPLICANT_PACKAGE = "${@bb.utils.contains('MACHINE_FEATURES', 'wifi', 'wpa-supplicant', '', d)}"
RDEPENDS:${PN} += "${WPA_SUPPLICANT_PACKAGE}"

