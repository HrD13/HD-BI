package com.hd.bi.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/13
 */
@Configuration
public class RabbitmqConfig {
    public static final String AI_WORK_EXCHANGE = "ai.work.exchange";
    public static final String AI_WORK_QUEUE = "ai.work.queue";
    public static final String AI_WORK_ROUTING_KEY = "ai.work";
    public static final String AI_DL_EXCHANGE = "ai.dl.exchange";
    public static final String AI_DL_QUEUE = "ai.dl.queue";
    public static final String AI_DL_ROUTING_KEY = "ai.work.dl";

//  ai任务工作交换机
    @Bean
    public DirectExchange aiExchange(){
        return new DirectExchange(AI_WORK_EXCHANGE,true,false);
    }
//  ai死信队列
    @Bean
    public DirectExchange aiDlExchange(){
        return new DirectExchange(AI_DL_EXCHANGE);
    }
//  ai任务工作队列
    @Bean
    public Queue aiWorkQueue(){
        Map<String, Object> arguments = new HashMap<>(2);
        //设置死信交换机
        arguments.put("x-dead-letter-exchange",AI_DL_EXCHANGE);
        //设置死信Routing-key
        arguments.put("x-dead-letter-routing-key", AI_DL_ROUTING_KEY);
        //设置TTL 单位是ms
        arguments.put("x-message-ttl",300*1000);
        return QueueBuilder.durable(AI_WORK_QUEUE).withArguments(arguments).build();
    }
//   ai死信队列
    @Bean
    public Queue aiDlQueue(){
        return QueueBuilder.durable(AI_DL_QUEUE).build();
    }
//    绑定工作队列
    @Bean
    public Binding workBinding(@Qualifier("aiWorkQueue") Queue aiWorkQueue,
                                  @Qualifier("aiExchange") DirectExchange aiExchange){
        return BindingBuilder.bind(aiWorkQueue).to(aiExchange).with(AI_WORK_ROUTING_KEY);
    }
//    绑定死信队列
    @Bean
    public Binding dlBinding(@Qualifier("aiDlQueue") Queue aiDlQueue,
                               @Qualifier("aiDlExchange") DirectExchange aiDlExchange){
        return BindingBuilder.bind(aiDlQueue).to(aiDlExchange).with(AI_DL_ROUTING_KEY);
    }
}
