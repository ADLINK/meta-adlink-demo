RPCBIND_SERVICE_OVERRIDE ?= ""
RPCBIND_SERVICE_OVERRIDE:sota = "\
[Unit]\n\
After=local-fs.target var.mount ostree-finalize-staged.path\n\
Requires=local-fs.target\n\
"

do_install:append () {
  if [ -n "${RPCBIND_SERVICE_OVERRIDE}" ]; then
    install -d ${D}${sysconfdir}/systemd/system/rpcbind.service.d/
    echo "${RPCBIND_SERVICE_OVERRIDE}" | tee -a ${D}${sysconfdir}/systemd/system/rpcbind.service.d/override.conf
  fi
}

FILES:${PN} += "${sysconfdir}/systemd/system/rpcbind.service.d/"
