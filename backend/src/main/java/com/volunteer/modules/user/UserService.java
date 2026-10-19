package com.volunteer.modules.user;

import com.volunteer.common.PageResponse;
import com.volunteer.modules.user.dto.PasswordChangeRequest;
import com.volunteer.modules.user.dto.ProfileUpdateRequest;
import com.volunteer.modules.user.dto.UserVO;

public interface UserService {
    /** 管理员分页查询用户列表 */
    PageResponse<UserVO> page(long pageNo, long pageSize, String keyword, Integer role);

    /** 当前登录用户资料（含累计时长统计） */
    UserVO currentProfile();

    UserVO updateProfile(ProfileUpdateRequest request);

    void changePassword(PasswordChangeRequest request);

    /** 管理员启用/禁用账号 */
    void changeStatus(Long userId, Integer status);
}
