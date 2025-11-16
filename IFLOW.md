# DW Admin 后台管理系统 - 项目指导文档

## 项目概览

DW Admin 是一个基于 Spring Boot 的现代化后台管理系统，采用微服务架构设计，提供完整的用户管理、权限控制、文件管理等功能。

### 技术栈

- **核心框架**: Spring Boot 3.5.6
- **开发语言**: Java 21
- **构建工具**: Maven
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis Plus 3.5.14
- **缓存**: Redis
- **文件存储**: 阿里云 OSS
- **安全认证**: JWT Token
- **其他组件**: 
  - Hutool 工具库
  - FastJSON2
  - Lombok
  - Transmittable Thread Local

## 项目架构

### 目录结构

```
src/
├── main/
│   ├── java/com/dw/admin/
│   │   ├── DwAdminApp.java                 # Spring Boot 启动类
│   │   ├── common/                         # 通用组件
│   │   │   ├── constant/                   # 常量定义
│   │   │   ├── entity/                     # 通用实体类
│   │   │   ├── enums/                      # 枚举类
│   │   │   ├── exception/                  # 异常处理
│   │   │   └── utils/                      # 工具类
│   │   ├── components/                     # 核心组件
│   │   │   ├── auth/                       # 认证组件
│   │   │   ├── limiter/                    # 限流组件
│   │   │   ├── log/                        # 日志组件
│   │   │   ├── oss/                        # 文件存储组件
│   │   │   ├── permission/                 # 权限组件
│   │   │   └── redis/                      # Redis 组件
│   │   ├── config/                         # 配置类
│   │   ├── controller/                     # 控制器层
│   │   ├── dao/                            # 数据访问层
│   │   ├── model/                          # 数据模型
│   │   │   ├── entity/                     # 实体类
│   │   │   ├── param/                      # 请求参数
│   │   │   └── vo/                         # 响应对象
│   │   └── service/                        # 业务逻辑层
│   │       └── impl/                       # 服务实现类
│   └── resources/
│       ├── application.yml                 # 应用配置
│       ├── mapper/                         # MyBatis XML 文件
│       └── logback/                        # 日志配置
└── test/                                    # 测试代码
```

### 核心功能模块

1. **用户管理**: 注册、登录、用户信息CRUD
2. **权限控制**: 基于注解的权限验证，支持角色和权限码双重控制
3. **文件管理**: 阿里云OSS集成，支持文件上传下载
4. **日志记录**: 自动记录操作日志
5. **限流控制**: 基于IP的接口限流
6. **登录日志**: 记录用户登录历史

## 构建和运行

### 环境要求

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Redis (可选，用于缓存)
- 阿里云 OSS 账号(用于文件存储)

