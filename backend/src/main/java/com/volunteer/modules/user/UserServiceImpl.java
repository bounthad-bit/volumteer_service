package com.volunteer.modules.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.common.enums.RoleEnum;
import com.volunteer.modules.checkin.mapper.CheckInRecordMapper;
import com.volunteer.modules.user.dto.PasswordChangeRequest;
import com.volunteer.modules.user.dto.ProfileUpdateRequest;
import com.volunteer.modules.user.dto.UserVO;
import com.volunteer.modules.user.entity.SysUser;
import com.volunteer.modules.user.mapper.SysUserMapper;
import com.volunteer.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final SysUserMapper userMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserVO> page(long pageNo, long pageSize, String keyword, Integer role) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(q -> q.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        w.eq(role != null, SysUser::getRole, role).orderByDesc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<UserVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public UserVO currentProfile() {
        SysUser user = userMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) throw new BizException("用户不存在");
        UserVO vo = toVO(user);
        vo.setTotalDuration(checkInRecordMapper.sumDurationByUser(user.getId()));
        vo.setActivityCount(checkInRecordMapper.countActivitiesByUser(user.getId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(ProfileUpdateRequest request) {
        SysUser user = userMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) throw new BizException("用户不存在");
        String phone = request.getPhone().trim();
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone).ne(SysUser::getId, user.getId())) > 0) {
            throw new BizException("该手机号已被其他账号使用");
        }
        user.setRealName(request.getRealName().trim());
        user.setPhone(phone);
        user.setStudentNo(StringUtils.hasText(request.getStudentNo()) ? request.getStudentNo().trim() : null);
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(PasswordChangeRequest request) {
        SysUser user = userMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) throw new BizException("用户不存在");
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态值不合法");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BizException("用户不存在");
        if (Integer.valueOf(3).equals(user.getRole())) {
            throw new BizException("不能禁用管理员账号");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserVO toVO(SysUser u) {
        return UserVO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .realName(u.getRealName())
                .phone(u.getPhone())
                .studentNo(u.getStudentNo())
                .role(u.getRole())
                .roleLabel(RoleEnum.labelOf(u.getRole()))
                .status(u.getStatus())
                .createTime(u.getCreateTime())
                .build();
    }
}
