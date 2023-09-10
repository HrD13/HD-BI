package com.hd.bi.manager;

import com.hd.bi.common.ErrorCode;
import com.hd.bi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/8
 */
@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public boolean limitGenChart(String key){
        // 创建一个名称为user_limiter的限流器，每1秒最多访问 2 次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 每当一个操作来了后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        return canOp;
    }

}
