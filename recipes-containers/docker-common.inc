DOCKERHUB_CHECK_CONN_URL ?= "index.docker.io"
DOCKERHUB_ALIVE ?= "no"

# Check if host can reach a specific URL
# Used for connectivity check
def connected(d):
    import socket

    if d.getVar('DOCKERHUB_ALIVE', True) == "no":
        REMOTE_SERVER = d.getVar('DOCKERHUB_CHECK_CONN_URL', True)
        try:
            host = socket.gethostbyname(REMOTE_SERVER)
            testSocket = socket.create_connection((host, 443))
            testSocket.close()
            d.setVar('DOCKERHUB_ALIVE', 'yes')
            return "yes"
        except OSError as err:
            bb.warn("Connection to %s:443 exception: %s" % (REMOTE_SERVER, err))
            return "no"
    else:
        return "yes"

# Check if docker is running and usable for current user
def usable_docker(d):
    import subprocess

    # Test docker execute permission
    cmd = "docker images > /dev/null 2>&1"
    child = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
    cmd_output = child.communicate()[0]
    if child.returncode != 0:
        return "no"

    return "yes"

python () {
    # We need docker on host
    if usable_docker(d) != "yes":
        bb.warn("docker: Requires docker to build docker related recipes.\n(If building on Host OS, please set correct permission to docker.sock for current user.\nIf building from container, enable access to Host OS's docker.sock.)")
}

# Default storage driver type used by docker daemon, e.g. aufs, overlay2, etc
CONTAINER_STORAGE_DRIVER_TYPE ?= "overlay2"
IMAGE_COMPRESS_TYPE ?= "tar.gz"
COMPRESSCMD ?= "gzip"

# Pull docker-hub images
DOCKERHUB_TAG ?= "latest"
DOCKERHUB_IMAGE ?= "lighttpd"

# docker-hub images with specific CPU architectures
DOCKERHUB_PLATFORM ?= "amd64"
DOCKERHUB_PLATFORM:aarch64 = "arm64v8"
DOCKERHUB_PLATFORM:arm = "arm32v7"

# Docker Containers Built Locally
# e.g. LOCAL_CONTAINER_IMAGES = "container-image-lighttpd"
LOCAL_CONTAINER_IMAGES ?= ""

# Docker Data Partition
EXPORT_DOCKER_PARTITION_SIZE ?= "8192"
EXPORT_DOCKER_PARTITION_IMAGE ?= "docker-data-partition.img"

# Using dockerhub images for health check
HEALTHCHECK_EXPORT_IMAGE = "healthcheck.tar"

HEALTHCHECK_REPOSITORY:aarch64 = "arm64v8/hello-world"
HEALTHCHECK_PLATFORM:aarch64 = "linux/arm64"

HEALTHCHECK_REPOSITORY:arm = "arm32v7/hello-world"
HEALTHCHECK_PLATFORM:arm = "linux/arm"

# Target DockerHub image
TARGET_PLATFORM = "linux/amd64"
TARGET_PLATFORM:aarch64 = "linux/arm64"

