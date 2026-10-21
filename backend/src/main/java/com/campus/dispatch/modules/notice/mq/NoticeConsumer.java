package com.campus.dispatch.modules.notice.mq;

import com.campus.dispatch.config.RabbitMQConfig;
import com.campus.dispatch.modules.notice.channel.NoticeChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeConsumer {

    private final List<NoticeChannel> channels;

    @RabbitListener(queues = RabbitMQConfig.NOTICE_QUEUE)
    public void onMessage(NoticeMessage message) {
        for (NoticeChannel channel : channels) {
            if (channel.supports(message.getType())) {
                channel.send(message);
            }
        }
    }
}