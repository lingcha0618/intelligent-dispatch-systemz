package com.campus.dispatch.modules.booking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.booking.dto.BookingCreateDTO;
import com.campus.dispatch.modules.booking.dto.BookingQueryDTO;
import com.campus.dispatch.modules.booking.dto.BookingVO;
import com.campus.dispatch.modules.booking.dto.IdempotentTokenVO;
import com.campus.dispatch.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "05 预约服务（学生端）")
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @io.swagger.v3.oas.annotations.Operation(summary = "获取预约防重令牌（提交前调用）")
    @PostMapping("/idempotent-token")
    public Result<IdempotentTokenVO> idempotentToken() {
        return Result.success(bookingService.getIdempotentToken());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "提交预约（分布式锁防超卖）")
    @PostMapping("/create")
    public Result<BookingVO> create(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.success(bookingService.create(dto));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "预约签到")
    @PostMapping("/{id}/checkin")
    public Result<Void> checkin(@PathVariable Long id) {
        bookingService.checkin(id);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "取消预约")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "查询我的预约列表")
    @GetMapping("/my")
    public Result<IPage<BookingVO>> my(BookingQueryDTO dto) {
        return Result.success(bookingService.my(dto));
    }
}