update_chromium_launcher() {
	bbnote "Add \n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n to ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini"
	mkdir -p ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/
	printf "\n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/bin/chromium --user-data-dir=/home/weston/.config/chromium\n" >> ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
	chown -R weston:weston ${IMAGE_ROOTFS}/home/weston
}

update_demo_icon() {
	bbnote "Replace icon=/home/root/.nxp-demo-experience/icon/icon_demo_launcher.png in ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini"
	sed -i "s|icon=/home/root/.nxp-demo-experience/icon/icon_demo_launcher.png|icon=/home/weston/.nxp-demo-experience/icon/icon_demo_launcher.png|g" ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
}

update_weston_envfile() {
	bbnote "Replace /etc/default/weston HOME=/root/ with HOME=/home/weston/"
	sed -E "s|HOME=/root/|HOME=/home/weston/|g" -i ${IMAGE_ROOTFS}${sysconfdir}/default/weston
}

ROOTFS_POSTPROCESS_COMMAND:append = " ${@bb.utils.contains('PACKAGE_INSTALL', 'chromium-ozone-wayland', 'update_chromium_launcher;', '', d)} "
ROOTFS_POSTPROCESS_COMMAND:append = " ${@bb.utils.contains('PACKAGE_INSTALL', 'nxp-demo-experience', 'update_demo_icon;', '', d)} "

ROOTFS_POSTPROCESS_COMMAND:append = " update_weston_envfile; "

