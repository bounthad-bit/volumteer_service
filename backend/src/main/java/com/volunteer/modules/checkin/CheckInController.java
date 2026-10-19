package com.volunteer.modules.checkin;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.checkin.dto.CheckInRequest;
import com.volunteer.modules.checkin.dto.CheckInVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/check-ins")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<CheckInVO> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ApiResponse.ok(checkInService.checkIn(request.getRegistrationId(), request.getPhone()));
    }

    @PutMapping("/check-out")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<CheckInVO> checkOut(@Valid @RequestBody CheckInRequest request) {
        return ApiResponse.ok(checkInService.checkOut(request.getRegistrationId(), request.getPhone()));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<CheckInVO>> mine(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.ok(checkInService.myRecords(pageNo, pageSize));
    }

    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ApiResponse<PageResponse<CheckInVO>> byActivity(
            @PathVariable("activityId") Long activityId,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.ok(checkInService.byActivity(pageNo, pageSize, activityId));
    }
}
