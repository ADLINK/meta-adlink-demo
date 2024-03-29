#!/bin/sh

echo "Setting up iptables..."

iptables -t nat -A POSTROUTING -j MASQUERADE

echo "Waiting for pipework to give us the eth1 interface..."

./pipework --wait

IP=$(ip addr show dev eth1 | awk -F '[ /]+' '/global/ {print $3}')
SUBNET=$(echo $myIP | cut -d '.' -f 1,2,3)

echo "Starting DHCP+TFTP server..."

dnsmasq --interface=eth1 \
	--dhcp-range=$SUBNET.101,$SUBNET.199,255.255.255.0,1h \
	--dhcp-boot=pxelinux.0,pxeserver,$IP \
	--pxe-service=x86PC,"Install Linux",pxelinux \
	--enable-tftp --tftp-root=/tftp/ --no-daemon

