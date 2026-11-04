FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = "${@bb.utils.contains('IMAGE_FEATURES', 'resize', ' file://init-resize.sh', '', d)}"

do_install:append() {
	if [ -f ${WORKDIR}/init-resize.sh ]; then
		install -m 0755 ${WORKDIR}/init-resize.sh ${D}/init
	fi
}
