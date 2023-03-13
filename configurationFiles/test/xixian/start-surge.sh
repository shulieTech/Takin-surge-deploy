#!/bin/sh

#JAVA_OPTS=\
#"-javaagent:/data/apps/takin-amdb/pinpoint-agent-2.0.3-origin/pinpoint-bootstrap.jar \
#-Dpinpoint.agentId=${HOSTNAME:0-16} \
#-Dpinpoint.applicationName=stresstest_cloud_xixian_test \
#-Dpinpoint.licence=1d07fa023d02d60d \
#-Duser.timezone=Asia/Shanghai"

JAVA_OPTS=\
"-Duser.timezone=Asia/Shanghai \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
-Xms3g \
-Xmx3g"

java ${JAVA_OPTS} -classpath takin-surge.jar \
-Dswitcher.supplier=true  \
io.shulie.surge.data.deploy.pradar.PradarKafkaBootstrap \
--properties ./deploy.properties
