package com.volunteer.modules.statistics;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.statistics.dto.HoursStatVO;

import java.util.List;

public interface StatisticsService {
    PageResponse<HoursStatVO> hoursPage(long pageNo, long pageSize, String keyword);

    List<HoursStatVO> hoursAll(String keyword);
}
