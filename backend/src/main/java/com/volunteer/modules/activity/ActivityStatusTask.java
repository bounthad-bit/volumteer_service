package com.volunteer.modules.activity;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.volunteer.modules.activity.entity.Activity;
import com.volunteer.modules.activity.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 每分钟根据时间自动推进活动状态：招募中 -> 进行中 -> 已结束。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusTask {
    private final ActivityMapper activityMapper;

    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void tick() {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Activity> toOngoing = new LambdaUpdateWrapper<>();
        toOngoing.eq(Activity::getStatus, ActivityStatus.RECRUITING)
                .le(Activity::getStartTime, now)
                .gt(Activity::getEndTime, now)
                .set(Activity::getStatus, ActivityStatus.ONGOING);
        int ongoing = activityMapper.update(null, toOngoing);

        LambdaUpdateWrapper<Activity> toFinished = new LambdaUpdateWrapper<>();
        toFinished.in(Activity::getStatus, ActivityStatus.RECRUITING, ActivityStatus.ONGOING)
                .le(Activity::getEndTime, now)
                .set(Activity::getStatus, ActivityStatus.FINISHED);
        int finished = activityMapper.update(null, toFinished);

        if (ongoing > 0 || finished > 0) {
            log.info("活动状态自动更新：{} 个转为进行中，{} 个转为已结束", ongoing, finished);
        }
    }
}
