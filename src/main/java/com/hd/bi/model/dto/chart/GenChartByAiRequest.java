package com.hd.bi.model.dto.chart;

import lombok.Data;

/**
 * Description:
 * Author: fqs
 * Since: 2023/8/25
 */
@Data
public class GenChartByAiRequest {
    /**
     * 名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
