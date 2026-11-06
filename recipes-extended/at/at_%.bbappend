ATD_TMPFILES_CONF ?= ""
ATD_TMPFILES_CONF:sota = "\
d /var/spool 0755 root root -\n\
d /var/spool/at/ 0755 root root -\n\
d /var/spool/at/jobs 0755 root root -\n\
"

do_install:append () {
  if [ -n "${ATD_TMPFILES_CONF}" ]; then
    install -d ${D}${sysconfdir}/tmpfiles.d/
    echo "${ATD_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/atd-ostree.conf
  fi
}

FILES:${PN} += "${sysconfdir}/tmpfiles.d/"
