SUMMARY = "Linaro Automated Validation Architecture"
SECTION = "devel"
DESCRIPTION = "LAVA is an automated validation architecture primarily aimed at testing deployments of systems based around the Linux kernel on ARM devices, specifically ARMv7 and later."

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"
HOMEPAGE = "https://github.com/Linaro/lava.git"


SRCSERVER ?= "git://GitLab.Adlinktech.com/EV/lava.git"
SRCBRANCH ?= "2022.10"
SRCREV ?= "68545ed6ed16cff397e1ccbe62e588a835f35da7"
SRCOPTIONS = ";protocol=http"
SRCOPTIONS_append_private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"

SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"
S = "${WORKDIR}/git"

#
# packagegroup-lava contain libraries/tools needed for lava-dispatcher-host
#
RDEPENDS_${PN} = " \
    python3-pyudev (>=0.21) \
    python3-requests \
"

#
# Python builds using setuptools3
#
inherit features_check
inherit setuptools3
REQUIRED_DISTRO_FEATURES = "systemd"

DISTUTILS_BUILD_ARGS = "lava-dispatcher-host"
DISTUTILS_INSTALL_ARGS_append = " ${DISTUTILS_BUILD_ARGS}"

do_compile_prepend () {
    sed -e "s,version =.*,version = ${PV},g" -i ${S}/setup.cfg
    sed -e "s,max-line-length,max_line_length,g" -i ${S}/setup.cfg
}

FILES_${PN} = "${sysconfdir} ${libdir} ${datadir} ${bindir} ${base_libdir} ${localstatedir}"
