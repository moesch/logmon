#!/bin/sh

rm nohup.out

nohup java -Djava.util.logging.config.file=logging.properties \
	-cp logmonModMail.jar:logmon.jar:lib/mail.jar \
	app.LogMon --config=config.xml  &


