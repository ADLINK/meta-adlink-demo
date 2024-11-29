update_chromium_launcher() {
	bbnote "Add \n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n to ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini"
	mkdir -p ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/
	printf "\n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n" >> ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
}

ROOTFS_POSTPROCESS_COMMAND += " ${@bb.utils.contains('PACKAGE_INSTALL', 'chromium-ozone-wayland', 'update_chromium_launcher;', '', d)} "

