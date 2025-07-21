ROOTFS_POSTPROCESS_COMMAND:append = " ipv4forward_sysctl_config; "

ipv4forward_sysctl_config() {
    test -d ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d && \
        echo "net.ipv4.ip_forward = 1" > ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d/ipv4-forward.conf
    test -f ${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf && \
        sed -e "/net.ipv4.ip_forward/d" -i ${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf; \
        echo "net.ipv4.ip_forward = 1" >> ${IMAGE_ROOTFS}${sysconfdir}/sysctl.conf;
}
