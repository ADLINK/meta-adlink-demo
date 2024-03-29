FILESEXTRAPATHS:prepend := "${THISDIR}/linux-yocto:"

SRC_URI:append = " file://enable_eth_i225.cfg"
SRC_URI:append = " file://enable_hid_penmount.cfg"
SRC_URI:append = " file://0001-hid-penmount-patch-source-code.patch"
SRC_URI:append = " file://force_probe_i915.cfg"

