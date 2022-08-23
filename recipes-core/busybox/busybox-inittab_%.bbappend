# add S1:12345:respawn:/sbin/getty 115200 ttyS1 vt100 to end of inittab

do_install:append () {
        for i in $tmp
        do
                j=`echo ${i} | sed s/\;/\ /g`
                id=`echo ${i} | sed -e 's/^.*;//' -e 's/;.*//' -e 's/tty//'`
                echo "$id:12345:respawn:${base_sbindir}/getty -L ${j}" >> ${D}${sysconfdir}/inittab
        done
}
