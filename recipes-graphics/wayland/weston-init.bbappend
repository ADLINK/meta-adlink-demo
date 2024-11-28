# Add background setting to [shell] section and update chromium launcher

WESTON_BACKGROUND_IMAGE ?= "adlink.jpg"

update_file() {
    if ! grep -q "$1" $3; then
        bbfatal $1 not found in $3
    fi
    bbnote "Replace $1 to $2 in $3"
    sed -i -e "s,$1,$2," $3
}

update_background() {
	update_file "\[shell\]" "\[shell\]\nbackground-image=/usr/share/weston/${WESTON_BACKGROUND_IMAGE}\nbackground-type=scale" ${D}${sysconfdir}/xdg/weston/weston.ini
}

update_weston_owner() {
    # weston should be run as weston, not as root
    update_file "User=root" "User=weston" ${D}${systemd_system_unitdir}/weston.service
    update_file "Group=root" "Group=weston" ${D}${systemd_system_unitdir}/weston.service
}

update_virtual_keyboard() {
    # setup virtual keyboard
    echo "\n[input-method]" >> ${D}${sysconfdir}/xdg/weston/weston.ini
    echo "path=/usr/libexec/weston-keyboard" >> ${D}${sysconfdir}/xdg/weston/weston.ini
    echo "\n[keyboard]" >> ${D}${sysconfdir}/xdg/weston/weston.ini
    echo "keymap_model=pc105" >> ${D}${sysconfdir}/xdg/weston/weston.ini
    echo "keymap_layout=us" >> ${D}${sysconfdir}/xdg/weston/weston.ini
    echo "vt-switching=true" >> ${D}${sysconfdir}/xdg/weston/weston.ini
}

update_kiosk_shell() {
    # set weston.ini to kiosk mode. FIXME: weston-keyboard won't work in kiosk-shell
    update_file "\[core\]" "\[core\]\n#shell=kiosk-shell.so" ${D}${sysconfdir}/xdg/weston/weston.ini
}

do_install:append() {
	update_weston_owner
	update_virtual_keyboard
	case "${MACHINE}" in
	lec-*)
		update_background
		;;
	sp2-*)
		update_background
		;;
	esac
	case "${IMAGE_FEATURES}" in
	*kiosk-mode*)
		update_kiosk_shell
		;;
	esac
}

