package com.campus.dispatch.modules.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.modules.notice.dto.NoticeQueryDTO;
import com.campus.dispatch.modules.notice.dto.NoticeVO;

public interface NoticeService {

    IPage<NoticeVO> page(NoticeQueryDTO dto);

    void read(Long id);

    void readAll();

    void sendAnnouncement(String title, String content);
}