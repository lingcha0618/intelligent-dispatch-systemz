package com.campus.dispatch.modules.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.dispatch.common.enums.ResourceStatus;
import com.campus.dispatch.common.enums.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("campus_resource")
public class CampusResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private ResourceType type;

    private String location;

    private Integer capacity;

    private LocalTime openStart;

    private LocalTime openEnd;

    private ResourceStatus status;

    private String description;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}