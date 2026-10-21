package com.campus.dispatch.modules.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ResourceMapper extends BaseMapper<CampusResource> {

    @Select("SELECT * FROM campus_resource WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    CampusResource selectByIdForUpdate(@Param("id") Long id);
}