package com.volunteer.modules.activity;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.dto.ActivityRequest;
import com.volunteer.modules.activity.dto.ActivityVO;

public interface ActivityService {
    /** 公开活动列表（招聘中/进行中/已结束），供志愿者浏览与报名 */
    PageResponse<ActivityVO> publicPage(long pageNo, long pageSize, String keyword, Integer status);

    /** 组织者查看自己发布的活动 */
    PageResponse<ActivityVO> myPage(long pageNo, long pageSize, String keyword, Integer status);

    /** 管理员查看全部活动（可按审核状态筛选） */
    PageResponse<ActivityVO> adminPage(long pageNo, long pageSize, String keyword, Integer status);

    ActivityVO detail(Long id, boolean forAdminView);

    ActivityVO create(ActivityRequest request);

    ActivityVO update(Long id, ActivityRequest request);

    /** 组织者取消自己的活动 */
    void cancel(Long id);

    /** 管理员审核：pass=true 通过（进入招募中），false 驳回 */
    void audit(Long id, boolean pass, String remark);
}
