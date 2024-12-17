update_chromium_launcher() {
	bbnote "Add \n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n to ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini"
	mkdir -p ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/
	printf "\n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n" >> ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
}

update_weston_envfile() {
	bbnote "Replace /etc/default/weston HOME=/root/ with HOME=/home/weston/"
	sed -E "s|HOME=/root/|HOME=/home/weston/|g" -i ${IMAGE_ROOTFS}${sysconfdir}/default/weston
}

ROOTFS_POSTPROCESS_COMMAND += " ${@bb.utils.contains('PACKAGE_INSTALL', 'chromium-ozone-wayland', 'update_chromium_launcher;', '', d)} "

ROOTFS_POSTPROCESS_COMMAND:append = " update_weston_envfile; "

