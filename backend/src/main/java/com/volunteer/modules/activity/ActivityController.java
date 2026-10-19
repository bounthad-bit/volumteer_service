package com.volunteer.modules.activity;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.dto.ActivityRequest;
import com.volunteer.modules.activity.dto.ActivityVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /** 公开活动列表，游客与志愿者均可浏览 */
    @GetMapping
    public ApiResponse<PageResponse<ActivityVO>> publicList(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(activityService.publicPage(pageNo, pageSize, keyword, status));
    }

    /** 组织者：我发布的活动 */
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<PageResponse<ActivityVO>> mine(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(activityService.myPage(pageNo, pageSize, keyword, status));
    }

    /** 管理员：全部活动（含待审核） */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ActivityVO>> adminList(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(activityService.adminPage(pageNo, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(activityService.detail(id, false));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<ActivityVO> create(@Valid @RequestBody ActivityRequest request) {
        return ApiResponse.ok(activityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<ActivityVO> update(@PathVariable("id") Long id, @Valid @RequestBody ActivityRequest request) {
        return ApiResponse.ok(activityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<Void> cancel(@PathVariable("id") Long id) {
        activityService.cancel(id);
        return ApiResponse.ok();
    }

    /** 管理员审核活动 */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> audit(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass")) || "true".equals(String.valueOf(body.get("pass")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        activityService.audit(id, pass, remark);
        return ApiResponse.ok();
    }
}
