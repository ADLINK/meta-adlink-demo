SUMMARY = "Adlink startup script"
DESCRIPTION = "This is a system daemon implementing the Adlink startup script"
LICENSE = "CLOSED"

DEPENDS += " \
    autoconf-archive-native dbus glib-2.0 glib-2.0-native \
    "

SRC_URI = "\
    file://adlink-startup.service \
    file://${MACHINE}-startup \
"

inherit pkgconfig systemd update-rc.d useradd

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE:${PN} = "adlink-startup.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

INITSCRIPT_NAME = "adlink-startup"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 19 0 1 6 ."

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "root"
USERADD_PARAM:${PN} = "--system -M -d /var/lib/adlink -s /bin/false -g root root"

do_install() {
  if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
    install -d "${D}${sbindir}"
    install -m 0755 ${WORKDIR}/${MACHINE}-startup ${D}${sbindir}/${INITSCRIPT_NAME}
    install -d "${D}${systemd_unitdir}/system"
    install -m 0644 "${WORKDIR}/adlink-startup.service" "${D}${systemd_unitdir}/system/adlink-startup.service"
    #enable the service
    install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
    ln -sf ${systemd_unitdir}/system/adlink-startup.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/adlink-startup.service
  else
    install -d "${D}${sysconfdir}/init.d"
    install -m 0755 ${WORKDIR}/${MACHINE}-startup ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}
  fi
}

FILES:${PN} += "\
    ${systemd_unitdir}/system-preset \
    ${sysconfdir}/init.d \
    ${datadir}/dbus-1/system-services/com.intel.adlink.Tabrmd.service \
    ${sbindir}/adlink-startup \
    ${systemd_unitdir}/system/adlink-startup.service \
  /usr \
  /usr/sbin \
"

