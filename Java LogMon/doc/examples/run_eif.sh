#!/bin/sh

java -Djava.util.logging.config.file=logging.properties \
    -cp logmonModEIF.jar:logmon.jar:lib/evd.jar:lib/log.jar \
    app.LogMon --config=config_lx.xml


