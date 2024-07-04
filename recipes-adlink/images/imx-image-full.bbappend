include ${@bb.utils.contains_any('DEPENDS', 'docker-datapart image-datapart', 'datapart-conf.inc', '', d)}
