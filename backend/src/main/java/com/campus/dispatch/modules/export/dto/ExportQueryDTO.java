package com.campus.dispatch.modules.export.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "数据导出查询参数")
public class ExportQueryDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "开始日期")
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "导出文件格式（csv/excel）")
    private String format = "csv";
}