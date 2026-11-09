FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SAMBA_USERNAME ?= "adlink"
SAMBA_PASSWORD ?= "PASSWORD=adlink"
SAMBA_NTHASH = "${@'%s' % (lambda h: (h.new('md4', "${SAMBA_PASSWORD}".encode('utf-16le')).hexdigest()))(__import__('hashlib'))}"

SAMBA_POSTINST_ONTARGET_COMMANDS ?= " \
  mkdir -p /media/share; \
  chmod -R a+rw /media/share; \
"

SMBUSER_SERVICE_OVERRIDE ?= ""
SMBUSER_SERVICE_OVERRIDE:sota = "\
[Unit]\n\
After=local-fs.target var.mount\n\
Requires=local-fs.target\n\
"

SMB_TMPFILES_CONF ?= ""
SMB_TMPFILES_CONF:sota = "\
d /var/rootdirs/media 0777 root root -\n\
d /var/rootdirs/media/share 0777 root root -\n\
d /var/rootdirs/media/share/DEVICE 0777 root root -\n\
d /var/rootdirs/media/share/PMIIMAGE 0777 root root -\n\
d /var/rootdirs/media/share/PMIJUDGE 0777 root root -\n\
d /var/rootdirs/media/share/PMIDATA 0777 root root -\n\
d /var/rootdirs/media/share/PTPA 0777 root root -\n\
"

SMBUSER_TMPFILES_CONF ?= ""
SMBUSER_TMPFILES_CONF:sota = "\
d /var/lib/samba 0755 root root -\n\
d /var/lib/samba/private 0755 root root -\n\
"

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
    if [ -n "${SMBUSER_SERVICE_OVERRIDE}" ]; then
      install -d ${D}${sysconfdir}/systemd/system/smb-user.service.d/
      echo "${SMBUSER_SERVICE_OVERRIDE}" | tee -a ${D}${sysconfdir}/systemd/system/smb-user.service.d/override.conf
    fi
    if [ -n "${SMBUSER_TMPFILES_CONF}" ]; then
      install -d ${D}${sysconfdir}/tmpfiles.d/
      echo "${SMBUSER_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/smbuser-ostree.conf
    fi
    if [ -f ${D}${sysconfdir}/tmpfiles.d/samba.conf ]; then
      echo "${SMB_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/samba.conf
    fi
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
	${SAMBA_POSTINST_ONTARGET_COMMANDS}
}

FILES:${PN} += "${sbindir} ${systemd_unitdir}/system ${sysconfdir}/systemd/system/multi-user.target.wants ${sysconfdir}/systemd/system/rpcbind.service.d/ ${sysconfdir}/tmpfiles.d/"

