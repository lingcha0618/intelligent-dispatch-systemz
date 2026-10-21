package com.campus.dispatch.modules.notice.channel;

import com.campus.dispatch.modules.notice.mq.NoticeMessage;

public interface NoticeChannel {

    boolean supports(Integer type);

    void send(NoticeMessage message);
}