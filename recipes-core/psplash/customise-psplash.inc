FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://psplash-colors.h \
	file://psplash-bar-img.png \
"

ROTATE_PSPLASH_ANGLE ?= "0"

# NB: this is only for the main logo image; if you add multiple images here,
#     poky will build multiple psplash packages with 'outsuffix' in name for
#     each of these ...
SPLASH_IMAGES = "file://psplash-poky-img.png;outsuffix=default"

# The core psplash recipe is only designed to deal with modifications to the
# 'logo' image; we need to change the bar image too, since we are changing colors
do_configure:append () {
	cp ${WORKDIR}/psplash-colors.h ${S}/
	# strip the -img suffix from the bar png -- we could just store the
	# file under that suffix-less name, but that would make it confusing
	# for anyone updating the assets
	cp ${WORKDIR}/psplash-bar-img.png ${S}/psplash-bar.png
	${S}/make-image-header.sh ${S}/psplash-bar.png BAR
}

# rotate the psplash
# ExecStart=/bin/sh -c "s=$(/usr/sbin/fbset -i -fb /dev/fb0 | grep epdc); if [ -z "$s" ]; then i=0; else i=1; fi; if [ -e /dev/fb$i ]; then /usr/bin/psplash -a 180 -n -f $i; fi"
do_install:append () {
	if [ ${ROTATE_PSPLASH_ANGLE} -eq 180 ]; then
		sed -E "s|/usr/bin/psplash -n -f|/usr/bin/psplash -a 180 -n -f|g" -i ${D}/${systemd_system_unitdir}/psplash-start.service
	fi
}

