SAMBA_USERNAME ?= "adlink"
SAMBA_PASSWORD ?= "PASSWORD=adlink"
SAMBA_NTHASH = "${@'%s' % (lambda h: (h.new('md4', "${SAMBA_PASSWORD}".encode('utf-16le')).hexdigest()))(__import__('hashlib'))}"

POSTINST_ONTARGET_COMMANDS ?= " \
  mkdir -p /media/share; \
  chmod -R a+rw /media/share; \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://smb-user \
	file://smb-user.service \
"

RDEPENDS:${PN} += "bash"

do_install:append () {
	# install smb-user script to add samba user with smbpasswd
	install -d ${D}${sbindir}
	install -m 0755 ${WORKDIR}/smb-user ${D}${sbindir}/smb-user
	# install smb-user service to run once like run-postinsts after smb.service
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/smb-user.service ${D}${systemd_unitdir}/system/smb-user.service
	# enable the smb-user.service
	install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants
	ln -sf ${systemd_unitdir}/system/smb-user.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/smb-user.service
}

do_post_install () {
	# append additional share section in smb.conf
	if [ -f ${D}${sysconfdir}/samba/smb.conf ]; then
		echo "\n[share]" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   comment = SP2-IMX8MP Share Directory" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   browseable = no" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   path = /media/share" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   create mask = 0700" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   directory mask = 0700" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   read only = no" >> ${D}${sysconfdir}/samba/smb.conf
		echo "   valid users = ${SAMBA_USERNAME}" >> ${D}${sysconfdir}/samba/smb.conf
	fi
	# update content of smb-user script
	if [ -f ${D}${sbindir}/smb-user ]; then
		sed -e 's,#SAMBA_PASSWORD#,${SAMBA_PASSWORD},g' -i ${D}${sbindir}/smb-user
		sed -e 's,#SAMBA_USERNAME#,${SAMBA_USERNAME},g' -i ${D}${sbindir}/smb-user
	fi
	# update content of smb-user.service
	if [ -f ${D}${systemd_unitdir}/system/smb-user.service ]; then
		sed -e 's,#SBINDIR#,${sbindir},g' -i ${D}${systemd_unitdir}/system/smb-user.service
		sed -e 's,#BASE_BINDIR#,${base_bindir},g' -i ${D}${systemd_unitdir}/system/smb-user.service
	fi
}
addtask post_install before do_package after do_install

pkg_postinst_ontarget:${PN} () {
	${POSTINST_ONTARGET_COMMANDS}
}

FILES:${PN} += "${sbindir} ${systemd_unitdir}/system ${sysconfdir}/systemd/system/multi-user.target.wants"

