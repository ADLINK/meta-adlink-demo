DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "gitsm://github.com/po-cheng-adlink/lava-lab.git"
SRCBRANCH = "ci-box-builder"
SRCOPTIONS = ";protocol=https"
SRCOPTIONS_append_private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "0c6b04fde18d86a1a26ea94b26eca18d056f18d9"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

include docker-build.inc

DOCKER_COMPOSE_IMAGE = "lava-worker"

do_configure_prepend () {
	cd ${S}
	./ci-box-gen.sh
}

inherit deploy

do_deploy () {
	install -d ${DEPLOY_DIR_IMAGE}
	if which ${COMPRESSCMD} ; then
		if [ -f ${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} ]; then
			install -m 644 ${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} ${DEPLOY_DIR_IMAGE}/${PN}.${IMAGE_COMPRESS_TYPE}
		else
			bbfatal "${B}/${DOCKER_COMPOSE_IMAGE}.${IMAGE_COMPRESS_TYPE} not found."
		 fi
	else
		if [ -f ${B}/${DOCKER_COMPOSE_IMAGE}.tar ]; then
			install -m 644 ${B}/${DOCKER_COMPOSE_IMAGE}.tar ${DEPLOY_DIR_IMAGE}/${PN}.tar
		else
			bbfatal "${B}/${DOCKER_COMPOSE_IMAGE}.tar not found."
		fi
	fi
}
addtask deploy before do_package after do_compile
