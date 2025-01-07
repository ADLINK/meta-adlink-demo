FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

CUSTOM_DEFAULT_LOCALE_LANG ?= "zh_TW.UTF-8"

SRC_URI += "file://locale.sh"

do_install:append () {
    if [ -f ${WORKDIR}/locale.sh ]; then
        sed -i "s|#CUSTOM_DEFAULT_LOCALE_LANG#|${CUSTOM_DEFAULT_LOCALE_LANG}|g" ${WORKDIR}/locale.sh
        install -m 0755 -d ${D}${sysconfdir}/profile.d/
        install -m 0644 ${WORKDIR}/locale.sh ${D}${sysconfdir}/profile.d/
    fi
}

FILES:${PN} += "${sysconfdir}/profile.d/*"

