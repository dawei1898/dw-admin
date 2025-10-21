package com.dw.admin.components.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * token DB 存储服务
 *
 * @author dawei
 */

@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = AuthConstant.AUTH_PROPERTIES_CACHE_TYPE,
        havingValue = AuthConstant.CACHE_TYPE_DB
)
public class TokenDBCacheHelper  implements TokenCacheHelper {

    public static final String DDL_SQL = """
            CREATE TABLE IF NOT EXISTS `dwa_token` (
               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
               `token_id` varchar(64) NOT NULL COMMENT 'token ID',
               `token` varchar(1000) NOT NULL COMMENT 'token内容',
               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
               `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
               PRIMARY KEY (`id`) USING BTREE
             ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='token存储表'
            """;

    public static final String INSERT_SQL = """
            INSERT INTO `dwa_token` (`token_id`, `token`, `create_time`, `expire_time`) 
            VALUES (?, ?, ?, ?)
            """;


    public static final String DELETE_SQL = """
            DELETE FROM dwa_token WHERE token_id = ?
            """;

    public static final String SELECT_SQL = """
            SELECT COUNT(1) FROM dwa_token WHERE token_id = ?
            """;

    public static final String CLEAN_SQL = """
            DELETE FROM dwa_token WHERE expire_time < ?
            """;

    @Resource
    private AuthProperties authProperties;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ThreadPoolTaskScheduler taskScheduler;

    /** token 本地缓存 */
    private Cache<String, String> LOCAL_CACHE;

    @PostConstruct
    public void init() {
        log.info("init TokenDBCacheHelper");

        LOCAL_CACHE = CacheBuilder.newBuilder()
                .maximumSize(100)
                .concurrencyLevel(8)
                .expireAfterWrite(authProperties.getExpireTime() / 2, TimeUnit.SECONDS)
                .build();

        // 初始化建表
        initDdlSql();

        // 设置定时清理 Token 任务
        initCleanExpireTokenTask();
    }




    /**
     * 是否存在 token
     */
    @Override
    public boolean contains(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            if (hasOfLocal(tokenId)) {
                return true;
            }
            if (hasOfDB(tokenId)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOfLocal(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            return LOCAL_CACHE.getIfPresent(tokenId) != null;
        }
        return false;
    }

    public  boolean hasOfDB(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            try {
                Object[] args = {tokenId};
                Integer i = jdbcTemplate.queryForObject(SELECT_SQL, Integer.class, args);
                return i != null && i > 0;
            } catch (Exception e) {
                log.error("Failed to hasOfDB. tokenId:{}", tokenId, e);
            }
        }
        return false;
    }


    /**
     * 保存 token
     */
    @Override
    public boolean put(String tokenId, String token) {
        if (StringUtils.isAnyEmpty(tokenId, token)) {
            return false;
        }
        saveToDB(tokenId, token);
        saveToLocal(tokenId, token);
        return true;
    }

    public void saveToDB(String tokenId, String token) {
        if (StringUtils.isAnyEmpty(tokenId, token)) {
            log.warn("tokenId or token is null.");
            return;
        }
        try {
            LocalDateTime expireTime = LocalDateTime.now()
                    .plus(Duration.ofSeconds(authProperties.getExpireTime()));
            LocalDateTime caretTime = LocalDateTime.now();

            Object[] args = {tokenId, token, caretTime, expireTime};

            int i = jdbcTemplate.update(INSERT_SQL, args);
        } catch (Exception e) {
            log.error("Failed to add token. tokenId:{}", tokenId, e);
        }
    }

    private void saveToLocal(String tokenId, String token) {
        if (!StringUtils.isAnyEmpty(tokenId, token)) {
            LOCAL_CACHE.put(tokenId, token);
        }
    }

    /**
     * 删除 token
     */
    @Override
    public boolean remove(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            removeDB(tokenId);
            removeLocal(tokenId);
        }
        return true;
    }

    private boolean removeLocal(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            LOCAL_CACHE.invalidate(tokenId);
        }
        return true;
    }

    public  void removeDB(String tokenId) {
        if (StringUtils.isEmpty(tokenId)) {
            return;
        }
        try {
            Object[] args = {tokenId};
            int i = jdbcTemplate.update(DELETE_SQL, args);
        } catch (Exception e) {
            log.error("Failed to delete token. tokenId:{}", tokenId, e);
        }
    }


    /**
     * 初始化建表
     */
    private void initDdlSql() {
        try {
            int i = jdbcTemplate.update(DDL_SQL);
            log.info("Finished init DDL SQL.");
        } catch (Exception e) {
            log.error("Failed to init DDL.", e);
        }
    }

    /**
     * 设置定时清理 Token 任务
     */
    private void initCleanExpireTokenTask() {
        String cron = authProperties.getCleanDBCacheCron();
        if (StringUtils.isEmpty(cron)) {
            log.warn("cron is null");
            return;
        }
        try {
            // 定时器
            Trigger trigger = new Trigger() {
                @Override
                public Instant nextExecution(TriggerContext triggerContext) {
                    CronTrigger cronTrigger = new CronTrigger(cron);
                    return cronTrigger.nextExecution(triggerContext);
                }
            };

            taskScheduler.schedule(cleanExpireToken() , trigger);
            log.info("initCleanExpireTokenTask. cron:{}", cron);
        } catch (Exception e) {
            log.error("Failed to initCleanExpireTokenTask.", e);
        }
    }

    /**
     * 清除过期的 token DB 缓存
     */
    private Runnable cleanExpireToken() {
        return () -> {
            try {
                Object[] args = {LocalDateTime.now()};
                int i = jdbcTemplate.update(CLEAN_SQL, args);
                log.info("cleanExpireToken count:{}", i);
            } catch (Exception e) {
                log.error("Failed to cleanExpireToken.", e);
            }
        };
    }

}
