package com.campus.dispatch.modules.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingMapper extends BaseMapper<ResourceBooking> {

    List<ResourceBooking> selectConflicts(@Param("resourceId") Long resourceId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}