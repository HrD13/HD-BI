package com.hd.bi.model.dto.chart;

import com.hd.bi.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * Description:
 * Author: fqs
 * Since: 2023/9/7
 */
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {

    private Long id;

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

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
