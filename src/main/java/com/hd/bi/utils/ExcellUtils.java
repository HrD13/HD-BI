package com.hd.bi.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: fqs
 * Since: 2023/8/25
 */
@Slf4j
public class ExcellUtils {

    public static String xlsToCsv(MultipartFile multipartFile){
        // 读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误", e);
        }
        if (CollectionUtil.isEmpty(list)){
            return "";
        }
        StringBuilder res = new StringBuilder();
        // 读取表头
        List<String> headList = list.get(0).values().stream().filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
        String head = StringUtils.join(headList,",");
        res.append(head).append("\n");

        for (int i = 1; i < list.size(); i++) {
            List<String> rowList = list.get(i).values().stream().filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
            String rowData = StringUtils.join(rowList,",");
            res.append(rowData).append("\n");
        }
        return res.toString();
    }
}
