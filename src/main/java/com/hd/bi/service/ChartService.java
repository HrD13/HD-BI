package com.hd.bi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hd.bi.common.BaseResponse;
import com.hd.bi.model.dto.chart.ChartQueryRequest;
import com.hd.bi.model.dto.chart.GenChartByAiRequest;
import com.hd.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hd.bi.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24612
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-08-25 00:09:02
*/
public interface ChartService extends IService<Chart> {

    BiResponse genChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    // 异步
    void genChartAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

//    mq异步调用

    void genChartAsyncByMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    Page<Chart>listMyChartPage(ChartQueryRequest queryRequest);

    void reDoGenChart(long id);
}
