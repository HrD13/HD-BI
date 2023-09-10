package com.hd.bi.bi;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/2
 */
@SpringBootTest
public class BIManager {
    @Resource
    private YuCongMingClient client;

    @Test
    public void biTest1(){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1697907890536382465L);
        devChatRequest.setMessage("分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30");
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        DevChatResponse data = response.getData();
        System.out.println(data.getContent());
    }
}
