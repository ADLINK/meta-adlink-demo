do_install[depends] += "adwaita-icon-theme-native:do_populate_sysroot"

do_install:append() {
    if [ -d ${RECIPE_SYSROOT_NATIVE}/usr/share/icons/Adwaita/cursors ]; then
        install -d ${D}${sysconfdir}/skel/.icons/default
        cp --no-preserve=ownership -R -f ${RECIPE_SYSROOT_NATIVE}/usr/share/icons/Adwaita/* ${D}${sysconfdir}/skel/.icons/default
    fi
}

