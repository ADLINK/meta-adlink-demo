#!/bin/bash -e

TARGET=${1:-genericx86-64}

if [ -f ${BUILDDIR}/conf/local.conf ]; then
  if ! grep -q "^BBMULTICONFIG" $BUILDDIR/conf/local.conf; then
    mkdir -p ${BUILDDIR}/conf/multiconfig
    cat > ${BUILDDIR}/conf/multiconfig/${TARGET}.conf << EOF
MACHINE = "${TARGET}"
TMPDIR = "\${TOPDIR}/tmp-${TARGET}"
EOF
    echo "# setup BBMULTICONFIG in local.conf for conf/multiconfig/${TARGET}.conf" | tee -a ${BUILDDIR}/conf/local.conf
    echo "BBMULTICONFIG = \"${TARGET}\"" >> ${BUILDDIR}/conf/local.conf
  else
    echo "BBMULTICONFIG already defined in local.conf"
  fi
else
  echo "No local.conf found"
fi
