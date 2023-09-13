package com.hd.bi.mq;

import cn.hutool.core.util.ObjectUtil;
import com.hd.bi.common.ErrorCode;
import com.hd.bi.config.RabbitmqConfig;
import com.hd.bi.constant.CommonConstant;
import com.hd.bi.exception.BusinessException;
import com.hd.bi.exception.ThrowUtils;
import com.hd.bi.manager.AiManager;
import com.hd.bi.model.entity.Chart;
import com.hd.bi.model.enums.ChartStatusEnum;
import com.hd.bi.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/13
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiWorkListener {

    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {RabbitmqConfig.AI_WORK_QUEUE}, ackMode = "MANUAL")
    public void handleGenChartByAi(String message,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        try{
            log.info("任务id：{}",message);
            ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.SYSTEM_ERROR, "消息为空");
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            ThrowUtils.throwIf(ObjectUtil.isEmpty(chart), ErrorCode.NOT_FOUND_ERROR, "没有该任务！");
            //        调用ai
            // 预设提问
            StringBuilder userInput = new StringBuilder();
            userInput.append("分析目标:").append("\n");
            String userGoal = chart.getGoal();
            if (StringUtils.isNotBlank(chart.getChartType())) {
                userGoal += "，请使用" + chart.getChartType();
            }
            userInput.append(userGoal).append("\n");
            userInput.append("原始数据：").append("\n");
            userInput.append(chart.getChartData()).append("\n");;

            String res = aiManager.doChat(CommonConstant.biModelId, userInput.toString());
            String[] split = res.split("【【【【【");
            if (split.length<3){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
            }
            String genChart = split[1].trim();
            String genResult = split[2].trim();
            Chart upChart = new Chart();
            upChart.setId(chartId);
            upChart.setGenChart(genChart);
            upChart.setGenResult(genResult);
            upChart.setStatus(ChartStatusEnum.SUCCESS.getValue());
            upChart.setExecMessage("执行成功！");
            ThrowUtils.throwIf(!chartService.updateById(upChart), ErrorCode.SYSTEM_ERROR, "保存数据失败！！");
            channel.basicAck(deliveryTag,false);
        } catch (Exception e){
            log.error(e.getMessage());
            // 拒绝消息并不重新排队
            channel.basicNack(deliveryTag, false,false);
        }

    }

    /**
     * 死信队列
     * @param message
     * @param channel
     * @param deliveryTag
     * @throws Exception
     */
    @RabbitListener(queues = {RabbitmqConfig.AI_DL_QUEUE}, ackMode = "MANUAL")
    public void handleError(String message,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception{
        try {
            ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.SYSTEM_ERROR, "执行任务失败！消息为空！");
            log.error("当前时间：{}，收到死信队列的消息：{}",new Date().toString(),message);
            long chartId = Long.parseLong(message);
            Chart chart = new Chart();
            ThrowUtils.throwIf(ObjectUtil.isEmpty(chart), ErrorCode.SYSTEM_ERROR, "执行任务失败！db没有该任务！");
            chart.setId(chartId);
            chart.setStatus(ChartStatusEnum.FAILED.getValue());
            chart.setExecMessage("执行任务失败！");
            ThrowUtils.throwIf(!chartService.updateById(chart), ErrorCode.SYSTEM_ERROR, "图表更新失败！");
        }catch (Exception e){
            log.error("Dead Letter:"+e.getMessage());
            channel.basicAck(deliveryTag,false);
        }

    }
}
