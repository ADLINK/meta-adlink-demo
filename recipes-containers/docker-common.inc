DOCKERHUB_CHECK_CONN_URL ?= "index.docker.io"

# Check if host can reach a specific URL
# Used for connectivity check
def connected(d):
    import socket

    REMOTE_SERVER = d.getVar('DOCKERHUB_CHECK_CONN_URL', True)
    try:
        host = socket.gethostbyname(REMOTE_SERVER)
        testSocket = socket.create_connection((host, 80), 2)
        testSocket.close()
        return "yes"
    except:
        pass
    return "no"

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
        bb.fatal("docker: Requires docker to run on your host OS and correct permission for current user.\n"
                 "(If building from container, you must be able to access host OS's docker.sock to create sibling containers.)")
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
DOCKERHUB_PLATFORM_aarch64 = "arm64v8"
DOCKERHUB_PLATFORM_arm = "arm32v7"

# Docker Containers Built Locally
# e.g. LOCAL_CONTAINER_IMAGES = "container-image-lighttpd"
LOCAL_CONTAINER_IMAGES ?= ""

# Docker Data Partition
EXPORT_DOCKER_PARTITION_SIZE ?= "8192"
EXPORT_DOCKER_PARTITION_IMAGE ?= "docker-data-partition.img"

# Using dockerhub images for health check
HEALTHCHECK_EXPORT_IMAGE = "healthcheck.tar"

HEALTHCHECK_REPOSITORY_aarch64 = "arm64v8/hello-world"
HEALTHCHECK_PLATFORM_aarch64 = "linux/arm64"

HEALTHCHECK_REPOSITORY_arm = "arm32v7/hello-world"
HEALTHCHECK_PLATFORM_arm = "linux/arm"

# Target DockerHub image
TARGET_PLATFORM = "linux/amd64"
TARGET_PLATFORM_aarch64 = "linux/arm64"

