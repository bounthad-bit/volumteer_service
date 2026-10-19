package com.volunteer.modules.statistics;

import com.volunteer.common.ApiResponse;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.statistics.dto.HoursStatVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<HoursStatVO>> hours(
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(statisticsService.hoursPage(pageNo, pageSize, keyword));
    }

    /** 供管理员导出 Excel（前端转成 CSV 下载） */
    @GetMapping("/hours/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HoursStatVO>> export(@RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(statisticsService.hoursAll(keyword));
    }
}
