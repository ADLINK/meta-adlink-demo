do_install:append () {
	install -d ${D}${nonarch_libdir}/python3/dist-packages
	for dir in $(ls ${D}${nonarch_libdir}/python3.10/site-packages); do
		ln -s -r ${D}${nonarch_libdir}/python3.10/site-packages/$(basename ${dir}) ${D}${nonarch_libdir}/python3/dist-packages/$(basename ${dir})
	done
}

