ALTERNATIVE_DNSMASQ_CONFIG ?= "\
interface=eth0\n\
expand-hosts\n\
filterwin2k\n\
domain-needed\n\
dhcp-range=192.168.10.10,192.168.10.30,12h\n\
dhcp-userclass=set:ipxe,iPXE\n\
dhcp-boot=tag:!ipxe,intel.efi\n\
dhcp-boot=tag:ipxe,boot64.efi\n\
dhcp-lease-max=20\n\
enable-tftp\n\
tftp-root=/tftproot\n\
"

ALTERNATIVE_DNSMASQ_CONFIG_raspberrypi3-64 = "\
interface=eth0\n\
expand-hosts\n\
filterwin2k\n\
domain-needed\n\
dhcp-range=192.168.10.10,192.168.10.30,12h\n\
dhcp-userclass=set:ipxe,iPXE\n\
dhcp-boot=tag:!ipxe,intel.efi\n\
dhcp-boot=tag:ipxe,boot64.efi\n\
dhcp-lease-max=20\n\
"

EXTRA_SYSTEMD_AFTER ?= "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd-networkd.service systemd-resolved.service', '', d)}"

do_install_append () {
  if [ -n "${ALTERNATIVE_DNSMASQ_CONFIG}" ]; then
    install -d ${D}/etc/dnsmasq.d
    echo "${ALTERNATIVE_DNSMASQ_CONFIG}" | tee -a ${D}/${sysconfdir}/dnsmasq.d/alternative.conf
    sed -e "s|After=network.target|After=network.target ${EXTRA_SYSTEMD_AFTER}|g" -i ${D}${systemd_unitdir}/system/dnsmasq.service
  fi
}

