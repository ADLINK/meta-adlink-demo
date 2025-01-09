python () {
  if "-cn-" in d.getVar('PN', True):
    d.setVar("CCODE", "CN")
    d.setVar("CNAME", "cn")
  elif "-jp-" in d.getVar('PN', True):
    d.setVar("CCODE", "JP")
    d.setVar("CNAME", "jp")
  elif "-kr-" in d.getVar('PN', True):
    d.setVar("CCODE", "KR")
    d.setVar("CNAME", "kr")
  elif "-tw-" in d.getVar('PN', True):
    d.setVar("CCODE", "TW")
    d.setVar("CNAME", "tw")
  else:
    bb.error("Package Name does not container cn, jp, kr, or tw")
}

SRC_URI = "https://github.com/adobe-fonts/source-han-sans/releases/download/2.004R/SourceHanSans${CCODE}.zip file://44-source-han-sans-${CNAME}.conf"

