package com.campus.dispatch.modules.notice.channel;

import com.campus.dispatch.common.enums.NoticeType;
import com.campus.dispatch.modules.notice.entity.MessageNotice;
import com.campus.dispatch.modules.notice.mapper.NoticeMapper;
import com.campus.dispatch.modules.notice.mq.NoticeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SiteMessageChannel implements NoticeChannel {

    private final NoticeMapper noticeMapper;

    @Override
    public boolean supports(Integer type) {
        return true;
    }

    @Override
    public void send(NoticeMessage message) {
        MessageNotice notice = new MessageNotice();
        notice.setUserId(message.getUserId());
        notice.setTitle(message.getTitle());
        notice.setContent(message.getContent());
        notice.setType(toNoticeType(message.getType()));
        notice.setIsRead(0);
        noticeMapper.insert(notice);
    }

    private NoticeType toNoticeType(Integer type) {
        if (type == null) {
            return NoticeType.ANNOUNCEMENT;
        }
        for (NoticeType t : NoticeType.values()) {
            if (t.getValue() == type) {
                return t;
            }
        }
        return NoticeType.ANNOUNCEMENT;
    }
}