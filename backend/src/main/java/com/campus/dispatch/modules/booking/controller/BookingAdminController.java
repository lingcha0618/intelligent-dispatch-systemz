package com.campus.dispatch.modules.booking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.booking.dto.BookingQueryDTO;
import com.campus.dispatch.modules.booking.dto.BookingVO;
import com.campus.dispatch.modules.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "06 预约审批（管理员）")
@RestController
@RequestMapping("/api/admin/booking")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingService bookingService;

    @io.swagger.v3.oas.annotations.Operation(summary = "预约记录分页查询（管理员）")
    @GetMapping("/page")
    public Result<IPage<BookingVO>> page(BookingQueryDTO dto) {
        return Result.success(bookingService.adminPage(dto));
    }
}