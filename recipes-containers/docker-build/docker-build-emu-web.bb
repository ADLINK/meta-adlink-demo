DESCRIPTION = "Package to create Docker Image using Dockerfile and docker-compose for EGiFlow front end docker"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"
SRCSERVER = "git://GitLab.Adlinktech.com/SEC_IST_CROSS_TEAM/emu-web.git"
SRCBRANCH = "sp2-demo"
SRCOPTIONS = ";protocol=http"
SRCOPTIONS:append:private = ";user=${PRIVATE_USER}:${PRIVATE_TOKEN}"
SRCREV = "94dab694a4810fbdb10f536741f90055c3afd5c6"
SRC_URI = "${SRCSERVER};branch=${SRCBRANCH}${SRCOPTIONS}"

SRC_URI += "file://emu-web.yaml.template"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

DOCKER_COMPOSE_IMAGES ?= "emu-web"

include docker-build.inc

do_configure[prefuncs] += "reconfigure_emu_web"
python reconfigure_emu_web() {
    dcomp = None
    workdir = d.getVar("WORKDIR")
    srcdir = d.getVar("S")
    tgtplatform = d.getVar("TARGET_PLATFORM")
    tgtimages =  d.getVar("DOCKER_COMPOSE_IMAGES")
    prefix = d.getVar("DOCKER_IMAGE_PREFIX")
    if tgtplatform in ('linux/arm64', 'linux/amd64'):
      with open("%s/emu-web.yaml.template" % workdir, "r+") as fd:
        import yaml
        dcomp = yaml.safe_load(fd)
        for service in dcomp["services"]:
          for tgtimage in tgtimages.split():
            if tgtimage == service:
              dcomp['services'][tgtimage]['build']['args']['tgtplatform'] = tgtplatform
              dcomp['services'][tgtimage]['image'] = "%s%s" % (prefix, tgtimage)
              break
      if dcomp:
        with open("%s/docker-compose.yml" % srcdir, "w") as f:
          yaml.safe_dump(dcomp, f, default_flow_style=False)
      else:
        bb.error("Error: Substituting docker-compose template: %s/emu-web.yaml.template" % workdir)
    else:
      bb.warn("TARGET_PLATFORM: %s not recognized" % tgtplatform)
}

do_configure:prepend () {
	# copy the docker/dev.dockerfile to docker/dockerfile and add the platform arguments
	cp ${S}/docker/dev.dockerfile ${S}/dockerfile
	sed -e 's,^FROM.*,ARG tgtplatform=linux/amd64\nFROM --platform=${tgtplatform} node:16-bullseye-slim,g' -i ${S}/dockerfile
}

DOCKER_BASE_IMAGES ?= "node:16-bullseye-slim nginx:alpine"
do_clear_base () {
	# We force the PATH to be the standard linux path in order to use the host's
	# docker daemon instead of the result of docker-native. This avoids version mismatches
	DOCKER=$(PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" which docker)

	bbnote "Remove existing base images..."
	for dimg in ${DOCKER_BASE_IMAGES}; do
		if $DOCKER images --format '{{.Repository}}:{{.Tag}}' | grep -q "${dimg}"; then
			$DOCKER rmi ${dimg}
		fi
	done
}
addtask clear_base before do_compile after do_configure

inherit deploy

do_deploy () {
	for dimg in ${DOCKER_COMPOSE_IMAGES}; do
		if which ${COMPRESSCMD} ; then
			if [ -f ${B}/${dimg}.${IMAGE_COMPRESS_TYPE} ]; then
				install -m 0644 ${B}/${dimg}.${IMAGE_COMPRESS_TYPE} ${DEPLOYDIR}/docker-build-${dimg}.${IMAGE_COMPRESS_TYPE}
			else
				bbfatal "${B}/${dimg}.${IMAGE_COMPRESS_TYPE} not found."
			fi
		else
			if [ -f ${B}/${dimg}.tar ]; then
				install -m 0644 ${B}/${dimg}.tar ${DEPLOYDIR}/docker-build-${dimg}.tar
			else
				bbfatal "${B}/${dimg}.tar not found."
			fi
		fi
	done
}
addtask deploy before do_package after do_compile

do_install () {
	mkdir -p ${D}/containers
	# copy dockerfile/docker-compose.yml for docker-startup-service
	if [ -f ${S}/dockerfile ]; then
		install -m 0644 ${S}/dockerfile ${D}/containers/dockerfile
	fi
	if [ -f ${S}/docker-compose.yml ]; then
		install -m 0644 ${S}/docker-compose.yml ${D}/containers/docker-compose.yml
	fi
}

FILES:${PN} += "/containers"

