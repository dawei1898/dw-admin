
-- 创建数据库
CREATE DATABASE dwa
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;



-- 用户表

DROP TABLE IF EXISTS `dwa_user`;
CREATE TABLE `dwa_user` (
    `id` bigint(20) NOT NULL COMMENT '用户ID',
    `name` varchar(128) NOT NULL COMMENT '用户名',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `email` varchar(256) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(64) DEFAULT NULL COMMENT '手机',
    `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像 URL',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 角色表

DROP TABLE IF EXISTS `dwa_role`;
CREATE TABLE `dwa_role` (
    `id` bigint(20) NOT NULL COMMENT '主键ID',
    `role_code` varchar(64) NOT NULL COMMENT '角色码',
    `role_name` varchar(255) NOT NULL COMMENT '角色名称',
    `status` varchar(4) NOT NULL COMMENT '状态（1：启动，0：禁用）',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
    `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- 用户角色关联表

DROP TABLE IF EXISTS `dwa_user_role`;
CREATE TABLE `dwa_user_role` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role_code` varchar(64) NOT NULL COMMENT '角色码',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- 文件信息表

DROP TABLE IF EXISTS `dwa_file`;
CREATE TABLE `dwa_file` (
    `file_id` bigint(20) NOT NULL COMMENT '文件ID',
    `file_name` varchar(255) NOT NULL COMMENT '文件名',
    `file_type` varchar(256) DEFAULT NULL COMMENT '文件类型',
    `file_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
    `file_url` varchar(512) DEFAULT NULL COMMENT '文件 URL',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';



-- 登录日志表

DROP TABLE IF EXISTS `dwa_login_log`;
CREATE TABLE `dwa_login_log` (
    `id` bigint(20) NOT NULL COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
    `username` varchar(128) DEFAULT NULL COMMENT '用户名',
    `ip_addr` varchar(512) NOT NULL COMMENT '登录 IP',
    `login_location` varchar(512) DEFAULT NULL COMMENT '登录IP归属地',
    `login_time` datetime DEFAULT NULL COMMENT '登录时间',
    `os` varchar(255) DEFAULT NULL COMMENT '操作系统',
    `browser` varchar(512) DEFAULT NULL COMMENT '浏览器',
    `status` varchar(16) NOT NULL COMMENT '登录状态（success：登录成功，fail：登录失败）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';