### 快速开始

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd dw-admin
   ```

2. **配置数据库**
   - 创建MySQL数据库 `dwa`
   - 修改 `src/main/resources/application.yml` 中的数据库连接信息

3. **初始化数据库**
   ```bash
   mysql -u root -p dwa < docs/sql/init_ddl.sql
   ```

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问应用**
   - 浏览器访问: http://localhost:8020
   - 默认无管理账户，需先注册用户

### 构建命令

| 命令 | 说明 |
|------|------|
| `mvn clean` | 清理项目 |
| `mvn compile` | 编译项目 |
| `mvn test` | 运行测试 |
| `mvn spring-boot:run` | 启动开发环境 |
| `mvn install -Dmaven.test.skip=true` | 打包(跳过测试) |
| `java -jar target/dw-admin-1.0.0.jar` | 运行JAR文件 |

### 停止应用

```bash
./stop.sh
```

## 核心组件

### 认证系统

- **@Auth 注解**: 标记需要登录的接口
- **JWT Token**: 无状态身份验证
- **Token缓存**: 支持数据库和Redis缓存
- **用户上下文**: ThreadLocal 存储当前用户信息

### 权限系统

- **@Permission 注解**: 权限验证
- **角色控制**: 基于角色的访问控制(RBAC)
- **权限码**: 细粒度权限控制
- **缓存支持**: 权限信息本地缓存和Redis缓存

### 限流系统

- **@Limiter 注解**: 接口限流
- **基于IP**: 支持基于IP地址的限流
- **灵活配置**: 支持不同接口不同的限流策略

### 日志系统

- **@Log 注解**: 自动记录操作日志
- **AOP实现**: 面向切面编程，无侵入性
- **操作追踪**: 记录用户操作历史

## 数据库设计

### 核心表结构

1. **dwa_user**: 用户表
   - 用户基本信息(用户名、密码、邮箱、手机)
   - 头像URL
   - 创建和修改时间

2. **dwa_role**: 角色表
   - 角色基本信息
   - 角色编码和名称

3. **dwa_user_role**: 用户角色关联表
   - 用户和角色的多对多关系

4. **dwa_file**: 文件表
   - 文件基本信息
   - 存储路径和URL

5. **dwa_login_log**: 登录日志表
   - 登录时间、IP地址、用户ID

## API 接口

### 认证接口

| 方法 | 路径 | 说明 | 限流 |
|------|------|------|------|
| POST | /user/register | 用户注册 | 1次/10秒 |
| POST | /user/login | 用户登录 | 1次/2秒 |
| DELETE | /user/logout | 退出登录 | - |

### 用户管理接口

| 方法 | 路径 | 说明 | 权限要求 |
|------|------|------|----------|
| GET | /user/{userId} | 查询用户信息 | @Auth |
| GET | /user/query | 查询当前用户信息 | @Auth |
| POST | /user/save | 保存用户 | @Auth + @Permission(roles="admin") |
| POST | /user/update | 修改当前用户信息 | @Auth |
| DELETE | /user/delete/{userId} | 删除用户 | @Auth + @Permission(roles="admin") |
| POST | /user/list | 查询用户列表 | @Auth + @Permission(roles="admin") |

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8020  # 服务端口

spring:
  application:
    name: dw-admin-app  # 应用名称
  datasource:  # 数据库配置
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dwa?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false
    username:  # 数据库用户名
    password:  # 数据库密码

mybatis-plus:
  mapperLocations: classpath:mapper/*.xml  # MyBatis XML文件路径

dwa:
  auth:  # 认证配置
    cache-type: DB  # 缓存类型(DB/REDIS)
    secret:  # JWT签名密钥
  redis:  # Redis配置
    url: localhost:6379
    password: 
  file:  # 文件存储配置
    oss:
      access-key:  # 阿里云OSS AccessKey
      secret-key:  # 阿里云OSS SecretKey
      endpoint:  # OSS Endpoint
      bucket:  # OSS Bucket名称
      prefix-path:  # 文件前缀路径
      url-expires: 604800  # URL有效期(秒)
```

### 环境变量配置

建议使用环境变量敏感配置:

- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_PASSWORD`: Redis密码
- `OSS_ACCESS_KEY`: 阿里云OSS AccessKey
- `OSS_SECRET_KEY`: 阿里云OSS SecretKey
- `AUTH_SECRET`: JWT签名密钥

## 开发规范

### 代码规范

1. **包命名**: 使用反向域名命名，如 `com.dw.admin`
2. **类命名**: 使用驼峰命名法，控制器以Controller结尾，服务以Service结尾
3. **方法命名**: 使用驼峰命名法，增删改查分别使用save、delete、update、query前缀
4. **注解使用**: 
   - 使用 `@Auth` 标记需要登录的接口
   - 使用 `@Permission` 标记需要权限的接口
   - 使用 `@Log` 标记需要记录日志的接口
   - 使用 `@Limiter` 标记需要限流的接口

### 数据库规范

1. **表命名**: 使用小写字母和下划线，如 `dwa_user`
2. **字段命名**: 使用小写字母和下划线，如 `user_name`
3. **主键**: 使用Long类型自增ID
4. **时间字段**: 使用 `create_time` 和 `update_time`
5. **软删除**: 推荐使用 `@TableLogic` 实现软删除

### 异常处理

- 统一使用 `BizException` 抛出业务异常
- 全局异常处理器 `GlobalExceptionHandler` 统一处理异常
- 返回统一的 `Response` 格式

### 日志记录

- 使用SLF4J进行日志记录
- 在 `@Log` 注解标记的接口中自动记录操作日志
- 日志级别: 开发环境使用DEBUG，生产环境使用INFO

## 部署指南

### 生产环境配置

1. **修改application.yml**
   ```yaml
   spring:
     profiles:
       active: prod
   ```

2. **数据库配置**
   - 使用生产环境数据库
   - 配置数据库连接池参数

3. **Redis配置**
   - 配置Redis密码
   - 调整缓存策略

4. **日志配置**
   - 配置日志输出到文件
   - 设置合适的日志级别

### 容器化部署

创建Dockerfile:
```dockerfile
FROM openjdk:21-jre-slim
COPY target/dw-admin-1.0.0.jar app.jar
EXPOSE 8020
ENTRYPOINT ["java","-jar","/app.jar"]
```

构建镜像:
```bash
mvn clean package -Dmaven.test.skip=true
docker build -t dw-admin:latest .
```

运行容器:
```bash
docker run -d -p 8020:8020 \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  -e AUTH_SECRET=your_secret \
  dw-admin:latest
