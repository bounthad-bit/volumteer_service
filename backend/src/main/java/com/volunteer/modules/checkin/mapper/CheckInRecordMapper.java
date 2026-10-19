package com.volunteer.modules.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.modules.checkin.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

    @Select("SELECT COALESCE(SUM(duration), 0) FROM check_in_record WHERE user_id = #{userId}")
    long sumDurationByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT activity_id) FROM check_in_record WHERE user_id = #{userId} AND duration IS NOT NULL")
    long countActivitiesByUser(@Param("userId") Long userId);
}
