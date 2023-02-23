SUMMARY = "ADLINK XFCE Desktop Manager "
SECTION = "x11/base"
AUTHOR = "Dinesh kumar"
LICENSE = "CLOSED"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

 
SRC_URI_append = " file://adlink.jpg "	

SRC_URI += "${@bb.utils.contains('DEVICE_NAME', 'Bay-Trail', 'file://0003-Added-Adlink-wallpaper.patch  file://0004-Adlink-Default-JPG-file.patch', 'file://0001-Added-Adlink-wallpaper.patch  file://0002-Adlink-Default-JPG-file.patch', d)}"

do_configure_append() {
if ${@bb.utils.contains('DEVICE_NAME','Bay-Trail','true','false',d)}; then
cp -a ${WORKDIR}/adlink.jpg  ${WORKDIR}/xfdesktop-4.16.0/backgrounds
else
cp -a ${WORKDIR}/adlink.jpg  ${WORDIR}/xfdesktop-4.12.2/backgrounds
fi
}

FILES_${PN} += "${datadir}/backgrounds"



