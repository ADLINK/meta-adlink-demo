include weston-topbar-icon.inc
include ${@bb.utils.contains_any('DEPENDS', 'docker-datapart image-datapart', '../../../../recipes-adlink/images/datapart-conf.inc', '', d)}
include ${@bb.utils.contains('IMAGE_FEATURES', 'nat', '../../../../recipes-adlink/images/ip-forward.inc', '', d)}
CORE_IMAGE_EXTRA_INSTALL += "${@bb.utils.contains('IMAGE_FEATURES', 'resize', 'expand-rootfs', '', d)}"

