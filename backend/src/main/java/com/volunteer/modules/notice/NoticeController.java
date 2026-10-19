package com.volunteer.modules.notice;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.notice.dto.NoticeRequest;
import com.volunteer.modules.notice.dto.NoticeVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NoticeVO>> page(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(noticeService.page(pageNo, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(noticeService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeVO> create(@Valid @RequestBody NoticeRequest request) {
        return ApiResponse.ok(noticeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeVO> update(@PathVariable("id") Long id, @Valid @RequestBody NoticeRequest request) {
        return ApiResponse.ok(noticeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return ApiResponse.ok();
    }
}
