#!/bin/bash -e

TARGET=${1:-genericx86-64}

if [ -f ${BUILDDIR}/conf/local.conf ]; then
  if ! grep -q "^BBMULTICONFIG" ${BUILDDIR}/conf/local.conf; then
    echo "# setup BBMULTICONFIG in local.conf for conf/multiconfig/${TARGET}.conf" | tee -a ${BUILDDIR}/conf/local.conf
    echo "BBMULTICONFIG = \"${TARGET}\"" >> ${BUILDDIR}/conf/local.conf
  else
    echo "BBMULTICONFIG already defined in local.conf"
  fi
  if [ ! -f ${BUILDDIR}/conf/multiconfig/${TARGET}.conf ]; then
    mkdir -p ${BUILDDIR}/conf/multiconfig
    cat > ${BUILDDIR}/conf/multiconfig/${TARGET}.conf << EOF
MACHINE = "${TARGET}"
TMPDIR = "\${TOPDIR}/tmp-${TARGET}"
EOF
  fi
  if [ -f ${BUILDDIR}/conf/multiconfig/${TARGET}.conf ]; then
	if [ "${MACHINE}" = "arm-container" ]; then
		echo "DISTRO = \"poky\"" >> ${BUILDDIR}/conf/multiconfig/${TARGET}.conf
		echo "DISTRO_FEATURES_append = \" virtualization\"" >> ${BUILDDIR}/conf/multiconfig/${TARGET}.conf
		echo "IMAGE_COMPRESS_TYPE ?= \"tar.gz\"" >> ${BUILDDIR}/conf/multiconfig/${TARGET}.conf
	fi
  fi
else
  echo "No local.conf found"
fi
