package com.hd.bi.config;

import com.hd.bi.common.ErrorCode;
import com.hd.bi.exception.BusinessException;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/8
 */
@Data
@ConfigurationProperties(prefix = "redisson.redis")
@Configuration
public class RedissonConfig {
    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
//                .setPassword(password)
                .setAddress("redis://" + host + ":" + port);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
