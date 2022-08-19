FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://wpa_supplicant.adlink"

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " wpa_supplicant@wlan0.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append () {
	install -d ${D}${sysconfdir}/wpa_supplicant/
	install -m 600 ${WORKDIR}/wpa_supplicant.adlink ${D}${sysconfdir}/wpa_supplicant/wpa_supplicant-wlan0.conf
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants
	ln -s /lib/systemd/system/wpa_supplicant@.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/wpa_supplicant@wlan0.service
}

