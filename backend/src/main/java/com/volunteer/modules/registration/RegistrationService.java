package com.volunteer.modules.registration;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.registration.dto.RegistrationVO;

public interface RegistrationService {
    /** 志愿者报名活动 */
    void apply(Long activityId);

    /** 志愿者取消报名 */
    void cancelMine(Long registrationId);

    /** 志愿者：我的报名记录 */
    PageResponse<RegistrationVO> myRegistrations(long pageNo, long pageSize, Integer status);

    /** 组织者：某个活动下的报名申请 */
    PageResponse<RegistrationVO> byActivity(long pageNo, long pageSize, Long activityId, Integer status);

    /** 组织者审核报名（通过/拒绝） */
    void review(Long registrationId, boolean pass, String remark);

    /** 组织者查看自己所有活动的报名记录 */
    PageResponse<RegistrationVO> myActivitiesRegistrations(long pageNo, long pageSize, Long activityId, Integer status);
}
