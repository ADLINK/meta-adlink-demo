FILESEXTRAPATHS:prepend := "${THISDIR}/linux-intel:"

SRC_URI:append = " file://enable_blkdev_nbd.cfg"
SRC_URI:append = " file://force_probe_i915.cfg"
