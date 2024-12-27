include ${@bb.utils.contains('IMAGE_FEATURES', 'logo', 'customise-psplash.inc', '', d)}

SYSTEMD_AUTO_ENABLE = "${@bb.utils.contains('IMAGE_FEATURES', 'resize', 'disable', 'enable', d)}"

