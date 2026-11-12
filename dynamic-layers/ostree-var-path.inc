OSTREE_TMPFILES_CONF ?= ""
OSTREE_OPTPATH ?= ""
OSTREE_OPTPATH:sota = "/var/local/opt/ /usr/lib/tmpfiles.d/"

do_install:append:sota () {
    if [ "$(ls -A ${D}/opt)" ]; then
        install -d ${D}/var/local/opt
        cp -R --no-dereference --preserve=mode,links -v ${D}/opt/* ${D}/var/local/opt/

        install -d ${D}/usr/lib/tmpfiles.d
        echo ${OSTREE_TMPFILES_CONF} | tee -a ${D}/usr/lib/tmpfiles.d/${PN}-ostree.conf
    fi
}

FILES:${PN} += "${OSTREE_OPTPATH}"
