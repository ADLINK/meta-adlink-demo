#!/bin/sh

set -o errexit
set -o nounset

DOCKER_TIMEOUT=20 # Wait 20 seconds for docker to start
DATA_VOLUME=/docker-partition
BUILD=/build
SRC=/src
PARTITION_SIZE=${PARTITION_SIZE:-8192}
PARTITION_IMAGE=${PARTITION_IMAGE:-docker-data-partition.img}
CONTAINER_SUFFIX=${CONTAINER_SUFFIX:-.tar.gz}
IMAGE_SUFFIX=".tar"

finish() {
	# Make all files owned by the build system
	chown -R "$USER_ID:$USER_GID" "${BUILD}"
}
trap finish EXIT

# Create user
echo "[INFO] Creating and setting $USER_ID:$USER_GID."
groupadd -g "$USER_GID" docker-datapart-group || true
useradd -u "$USER_ID" -g "$USER_GID" -p "" docker-datapart-user || true

mkdir -p $DATA_VOLUME/docker
# Start docker
echo "[INFO] Starting docker daemon with $STORAGE_DRIVER storage driver."
dockerd --data-root ${DATA_VOLUME}/docker -s "${STORAGE_DRIVER}" -b none --experimental &
echo "[INFO] Waiting for docker to become ready.."
STARTTIME="$(date +%s)"
ENDTIME="$STARTTIME"
while [ ! -S /var/run/docker.sock ]
do
    if [ $((ENDTIME - STARTTIME)) -le $DOCKER_TIMEOUT ]; then
        sleep 1 && ENDTIME=$((ENDTIME + 1))
    else
        echo "[ERROR] Timeout while waiting for docker to come up."
        exit 1
    fi
done
echo "[INFO] Start building docker-datapart..."
echo "[INFO] Passed in Params:"
echo "[INFO]        PRIVATE_REGISTRY: ${PRIVATE_REGISTRY}, PRIVATE_REGISTRY_USER: ${PRIVATE_REGISTRY_USER}, PRIVATE_REGISTRY_PASSWORD: ${PRIVATE_REGISTRY_PASSWORD}"
echo "[INFO]        TARGET_PLATFORM: ${TARGET_PLATFORM}, TARGET_REPOSITORY: ${TARGET_REPOSITORY}, TARGET_TAG: ${TARGET_TAG}"
echo "[INFO]        HEALTHCHECK_PLATFORM: ${HEALTHCHECK_PLATFORM}, HEALTHCHECK_REPOSITORY: ${HEALTHCHECK_REPOSITORY}, IMAGE_SUFFIX: ${IMAGE_SUFFIX}"
echo "[INFO]        CONTAINER_IMAGE: ${CONTAINER_IMAGE}, CONTAINER_SUFFIX: ${CONTAINER_SUFFIX}"
echo "[INFO]        SRC: ${SRC}, BUILD: ${BUILD}"

if [ -n "${PRIVATE_REGISTRY}" ] && [ -n "${PRIVATE_REGISTRY_USER}" ] && [ -n "${PRIVATE_REGISTRY_PASSWORD}" ]; then
	echo "[INFO] Private login ${PRIVATE_REGISTRY}..."
	docker login -u "${PRIVATE_REGISTRY_USER}" -p "${PRIVATE_REGISTRY_PASSWORD}" "${PRIVATE_REGISTRY}"
fi

# Pull in the docker image from dockerhub
if [ -n "${TARGET_REPOSITORY}" -a -n "${TARGET_TAG}" ]; then
    if [ -z "${TARGET_PLATFORM}" ]; then
      echo "[INFO] Pulling ${TARGET_REPOSITORY}:${TARGET_TAG}..."
      docker pull "${TARGET_REPOSITORY}:${TARGET_TAG}" || echo "[WARN] Unabe to pull from dockerhub, skip..."
    else
      echo "[INFO] Pulling --platform ${TARGET_PLATFORM} ${TARGET_REPOSITORY}:${TARGET_TAG}..."
      docker pull --platform="${TARGET_PLATFORM}" "${TARGET_REPOSITORY}:${TARGET_TAG}" || echo "[WARN] Unabe to pull from dockerhub, skip..."
    fi
fi

# Pull in arch specific hello-world image and tag it healthcheck-image
if [ -n "${HEALTHCHECK_REPOSITORY}" ]; then
  echo "[INFO] Pulling ${HEALTHCHECK_REPOSITORY}:latest..."
  docker pull --platform="${TARGET_PLATFORM}" "${HEALTHCHECK_REPOSITORY}"
  docker tag "${HEALTHCHECK_REPOSITORY}" ${HEALTHCHECK_EXPORT_IMAGE//${IMAGE_SUFFIX}}
  docker rmi "${HEALTHCHECK_REPOSITORY}"
  docker save ${HEALTHCHECK_EXPORT_IMAGE//${IMAGE_SUFFIX}} > ${BUILD}/${HEALTHCHECK_EXPORT_IMAGE}
fi

# Import the container image from local build
if [ -n "${CONTAINER_IMAGE}" ]; then
  for dimg in ${CONTAINER_IMAGE}; do
    echo "[WARN] Importing in DinD is NOT platform-aware all docker tar.gz are imported as x86_64 architecture."
    echo "[INFO] Importing ${SRC}/${dimg}..."
    if [ -f "${SRC}/${dimg}" ]; then
      docker image import ${SRC}/${dimg} ${dimg//".${CONTAINER_SUFFIX}"}:latest
    else
      echo "[WARN] ${SRC}/${dimg} does not exist"
    fi
  done
fi

echo "[INFO] Show Docker Images..."
docker images

echo "[INFO] Stop building docker-datapart..."
kill -TERM "$(cat /var/run/docker.pid)"
# don't let wait() error out and crash the build if the docker daemon has already been stopped
wait "$(cat /var/run/docker.pid)" || true

# Export the final data filesystem
echo "[INFO] Compress docker data partition..."
dd if=/dev/zero of=${BUILD}/${PARTITION_IMAGE} bs=1M count=0 seek="${PARTITION_SIZE}"
mkfs.ext4 -E lazy_itable_init=0,lazy_journal_init=0 -i 8192 -d ${DATA_VOLUME}/docker -F ${BUILD}/${PARTITION_IMAGE}
tar zcf ${BUILD}/${PARTITION_IMAGE}.${CONTAINER_SUFFIX} -C ${DATA_VOLUME}/docker .
ls -l ${BUILD}/