```

## 性能优化

### 缓存策略

1. **权限缓存**: 权限信息缓存在本地或Redis
2. **Token缓存**: 用户Token缓存减少数据库查询
3. **配置缓存**: 系统配置信息缓存

### 数据库优化

1. **索引优化**: 为常用查询字段添加索引
2. **分页查询**: 使用MyBatis Plus的分页插件
3. **连接池**: 合理配置HikariCP连接池参数

### 限流配置

1. **接口限流**: 根据接口重要性和性能设置合适的限流策略
2. **IP限制**: 防止恶意请求
3. **用户限制**: 对单个用户的请求频率限制

## 监控和运维

### 健康检查

- Spring Boot Actuator 集成
- 数据库连接健康检查
- Redis连接健康检查

### 日志监控

- 操作日志记录
- 异常日志追踪
- 登录日志监控

### 性能监控

- 接口响应时间监控
- 数据库查询性能监控
- 缓存命中率监控

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证连接URL、用户名、密码
   - 检查网络连接

2. **Redis连接失败**
   - 检查Redis服务是否启动
   - 验证Redis配置
   - 确认防火墙设置

3. **JWT Token验证失败**
   - 检查JWT密钥配置
   - 验证Token格式
   - 检查Token是否过期

4. **阿里云OSS上传失败**
   - 验证OSS配置信息
   - 检查网络连接
   - 确认Bucket权限设置

### 调试技巧

1. **启用DEBUG日志**
   ```yaml
   logging:
     level:
       com.dw.admin: DEBUG
   ```

2. **使用Postman测试API**
   - 设置正确的请求头
   - 验证请求参数格式

3. **查看日志文件**
   - 应用日志: `logs/` 目录
   - 数据库日志: MySQL错误日志
   - Redis日志: Redis日志文件

## 扩展开发

### 添加新功能

1. **创建实体类**: 在 `model/entity/` 目录下创建实体
2. **创建Mapper**: 在 `dao/` 目录下创建Mapper接口
3. **创建Service**: 在 `service/` 目录下创建服务接口和实现
4. **创建Controller**: 在 `controller/` 目录下创建控制器
5. **创建XML文件**: 在 `resources/mapper/` 目录下创建MyBatis XML

### 自定义组件

1. **AOP组件**: 继承现有的组件模式
2. **工具类**: 在 `common/utils/` 目录下添加
3. **配置类**: 在 `config/` 目录下添加
4. **常量定义**: 在 `common/constant/` 目录下添加

## 安全建议

1. **JWT密钥**: 使用强密钥并定期更换
2. **数据库权限**: 最小权限原则
3. **API限流**: 根据业务需求设置合理的限流策略
4. **敏感信息**: 不在代码中硬编码敏感信息
5. **HTTPS**: 生产环境使用HTTPS协议
6. **输入验证**: 严格验证用户输入
7. **SQL注入**: 使用参数化查询
8. **XSS防护**: 对输出内容进行转义

## 版本更新

### 升级指南

1. **备份数据**: 升级前备份数据库
2. **测试环境验证**: 先在测试环境验证
3. **分步升级**: 逐步升级系统组件
4. **功能验证**: 升级后验证所有功能

### 兼容性说明

- Java版本兼容性
- Spring Boot版本兼容性
- 数据库版本兼容性
- 第三方库版本兼容性

---

**联系方式**: 如有问题请联系开发团队

**最后更新**: 2025-11-17