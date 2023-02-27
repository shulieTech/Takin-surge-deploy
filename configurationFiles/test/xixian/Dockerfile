FROM 10.172.49.246/skyeye-platform/stresstest/base:1.0.0
WORKDIR /data/apps/takin-surge
COPY configurationFiles/test/xixian/properties/deploy.properties /data/apps/takin-surge/deploy.properties
COPY configurationFiles/common/pinpoint-agent-2.0.3-tianyan  /data/apps/takin-surge/pinpoint-agent-2.0.3-tianyan
COPY surge-deploy-pradar-kafka/target/surge-deploy-pradar-kafka-1.0.jar /data/apps/takin-surge/takin-surge.jar
