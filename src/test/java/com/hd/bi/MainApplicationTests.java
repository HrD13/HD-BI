package com.hd.bi;

import com.hd.bi.common.ErrorCode;
import com.hd.bi.config.WxOpenConfig;

import javax.annotation.Resource;

import com.hd.bi.exception.BusinessException;
import com.hd.bi.manager.RedisLimiterManager;
import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }

    @Test
    void limiterTest(){
        for (int i = 0; i < 5; i++) {
            boolean op = redisLimiterManager.limitGenChart("123");
            System.out.println(op);
        }
    }
    @Test
    void t1(){
        Integer a = 129;
        Integer b = 129;
        System.out.println(a==b);
    }
}
