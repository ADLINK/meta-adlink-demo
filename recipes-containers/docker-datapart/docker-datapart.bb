DESCRIPTION = "Package to create Docker data partition image using sibling container"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

INSANE_SKIP_${PN} += "already-stripped"
SKIP_FILEDEPS = "1"
EXCLUDE_FROM_SHLIBS = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI += " \
	file://Dockerfile \
	file://entry.sh \
	"

S = "${WORKDIR}"
B = "${S}/build"

#
# IMPORTANT:
#
# docker-datapart recipe uses "docker" command (pre-installed in yocto build environment)
# to start a docker-in-docker container for generating the require /var/lib/docker
# directory, setup and utilised by the docker daemon (dockerd)
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
# 4. In "yocto-builder-container", when bitbaking docker-datapart recipe, a sibling
#    container (i.e. another container) is created on the Host OS, because we
#    are using dockerd's /var/run/docker.sock on Host OS.
#    (i.e. imagining a sister "sibling-container" is started on the Host OS next
#    to the "yocto-builder-contaier")
#
#      +--------------------------------------+
#      | Host OS (dockerd)                    |
#      |                                      |
#      | +----------+          +-----------+  |
#      | | yocto-   |     +--> | sibling-  |  |
#      | | builder  |     |    | container |  |
#      | |          |     |    |           |  |
#      | | $docker  |-----+    |           |  |
#      | +----------+  starts  +-----------+  |
#      |                                      |
#      +--------------------------------------+
#
# 5. From 4, when doing do_install() and do_deploy() tasks of docker-datapart recipe
#     in the "yocto-builder-container", both "yocto-builder-container" and
#    "sibling-container" containers must share a OUTPUT_IMAGE_DIR directory on
#    Host OS, otherwise no files can be found within either containers.
#
#      +----------------------------------------------------------+
#      | Host OS (dockerd)    /path/to/shared/OUTPUT_IMAGE_DIR    |
#      |                           |                              |
#      | +------------------+      |      +--------------------+  |
#      | | yocto-builder    |      |      | sibling-container  |  |
#      | |                  |      |      |                    |  |
#      | |                  |      |      |                    |  |
#      | | ${MOUNT_DIR}/    |      |      | ${MOUNT_PATH}/     |  |
#      | |   OUT_IMAGE_DIR  | <----+----> |   OUT_IMAGE_DIR    |  |
#      | +------------------+             +--------------------+  |
#      |                                                          |
#      +----------------------------------------------------------+
#
#    Consequently, DOCKER_SHAREDIR variable is introduced to set /path/to/shared/
#    path on the Host OS (visible and common to both sibling containers)
#
#    For example, on the jenkins server which runs yocto builds in a
#    "yocto-builder-container". Set the following in local.conf
#
#    DOCKER_SHAREDIR = "/home/admin/jenkins_home"
#

#
# 6. NOTE: Running different CPU architecture dockers on x86 host
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

DOCKER_SHAREDIR ?= "${HOME}"
SHAREDSRC ?= "${@ '%s' % (d.getVar('S', True).replace(d.getVar('HOME', True), d.getVar('DOCKER_SHAREDIR', True)))}"
SHAREDBUILD ?= "${@ '%s' % (d.getVar('B', True).replace(d.getVar('HOME', True), d.getVar('DOCKER_SHAREDIR', True)))}"

inherit deploy
require ../docker-common.inc

TARGET_REPOSITORY ?= "${DOCKERHUB_PLATFORM}/${DOCKERHUB_IMAGE}"
TARGET_TAG ?= "${DOCKERHUB_TAG}"

python () {
    pv = d.getVar("PV", True)
    imgcomp = d.getVar("IMAGE_COMPRESS_TYPE", True)
    exportimages = []
    if d.getVar("LOCAL_CONTAINER_IMAGES", True) is not None:
        for img in d.getVar("LOCAL_CONTAINER_IMAGES", True).split():
            exportimages.append('%s.%s' % (img, imgcomp))
    d.setVar('EXPORT_CONTAINER_IMAGES', " ".join(exportimages))
}

# multiple dependancies on container images
DEPENDS += "${LOCAL_CONTAINER_IMAGES}"
do_postfetch () {
	mkdir -p ${S}/container
	if [ -z "${LOCAL_CONTAINER_IMAGES}" ]; then
		bbwarn "Skip bundling local built containers..."
	else
		echo "EXPORT_CONTAINER_IMAGES: ${EXPORT_CONTAINER_IMAGES}"
		for img in ${EXPORT_CONTAINER_IMAGES}; do
			if [ -e ${TMPDIR}/deploy/images/${MACHINE}/${img} ]; then
				install -m 644 ${TMPDIR}/deploy/images/${MACHINE}/${img} ${S}/container/${img}
			else
				bbfatal "${TMPDIR}/deploy/images/${MACHINE}/${img} not found.\nplease bitbake ${img} separately..."
			fi
		done
	fi
}
addtask postfetch before do_compile and after do_fetch

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_package_qa[noexec] = "1"

