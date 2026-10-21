package com.campus.dispatch.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.common.security.SecurityUtils;
import com.campus.dispatch.common.enums.NoticeType;
import com.campus.dispatch.modules.notice.dto.NoticeQueryDTO;
import com.campus.dispatch.modules.notice.dto.NoticeVO;
import com.campus.dispatch.modules.notice.entity.MessageNotice;
import com.campus.dispatch.modules.notice.mapper.NoticeMapper;
import com.campus.dispatch.modules.notice.service.NoticeService;
import com.campus.dispatch.modules.user.entity.SysUser;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    @Override
    public IPage<NoticeVO> page(NoticeQueryDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<MessageNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageNotice::getUserId, userId)
                .eq(dto.getIsRead() != null, MessageNotice::getIsRead, dto.getIsRead())
                .orderByDesc(MessageNotice::getCreateTime);
        Page<MessageNotice> page = noticeMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        Page<NoticeVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public void read(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        MessageNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "消息不存在");
        }
        if (!notice.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_OWNER);
        }
        notice.setIsRead(1);
        noticeMapper.updateById(notice);
    }

    @Override
    public void readAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        noticeMapper.update(null, new LambdaUpdateWrapper<MessageNotice>()
                .eq(MessageNotice::getUserId, userId)
                .eq(MessageNotice::getIsRead, 0)
                .set(MessageNotice::getIsRead, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendAnnouncement(String title, String content) {
        List<SysUser> users = userMapper.selectList(null);
        for (SysUser user : users) {
            MessageNotice notice = new MessageNotice();
            notice.setUserId(user.getId());
            notice.setTitle(title);
            notice.setContent(content);
            notice.setType(NoticeType.ANNOUNCEMENT);
            notice.setIsRead(0);
            noticeMapper.insert(notice);
        }
    }

    private NoticeVO toVO(MessageNotice notice) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        vo.setType(notice.getType() == null ? null : notice.getType().getValue());
        vo.setTypeDesc(notice.getType() == null ? null : notice.getType().getDesc());
        return vo;
    }
}