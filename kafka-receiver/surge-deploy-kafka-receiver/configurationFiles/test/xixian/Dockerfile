FROM 10.172.49.246/skyeye-platform/stresstest/base:1.0.0
WORKDIR /data/apps/kafka-receiver
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/test/xixian/properties/application.yml /data/apps/kafka-receiver/config/application.yml
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/test/xixian/properties/kafka.properties /data/apps/kafka-receiver/config/kafka.properties
COPY kafka-receiver/surge-deploy-kafka-receiver/target/surge-deploy-kafka-receiver-1.0.0.jar /data/apps/kafka-receiver/takin-kafka-receiver.jar
