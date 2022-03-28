FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://iptables.rpi3"

do_replace() {
  if ${@bb.utils.contains_any('MACHINE', 'raspberrypi3-64 raspberrypi3', 'true', 'false', d)} ; then
    cp -f ${WORKDIR}/iptables.rpi3 ${WORKDIR}/iptables.rules
  fi
}
addtask replace before do_configure after do_patch

