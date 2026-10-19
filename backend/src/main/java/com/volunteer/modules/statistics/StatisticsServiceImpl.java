package com.volunteer.modules.statistics;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.statistics.dto.HoursStatVO;
import com.volunteer.modules.statistics.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsMapper statisticsMapper;

    @Override
    public PageResponse<HoursStatVO> hoursPage(long pageNo, long pageSize, String keyword) {
        List<HoursStatVO> all = statisticsMapper.selectHoursRanking(keyword);
        int from = (int) Math.max((pageNo - 1) * pageSize, 0);
        int to = (int) Math.min(from + pageSize, all.size());
        List<HoursStatVO> records = from >= all.size() ? List.of() : all.subList(from, to);
        return new PageResponse<>(records, all.size(), pageNo, pageSize);
    }

    @Override
    public List<HoursStatVO> hoursAll(String keyword) {
        return statisticsMapper.selectHoursRanking(keyword);
    }
}
