#!/bin/sh -e

OUT_TTY=$(tty)
TMP_COUNT=/tmp/powerbtn.count
ACPI_EVENT=/etc/acpi/events/power
COUNT_SCRIPT=/etc/acpi/power.sh

echo "0" > $TMP_COUNT

echo "event=button/power PBTN" >> $ACPI_EVENT
echo "action=$COUNT_SCRIPT" >> $ACPI_EVENT

echo -e "#!/bin/sh" > $COUNT_SCRIPT
echo -e "COUNTER=\$(cat $TMP_COUNT)" >> $COUNT_SCRIPT
echo -e "COUNTER=\$(( COUNTER+1 ))" >> $COUNT_SCRIPT
echo -e "echo \"\$COUNTER\" > $TMP_COUNT" >> $COUNT_SCRIPT
echo -e "echo \"Power Button Pressed. Current Count: \$COUNTER\" > $OUT_TTY" >> $COUNT_SCRIPT
echo -e "logger -s -p daemon.notice -t ACPIEVENT \"Power Button Pressed. Current Count: \$COUNTER\" 2>> /home/root/counter.log" >> $COUNT_SCRIPT
chmod +x $COUNT_SCRIPT
rm -f /home/root/counter.log

/etc/init.d/acpid stop
sleep 1
/etc/init.d/acpid start

