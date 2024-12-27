FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
	file://psplash-colors.h \
	file://psplash-bar-img.png \
"

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

