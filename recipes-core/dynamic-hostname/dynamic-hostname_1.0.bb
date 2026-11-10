SUMMARY = "Dynamic hostname generation service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd

SRC_URI = " \
    file://dynamic-hostname.service \
"

HOSTNAME_SERVICE_OVERRIDE ?= "\
[Service]\n\
ExecStartPost=+/bin/sh -c 'hostname > /etc/hostname'\n\
"
HOSTNAME_SERVICE_OVERRIDE:sota = "\
[Unit]\n\
After=ostree-finalize-staged.path\n\
[Service]\n\
ExecStartPost=+/bin/sh -c 'hostname > /run/hostname'\n\
"
HOSTNAME_TMPFILES_CONF ?= ""
HOSTNAME_TMPFILES_CONF:sota = "\
L+ /etc/hostname - - - - /run/hostname\n\
"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SYSTEMD_SERVICE:${PN} = "dynamic-hostname.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install () {
    # Install the systemd service file
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/dynamic-hostname.service ${D}${systemd_unitdir}/system/dynamic-hostname.service

    # service override
    if [ -n "${HOSTNAME_SERVICE_OVERRIDE}" ]; then
      install -d ${D}${sysconfdir}/systemd/system/dynamic-hostname.service.d/
      echo "${HOSTNAME_SERVICE_OVERRIDE}" | tee -a ${D}${sysconfdir}/systemd/system/dynamic-hostname.service.d/override.conf
    fi

    # tmpfile.d
    if [ -n "${HOSTNAME_TMPFILES_CONF}" ]; then
      install -d ${D}${sysconfdir}/tmpfiles.d/
      echo "${HOSTNAME_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/hostname-ostree.conf
    fi
}

FILES:${PN} += " \
    ${systemd_unitdir}/system/dynamic-hostname.service \
    ${sysconfdir}/tmpfiles.d/hostname-ostree.conf \
    ${sysconfdir}/systemd/system/dynamic-hostname.service.d/override.conf \
"

# Ensure systemd is available
RDEPENDS:${PN} += "systemd"
