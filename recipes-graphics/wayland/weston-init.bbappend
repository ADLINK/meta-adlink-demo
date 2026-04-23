# Add background setting to [shell] section and update chromium launcher

WESTON_BACKGROUND_IMAGE ?= "adlink.jpg"
WESTON_SERVICE_DEBUG_FLAG ?= ""
REMOTE_PKI_KEYNAME ?= "remote-desktop"
REMOTE_PKI_CHALLENGE ?= "${MACHINE}"
CERT_COUNTRY ?= "TW"
CERT_STATE ?= "TaoYuan"
CERT_CITY ?= "Guishan"
CERT_NAME ?= "*.adlinktech.com"
CERT_CORP ?= "adlink"

ROTATE_DESKTOP_ANGLE ?= "0"

PACKAGECONFIG[vnc] = ",,"

DEPENDS:append = " openssl-native"

update_file() {
    if ! grep -q "$1" $3; then
        bbfatal $1 not found in $3
    fi
    bbnote "Replace $1 to $2 in $3"
    sed -i -e "s,$1,$2," $3
}

update_background() {
    update_file "\[shell\]" "\[shell\]\nbackground-image=/usr/share/weston/${WESTON_BACKGROUND_IMAGE}\nbackground-type=scale" ${D}${sysconfdir}/xdg/weston/weston.ini
    if grep -q "#\[shell\]" ${D}${sysconfdir}/xdg/weston/weston.ini; then
        update_file "#\[shell\]" "\[shell\]" ${D}${sysconfdir}/xdg/weston/weston.ini
    fi
}

update_weston_owner() {
    # weston should be run as weston, not as root
    update_file "User=root" "User=weston" ${D}${systemd_system_unitdir}/weston.service
    update_file "Group=root" "Group=weston" ${D}${systemd_system_unitdir}/weston.service
}

update_weston_remote() {
    # setup weston service as remote desktop connection.
    sed -E "s|(ExecStart=.*)|#\1\nExecStart=/usr/bin/weston ${WESTON_SERVICE_DEBUG_FLAG} --log=\${XDG_RUNTIME_DIR}/weston.log --modules=systemd-notify.so,screen-share.so|g" -i ${D}${systemd_system_unitdir}/weston.service
    # for screen-share, run command as
    # /usr/bin/weston --backend=vnc-backend.so
    #                 --vnc-tls-cert=/etc/remote-desktop/keys/remote-desktop.crt
    #                 --vnc-tls-key=/etc/remote-desktop/keys/remote-desktop.key
    #                 --shell=fullscreen-shell.so
    if [ "${@bb.utils.contains('PACKAGECONFIG', 'vnc', 'yes', 'no', d)}" = "yes" ]; then
        sed -E "s|^command=${bindir}/weston .*|command=/usr/bin/weston --backend=vnc-backend.so --vnc-tls-cert=/etc/remote-desktop/keys/remote-desktop.crt --vnc-tls-key=/etc/remote-desktop/keys/remote-desktop.key --shell=fullscreen-shell.so|g" -i ${D}${sysconfdir}/xdg/weston/weston.ini
        sed -E "s|#start-on-startup=.*|start-on-startup=true|g" -i ${D}${sysconfdir}/xdg/weston/weston.ini
    fi
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
    update_file "\[shell\]" "\[shell\]\npanel-position=none" ${D}${sysconfdir}/xdg/weston/weston.ini
    if grep -q "#\[shell\]" ${D}${sysconfdir}/xdg/weston/weston.ini; then
        update_file "#\[shell\]" "\[shell\]" ${D}${sysconfdir}/xdg/weston/weston.ini
    fi
}

update_output_rotation() {
	# rotate desktop by 180 on LVDS panel
	if [ ${ROTATE_DESKTOP_ANGLE} -eq 180 ]; then
		echo "\n[output]" >> ${D}${sysconfdir}/xdg/weston/weston.ini
		echo "name=LVDS-1" >> ${D}${sysconfdir}/xdg/weston/weston.ini
		echo "mode=1280x800@60" >> ${D}${sysconfdir}/xdg/weston/weston.ini
		echo "transform=rotate-180" >> ${D}${sysconfdir}/xdg/weston/weston.ini
	fi
}

do_gen_key() {
    mkdir -p ${B}/CA/private/
    cd ${B}/CA/
    openssl genrsa -out private/cakey.pem 2048
    openssl req -new -x509 -nodes -days 365000 -key private/cakey.pem -out cacert.pem -subj "/C=${CERT_COUNTRY}/ST=${CERT_STATE}/L=${CERT_CITY}/O=${CERT_CORP}/CN=${CERT_NAME}/challengePassword=${REMOTE_PKI_CHALLENGE}"
    openssl genrsa -out ${REMOTE_PKI_KEYNAME}.key 2048
    openssl req -new -key ${REMOTE_PKI_KEYNAME}.key -out ${REMOTE_PKI_KEYNAME}.csr -subj "/C=${CERT_COUNTRY}/ST=${CERT_STATE}/L=${CERT_CITY}/O=${CERT_CORP}/CN=${CERT_NAME}/challengePassword=${REMOTE_PKI_CHALLENGE}"
    openssl x509 -req -days 365 -in ${REMOTE_PKI_KEYNAME}.csr -out ${REMOTE_PKI_KEYNAME}.crt -CA cacert.pem -CAkey private/cakey.pem
}
addtask gen_key before do_install after do_compile

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
	*remote*)
		update_weston_remote
		# rdp/vnc keys
		install -m 0755 -d ${D}${sysconfdir}/remote-desktop/keys/private
		chown weston:weston -R ${D}${sysconfdir}/remote-desktop/keys/
		install -m 0644 ${B}/CA/cacert.pem ${D}${sysconfdir}/remote-desktop/keys/
		install -m 0644 ${B}/CA/private/cakey.pem ${D}${sysconfdir}/remote-desktop/keys/private/
		install -m 0644 ${B}/CA/${REMOTE_PKI_KEYNAME}.key ${D}${sysconfdir}/remote-desktop/keys/
		install -m 0644 ${B}/CA/${REMOTE_PKI_KEYNAME}.crt ${D}${sysconfdir}/remote-desktop/keys/
		;;
	esac
	if [ ${ROTATE_DESKTOP_ANGLE} -eq 180 ]; then
		update_output_rotation
	fi
}

FILES:${PN} += "${sysconfdir}/remote-desktop/keys"

