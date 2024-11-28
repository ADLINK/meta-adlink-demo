CHROMIUM_EXTRA_ARGS:append = " --enable-features=VaapiVideoDecoder --enable-accelerated-video-decode --enable-accelerated-2d-canvas --ignore-gpu-blacklist"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

PACKAGECONFIG:append = "${@bb.utils.contains('IMAGE_FEATURES', 'kiosk-mode', ' kiosk-mode', '', d)}"

BROWSER_SERVICE = "${@bb.utils.contains('IMAGE_FEATURES', 'kiosk-mode', 'browser.service', '', d)}"
BROWSER_SERVICEFILE = "${@bb.utils.contains('IMAGE_FEATURES', 'kiosk-mode', 'file://browser.service', '', d)}"

SRC_URI += "${BROWSER_SERVICEFILE}"

# enable wayland virtual keyboard in chromium
CHROMIUM_EXTRA_ARGS:append = " --enable-wayland-ime"
CHROMIUM_EXTRA_ARGS:remove = "${@bb.utils.contains('IMAGE_FEATURES', 'kiosk-mode', '--incognito', '', d)}"

do_install:append () {
	if [ -f ${WORKDIR}/${BROWSER_SERVICE} ]; then
		install -d ${D}${systemd_unitdir}/system
		install -m 644 ${WORKDIR}/${BROWSER_SERVICE} ${D}${systemd_unitdir}/system/${BROWSER_SERVICE}
		install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants
		ln -sf ${systemd_unitdir}/system/${BROWSER_SERVICE} ${D}${sysconfdir}/systemd/system/multi-user.target.wants/${BROWSER_SERVICE}
	fi
}

SYSTEM_SERVICE_DIRECTORIES = "${@bb.utils.contains('IMAGE_FEATURES', 'kiosk-mode', '${sysconfdir}/systemd/system/multi-user.target.wants/ ${systemd_unitdir}/system/', '', d)}"
FILES:${PN} += "${SYSTEM_SERVICE_DIRECTORIES}"

