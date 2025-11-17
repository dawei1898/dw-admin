# DW Admin - iFlow CLI 项目文档

## 项目概述

DW Admin 是一个基于 Spring Boot 3.x 的后台管理系统，提供用户管理、角色权限、文件存储、登录日志等核心功能。项目采用模块化设计，支持双重云存储（阿里云OSS和腾讯云COS）灵活切换。

**核心特性：**
- 用户认证与授权（JWT + RBAC）
- 双重云存储支持（阿里云OSS/腾讯云COS）
- 接口限流与日志记录
- 权限管理（基于注解的AOP实现）
- 支持Redis缓存和数据库缓存
- 代码生成器（MyBatis-Plus）

## 技术栈

### 核心技术
- **Java 21** - 编程语言
- **Spring Boot 3.5.6** - 应用框架
- **MyBatis-Plus 3.5.14** - ORM框架
- **MySQL 8.0+** - 关系型数据库
- **Redis** - 缓存服务

### 依赖库
- **JWT (jjwt 0.12.6)** - 身份认证
- **FastJSON2 (2.0.59)** - JSON处理
- **Hutool (5.8.41)** - 工具类库
- **Guava (33.4.6-jre)** - Google工具库
- **Lombok (1.18.42)** - 减少样板代码
- **Aliyun OSS SDK (3.18.2)** - 阿里云对象存储
- **Tencent COS SDK (5.6.227)** - 腾讯云对象存储

## 项目结构

```
src/main/java/com/dw/admin/
├── DwAdminApp.java                 # 启动类
├── common/                         # 公共模块
│   ├── constant/                   # 常量定义
│   ├── entity/                     # 通用实体（分页、响应）
│   ├── enums/                      # 枚举类
│   ├── exception/                  # 异常处理
│   └── utils/                      # 工具类
├── components/                     # 业务组件
│   ├── auth/                       # 认证组件（JWT、Token管理）
│   ├── generator/                  # 代码生成器
│   ├── limiter/                    # 限流组件
│   ├── log/                        # 日志组件（AOP）
│   ├── permission/                 # 权限组件
│   ├── redis/                      # Redis配置
│   └── storage/                    # 存储服务（OSS/COS/本地）
├── config/                         # 配置类
├── controller/                     # 控制器层
├── dao/                           # 数据访问层（Mapper）
├── model/                         # 业务模型
│   ├── entity/                     # 数据库实体
│   ├── param/                      # 请求参数
│   └── vo/                         # 视图对象
└── service/                       # 服务层
    └── impl/                       # 服务实现

src/main/resources/
├── application.yml                 # 主配置文件
├── logback/                       # 日志配置
└── mapper/                        # MyBatis XML映射文件
```

## 构建与运行

### 环境要求
- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 常用命令

```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目（跳过测试）
mvn install -Dmaven.test.skip=true

# 启动项目（开发模式）
mvn spring-boot:run

# 运行打包后的JAR文件
java -jar target/dw-admin-1.0.0.jar

# 停止项目
./stop.sh
```

### 数据库初始化

```bash
# 执行SQL脚本创建数据库和表结构
mysql -u root -p < docs/sql/init_ddl.sql
```

## 配置说明

### 核心配置（application.yml）

```yaml
server:
  port: 8020                    # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dwa?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false
    username:                   # 数据库用户名
    password:                   # 数据库密码

# JWT认证配置
dwa:
  auth:
    cache-type: DB              # Token缓存类型：DB/REDIS
    secret: your-secret-key     # JWT密钥
  
  # Redis配置
  redis:
    url: localhost:6379
    password:                   # Redis密码
  
  # 文件存储配置
  storage:
    provider: aliyun-oss        # 存储提供商：aliyun-oss/tencent-cos/local
    aliyun-oss:                 # 阿里云OSS配置
      access-key: xxxxxx
      secret-key: xxxxxx
      endpoint: xxxxxx.aliyuncs.com
      bucket-name: xxxxxx
    tencent-cos:                # 腾讯云COS配置
      secret-id: xxxxxx
      secret-key: xxxxxx
      region: ap-beijing
      bucket-name: xxxxxx
    local:                      # 本地存储配置
      preview-domain: http://127.0.0.1:8020
      prefix-path: data/dwa/
```

## 架构设计

### 认证授权架构
- **JWT Token**：基于jjwt实现的无状态认证
- **Token缓存**：支持DB和Redis两种缓存方式
- **RBAC模型**：基于角色的访问控制
- **权限注解**：`@Permission` + AOP实现细粒度权限控制

### 存储服务架构
- **统一接口**：`FileStorageService`定义标准操作
- **工厂模式**：`FileStorageFactory`根据配置动态选择服务商
- **多实现**：`OssService`（阿里云）、`CosService`（腾讯云）、`LocalService`（本地）
- **配置驱动**：通过`dwa.storage.provider`配置项切换

### 核心组件设计

#### 1. 认证组件（components/auth/）
- `JwtUtils`：JWT生成与验证
- `TokenCacheHelper`：Token缓存抽象
- `TokenDBCacheHelper`：数据库Token缓存实现
- `TokenRedisCacheHelper`：Redis Token缓存实现
- `AuthAspect`：认证拦截AOP

