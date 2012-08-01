#!/bin/sh
#
#

# Enable jmx
JMX="-Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4711 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

# Memory dump on exit vm:
IBMMEMDUMP="-Xdump:system+heap+java:events=vmstop,request=exclusive+prepwalk+compact"

# Enable logging
LOG="-Djava.util.logging.config.file=logging.properties"

# Add $JMX,$IBMMEMDUMP or $LOG if need

rm nohup.out
nohup java $LOG  -cp logmonModMail.jar:logmon.jar:lib/mail.jar app.LogMon --config=config.xml  &


