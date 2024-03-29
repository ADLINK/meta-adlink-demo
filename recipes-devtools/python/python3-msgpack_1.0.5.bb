SUMMARY = "MessagePack is an efficient binary serialization format. \
It lets you exchange data among multiple languages like JSON. \
But it's faster and smaller."
HOMEPAGE = "https://msgpack.org/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit pypi setuptools3

SRC_URI[sha256sum] = "c075544284eadc5cddc70f4757331d99dcbc16b2bbd4849d15f8aae4cf36d31c"

