include ${@bb.utils.contains('IMAGE_FEATURES', 'hidecursor', 'transparent-cursor.inc', '', d)}
include ${@bb.utils.contains('IMAGE_FEATURES', 'locale', 'locale-conf.inc', '', d)}

# setup unique hostname, i.e. machine + 1st mac address, at first boot up
# host=myhost@192.168.11.25; echo "${host##*@} ${host%%@*}"
pkg_postinst_ontarget:${PN} () {
    echo "${MACHINE}-$(cat /sys/class/net/$(ls /sys/class/net/ | grep -m 1 -e 'en' -e 'eth')/address | sed 's|:||g')" > ${sysconfdir}/hostname
    test -n "${EXTRA_ETC_HOSTS_ENTRIES}" && test -f ${sysconfdir}/hosts && for host in ${EXTRA_ETC_HOSTS_ENTRIES}; do echo -e "\n${host##*@}\t${host%%@*}" >> ${sysconfdir}/hosts; done
}

