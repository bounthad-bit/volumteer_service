package com.volunteer.modules.activity;

import com.volunteer.common.BizException;

/** 活动状态字典。 */
public final class ActivityStatus {
    public static final int PENDING = 0;   // 待审核
    public static final int RECRUITING = 1; // 招募中
    public static final int ONGOING = 2;    // 进行中
    public static final int FINISHED = 3;   // 已结束
    public static final int CANCELLED = 4;  // 已取消

    private ActivityStatus() {
    }

    public static String label(Integer status) {
        if (status == null) return "-";
        return switch (status) {
            case PENDING -> "待审核";
            case RECRUITING -> "招募中";
            case ONGOING -> "进行中";
            case FINISHED -> "已结束";
            case CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    public static void assertValid(Integer status) {
        if (status == null || status < PENDING || status > CANCELLED) {
            throw new BizException("非法的活动状态");
        }
    }
}
