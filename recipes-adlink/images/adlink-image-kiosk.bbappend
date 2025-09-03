include ../../dynamic-layers/meta-imx-sdk/recipes-fsl/images/weston-topbar-icon.inc
include ${@bb.utils.contains_any('DEPENDS', 'docker-datapart image-datapart', 'datapart-conf.inc', '', d)}
include ${@bb.utils.contains('IMAGE_FEATURES', 'nat', 'ip-forward.inc', '', d)}
CORE_IMAGE_EXTRA_INSTALL += "${@bb.utils.contains('IMAGE_FEATURES', 'resize', 'expand-rootfs', '', d)}"
