ALTERNATIVE_DNSMASQ_CONFIG ?= "\
interface=eth0\n\
expand-hosts\n\
filterwin2k\n\
domain-needed\n\
dhcp-range=192.168.10.10,192.168.10.30,12h\n\
dhcp-userclass=set:ipxe,iPXE\n\
dhcp-boot=tag:!ipxe,intel.efi\n\
dhcp-boot=tag:ipxe,bootx64.efi\n\
dhcp-lease-max=20\n\
enable-tftp\n\
tftp-root=/opt/dnsmasq/tftproot\n\
"

ALTERNATIVE_DNSMASQ_CONFIG:raspberrypi3-64 = "\
interface=eth0\n\
expand-hosts\n\
filterwin2k\n\
domain-needed\n\
dhcp-range=192.168.10.10,192.168.10.30,12h\n\
dhcp-userclass=set:ipxe,iPXE\n\
dhcp-boot=tag:!ipxe,snponly.efi\n\
dhcp-boot=tag:ipxe,bootx64.efi\n\
dhcp-lease-max=20\n\
"

DNSMASQ_SERVICE_OVERRIDE ?= ""
DNSMASQ_SERVICE_OVERRIDE:sota = "\
[Unit]\n\
After=local-fs.target var.mount\n\
Requires=local-fs.target\n\
\n\
[Service]\n\
ExecStartPre=/usr/bin/test -d /var/lib/misc\n\
"

DNSMASQ_TMPFILES_CONF ?= ""
DNSMASQ_TMPFILES_CONF:sota = "\
d /var/lib/misc 0755 root root -\n\
d /var/rootdirs/opt/dnsmasq/tftproot 0755 root root -\n\
"

EXTRA_SYSTEMD_AFTER ?= "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd-networkd.service systemd-resolved.service', '', d)}"

do_install:append () {
  if [ -n "${ALTERNATIVE_DNSMASQ_CONFIG}" ]; then
    install -d ${D}/etc/dnsmasq.d
    echo "${ALTERNATIVE_DNSMASQ_CONFIG}" | tee -a ${D}/${sysconfdir}/dnsmasq.d/alternative.conf
    sed -e "s|After=network.target|After=network.target ${EXTRA_SYSTEMD_AFTER}|g" -i ${D}${systemd_unitdir}/system/dnsmasq.service
  fi
  if [ -n "${DNSMASQ_SERVICE_OVERRIDE}" ]; then
    install -d ${D}${sysconfdir}/systemd/system/dnsmasq.service.d/
    echo "${DNSMASQ_SERVICE_OVERRIDE}" | tee -a ${D}${sysconfdir}/systemd/system/dnsmasq.service.d/override.conf
  fi
  if [ -n "${DNSMASQ_TMPFILES_CONF}" ]; then
    install -d ${D}${sysconfdir}/tmpfiles.d/
    echo "${DNSMASQ_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/dnsmasq-ostree.conf
  fi
}

FILES:${PN} += "${sysconfdir}/tmpfiles.d/ ${sysconfdir}/systemd/system/dnsmasq.service.d/"
