# 基于代码，打包 jar 后，再构建 image
# ---------- build ----------
FROM eclipse-temurin:21-alpine-3.23 AS builder
WORKDIR /build

COPY . .
RUN ./mvnw -B -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine-3.23
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

# 暴露端口
EXPOSE 8020

# 启动项目命令
ENTRYPOINT ["java", "-jar", "app.jar"]