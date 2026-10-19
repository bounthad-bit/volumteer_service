package com.volunteer.modules.checkin;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.checkin.dto.CheckInRequest;
import com.volunteer.modules.checkin.dto.CheckInVO;

public interface CheckInService {
    /** 签到：registrationId 为报名记录 ID，phone 为报名时登记的手机号（二次确认防代签） */
    CheckInVO checkIn(Long registrationId, String phone);

    /** 签退：自动计算本次服务时长（分钟）并写入签到记录 */
    CheckInVO checkOut(Long registrationId, String phone);

    /** 志愿者：我的签到记录 */
    PageResponse<CheckInVO> myRecords(long pageNo, long pageSize);

    /** 管理员/组织者：按活动查询签到记录 */
    PageResponse<CheckInVO> byActivity(long pageNo, long pageSize, Long activityId);
}
