SUMMARY = "libvirtd scripts to expose VM ssh port to jenekins libvirt plugin"
DESCRIPTION = "This is an additional libvirtd scripts to expose ssh port of VM to external network"
LICENSE = "CLOSED"

SRC_URI = "\
    file://qemu \
    file://vmportforward \
    file://vmportforward@.service \
"

inherit pkgconfig systemd update-rc.d useradd

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE:${PN} = "vmportforward@.service"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"

INITSCRIPT_NAME = "vmportforward"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 19 0 1 6 ."

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "root"
USERADD_PARAM:${PN} = "--system -M -d /var/lib/adlink -s /bin/false -g root root"

do_install() {
    install -d "${D}${sysconfdir}/libvirt/hooks"
    install -m 0755 "${WORKDIR}/qemu" "${D}${sysconfdir}/libvirt/hooks/qemu"

    if ${@bb.utils.contains("DISTRO_FEATURES", 'systemd', 'true', 'false', d)}; then
       install -d "${D}${sbindir}"
       install -m 0755 "${WORKDIR}/vmportforward" "${D}${sbindir}/vmportforward"

       install -d "${D}${systemd_unitdir}/system"
       install -m 0644 "${WORKDIR}/vmportforward@.service" "${D}${systemd_unitdir}/system/vmportforward@.service"
    fi
}

FILES:${PN} += "\
    ${systemd_unitdir}/system/vmportforward@.service \
    ${sbindir}/vmportforward \
    ${sysconfdir}/libvirt/hooks/qemu \
"
