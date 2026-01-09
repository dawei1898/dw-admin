# 使用官方 OpenJDK 21 镜像作为基础镜像
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制项目 jar 文件到容器中
COPY target/dw-admin-*.jar app.jar

# 暴露应用端口
EXPOSE 8020

# 启动应用
ENTRYPOINT ["java", "-jar",  "-Dspring.profiles.active=dev", "app.jar"]
