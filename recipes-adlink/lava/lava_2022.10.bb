SUMMARY = "Linaro Automated Validation Architecture"
SECTION = "devel"
DESCRIPTION = "LAVA is an automated validation architecture primarily aimed at testing deployments of systems based around the Linux kernel on ARM devices, specifically ARMv7 and later."

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"
HOMEPAGE = "https://github.com/Linaro/lava.git"

SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}"
S = "${WORKDIR}/git"

SRCSERVER ?= "git://GitLab.Adlinktech.com/EV/lava.git;protocol=http"
SRCBRANCH ?= "2022.10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGES = " \
    lava-common \
    lava-coordinator \
    lava-dispatcher \
    lava-dispatcher-host \
    lava-server \
    lava \
"

#
# packagegroup-lava contain libraries/tools needed for lava
#
RDEPENDS:packagegroup-lava = " \
    ${@python .gitlab-ci/build/debian/check-debian-deps.py --suite bullseye --package lava-common _build/lava-common_*bullseye*} \
    ${@python .gitlab-ci/build/debian/check-debian-deps.py --suite bullseye --package lava-dispatcher _build/lava-dispatcher_*bullseye*} \
    ${@python .gitlab-ci/build/debian/check-debian-deps.py --suite bullseye --package lava-dispatcher-host _build/lava-dispatcher-host_*bullseye*} \
    ${@python .gitlab-ci/build/debian/check-debian-deps.py --suite bullseye --package lava-server _build/lava-server_*bullseye*} \
"

#
# packagegroup-lava contain libraries/tools needed for lava-common
#
RDEPENDS:packagegroup-lava-common = " \
    python3-voluptuous (>=0.8.8) \
    python3-yaml \
"

#
# packagegroup-lava contain libraries/tools needed for lava-coordinator
#
RDEPENDS:packagegroup-lava-coordinator = ""

#
# packagegroup-lava contain libraries/tools needed for lava-dispatcher
#
RDEPENDS:packagegroup-lava-dispatcher = " \
    bmap-tools \
    python3-aiohttp \
    python3-configobj \
    python3-guestfs \
    python3-jinja2 \
    python3-magic \
    python3-netifaces \
    python3-pexpect (>=4.2) \
    python3-pyudev (>=0.21) \
    python3-requests \
    python3-setproctitle (>=1.1.8) \
    python3-tz \
    python3-yaml \
"

#
# packagegroup-lava contain libraries/tools needed for lava-dispatcher-host
#
RDEPENDS:packagegroup-lava-dispatcher-host = " \
    python3-pyudev (>=0.21) \
    python3-requests \
"

#
# packagegroup-lava contain libraries/tools needed for lava-server
#
RDEPENDS:packagegroup-lava-server = " \
    python3-aiohttp \
    python3-celery \
    python3-django (>=1.10) \
    python3-django-allauth \
    python3-django-auth-ldap (>=1.2.12) \
    python3-django-environ \
    python3-django-filters \
    python3-django-tables2 (>=1.14.2) \
    python3-djangorestframework \
    python3-djangorestframework-extensions \
    python3-djangorestframework-filters \
    python3-docutils (>=0.6) \
    python3-eventlet \
    python3-jinja2 \
    python3-junit.xml (>=1.8) \
    python3-psycopg2 \
    python3-requests \
    python3-simplejson \
    python3-tap \
    python3-tz \
    python3-voluptuous (>=0.8.8) \
    python3-whitenoise \
    python3-yaml \
    python3-zmq \
"

#
# Python builds using setuptools3
#
inherit setuptools3

REQUIRED_DISTRO_FEATURES = "systemd"

# setup.py from lava src
# PKGS = {
#    "lava-common": COMMON,
#    "lava-coordinator": COORDINATOR,
#    "lava-dispatcher": DISPATCHER,
#    "lava-dispatcher-host": DISPATCHER_HOST,
#    "lava-server": SERVER,
# }
DISTUTILS_BUILD_ARGS:packagegroup-lava-common = "lava-common"
DISTUTILS_BUILD_ARGS:packagegroup-lava-dispatcher = "lava-dispatcher"
DISTUTILS_BUILD_ARGS:packagegroup-lava-dispatcher-host = "lava-dispatcher-host"
DISTUTILS_BUILD_ARGS:packagegroup-lava-server = "lava-server"
DISTUTILS_BUILD_ARGS:packagegroup-lava-coordinator = "lava-coordinator"
DISTUTILS_BUILD_ARGS:packagegroup-lava = ""

