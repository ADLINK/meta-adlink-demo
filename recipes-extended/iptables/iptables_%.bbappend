include ${@bb.utils.contains('IMAGE_FEATURES', 'nat', 'iptables-nat.inc', '', d)}

