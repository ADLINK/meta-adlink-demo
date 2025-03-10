include ${@bb.utils.contains('IMAGE_FEATURES', 'locale', 'locale-conf.inc', '', d)}

# setup unique hostname, i.e. machine + 1st mac address, at first boot up
pkg_postinst_ontarget:${PN} () {
    echo "${MACHINE}-$(cat /sys/class/net/$(ls /sys/class/net/ | grep -m 1 -e 'en' -e 'eth')/address | sed 's|:||g')" > ${sysconfdir}/hostname
}

