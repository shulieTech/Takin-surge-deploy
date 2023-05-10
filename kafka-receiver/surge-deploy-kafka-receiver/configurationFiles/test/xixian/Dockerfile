FROM 10.172.49.246/skyeye-platform/stresstest/base:1.0.0
WORKDIR /data/apps/kafka-receiver
COPY configurationFiles/common/pinpoint-agent-2.0.3-origin  ./pinpoint-agent-2.0.3-origin
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/test/xixian/properties/application.yml ./config/application.yml
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/test/xixian/properties/kafka.properties ./config/kafka.properties
COPY kafka-receiver/surge-deploy-kafka-receiver/target/surge-deploy-kafka-receiver-1.0.0.jar ./takin-kafka-receiver.jar
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/test/xixian/start.sh ./start.sh
RUN chmod +x ./start.sh
