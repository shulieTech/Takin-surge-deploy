FROM  harbor.dcos.xixian.unicom.local/skyeye-platform-xx-pbc/stresstest/base:1.0.0
WORKDIR /data/apps/kafka-receiver
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/prod/xixian/properties/application.yml ./config/application.yml
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/prod/xixian/properties/kafka.properties ./config/kafka.properties
COPY kafka-receiver/surge-deploy-kafka-receiver/target/surge-deploy-kafka-receiver-1.0.0.jar ./takin-kafka-receiver.jar
COPY kafka-receiver/surge-deploy-kafka-receiver/configurationFiles/prod/xixian/start.sh ./start.sh
RUN chmod +x ./start.sh