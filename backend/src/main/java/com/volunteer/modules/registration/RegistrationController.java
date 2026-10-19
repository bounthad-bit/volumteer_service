package com.volunteer.modules.registration;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.registration.dto.RegistrationVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** 志愿者报名活动 */
    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> apply(@RequestParam("activityId") Long activityId) {
        registrationService.apply(activityId);
        return ApiResponse.ok();
    }

    /** 志愿者取消自己的报名 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> cancelMine(@PathVariable("id") Long id) {
        registrationService.cancelMine(id);
        return ApiResponse.ok();
    }

    /** 志愿者：我的报名记录 */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResponse<RegistrationVO>> mine(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(registrationService.myRegistrations(pageNo, pageSize, status));
    }

    /** 组织者：某活动下的报名申请（含审核） */
    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<PageResponse<RegistrationVO>> byActivity(
            @PathVariable("activityId") Long activityId,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(registrationService.byActivity(pageNo, pageSize, activityId, status));
    }

    /** 组织者：我所有活动的报名记录 */
    @GetMapping("/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<PageResponse<RegistrationVO>> organizerList(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "activityId", required = false) Long activityId,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(registrationService.myActivitiesRegistrations(pageNo, pageSize, activityId, status));
    }

    /** 组织者审核报名 */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass")) || "true".equals(String.valueOf(body.get("pass")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        registrationService.review(id, pass, remark);
        return ApiResponse.ok();
    }
}