do_compile () {
	# Some sanity first for the target/docker variables
	if [ -z "${TARGET_REPOSITORY}" -o -z "${TARGET_TAG}" ]; then
		bbwarn "docker-datapart: Dockerhub TARGET_REPOSITORY and/or TARGET_TAG not set. Remote docker container will not be bundled."
	fi
	if [ -z "${EXPORT_CONTAINER_IMAGES}" ]; then
		bbwarn "docker-datapart: LOCAL_CONTAINER_IMAGES not set. No local container will be bundled."
	fi
	if [ -z "${EXPORT_DOCKER_PARTITION_SIZE}" ]; then
		bbfatal "docker-datapart: EXPORT_DOCKER_PARTITION_SIZE needs to have a value (megabytes)."
	fi
	if [ -z "${EXPORT_DOCKER_PARTITION_IMAGE}" ]; then
		bbfatal "docker-datapart: EXPORT_DOCKER_PARTITION_IMAGE needs to have an image name."
	fi
	# At this point we really need Internet connectivity for building the docker image
	if [ "x${@connected(d)}" != "xyes" ]; then
		bbfatal "docker-datapart: Can't compile as there is no Internet connectivity on this host."
	fi

	# We force the PATH to be the standard linux path in order to use the host's
	# docker daemon instead of the result of docker-native. This avoids version
	# mismatches
	DOCKER=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)

	bbnote "docker-datapart: starting docker..."
	bbnote "docker-datapart: SHAREDSRC=${SHAREDSRC}/container"
	bbnote "docker-datapart: SHAREDBUILD=${SHAREDBUILD}"

	# Generate the data filesystem (i.e. /var/lib/docker) using sibling container with docker daemon
	RANDOM=$$
	IMAGE_NAME="docker-datapart-$RANDOM"
	CONTAINER_NAME="${DOCKERHUB_IMAGE}-${DOCKERHUB_TAG}-$RANDOM"
	$DOCKER rmi ${IMAGE_NAME} > /dev/null 2>&1 || true
	$DOCKER build -t ${IMAGE_NAME} -f ${WORKDIR}/Dockerfile ${WORKDIR}
	$DOCKER run --privileged --rm multiarch/qemu-user-static --reset -p yes
	$DOCKER run --privileged --rm \
		-e STORAGE_DRIVER=${CONTAINER_STORAGE_DRIVER_TYPE} \
		-e USER_ID=$(id -u) -e USER_GID=$(id -u) \
		-e TARGET_PLATFORM="${TARGET_PLATFORM}" \
		-e TARGET_REPOSITORY="${TARGET_REPOSITORY}" \
		-e TARGET_TAG="${TARGET_TAG}" \
		-e CONTAINER_IMAGE="${EXPORT_CONTAINER_IMAGES}" \
		-e CONTAINER_SUFFIX="${IMAGE_COMPRESS_TYPE}" \
		-e HEALTHCHECK_REPOSITORY="${HEALTHCHECK_REPOSITORY}" \
		-e HEALTHCHECK_PLATFORM="${HEALTHCHECK_PLATFORM}" \
		-e HEALTHCHECK_EXPORT_IMAGE="${HEALTHCHECK_EXPORT_IMAGE}" \
		-e DOCKERHUB_REGISTRY="${DOCKERHUB_REGISTRY}" \
		-e DOCKERHUB_USER="${DOCKERHUB_USER}" \
		-e DOCKERHUB_PASSWORD="${DOCKERHUB_PASSWORD}" \
		-e PARTITION_SIZE="${EXPORT_DOCKER_PARTITION_SIZE}" \
		-e PARTITION_IMAGE="${EXPORT_DOCKER_PARTITION_IMAGE}" \
		-v /sys/fs/cgroup:/sys/fs/cgroup:ro \
		-v ${SHAREDSRC}/container:/src \
		-v ${SHAREDBUILD}:/build \
		--name ${CONTAINER_NAME} ${IMAGE_NAME}
	$DOCKER rmi ${IMAGE_NAME}
}

do_deploy () {
	install -d ${DEPLOY_DIR_IMAGE}
	if [ -f ${B}/${EXPORT_DOCKER_PARTITION_IMAGE} ]; then
		install -m 644 ${B}/${EXPORT_DOCKER_PARTITION_IMAGE} ${DEPLOY_DIR_IMAGE}/${EXPORT_DOCKER_PARTITION_IMAGE}
	else
		bbfatal "${B}/${EXPORT_DOCKER_PARTITION_IMAGE} not found. Please ensure docker-datapart exported docker container images correctly. (Please also check your DOCKER_SHAREDIR setting is accessible for both build and sibling container environments"
	fi
	if [ -f ${B}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} ]; then
		install -m 644 ${B}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} ${DEPLOY_DIR_IMAGE}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE}
	fi
}
addtask deploy before do_package after do_install


do_install[fakeroot] = "1"

fakeroot do_install_append() {
	if [ -n "${DOCKER_PARTITION_MOUNT_PATH}" ]; then
		install -d ${D}${DOCKER_PARTITION_MOUNT_PATH}
		if [ -f ${B}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} ]; then
			tar zxf ${B}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} -C ${D}${DOCKER_PARTITION_MOUNT_PATH}
		else
			bbfatal "${B}/${EXPORT_DOCKER_PARTITION_IMAGE}.${IMAGE_COMPRESS_TYPE} not found. Please ensure docker-datapart exported docker containers directory as tar.gz file correctly. (Please also check your DOCKER_SHAREDIR setting is accessible for both build and sibling container environment)"
		fi
	fi
}

FILES_${PN} += "${DOCKER_PARTITION_MOUNT_PATH}"
