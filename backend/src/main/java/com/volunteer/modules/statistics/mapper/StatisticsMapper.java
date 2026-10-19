package com.volunteer.modules.statistics.mapper;

import com.volunteer.modules.statistics.dto.HoursStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatisticsMapper {

    /** 按志愿者汇总服务时长，SQL 见 resources/mapper/StatisticsMapper.xml */
    List<HoursStatVO> selectHoursRanking(@Param("keyword") String keyword);
}
