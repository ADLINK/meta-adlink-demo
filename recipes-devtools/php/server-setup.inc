FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
# ===
# php.ini:
# ===
# #display_errors = On
# => display_errors = Off
# #error_reporting = E_ALL
# #error_reporting = E_ALL & ~E_NOTICE
# => error_reporting = 0
# extension=pgsql.so
# extension=pdo_pgsql.so

SRC_URI += "file://php-fpm-apache2-proxy.conf"

#PACKAGECONFIG:append = " apache2 psql"
PACKAGECONFIG[pgsql] = "--with-pgsql=${STAGING_DIR_TARGET}${exec_prefix} \
                        --with-pdo-pgsql=${STAGING_DIR_TARGET}${exec_prefix} \
                        ,--without-pgsql --without-pdo-pgsql \
                        ,postgresql"

do_compile:append:class-target() {
    sed -i "s|^include_dir=\"|include_dir=\"${STAGING_DIR_TARGET}|" $(which php-config)
    cd ${S}/ext/pgsql
    phpize
    ./configure ${CONFIGUREOPTS} --with-pgsql=${STAGING_DIR_TARGET}${exec_prefix} "$@"
	oe_runmake
    cd ${S}/ext/pdo_pgsql
    phpize
    ./configure ${CONFIGUREOPTS} --with-pdo-pgsql=${STAGING_DIR_TARGET}${exec_prefix} "$@"
	oe_runmake
}

do_install:append:class-target() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'pgsql', 'true', 'false', d)}; then
        # install pgsql extension modules for php
        install -d ${D}${libdir}/extensions
        if [ ${S}/ext/pgsql/modules/pgsql.so ]; then
            bbnote "Copy ${S}/ext/pgsql/modules/pgsql.so"
            install -m 0644 ${S}/ext/pgsql/modules/pgsql.so ${D}${libdir}/extensions
        fi
        if [ ${S}/ext/pdo_pgsql/modules/pdo_pgsql.so ]; then
            bbnote "Copy ${S}/ext/pdo_pgsql/modules/pdo_pgsql.so"
            install -m 0644 ${S}/ext/pdo_pgsql/modules/pdo_pgsql.so ${D}${libdir}/extensions
        fi
    fi

    if ${@bb.utils.contains('PACKAGECONFIG', 'apache2', 'true', 'false', d)}; then
        install -m 0644 ${WORKDIR}/php-fpm-apache2-proxy.conf ${D}/${sysconfdir}/apache2/conf.d/php-fpm.conf
        if [ -f ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini ]; then
            bbnote "Modify /etc/apache2-php8/php.ini with extensions"
            # set error display/report off
            # error_reporting = E_ALL & ~E_NOTICE
            sed -i 's|^error_reporting = E.*|error_reporting = 0|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            sed -i 's|^;extension_dir = "/usr/lib/extensions"|extension_dir = "/usr/lib/extensions"|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            # additional extension for psql
            sed -i 's|^;extension=pgsql|extension=pgsql|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            sed -i 's|^;extension=pdo_pgsql|extension=pdo_pgsql|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
        fi
    fi
}

SYSROOT_PREPROCESS_FUNCS += "phpize_sysroot_preprocess"

phpize_sysroot_preprocess () {
    if [ -f ${SYSROOT_DESTDIR}${bindir_crossscripts}/phpize ]; then
        bbnote "Modify ${SYSROOT_DESTDIR}${bindir_crossscripts}/phpize..."
        sed -i 's!eval echo FIXMESTAGINGDIRTARGET!eval echo ${STAGING_DIR_HOST}/!' ${SYSROOT_DESTDIR}${bindir_crossscripts}/phpize
    fi
    if [ -f ${SYSROOT_DESTDIR}${bindir_crossscripts}/php-config ]; then
        bbnote "Modify ${SYSROOT_DESTDIR}${bindir_crossscripts}/php-config..."
        sed -i 's!^include_dir=FIXMESTAGINGDIRTARGET!include_dir=${STAGING_INCDIR}!' ${SYSROOT_DESTDIR}${bindir_crossscripts}/php-config
    fi
}

FILES:${PN}-modphp += "${libdir}/extensions"
