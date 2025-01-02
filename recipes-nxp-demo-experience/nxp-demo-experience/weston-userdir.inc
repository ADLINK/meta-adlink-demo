do_replace_path () {
	sed -i "s|home/root|home/weston|g" ${S}/engine/DemoPage.h
	sed -i "s|home/root|home/weston|g" ${S}/gui/DemoGrid.qml
}
addtask replace_path before do_configure after do_patch

do_install:append () {
    install -d ${D}/home/weston/.nxp-demo-experience
    cp -r ${WORKDIR}/demos/* ${D}/home/weston/.nxp-demo-experience
    sed -i "s|/home/root|/home/weston|g" ${D}/home/weston/.nxp-demo-experience/demos.json
}

FILES:${PN} += "/home/weston/.nxp-demo-experience/*"

