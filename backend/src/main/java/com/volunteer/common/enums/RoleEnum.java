package com.volunteer.common.enums;

import com.volunteer.common.BizException;

/** 角色：与 sys_user.role 字段一一对应。 */
public enum RoleEnum {
    VOLUNTEER(1, "志愿者"),
    ORGANIZER(2, "活动组织者"),
    ADMIN(3, "系统管理员");

    private final int code;
    private final String label;

    RoleEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static RoleEnum of(Integer code) {
        if (code != null) {
            for (RoleEnum r : values()) {
                if (r.code == code) return r;
            }
        }
        throw new BizException("非法的角色类型");
    }

    public static String labelOf(Integer code) {
        if (code == null) return "-";
        for (RoleEnum r : values()) {
            if (r.code == code) return r.label;
        }
        return "-";
    }
}
