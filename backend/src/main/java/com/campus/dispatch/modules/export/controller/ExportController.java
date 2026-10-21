package com.campus.dispatch.modules.export.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.export.dto.ExportQueryDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "12 数据导出")
@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BookingMapper bookingMapper;

    @io.swagger.v3.oas.annotations.Operation(summary = "导出预约数据报表(CSV)")
    @GetMapping("/bookings")
    public void exportBookings(ExportQueryDTO dto, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStartDate() != null) {
            wrapper.ge(ResourceBooking::getCreateTime, dto.getStartDate().atStartOfDay());
        }
        if (dto.getEndDate() != null) {
            wrapper.lt(ResourceBooking::getCreateTime, dto.getEndDate().plusDays(1).atStartOfDay());
        }
        wrapper.orderByAsc(ResourceBooking::getCreateTime);
        List<ResourceBooking> bookings = bookingMapper.selectList(wrapper);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=bookings.csv");

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        sb.append("预约ID,用户ID,资源ID,开始时间,结束时间,状态,签到时间,创建时间\n");
        for (ResourceBooking b : bookings) {
            sb.append(b.getId()).append(',')
                    .append(b.getUserId()).append(',')
                    .append(b.getResourceId()).append(',')
                    .append(format(b.getStartTime())).append(',')
                    .append(format(b.getEndTime())).append(',')
                    .append(b.getStatus() == null ? "" : b.getStatus().getDesc()).append(',')
                    .append(format(b.getCheckInTime())).append(',')
                    .append(format(b.getCreateTime())).append('\n');
        }
        response.getWriter().write(sb.toString());
        response.getWriter().flush();
    }

    private String format(LocalDateTime time) {
        return time == null ? "" : time.format(FORMATTER);
    }
}