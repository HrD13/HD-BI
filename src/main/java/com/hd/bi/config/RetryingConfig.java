package com.hd.bi.config;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/9
 */
@Configuration
public class RetryingConfig {

    @Bean
    public Retryer<Boolean> myRetryer() {
        return RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(Predicates.equalTo(false))

//                .retryIfException()
////                .retryIfExceptionOfType(IOException.class) // 发生IO异常则重试
                .retryIfRuntimeException() // 发生运行时异常则重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)) // Number of retries
//                .withAttemptTimeLimiter(AttemptTimeLimiters.fixedTimeLimit(5, TimeUnit.SECONDS)) // 每次重试的最长允许5s
                .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS)) // Wait time between retries
                .build();
    }
}
