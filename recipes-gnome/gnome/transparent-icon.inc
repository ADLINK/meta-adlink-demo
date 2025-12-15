do_replace_icon[depends] += "xcursor-transparent-theme-native:do_populate_sysroot"

do_replace_icon() {
    if [ -d ${RECIPE_SYSROOT_NATIVE}/usr/share/icons/xcursor-transparent/cursors ]; then
        cp --no-preserve=ownership -R -f ${RECIPE_SYSROOT_NATIVE}/usr/share/icons/xcursor-transparent/cursors ${S}/Adwaita/
    fi
}
addtask replace_icon before do_configure after do_patch
