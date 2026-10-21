package com.campus.dispatch.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dispatch.common.enums.BookingStatus;
import com.campus.dispatch.common.enums.ResourceStatus;
import com.campus.dispatch.common.enums.ResourceType;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.modules.booking.entity.ResourceBooking;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.forecast.entity.ResourceForecast;
import com.campus.dispatch.modules.forecast.mapper.ForecastMapper;
import com.campus.dispatch.modules.resource.dto.ResourceDTO;
import com.campus.dispatch.modules.resource.dto.ResourceDetailVO;
import com.campus.dispatch.modules.resource.dto.ResourceQueryDTO;
import com.campus.dispatch.modules.resource.dto.ResourceVO;
import com.campus.dispatch.modules.resource.dto.SlotVO;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import com.campus.dispatch.modules.resource.mapper.ResourceMapper;
import com.campus.dispatch.modules.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final BookingMapper bookingMapper;
    private final ForecastMapper forecastMapper;

    @Override
    public IPage<ResourceVO> page(ResourceQueryDTO dto) {
        LambdaQueryWrapper<CampusResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getType() != null, CampusResource::getType, dto.getType())
                .eq(dto.getStatus() != null, CampusResource::getStatus, dto.getStatus())
                .orderByDesc(CampusResource::getCreateTime);
        Page<CampusResource> page = resourceMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper);
        Page<ResourceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(resource -> {
            ResourceVO vo = toVO(resource);
            fillHeat(resource, vo);
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public ResourceDetailVO detail(Long id) {
        CampusResource resource = getOrThrow(id);
        ResourceDetailVO detail = new ResourceDetailVO();
        BeanUtils.copyProperties(resource, detail);
        fillDesc(resource, detail);
        fillHeat(resource, detail);
        detail.setSlots(slots(id));
        return detail;
    }

    @Override
    public List<SlotVO> slots(Long id) {
        CampusResource resource = getOrThrow(id);
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = LocalDateTime.of(today, resource.getOpenStart());
        LocalDateTime dayEnd = LocalDateTime.of(today, resource.getOpenEnd());

        List<ResourceBooking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getResourceId, id)
                .in(ResourceBooking::getStatus, BookingStatus.PENDING, BookingStatus.IN_USE)
                .ge(ResourceBooking::getStartTime, dayStart)
                .le(ResourceBooking::getEndTime, dayEnd));

        List<SlotVO> slots = new ArrayList<>();
        LocalDateTime cursor = dayStart;
        while (cursor.isBefore(dayEnd)) {
            LocalDateTime next = cursor.plusHours(1);
            SlotVO slot = new SlotVO();
            slot.setStartTime(cursor);
            slot.setEndTime(next);
            slot.setBookable(isBookable(cursor, next, bookings));
            slots.add(slot);
            cursor = next;
        }
        return slots;
    }

    @Override
    public void add(ResourceDTO dto) {
        CampusResource resource = new CampusResource();
        BeanUtils.copyProperties(dto, resource);
        resource.setType(toResourceType(dto.getType()));
        resource.setStatus(dto.getStatus() == null ? ResourceStatus.IDLE : toResourceStatus(dto.getStatus()));
        resourceMapper.insert(resource);
    }

    @Override
    public void update(Long id, ResourceDTO dto) {
        CampusResource resource = getOrThrow(id);
        resource.setName(dto.getName());
        resource.setType(toResourceType(dto.getType()));
        resource.setLocation(dto.getLocation());
        resource.setCapacity(dto.getCapacity());
        resource.setOpenStart(dto.getOpenStart());
        resource.setOpenEnd(dto.getOpenEnd());
        if (dto.getStatus() != null) {
            resource.setStatus(toResourceStatus(dto.getStatus()));
        }
        resource.setDescription(dto.getDescription());
        resourceMapper.updateById(resource);
    }

    @Override
    public void delete(Long id) {
        getOrThrow(id);
        Long activeBookings = bookingMapper.selectCount(new LambdaQueryWrapper<ResourceBooking>()
                .eq(ResourceBooking::getResourceId, id)
                .in(ResourceBooking::getStatus, BookingStatus.PENDING, BookingStatus.IN_USE));
        if (activeBookings != null && activeBookings > 0) {
            throw new BusinessException(ResultCode.RESOURCE_HAS_BOOKING);
        }
        resourceMapper.deleteById(id);
    }

    private CampusResource getOrThrow(Long id) {
        CampusResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return resource;
    }

    private boolean isBookable(LocalDateTime start, LocalDateTime end, List<ResourceBooking> bookings) {
        for (ResourceBooking booking : bookings) {
            if (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) {
                return false;
            }
        }
        return true;
    }

    private ResourceVO toVO(CampusResource resource) {
        ResourceVO vo = new ResourceVO();
        BeanUtils.copyProperties(resource, vo);
        fillDesc(resource, vo);
        return vo;
    }

    private void fillDesc(CampusResource resource, ResourceVO vo) {
        vo.setType(resource.getType() == null ? null : resource.getType().getValue());
        vo.setTypeDesc(resource.getType() == null ? null : resource.getType().getDesc());
        vo.setStatus(resource.getStatus() == null ? null : resource.getStatus().getValue());
        vo.setStatusDesc(resource.getStatus() == null ? null : resource.getStatus().getDesc());
    }

    private void fillHeat(CampusResource resource, ResourceVO vo) {
        ResourceForecast latest = forecastMapper.selectLatest(resource.getId());
        if (latest != null) {
            vo.setHeatValue(latest.getHeatValue());
        }
    }

    private ResourceType toResourceType(Integer type) {
        if (type == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "资源类型不能为空");
        }
        for (ResourceType t : ResourceType.values()) {
            if (t.getValue() == type) {
                return t;
            }
        }
        throw new BusinessException(ResultCode.PARAM_ERROR, "非法的资源类型");
    }

    private ResourceStatus toResourceStatus(Integer status) {
        for (ResourceStatus s : ResourceStatus.values()) {
            if (s.getValue() == status) {
                return s;
            }
        }
        throw new BusinessException(ResultCode.PARAM_ERROR, "非法的资源状态");
    }
}