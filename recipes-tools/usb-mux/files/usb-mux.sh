#!/bin/bash -e

gpiochip=0
gpiopin=1
value=0

function usage() {
	echo "usage: usb-mux USB=[0|1] [-c <gpio chip num>] [-p <gpio pin num>]"
}

function parse_args() {
	for i in "$@"; do
		case "$i" in
		"USB=1")
			value=0
			modprobe g_mass_storage file=/dev/mmcblk2 stall=0 removable=1 || :
			;;
		"USB=0")
			value=1
			rmmod g_mass_storage || :
			;;
		"-c")
			if [ $((i+1)) -qe 0 ]; then
				gpiochip=$((i+1))
			else
				usage
				exit -22
			fi
			;;
		"-p")
			if [ $((i+1)) -ge 0 ]; then
				gpiopin=$((i+1))
			else
				usage
				exit -22
			fi
			;;
		esac
	done
}

# main
if [ $# -lt 1 ]; then
	usage
	exit -22
fi

parse_args $@
if [ -n $value ]; then
	echo "parse $@, set gpipchip: $gpiochip pin: $gpiopin to $value" >> /tmp/usb-mux.log
	gpioset $gpiochip $gpiopin=$value
else
	echo "Could not parse arguments $@"
	usage
	exit -22
fi

if [ $? -lt 0 ]; then
	echo "Failed to set usb mux gpio. error: $?" >> /tmp/usb-mux.log
	exit $?
else
	exit 0
fi

