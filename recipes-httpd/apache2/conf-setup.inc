do_install:append:class-target () {
    # add /var/www/html directory
    install -d ${D}/var/www/html
    # load proxy_module and proxy_fcgi modules, (should be done by php-pfm-apache2)
    #sed -i 's|#LoadModule proxy_module|LoadModule proxy_module|' ${D}/${sysconfdir}/${BPN}/httpd.conf
    #sed -i 's|#LoadModule proxy_fcgi_module|LoadModule proxy_fcgi_module|' ${D}/${sysconfdir}/${BPN}/httpd.conf
    # setup DocumentRoot directory and setup php cgi scripts
    sed -i 's|DocumentRoot "/usr/share/apache2/default-site/htdocs"|DocumentRoot "/var/www/html"|' ${D}/${sysconfdir}/${BPN}/httpd.conf
    sed -i 's|<Directory "/usr/share/apache2/default-site/htdocs">|<Directory "/var/www/html">|' ${D}/${sysconfdir}/${BPN}/httpd.conf
}
FILES:${PN} += "/var/www/html"

