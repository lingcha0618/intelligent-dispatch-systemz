package com.campus.dispatch.modules.booking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dispatch.modules.booking.dto.BookingCreateDTO;
import com.campus.dispatch.modules.booking.dto.BookingQueryDTO;
import com.campus.dispatch.modules.booking.dto.BookingVO;
import com.campus.dispatch.modules.booking.dto.IdempotentTokenVO;

public interface BookingService {

    IdempotentTokenVO getIdempotentToken();

    BookingVO create(BookingCreateDTO dto);

    void checkin(Long id);

    void cancel(Long id);

    IPage<BookingVO> my(BookingQueryDTO dto);

    IPage<BookingVO> adminPage(BookingQueryDTO dto);
}