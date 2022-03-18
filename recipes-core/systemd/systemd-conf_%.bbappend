FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://wlan0.rpi3 file://eth0.rpi3"

do_install_append () {
  if [ "${MACHINE}" = "raspberrypi3-64" ]; then
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/eth0.rpi3 ${D}${sysconfdir}/systemd/network/50-eth0.network
    install -m 0644 ${WORKDIR}/wlan0.rpi3 ${D}${sysconfdir}/systemd/network/50-wlan0.network
  fi
}

FILES_${PN} += "${sysconfdir}/systemd/network"

