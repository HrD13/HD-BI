package com.hd.bi;

import com.hd.bi.config.RabbitmqConfig;

import javax.annotation.Resource;

import com.hd.bi.manager.RedisLimiterManager;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *
 
 */
@SpringBootTest
class MainApplicationTests {


    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private RabbitTemplate rabbitTemplate;



    @Test
    void limiterTest(){
        for (int i = 0; i < 5; i++) {
            boolean op = redisLimiterManager.limitGenChart("123");
            System.out.println(op);
        }
    }
    @Test
    void t1(){
        rabbitTemplate.convertAndSend(RabbitmqConfig.AI_WORK_EXCHANGE,RabbitmqConfig.AI_WORK_ROUTING_KEY,"13465");
    }
}
