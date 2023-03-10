FROM 10.172.49.246/skyeye-platform/stresstest/base:1.0.0
WORKDIR /data/apps/takin-surge
COPY configurationFiles/test/xixian/properties/deploy.properties /data/apps/takin-surge/deploy.properties
COPY configurationFiles/common/pinpoint-agent-2.0.3-origin  /data/apps/takin-surge/pinpoint-agent-2.0.3-origin
COPY surge-deploy-pradar-kafka/target/surge-deploy-pradar-kafka-1.0.jar /data/apps/takin-surge/takin-surge.jar
COPY configurationFiles/test/xixian/start-surge.sh ./start-surge.sh
COPY configurationFiles/test/xixian/start-surge-link.sh ./start-surge-link.sh
RUN chmod +x ./start-surge.sh
RUN chmod +x ./start-surge-link.sh
