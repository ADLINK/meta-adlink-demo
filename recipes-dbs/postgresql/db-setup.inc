# === initialize db ===
# sudo -i -u postgres initdb -D /var/lib/postgresql/data
# === pgsql config ===
# nano /var/lib/postgresql/data/postgresql.conf
#       listen_addresses = '*'          # what IP address(es) to listen on;
#                                       # comma-separated list of addresses;
#                                       # defaults to 'localhost'; use '*' for all
#       port = 5432                     # (change requires restart)
# nano /var/lib/postgresql/data/pg_hba.conf:
# host    all             all             0.0.0.0/0          md5
# host    all             all             0.0.0.0/0          trust
# host    all             all             ::/0               trust
# === change user profile ===
# $ su postgres -c 'psql \password'
# === database ===
# $ su postgres -c 'createuser adlink -P --interactive'
# Enter password for new role:
# Enter it again:
# Shall the new role be a superuser? (y/n) n
# Shall the new role be allowed to create databases? (y/n) y
# Shall the new role be allowed to create more new roles? (y/n) y
# === create db tables ===
# CREATE DATABASE mydatabase;
# CREATE USER myuser WITH ENCRYPTED PASSWORD 'mypassword';
# GRANT ALL PRIVILEGES ON DATABASE mydatabase TO myuser;

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://db-setup.service"

SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install:append () {
    # Install systemd unit files
    install -d ${D}${systemd_unitdir}/system
    install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants/
    install -m 0644 ${WORKDIR}/db-setup.service ${D}${systemd_unitdir}/system
    # enable the service manually
    ln -sf ${systemd_unitdir}/system/db-setup.service ${D}${sysconfdir}/systemd/system/multi-user.target.wants/db-setup.service
}

pkg_postinst_ontarget:${PN} () {
    #su -l postgres -c "initdb -D /var/lib/postgresql/data;"
    postgresql-setup initdb
    # su -l postgres -c "createuser adlink -s -d -r -w; createuser myuser -w; createdb mydatabase -O myuser;"
    if [ -f /var/lib/postgresql/data/postgresql.conf ]; then
        printf "\nlisten_addresses = '*'\n" >> /var/lib/postgresql/data/postgresql.conf
    fi
    if [ -f /var/lib/postgresql/data/pg_hba.conf ]; then
        printf "\nhost\tall\tall\t0.0.0.0/0\tmd5\n" >> /var/lib/postgresql/data/pg_hba.conf
        printf "host\tall\tall\t0.0.0.0/0\ttrust\n" >> /var/lib/postgresql/data/pg_hba.conf
        printf "host\tall\tall\t::/0\ttrust\n" >> /var/lib/postgresql/data/pg_hba.conf
    fi
}

FILES:${PN} += "${systemd_unitdir}/system ${sysconfdir}/systemd/system/multi-user.target.wants/"
