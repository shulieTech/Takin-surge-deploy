FROM harbor.dcos.xixian.unicom.local/skyeye-platform-xx-pbc/stresstest/base:1.0.0
WORKDIR /data/apps/takin-surge
COPY configurationFiles/prod/xixian/properties/deploy.properties /data/apps/takin-surge/deploy.properties
COPY surge-deploy-pradar-kafka/target/surge-deploy-pradar-kafka-1.0.jar /data/apps/takin-surge/takin-surge.jar
