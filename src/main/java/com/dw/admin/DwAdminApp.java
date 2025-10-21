package com.dw.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.dw.admin.dao")
public class DwAdminApp {

    private final static Logger logger = LoggerFactory.getLogger(DwAdminApp.class);

    public static void main(String[] args) {
        SpringApplication.run(DwAdminApp.class, args);
        logger.info("==========  DW-ADMIN-APP started  ==========");
    }

}
