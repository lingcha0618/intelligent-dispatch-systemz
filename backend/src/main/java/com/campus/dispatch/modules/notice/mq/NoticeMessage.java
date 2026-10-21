package com.campus.dispatch.modules.notice.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeMessage implements Serializable {

    private Long userId;

    private String title;

    private String content;

    private Integer type;
}