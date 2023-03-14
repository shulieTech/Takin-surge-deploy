FROM harbor.dcos.xixian.unicom.local/skyeye-platform-xx-pbc/stresstest/base:1.0.0
WORKDIR /data/apps/takin-surge
COPY configurationFiles/prod/xixian/properties/deploy.properties ./deploy.properties
COPY configurationFiles/common/pinpoint-agent-2.0.3-origin  ./pinpoint-agent-2.0.3-origin
COPY surge-deploy-pradar-kafka/target/surge-deploy-pradar-kafka-1.0.jar ./takin-surge.jar
COPY configurationFiles/prod/xixian/start-surge.sh ./start-surge.sh
COPY configurationFiles/prod/xixian/start-surge-link.sh ./start-surge-link.sh
RUN chmod +x ./start-surge.sh
RUN chmod +x ./start-surge-link.sh