package com.hd.bi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hd.bi.common.BaseResponse;
import com.hd.bi.common.ErrorCode;
import com.hd.bi.common.ResultUtils;
import com.hd.bi.config.RabbitmqConfig;
import com.hd.bi.exception.BusinessException;
import com.hd.bi.model.dto.chart.ChartQueryRequest;
import com.hd.bi.model.dto.chart.GenChartByAiRequest;
import com.hd.bi.model.entity.Chart;
import com.hd.bi.model.entity.User;
import com.hd.bi.model.vo.BiResponse;
import com.hd.bi.service.ChartService;
import com.hd.bi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Description:
 * Author: fqs
 * Since: 2023/8/25
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Autowired
    private ChartService chartService;
    @Resource
    private UserService userService;

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        BiResponse biResponse = chartService.genChart(multipartFile, genChartByAiRequest, request);
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        chartService.genChartAsync(multipartFile, genChartByAiRequest, request);
        return ResultUtils.success(null);
    }

    /**
     * 智能分析（异步）By Rabbitmq
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                          GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        chartService.genChartAsyncByMq(multipartFile, genChartByAiRequest, request);
        return ResultUtils.success(null);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        Page<Chart> page = chartService.listMyChartPage(chartQueryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 重新生成
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/redo/{id}")
    public BaseResponse reGenChart(@PathVariable long id,HttpServletRequest request){
        userService.getLoginUser(request);
        //        提交任务
        chartService.reDoGenChart(id);
        return ResultUtils.success(null);
    }

    /**
     * 删除图表
     */
    @GetMapping("/del/{id}")
    public BaseResponse deleteChart(@PathVariable long id,HttpServletRequest request){
        userService.getLoginUser(request);
        log.info("====================:"+id);
        boolean b = chartService.removeById(id);
        return b?ResultUtils.success(null):ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
