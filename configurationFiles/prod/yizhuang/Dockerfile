FROM harbor.dcos.xixian.unicom.local/zongbu_tianyan_test/stresstest/base:1.0.0
WORKDIR /data/apps/takin-surge
COPY configurationFiles/prod/yizhuang/properties/deploy.properties /data/apps/takin-surge/deploy.properties
COPY configurationFiles/common/pinpoint-agent-2.0.3-origin  /data/apps/takin-surge/pinpoint-agent-2.0.3-origin
COPY surge-deploy-pradar-kafka/target/surge-deploy-pradar-kafka-1.0.jar /data/apps/takin-surge/takin-surge.jar
