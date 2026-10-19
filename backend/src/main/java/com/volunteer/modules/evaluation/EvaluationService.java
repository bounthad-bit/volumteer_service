package com.volunteer.modules.evaluation;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.evaluation.dto.EvaluationRequest;
import com.volunteer.modules.evaluation.dto.EvaluationVO;

public interface EvaluationService {
    void submit(EvaluationRequest request);

    /** 某活动下的评价列表，公开可读 */
    PageResponse<EvaluationVO> byActivity(long pageNo, long pageSize, Long activityId);

    /** 组织者：自己活动收到的评价 */
    PageResponse<EvaluationVO> organizerReceived(long pageNo, long pageSize, Long activityId);
}
