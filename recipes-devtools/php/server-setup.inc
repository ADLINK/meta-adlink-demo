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
PGSQL_FLAGS = "--with-pgsql=shared,${STAGING_DIR_TARGET}${exec_prefix} --with-pdo-pgsql=shared,${STAGING_DIR_TARGET}${exec_prefix}"

PACKAGECONFIG[pgsql] = "${PGSQL_FLAGS},--without-pgsql --without-pdo-pgsql,postgresql"

do_install:append:class-target() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'apache2', 'true', 'false', d)}; then
        install -m 0644 ${WORKDIR}/php-fpm-apache2-proxy.conf ${D}/${sysconfdir}/apache2/conf.d/php-fpm.conf
        if [ -f ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini ]; then
            bbnote "Modify /etc/apache2-php8/php.ini with extensions"
            # set error display/report off
            # error_reporting = E_ALL & ~E_NOTICE
            sed -i 's|^error_reporting = E.*|error_reporting = 0|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            #sed -i 's|^;extension_dir = "/usr/lib/extensions"|extension_dir = "/usr/lib/extensions"|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            # additional extension for psql
            sed -i 's|^;extension=pgsql|extension=pgsql|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
            sed -i 's|^;extension=pdo_pgsql|extension=pdo_pgsql|' ${D}${sysconfdir}/php/apache2-php${PHP_MAJOR_VERSION}/php.ini
        fi
    fi
}

FILES:${PN}-modphp += "${libdir}/php8/extensions/*"
