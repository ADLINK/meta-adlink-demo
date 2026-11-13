OSTREE_TMPFILES_CONF = "L+ /var/www - - - - /var/local/www"

do_install:append () {
    if [ "$(ls -A ${D}/var/www)" ]; then
        # 1. Store default /var/www/* to /var/local/www
        install -d ${D}/var/local/www
        cp -R --no-dereference --preserve=mode,links -v ${D}/var/www/* ${D}/var/local/www/

        # 2. Install a tmpfiles.d rule to symlink /var/www to /var/local/www at boot
        install -d ${D}${nonarch_libdir}/tmpfiles.d
        echo ${OSTREE_TMPFILES_CONF} | tee -a ${D}${nonarch_libdir}/tmpfiles.d/${PN}-ostree.conf
    fi
}

FILES:${PN} += "/var/local/ ${nonarch_libdir}/tmpfiles.d/"
