package com.campus.dispatch.modules.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.modules.resource.dto.ResourceDTO;
import com.campus.dispatch.modules.resource.dto.ResourceDetailVO;
import com.campus.dispatch.modules.resource.dto.ResourceQueryDTO;
import com.campus.dispatch.modules.resource.dto.ResourceVO;
import com.campus.dispatch.modules.resource.dto.SlotVO;

import java.util.List;

public interface ResourceService {

    IPage<ResourceVO> page(ResourceQueryDTO dto);

    ResourceDetailVO detail(Long id);

    List<SlotVO> slots(Long id);

    void add(ResourceDTO dto);

    void update(Long id, ResourceDTO dto);

    void delete(Long id);
}