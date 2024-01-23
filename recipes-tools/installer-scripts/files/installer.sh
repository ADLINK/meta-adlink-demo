#!/bin/bash -e

screen_res=$(cat /sys/class/graphics/fb0/virtual_size | xargs)
width=${screen_res//,*/}
height=${screen_res//*,/}
w=$((width/2))
h=$((height/8))
x=250
y=150
wic_dir=
wic_file=
flash_pid=
bg_pid=
complete=0
PASSWD=

function abort_installer () {
  if ps -p $flash_pid > /dev/null; then
    kill -2 $flash_pid;
  fi;
  if ps -p $bg_pid > /dev/null; then
    kill -9 $bg_pid;
  fi;
  yad --title="Installer" --text="Flashing Aborted" --width=${w} --height=${h} --center --on-top --no-buttons --timeout=3;
  exit 1;
}

trap 'abort_installer' INT

# flash image to emmc
function flash_emmc () {
  if [ -b $2 ]; then
    if [ -x "$(which bmaptool)" ]; then
      echo "bmaptool flash_file: $1 to target: $2"
      # monitor progress bar
      rm -f /tmp/npipe
      mkfifo -m 666 /tmp/npipe
      $(while : ; do
        read line < /tmp/npipe || break;
        echo "${line//PROGRESS /} # ${line//PROGRESS /}%"
        sleep 1
      done | tee /dev/ttymxc1 | yad --progress --title="Flashing..." --text="Writing content of $1 to $2" --width=${w} --height=${h} --center --on-top --button=cancel --auto-close; kill -2 $$) &
      if [ -f "${1//.wic*/}.wic.bmap" ]; then
        echo "bmaptool with bmap file: ${1//.wic*/}.wic.bmap"
        echo -e ${PASSWD} | sudo -S bmaptool copy $1 $2 --psplash-pipe /tmp/npipe 2>/dev/null 1>/dev/null &
      else
        echo "bmaptool with --no-bmap"
        echo -e ${PASSWD} | sudo -S bmaptool copy $1 $2 --nobmap --psplash-pipe /tmp/npipe 2>/dev/null 1>/dev/null &
      fi
      flash_pid=$!
      if wait $flash_pid; then complete=1; fi
    else
      echo "dd flash_file: $1"
      if [ $1 = *"wic.gz" ]; then
        gzip -c -d $1 | dd of=$2
      else
        dd if=$1 of=$2
      fi
    fi
  fi
}

# background info dialog
yad --title="ADLINK Installer" --text="Welcome to ADLINK Installer, choose a wic image file to flash to eMMC" --maximized --no-buttons --undecorated &
bg_pid=$!

# check for root
if [ $(id -u) -ne 0 ]; then
  PASSWD=$(yad --entry --title="Authentication" --text="Please enter password to gain root permission:" --entry-text="adlink" --hide-text --width=${w} --height=${h} --center --on-top; if [ ! $? -eq 0 ]; then kill -2 $$; fi)
fi

# mount all to mount unmounted /dev/mmcblk1p3
mount -a

# file selection to choose image file
if $(mount | grep -q /dev/mmcblk1p3); then
  wic_dir=$(mount | grep /dev/mmcblk1p3 | awk '{print $3}')
  if [ -n $wic_dir -a -d $wic_dir ]; then
    if $(ls $wic_dir | grep -q "wic$"); then
      wic_file=$(ls $wic_dir | grep "wic$")
    elif $(ls $wic_dir | grep -q "wic.gz$"); then
      wic_file=$(ls $wic_dir | grep "wic.gz$")
    fi
  fi
fi

if [ -n $wic_dir -a -n $wic_file -a -f $wic_dir/$wic_file ]; then
  flash_file=$(yad --file --filename=$wic_dir/$wic_file --file-filter "Wic Image Files: *.wic *.wic.gz" --width=${w} --height=${h} --center --on-top; if [ ! $? -eq 0 ]; then kill -2 $$; fi)
elif [ -n $wic_dir -a -d $wic_dir ]; then
  flash_file=$(yad --file --filename=$wic_dir --file-filter "Wic Image Files: *.wic *.wic.gz" --width=${w} --height=${h} --center --on-top; if [ ! $? -eq 0 ]; then kill -2 $$; fi)
else
  yad --title="Error. No wic image file found, and no image partition found." --width=${w} --height=${h} --center --on-top --timeout=3
  if ps -p $bg_pid > /dev/null; then kill -9 $bg_pid; fi
  exit 1;
fi

flash_emmc $flash_file /dev/mmcblk2

if [ $complete -eq 1 ]; then
  yad --title="Flashing..." --text="Programming completed... Please power off the device..." --width=${w} --height=${h} --center --on-top --button=ok
else
  yad --title="Flashing..." --text="Programming Failed... Please try again..." --width=${w} --height=${h} --center --on-top --button=ok
fi

kill -9 $bg_pid

