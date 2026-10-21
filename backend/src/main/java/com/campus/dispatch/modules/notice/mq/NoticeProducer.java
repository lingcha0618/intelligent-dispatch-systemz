package com.campus.dispatch.modules.notice.mq;

import com.campus.dispatch.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(NoticeMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTICE_EXCHANGE, RabbitMQConfig.NOTICE_ROUTING_KEY, message);
        } catch (Exception ex) {
            log.error("通知消息投递失败: userId={}, title={}", message.getUserId(), message.getTitle(), ex);
        }
    }
}
