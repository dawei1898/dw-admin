# Dw Admin





### 清理项目
```shell
mvn  clean 
```

### 编译项目
```shell
mvn compile 
```

### 启动项目
```shell
mvn spring-boot:run
```

### 停止项目
```shell
./stop.sh
```

### 测试项目
```shell
mvn test
```

### 打包项目 跳过测试
```shell
mvn install -Dmaven.test.skip=true
 ```

### 运行打包后的JAR文件
```shell
java -jar target/dw-admin-1.0.0.jar
```

构建镜像
```shell
docker build -t dw-admin:1.0.0 .
```

运行容器
```shell
docker run -p 8020:8020 -v $(pwd)/logs:/app/logs --name dw-admin  dw-admin:1.0.0
```
