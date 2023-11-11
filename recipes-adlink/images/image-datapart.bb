DESCRIPTION = "Create data partition to store raw emmc image for flashing"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "mtools-native e2fsprogs-native"

INSANE_SKIP:${PN} += "already-stripped"
SKIP_FILEDEPS = "1"
EXCLUDE_FROM_SHLIBS = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}"

python () {
    # We need INSTALLER_TARGET_IMAGE specified
    tgtimg = d.getVar("INSTALLER_TARGET_IMAGE", expand=True)
    if not tgtimg:
        bb.fatal("Please specify INSTALLER_TARGET_IMAGE to be packaged as a partition image file...\n")
    tgtpart = d.getVar("WIC_PARTITION_TYPE", expand=True)
    if not tgtpart:
        bb.fatal("Please specify WIC_PARTITION_TYPE for the packaged partition image file...\n")
}

# target images to be packaged to the partition for installer script
do_postfetch[depends] = "${INSTALLER_TARGET_IMAGE}:do_image_complete"
do_postfetch () {
  for type in ${IMAGE_FSTYPES}; do
    case ${type} in
    wic*)
      if [ -f ${DEPLOY_DIR_IMAGE}/${INSTALLER_TARGET_IMAGE}-${MACHINE}.${type} ]; then
        bbnote "copy to ${B}/${INSTALLER_TARGET_IMAGE}.${type}..."
        cp ${DEPLOY_DIR_IMAGE}/${INSTALLER_TARGET_IMAGE}-${MACHINE}.${type} ${S}/${INSTALLER_TARGET_IMAGE}.${type}
      fi
      ;;
    esac
  done
}
addtask postfetch before do_compile after do_fetch

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_package_qa[noexec] = "1"

do_compile () {
  if [ -f "${S}/${INSTALLER_TARGET_IMAGE}.wic.gz" ]; then
    INSTALLER_IMAGE_FSTYPE="wic.gz"
  elif [ -f "${S}/${INSTALLER_TARGET_IMAGE}.wic" ]; then
    INSTALLER_IMAGE_FSTYPE="wic"
  else
    bbfatal "${INSTALLER_TARGET_IMAGE}-${MACHINE}.wic[.gz] image not found. Please bitbake ${INSTALLER_TARGET_IMAGE} or set wic/wic.gz to IMAGE_FSTYPES first..."
  fi

  #
  # Get target installer image size, note: 'du -m' lists file size in mb
  IMG_FILE_SIZE=$(du -m ${S}/${INSTALLER_TARGET_IMAGE}.${INSTALLER_IMAGE_FSTYPE} | awk '{print $1}')
  bbnote "${S}/${INSTALLER_TARGET_IMAGE}.${INSTALLER_IMAGE_FSTYPE} = ${IMG_FILE_SIZE} MB..."
  # generate the partiton image with ${WIC_PARTITION_TYPE} with sufficient size, e.g. minimal 1024 + 126,
  # otherwise mkfs.ext4 error: Could not allocate block in ext2 filesystem while populating file system
  #
  if [ ${IMG_FILE_SIZE} -lt 4096 ]; then
    dd if=/dev/zero of=${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} bs=1M count=4096
  elif [ ${IMG_FILE_SIZE} -lt 8192 ]; then
    dd if=/dev/zero of=${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} bs=1M count=8192
  else
    dd if=/dev/zero of=${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} bs=1M count=16384
  fi

  if [ "${WIC_PARITION_TYPE}" = "vfat" ]; then
    # dos copy (mtools)
    mkfs -t ${WIC_PARTITION_TYPE} ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE}
    mcopy -i ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} ${INSTALLER_TARGET_IMAGE}.${INSTALLER_IMAGE_FSTYPE} ::
    if [ -f "${S}/${INSTALLER_TARGET_IMAGE}.wic.bmap" ]; then
	    mcopy -i ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} ${INSTALLER_TARGET_IMAGE}.wic.bmap ::
    fi
  elif [ "${WIC_PARTITION_TYPE}" = "ext4" ]; then
    # ext4 copy (e2tools)
    mkdir -p ${S}/mount
    cp ${INSTALLER_TARGET_IMAGE}.${INSTALLER_IMAGE_FSTYPE} ${S}/mount
    if [ -f "${S}/${INSTALLER_TARGET_IMAGE}.wic.bmap" ]; then
	    cp ${INSTALLER_TARGET_IMAGE}.wic.bmap ${S}/mount
    fi
    mkfs.ext4 -E lazy_itable_init=0,lazy_journal_init=0 -i 8192 -d ${S}/mount -F ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE}
    rm -rf ${S}/mount
  fi
}

inherit deploy
do_deploy () {
  install -d ${DEPLOY_DIR_IMAGE}
  if [ -f ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} ]; then
    install -m 644 ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} ${DEPLOY_DIR_IMAGE}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE}
  else
    bbfatal "${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} was not generated."
  fi
}
addtask deploy before do_package after do_install

do_install[fakeroot] = "1"
fakeroot do_install:append() {
  if [ -n "${TARGET_PARTITION_MOUNT}" ]; then
    install -d ${D}${TARGET_PARTITION_MOUNT}
    if [ -f ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} ]; then
      tar zxf ${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} -C ${D}${TARGET_PARTITION_MOUNT}
    else
      bbfatal "${S}/${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE} not found."
    fi
  fi
}

FILES:${PN} += "${INSTALLER_TARGET_IMAGE}_${PN}.${WIC_PARTITION_TYPE}"
