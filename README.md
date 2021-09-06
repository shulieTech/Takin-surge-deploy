# surge-deploy
[![许可证](https://img.shields.io/github/license/pingcap/tidb.svg)](https://github.com/pingcap/tidb/blob/master/LICENSE)
[![语言](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com/)

surge-deploy 是shulieTech使用JAVA语言开发的一个组件，用来实时收集和分析探针日志，计算服务性能指标，梳理服务调用链，目前属于shulieTech 全链路压测产品组件之一。用户也可以根据探针日志格式规范接入自己的日志，独立使用这个组件。

[Takin详细介绍](https://docs.shulie.io/docs/opensource/opensource-1d2ckv049184j)

[Takin产品架构](https://docs.shulie.io/docs/opensource/opensource-1d4d0l6o0b6u9)

# 模块介绍
surge-data 基础工具包(运行时注入，数据连接、服务暴露)

surge-deploy-pradar 日志接受和存储功能

surge-deploy-pradar-link 链路梳理处理功能

surge-deploy-pradar-storm 支持在storm的运行，支持java方式启动

surge-deploy-pradar-parser 链路日志解析工具包
# 快速开始
## 基础环境
JAVA JDK 1.8+
## 依赖中间件
- zookeeper 必须
- mysql 必须
- influx 必须
- storm 非必须。可用java 方式启动
- clickhouse 非必须。可选mysql代替（启动时加参数）  
  1.java启动方式 默认mysql
  2.strom 启动方式 完整命令： storm jar xxx.jar -DSourceType=mysql
## 本地启动
## 工程打包命令
```
mvn clean package -DskipTests
```
## 运行包路径
```
surge-deploy-pradar-storm/target/surge-deploy-1.0-jar-with-dependencies.jar
```


## 运行
- docker镜像内启动

参考takin部署 [地址](https://docs.shulie.io/docs/opensource/opensource-1d40ib39m90bu)
```
# 查询 docker po
docker ps
# 进入docker 容器
docker exec -it ${container_id}  /bin/bash
# 运行jar
java -cp surge-deploy-pradar-storm-1.0.jar io.shulie.surge.data.deploy.pradar.bootstrap.PradarTopologyBootStrap
```

- java方式本地启动

```
mvn clean package -DskipTests
java -cp surge-deploy-pradar-storm/target/surge-deploy-pradar-storm-1.0.jar io.shulie.surge.data.deploy.pradar.bootstrap.PradarTopologyBootStrap

# 看到 PradarLink start successful... 表示启动成功

```
- storm方式启动

```
mvn clean package -DskipTests
storm jar surge-deploy-pradar-storm/target/surge-deploy-1.0-jar-with-dependencies.jar -DSourceType=MYSQL
# 不加 -DSourceType=MYSQL 时，需依赖clickhouse
```
# 许可证

Takin amdb-receiver-service遵循 the Apache 2.0 许可证. 详见 the [LICENSE](https://github.com/shulieTech/Takin/blob/main/LICENSE) file for details.