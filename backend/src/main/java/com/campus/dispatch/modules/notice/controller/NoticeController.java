package com.campus.dispatch.modules.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.notice.dto.NoticeQueryDTO;
import com.campus.dispatch.modules.notice.dto.NoticeVO;
import com.campus.dispatch.modules.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "08 通知公告")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @io.swagger.v3.oas.annotations.Operation(summary = "通知公告分页查询")
    @GetMapping("/page")
    public Result<IPage<NoticeVO>> page(NoticeQueryDTO dto) {
        return Result.success(noticeService.page(dto));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "标记单条通知为已读")
    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        noticeService.read(id);
        return Result.success();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "全部通知标记为已读")
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll();
        return Result.success();
    }
}