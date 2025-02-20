FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# STATIC_NIC_IPV4_SETTINGS format: <NIC>,<IP>/<MASK>[,<GATEWAY>] [<NIC>,<IP>/<MASK>[,<GATEWAY>]]
# e.g. eth0,192.168.0.100/24[,192.168.0.1]
STATIC_NIC_IPV4_SETTINGS ?= ""
STATIC_NIC_IPV4_SETTINGS:raspberrypi3-64 = "eth0,192.168.10.1/24"

SRC_URI += "file://wlan0.rpi3 file://veth.rpi3 file://static-ip.template"

do_install:append () {
  if [ "${MACHINE}" = "raspberrypi3-64" ]; then
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/wlan0.rpi3 ${D}${sysconfdir}/systemd/network/50-wlan0.network
    install -m 0644 ${WORKDIR}/veth.rpi3 ${D}${sysconfdir}/systemd/network/50-veth.network
  fi
  if [ "${MACHINE}" = "sp2-imx8mp" -a ${WIFI_IFNAME} = "wlan0" ]; then
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/wlan0.rpi3 ${D}${sysconfdir}/systemd/network/50-wlan0.network
  fi
  if [ -n "${STATIC_NIC_IPV4_SETTINGS}" ]; then
    for nicip in ${STATIC_NIC_IPV4_SETTINGS}; do
      nic=${nicip%%,*}
      ipmask=$(echo ${nicip} | cut -d',' -f2)
      ip=${ipmask%%/*}
      mask=${ipmask##*/}
      gateway=$(echo ${nicip} | cut -d',' -f3)
      if [ -n "${nic}" -a -n "${ip}" -a ${mask} -lt 32 ]; then
        install -d ${D}${sysconfdir}/systemd/network
        install -m 0644 ${WORKDIR}/static-ip.template ${D}${sysconfdir}/systemd/network/50-${nic}.network
        sed -E "s|@NICNAME@|${nic}|g" -i ${D}${sysconfdir}/systemd/network/50-${nic}.network
        sed -E "s|@STATICIP@|${ip}|g" -i ${D}${sysconfdir}/systemd/network/50-${nic}.network
        sed -E "s|@STATICMASK@|${mask}|g" -i ${D}${sysconfdir}/systemd/network/50-${nic}.network
      else
        bberror "Error: STATIC_NIC_IPV4_SETTINGS invalid format: <NIC>,<IP>/<MASK>[,GATEWAY]"
      fi
      if [ -n "${gateway}" ]; then
        echo "Gateway=${gateway}" >> ${D}${sysconfdir}/systemd/network/50-${nic}.network
      fi
    done
  fi
}

RDEPENDS:${PN}:append = "${@ ' connman-conf' if any(nic in d.getVar('STATIC_NIC_IPV4_SETTINGS') for nic in ['en', 'eth']) else ''}"

FILES:${PN} += "${sysconfdir}/systemd/network"