#### 2. 权限组件（components/permission/）
- `PermissionAspect`：权限校验AOP
- `PermissionCacheHelper`：权限缓存抽象
- `UserContextHolder`：用户上下文（TransmittableThreadLocal）

#### 3. 限流组件（components/limiter/）
- `LimiterAspect`：限流拦截AOP
- `LimiterProperties`：限流配置

#### 4. 日志组件（components/log/）
- `LogAspect`：操作日志记录AOP

## 开发规范

### 代码规范
- 使用Lombok减少样板代码（@Data、@Builder等）
- 统一使用FastJSON2进行JSON处理
- 异常统一处理（GlobalExceptionHandler）
- 接口统一返回Response<T>格式

### 分层规范
- **Controller**：只处理HTTP请求和响应
- **Service**：业务逻辑层，可调用多个Mapper
- **Dao/Mapper**：数据访问层，只处理单表操作
- **Param**：请求参数封装（用于Controller）
- **Vo**：视图对象（用于返回给前端）

### 命名规范
- 数据库表：`dwa_`前缀（如dwa_user）
- 实体类：与表名对应（如DwaUser）
- Mapper接口：表名+Mapper（如UserMapper）
- Service接口：业务名+Service（如UserService）
- 配置类：`*Properties`或`*Config`后缀

### 注解使用规范
- `@Auth`：需要登录的接口
- `@Permission`：需要特定权限的接口
- `@Limiter`：需要限流的接口
- `@Log`：需要记录操作日志的接口

## API文档

### 用户管理模块
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息
- `DELETE /user/{id}` - 删除用户
- `GET /user/page` - 分页查询用户

### 角色管理模块
- `POST /role/add` - 添加角色
- `PUT /role/update` - 更新角色
- `DELETE /role/{id}` - 删除角色
- `GET /role/page` - 分页查询角色
- `POST /role/assign` - 分配角色给用户

### 文件管理模块
- `POST /file/upload` - 上传文件
- `GET /file/preview/{fileId}` - 预览文件
- `GET /file/download/{fileId}` - 下载文件
- `DELETE /file/{fileId}` - 删除文件
- `GET /file/page` - 分页查询文件

### 登录日志模块
- `GET /loginLog/page` - 分页查询登录日志
- `DELETE /loginLog/{id}` - 删除登录日志

## 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 运行指定测试方法
mvn test -Dtest=UserServiceTest#testLogin
```

### 测试类位置
- `src/test/java/com/dw/admin/test/` - 测试主类
- `src/test/java/com/dw/admin/test/storage/` - 存储服务测试

## 部署

### 生产环境部署

1. **配置文件准备**
   - 创建`application-prod.yml`生产环境配置
   - 配置数据库、Redis、云存储等生产环境参数
   - 使用环境变量管理敏感信息

2. **打包部署**
   ```bash
   # 打包
   mvn clean package -Dmaven.test.skip=true
   
   # 上传到服务器
   scp target/dw-admin-1.0.0.jar user@server:/app/
   
   # 启动服务
   nohup java -jar dw-admin-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
   ```

3. **Docker部署（推荐）**
   ```dockerfile
   FROM openjdk:21-jre-slim
   COPY target/dw-admin-1.0.0.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

### 监控与日志

- **日志配置**：`logback/logback-spring.xml`
- **日志级别**：可通过配置调整（logging.level.com.dw.admin.dao=debug）
- **日志路径**：默认输出到控制台和logs目录

## 扩展功能

### 1. 双重云存储支持
项目已实现阿里云OSS和腾讯云COS的双重支持，通过配置`dwa.storage.provider`切换：
- `aliyun-oss`：阿里云对象存储
- `tencent-cos`：腾讯云对象存储
- `local`：本地文件存储

详细设计方案见：`docs/双重云存储支持设计方案.md`

### 2. 代码生成器
使用MyBatis-Plus代码生成器快速生成CRUD代码：
```java
// 在components/generator/CodeGenerator.java中配置生成参数
```

### 3. 权限管理扩展
- 支持接口级别权限控制（@Permission注解）
- 支持权限缓存（Redis/本地缓存）
- 支持动态权限配置

## 常见问题

### 1. Token过期问题
- 默认Token有效期：7天
- 可在`JwtUtils`中调整过期时间
- 支持Token刷新机制

### 2. 数据库连接问题
- 检查数据库URL、用户名、密码配置
- 确保MySQL服务正常运行
- 检查防火墙端口（默认3306）

### 3. Redis连接问题
- 检查Redis地址和密码配置
- 确保Redis服务正常运行
- 检查防火墙端口（默认6379）

### 4. 文件上传失败
- 检查云存储配置（access-key、secret-key等）
- 检查存储桶权限设置
- 检查文件大小限制（默认5MB）

## 相关文档

- `README.md` - 项目基本说明
- `docs/双重云存储支持设计方案.md` - 云存储架构设计
- `docs/sql/init_ddl.sql` - 数据库初始化脚本
- `pom.xml` - Maven依赖配置

## 版本信息

- **当前版本**：1.0.0
- **Java版本**：21
- **Spring Boot**：3.5.6
- **MyBatis-Plus**：3.5.14

## 联系方式

- **项目地址**：https://github.com/dawei1898/dw-admin
- **问题反馈**：请通过GitHub Issues提交

---

**文档生成时间**：2025-11-18  
**文档版本**：1.0  
**维护者**：DW Admin开发团队
