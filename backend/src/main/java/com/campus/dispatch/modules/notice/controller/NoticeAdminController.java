package com.campus.dispatch.modules.notice.controller;

import com.campus.dispatch.common.result.Result;
import com.campus.dispatch.modules.notice.dto.AnnouncementDTO;
import com.campus.dispatch.modules.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "08 通知公告（管理端）")
@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
public class NoticeAdminController {

    private final NoticeService noticeService;

    @io.swagger.v3.oas.annotations.Operation(summary = "发布系统公告")
    @PostMapping("/announcement")
    public Result<Void> announcement(@Valid @RequestBody AnnouncementDTO dto) {
        noticeService.sendAnnouncement(dto.getTitle(), dto.getContent());
        return Result.success();
    }
}