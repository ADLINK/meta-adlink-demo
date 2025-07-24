include ${@bb.utils.contains_any('DEPENDS', 'docker-datapart image-datapart', '../../../../recipes-adlink/images/datapart-conf.inc', '', d)}

