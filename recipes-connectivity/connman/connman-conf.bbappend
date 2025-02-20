
STATIC_NIC_IPV4_SETTINGS ?= ""

do_install:append() {
  if [ -n "${STATIC_NIC_IPV4_SETTINGS}" ]; then
    install -d ${D}${sysconfdir}/connman
    install -m 0644 ${S}/main.conf ${D}${sysconfdir}/connman/main.conf
  fi
}

