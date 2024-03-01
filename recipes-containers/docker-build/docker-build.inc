#
# IMPORTANT:
#
# docker-build recipe uses "docker-compose" command (pre-installed in yocto build environment)
# to build a docker image into the host dockerd, and export to tar.gz for importing
# into /var/lib/docker directory, by docker-datapart
#
# IMPLICATION:
#
# 1. Using docker/dockerd-native for the hosttools in the bitbake architecture
#    is troublesome because docker requires a dockerd to run in the hosttools,
#    and this is not easily setup within the bitbake build time. (Hence the
#    use of 'docker' command in the pre-installed yocto build environment)
#
# 2. By using pre-installed 'docker' command in yocto build environment, it
#    implies that we are using Host OS's dockerd, therefore the user(account)
#    bitbaking this docker-datapart recipe needs access to dockerd's /var/run/docker.sock
#    on Host OS, thus allowing docker command to start a sibling docker container.
#    (NOTE: the sibling docker container is base on docker:18.06-dind, see files/Dockerfile)
#
# 3. In the case where yocto build environment is running in a docker container
#    (referred to as "yocto-builder-container")
#    - From 1. it requires yocto-builder-container to install a 'docker' command (binary file).
#    - From 2. When running the "yocto-builder-container", in order to use 'docker' command
#              properly, we need to share Host OS's /var/run/docker.sock with
#              the "yocto-builder-container".
#

#
# NOTE: Running different CPU architecture dockers on x86 host
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

require ../docker-common.inc

PACKAGE_ARCH = "${MACHINE_ARCH}"

DOCKER_IMAGE_PREFIX ?= "adlink/"
DOCKER_COMPOSE_IMAGES ?= ""

# NOTE: Host Build Environment must provide docker and docker-compose commands

INSANE_SKIP:${PN} += "already-stripped"
SKIP_FILEDEPS = "1"
EXCLUDE_FROM_SHLIBS = "1"

do_patch[noexec] = "1"
do_package_qa[noexec] = "1"

do_configure () {
	# check for docker-compose.yml
	if [ ! -f ${S}/docker-compose.yml ]; then
		bbfatal "No docker-compose.yml found"
	fi
}

do_compile () {
	# At this point we really need Internet connectivity for building the docker image
	if [ "x${@connected(d)}" != "xyes" ]; then
		bbfatal "docker-build: Can't build as there is no Internet connectivity on this host."
	fi

	# We force the PATH to be the standard linux path in order to use the host's
	# docker daemon instead of the result of docker-native. This avoids version mismatches
	DOCKER=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)
	DOCKERCOMPOSE=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker-compose)

	bbnote "Remove existing docker image..."
	# docker-compose build the docker image
	for dimg in "${DOCKER_COMPOSE_IMAGES}"; do
		if $DOCKER images | grep -q -P "(^|\s)\K${DOCKER_IMAGE_PREFIX}${dimg}(?=\s|$)"; then
			$DOCKER rmi ${DOCKER_IMAGE_PREFIX}${dimg}
		fi
	done

	bbnote "Starting building docker image..."
	# docker-compose build the docker image
	if [ -z "${DOCKER_COMPOSE_IMAGES}" ]; then
		${DOCKERCOMPOSE} -f ${S}/docker-compose.yml build
	else
		for dimg in "${DOCKER_COMPOSE_IMAGES}"; do
			${DOCKERCOMPOSE} -f ${S}/docker-compose.yml build ${dimg}
		done
	fi

	bbnote "Saving built docker image..."
	# Export the Docker Image
	for dimg in ${DOCKER_COMPOSE_IMAGES}; do
		if $DOCKER images | grep ${dimg} ; then
			if which ${COMPRESSCMD} ; then
				$DOCKER image save "${DOCKER_IMAGE_PREFIX}${dimg}" | ${COMPRESSCMD} > ${B}/${dimg}.${IMAGE_COMPRESS_TYPE}
			else
				$DOCKER image save "${DOCKER_IMAGE_PREFIX}${dimg}" -o ${B}/${dimg}.tar
			fi
		else
			bbfatal "Can't find built docker image."
		fi
	done
}

FILES:${PN} += "*.tar"
