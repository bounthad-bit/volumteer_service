package com.volunteer.modules.notice;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.notice.dto.NoticeRequest;
import com.volunteer.modules.notice.dto.NoticeVO;

public interface NoticeService {
    PageResponse<NoticeVO> page(long pageNo, long pageSize, String keyword);

    NoticeVO detail(Long id);

    NoticeVO create(NoticeRequest request);

    NoticeVO update(Long id, NoticeRequest request);

    void delete(Long id);
}
