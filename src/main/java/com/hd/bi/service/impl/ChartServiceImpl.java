package com.hd.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.hd.bi.common.ErrorCode;
import com.hd.bi.constant.CommonConstant;
import com.hd.bi.exception.BusinessException;
import com.hd.bi.exception.ThrowUtils;
import com.hd.bi.manager.AiManager;
import com.hd.bi.manager.RedisLimiterManager;
import com.hd.bi.model.dto.chart.ChartQueryRequest;
import com.hd.bi.model.dto.chart.GenChartByAiRequest;
import com.hd.bi.model.entity.Chart;
import com.hd.bi.model.entity.User;
import com.hd.bi.model.enums.ChartStatusEnum;
import com.hd.bi.model.vo.BiResponse;
import com.hd.bi.service.ChartService;
import com.hd.bi.mapper.ChartMapper;
import com.hd.bi.service.UserService;
import com.hd.bi.utils.ExcellUtils;
import com.hd.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @author 24612
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-08-25 00:09:02
*/
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    private final long biModelId = 1697907890536382465L;

    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Resource
    private RedisLimiterManager limiterManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private Retryer<Boolean> retryer;


    @Override
    public BiResponse genChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
//        调用ai需要登录
        User loginUser = userService.getLoginUser(request);
//        每秒只能请求2次
        limiterManager.limitGenChart("ChartLimiter_"+loginUser.getId());
        // 预设提问
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析目标:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩数据
        String csvData = ExcellUtils.xlsToCsv(multipartFile);
        userInput.append(csvData).append("\n");;
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        // 调用AI
        handleAI(chart,biModelId,userInput.toString());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse vo = new BiResponse();
        vo.setGenChart(chart.getGenChart());
        vo.setGenResult(chart.getGenResult());
        vo.setChartId(chart.getId());
        return vo;
    }

    @Override
    public void genChartAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
//        调用ai需要登录
        User loginUser = userService.getLoginUser(request);
//        每秒只能请求2次
        limiterManager.limitGenChart("ChartLimiter_"+loginUser.getId());
        // 预设提问
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析目标:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩数据
        String csvData = ExcellUtils.xlsToCsv(multipartFile);
        userInput.append(csvData).append("\n");;
        // 保存提交的任务
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        chart.setExecMessage("图表待生成！");
        if (!this.save(chart)) {
            handleChartUpdateError(chart.getId(), "数据存储失败！");
        }
        // 异步任务
        CompletableFuture.runAsync(() -> {
            Chart upChart = new Chart();
            upChart.setId(chart.getId());
            upChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            upChart.setExecMessage("图表生成中...");
            if (!this.updateById(upChart)) {
                handleChartUpdateError(chart.getId(), "图表更新失败！！");
            }
            // 调用AI
            handleAI(upChart,biModelId,userInput.toString());
            if (!this.updateById(upChart)) {
                handleChartUpdateError(chart.getId(), "图表更新失败！！");
            }
        }, threadPoolExecutor);

    }

    /**
     * ai重试
     * @param chart
     * @param biModelId
     * @param userInput
     */
    public void handleAI(Chart chart, long biModelId, String userInput){
        try {
            AtomicInteger count = new AtomicInteger();
            retryer.call(()->{
                log.info("第{}次调用AI", count.incrementAndGet());
                String res = aiManager.doChat(biModelId, userInput.toString());
                String[] split = res.split("【【【【【");
                if (split.length<3){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
                }
                String genChart = split[1].trim();
                String genResult = split[2].trim();
                chart.setGenChart(genChart);
                chart.setGenResult(genResult);
                chart.setStatus(ChartStatusEnum.SUCCESS.getValue());
                chart.setExecMessage("执行成功！");
                return true;
            });
        } catch (ExecutionException | RetryException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试调用AI失败，系统异常！");
        }
    }

    /**
     * 处理图表异步时状态更新
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId, String execMessage){
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(ChartStatusEnum.FAILED.getValue());
        chart.setExecMessage(execMessage);
        ThrowUtils.throwIf(!this.updateById(chart), ErrorCode.SYSTEM_ERROR, "图表更新失败");
    }

    @Override
    public Page<Chart> listMyChartPage(ChartQueryRequest queryRequest) {
        QueryWrapper<Chart> queryWrapper = getQueryWrapper(queryRequest);
        Page page = new Page(queryRequest.getCurrent(), queryRequest.getPageSize());
        return this.page(page, queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




