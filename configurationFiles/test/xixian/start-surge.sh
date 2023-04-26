#!/bin/bash

dir=$PWD
hostName=${HOSTNAME:0:16}

JAVA_OPTS=\
"-javaagent:$dir/pinpoint-agent-2.0.3-origin/pinpoint-bootstrap.jar \
-Dpinpoint.agentId=$hostName \
-Dpinpoint.applicationName=pressure_surge_xixian_test \
-Dpinpoint.licence=1d07fa023d02d60d \
-Dpinpoint.log=$dir/pinpoint-agent-2.0.3-origin/ \
-Duser.timezone=Asia/Shanghai \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
-Xms5g \
-Xmx5g"


# JAVA_OPTS=\
# "-Duser.timezone=Asia/Shanghai \
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
# -Xms3g \
# -Xmx3g"

java ${JAVA_OPTS} -classpath takin-surge.jar \
-Dswitcher.supplier=true  \
io.shulie.surge.data.deploy.pradar.PradarKafkaBootstrap \
--properties ./deploy.properties
