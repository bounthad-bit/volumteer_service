package com.volunteer.modules.evaluation;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.evaluation.dto.EvaluationRequest;
import com.volunteer.modules.evaluation.dto.EvaluationVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/evaluations")
public class EvaluationController {
    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> submit(@Valid @RequestBody EvaluationRequest request) {
        evaluationService.submit(request);
        return ApiResponse.ok();
    }

    @GetMapping("/activity/{activityId}")
    public ApiResponse<PageResponse<EvaluationVO>> byActivity(
            @PathVariable("activityId") Long activityId,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.ok(evaluationService.byActivity(pageNo, pageSize, activityId));
    }

    /** 组织者：查看自己活动收到的评价 */
    @GetMapping("/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<PageResponse<EvaluationVO>> organizer(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "activityId", required = false) Long activityId) {
        return ApiResponse.ok(evaluationService.organizerReceived(pageNo, pageSize, activityId));
    }
}
