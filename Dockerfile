FROM swr.cn-east-3.myhuaweicloud.com/shulie-hangzhou/openjdk:8-jdk-alpine
WORKDIR /data/takin-surge
RUN cd / && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
COPY  surge-deploy-pradar-storm/target/surge-deploy-pradar-storm-*.jar  /data/takin-surge/surge-deploy-pradar-storm.jar
ENTRYPOINT ["java","-jar","surge-deploy-pradar-storm.jar"]