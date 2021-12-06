DESCRIPTION = "Package to create a rootfs.cpio using docker container"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

INSANE_SKIP_${PN} += "already-stripped"
SKIP_FILEDEPS = "1"
EXCLUDE_FROM_SHLIBS = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}"
B = "${S}/build"

#
# IMPORTANT:
#
# docker-rootfs recipe uses "docker" command (pre-installed in yocto build environment)
# to start a docker container to extract its complete filesystem
#
# IMPLICATION:
#
# 1. Using docker/dockerd-native for the hosttools in the bitbake architecture
#    is troublesome because docker requires a running dockerd in the hosttools,
#    and this is not easily setup within the bitbake build time. (Hence the
#    use of 'docker' command in the pre-installed yocto build environment)
#
# 2. By using pre-installed 'docker' command in yocto build environment, it
#    implies that we are using Host OS's dockerd, therefore the user(account)
#    bitbaking this docker-rootfs recipe needs access to dockerd's /var/run/docker.sock
#    on Host OS, thus allowing docker command to start a docker container.
#

#
# 3. NOTE: Running different CPU architecture dockers on x86 host
#
#    On the host x86 machine:
#
#    Install the qemu packages
#    $ sudo apt-get install qemu binfmt-support qemu-user-static
#
#    Execute the registering scripts
#    $ docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
#
#    Test the emulation environment
#    $ docker run --rm -t arm64v8/ubuntu uname -m
#    $   aarch64
#

inherit deploy
require ../docker-common.inc

python () {
    # sets the yocto recipe's Package Version
    import re
    img = d.getVar("DOCKERHUB_IMAGE", True)
    tag = d.getVar("DOCKERHUB_TAG", True)
    pv = re.sub(r"[^a-z0-9A-Z_.-]", "_", "%s-%s" % (img,tag))
    d.setVar('PV', pv)
}

# default PV to docker tag
PV ?= "${DOCKERHUB_TAG}"

# By default docker pull docker-hub's images
TARGET_DOCKERHUB_IMAGE ?= "${DOCKERHUB_IMAGE}:${DOCKERHUB_TAG}"

DOCKER_APT_PACKAGES ?= "base-files"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_package_qa[noexec] = "1"
do_populate_sysroot[noexec] = "1"

do_compile () {
  # Some sanity first for the docker variables
  if [ -z "${TARGET_DOCKERHUB_IMAGE}" -o -z "${DOCKER_PLATFORM}" ]; then
    bbfatal "docker-rootfs: DOCKER_PLATFORM, DOCKERHUB_IMAGE and/or DOCKERHUB_TAG not set."
  fi

  # At this point we really need Internet connectivity for building the docker image
  if [ "x${@connected(d)}" != "xyes" ]; then
    bbfatal "docker-rootfs: Can't do do_compile as there is no Internet connectivity on this host."
  fi

  # We force the PATH to be the standard linux path in order to use the host's
  # docker daemon instead of the result of docker-native. This avoids version
  # mismatches
  DOCKER=$(PATH="${HOME}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)

  if DOCKER_CLI_EXPERIMENTAL=enabled ${DOCKER} manifest inspect ${DOCKER_PLATFORM}/${TARGET_DOCKERHUB_IMAGE} >/dev/null; then
    bbnote "docker-rootfs: run docker image ${DOCKER_PLATFORM}/${TARGET_DOCKERHUB_IMAGE} and wait for input..."
    RANDOM=$$
    CONTAINER_NAME="${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}-$RANDOM"
    ${DOCKER} run -d --name ${CONTAINER_NAME} --rm -t "${DOCKER_PLATFORM}/${TARGET_DOCKERHUB_IMAGE}" bash -c read -n 1

    bbnote "docker-rootfs: apt install additional stuff..."
    ${DOCKER} exec ${CONTAINER_NAME} apt-get update
    ${DOCKER} exec -e DEBIAN_FRONTEND=noninteractive ${CONTAINER_NAME} apt-get install -y -o Dpkg::Options::="--force-confnew" --no-install-recommends ${DOCKER_APT_PACKAGES}
    ${DOCKER} exec ${CONTAINER_NAME} apt-get clean -y
#    ${DOCKER} exec ${CONTAINER_NAME} oem-config-prepare
    ${DOCKER} exec ${CONTAINER_NAME} useradd -s '/bin/bash' -m -G adm,sudo adlink
    ${DOCKER} exec ${CONTAINER_NAME} bash -c "echo -e 'adlink\nadlink' | passwd adlink"
    ${DOCKER} exec ${CONTAINER_NAME} bash -c "echo -e 'root\nroot' | passwd root"
    ${DOCKER} exec ${CONTAINER_NAME} bash -c "echo -e 'localhost' >> /etc/hostname"
    ${DOCKER} exec ${CONTAINER_NAME} bash -c "echo -e '127.0.0.1\tlocalhost' >> /etc/hosts"

    bbnote "docker-rootfs: export docker file system..."
    ${DOCKER} export -o ${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar ${CONTAINER_NAME}

    bbnote "docker-rootfs: kill and clean up docker container..."
    ${DOCKER} container kill ${CONTAINER_NAME}
    ${DOCKER} container prune -f

    bbnote "docker-rootfs: clean up docker image..."
    ${DOCKER} rmi -f "${DOCKER_PLATFORM}/${TARGET_DOCKERHUB_IMAGE}"
    ${DOCKER} image prune -f
  else
    bbfatal "docker-rootfs: No such image (${DOCKER_PLATFORM}/${TARGET_DOCKERHUB_IMAGE}) on docker hub"
  fi
}

do_deploy () {
  install -d ${DEPLOY_DIR_IMAGE}
  if [ -f ${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar ]; then
    install -m 644 ${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar ${DEPLOY_DIR_IMAGE}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar
  else
    bbfatal "${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar not found. Please ensure docker-rootfs exported docker container file system correctly."
  fi
}
addtask deploy before do_package after do_install

do_install[fakeroot] = "1"

fakeroot do_install_append() {
	if [ -n "${D}" ]; then
		install -d ${D}
		if [ -f ${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar ]; then
			tar xvf ${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar -C ${D}
			rm -f ${D}/.dockerenv
			rm -fr ${D}/proc ${D}/media/*
		else
			bbfatal "${B}/${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}.tar not found. Please ensure docker-rootfs exported docker containers file system as tar file correctly."
		fi
	fi
}

FILES_${PN} = "/bin /etc /lib /mnt /run /sys /usr /boot /dev /home /media /opt /root /sbin /tmp /var /srv"
FILES_${PN}-staticdev = ""
FILES_${PN}-dev = ""

