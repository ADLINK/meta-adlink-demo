include ${@bb.utils.contains('IMAGE_FEATURES', 'lamp', 'db-setup.inc', '', d)}

POSTGRESQL_SERVICE_OVERRIDE ?= ""
POSTGRESQL_SERVICE_OVERRIDE:sota = "\
[Unit]\n\
After=local-fs.target var.mount\n\
Requires=local-fs.target\n\
\n\
[Service]\n\
User=postgres\n\
Environment=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\n\
ExecCondition=/usr/bin/test -d /var/lib/postgresql/data\n\
ExecCondition=+/bin/sh -c 'postgresql-setup initdb || :'\n\
"

POSTGRESQL_TMPFILES_CONF ?= ""
POSTGRESQL_TMPFILES_CONF:sota = "\
d /var/lib/postgresql/data 0700 postgres postgres -\n\
"

do_install:append () {
  if [ -n "${POSTGRESQL_SERVICE_OVERRIDE}" ]; then
    install -d ${D}${sysconfdir}/systemd/system/postgresql.service.d/
    echo "${POSTGRESQL_SERVICE_OVERRIDE}" | tee -a ${D}${sysconfdir}/systemd/system/postgresql.service.d/override.conf
  fi
  if [ -n "${POSTGRESQL_TMPFILES_CONF}" ]; then
    install -d ${D}${sysconfdir}/tmpfiles.d/
    echo "${POSTGRESQL_TMPFILES_CONF}" | tee -a ${D}${sysconfdir}/tmpfiles.d/postgresql-ostree.conf
  fi
}

FILES:${PN} += "${sysconfdir}/tmpfiles.d/ ${sysconfdir}/systemd/system/postgresql.service.d/"
