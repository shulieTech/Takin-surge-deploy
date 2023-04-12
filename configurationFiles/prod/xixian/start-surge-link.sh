#!/bin/bash

dir=$PWD
hostName=${HOSTNAME:0:16}

JAVA_OPTS=\
"-javaagent:$dir/pinpoint-agent-2.0.3-origin/pinpoint-bootstrap.jar \
-Dpinpoint.agentId=$hostName \
-Dpinpoint.applicationName=pressure_surge_link_xixian_prod \
-Dpinpoint.licence=4AHVKh0zZIp3DLkE \
-Dpinpoint.log=$dir/pinpoint-agent-2.0.3-origin/ \
-Duser.timezone=Asia/Shanghai \
-Xms3g \
-Xmx3g"

# JAVA_OPTS=\
# "-Duser.timezone=Asia/Shanghai \
# -Xms3g \
# -Xmx3g"

java ${JAVA_OPTS} -classpath takin-surge.jar \
-Dswitcher.link.task=true \
-Dswitcher.aggregation=true \
io.shulie.surge.data.deploy.pradar.PradarKafkaBootstrap \
--properties ./deploy.properties