FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://wpa_supplicant.adlink"

WIFI_IFNAME ?= "wlan0"

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " wpa_supplicant@${WIFI_IFNAME}.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append () {
	install -d ${D}${sysconfdir}/wpa_supplicant/
	install -m 600 ${WORKDIR}/wpa_supplicant.adlink ${D}${sysconfdir}/wpa_supplicant/wpa_supplicant-${WIFI_IFNAME}.conf
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants
	ln -s /lib/systemd/system/wpa_supplicant@.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/wpa_supplicant@${WIFI_IFNAME}.service
}

