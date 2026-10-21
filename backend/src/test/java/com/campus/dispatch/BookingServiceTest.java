package com.campus.dispatch;

import com.campus.dispatch.common.enums.ResourceStatus;
import com.campus.dispatch.common.exception.BusinessException;
import com.campus.dispatch.common.lock.RedisDistributedLock;
import com.campus.dispatch.common.result.ResultCode;
import com.campus.dispatch.common.security.LoginUser;
import com.campus.dispatch.modules.booking.dto.BookingCreateDTO;
import com.campus.dispatch.modules.booking.mapper.BookingMapper;
import com.campus.dispatch.modules.booking.service.impl.BookingServiceImpl;
import com.campus.dispatch.modules.config.service.ConfigService;
import com.campus.dispatch.modules.notice.mq.NoticeProducer;
import com.campus.dispatch.modules.resource.entity.CampusResource;
import com.campus.dispatch.modules.resource.mapper.ResourceMapper;
import com.campus.dispatch.modules.user.entity.SysUser;
import com.campus.dispatch.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ConfigService configService;
    @Mock
    private RedisDistributedLock redisDistributedLock;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private NoticeProducer noticeProducer;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setup() {
        LoginUser loginUser = new LoginUser(1L, 0);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 重复提交应返回40040() {
        when(redisTemplate.delete(anyString())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookingService.create(createDTO()));
        assertEquals(ResultCode.DUPLICATE_SUBMIT.getCode(), ex.getCode());
    }

    @Test
    void 资源维护中应返回40044() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        CampusResource resource = resource(ResourceStatus.MAINTENANCE);
        when(resourceMapper.selectById(1L)).thenReturn(resource);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookingService.create(createDTO()));
        assertEquals(ResultCode.RESOURCE_MAINTENANCE.getCode(), ex.getCode());
    }

    @Test
    void 每日次数超限应返回40042() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(resourceMapper.selectById(1L)).thenReturn(resource(ResourceStatus.IDLE));
        when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(resourceMapper.selectByIdForUpdate(1L)).thenReturn(resource(ResourceStatus.IDLE));
        when(userMapper.selectByIdForUpdate(1L)).thenReturn(new SysUser());
        when(configService.getInt("booking.max_daily_per_user", 3)).thenReturn(3);
        when(bookingMapper.selectCount(any())).thenReturn(3L);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookingService.create(createDTO()));
        assertEquals(ResultCode.BOOKING_LIMIT_EXCEEDED.getCode(), ex.getCode());
    }

    private BookingCreateDTO createDTO() {
        BookingCreateDTO dto = new BookingCreateDTO();
        dto.setRequestId("test-request-id");
        dto.setResourceId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 10, 10, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 10, 10, 11, 0));
        return dto;
    }

    private CampusResource resource(ResourceStatus status) {
        CampusResource resource = new CampusResource();
        resource.setId(1L);
        resource.setName("测试资源");
        resource.setOpenStart(LocalTime.of(8, 0));
        resource.setOpenEnd(LocalTime.of(22, 0));
        resource.setStatus(status);
        return resource;
    }
}