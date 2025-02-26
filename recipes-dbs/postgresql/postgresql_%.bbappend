include ${@bb.utils.contains('IMAGE_FEATURES', 'lamp', 'db-setup.inc', '', d)}
