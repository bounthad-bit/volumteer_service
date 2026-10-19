package com.volunteer.modules.checkin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.ActivityStatus;
import com.volunteer.modules.activity.entity.Activity;
import com.volunteer.modules.activity.mapper.ActivityMapper;
import com.volunteer.modules.checkin.dto.CheckInRequest;
import com.volunteer.modules.checkin.dto.CheckInVO;
import com.volunteer.modules.checkin.entity.CheckInRecord;
import com.volunteer.modules.checkin.mapper.CheckInRecordMapper;
import com.volunteer.modules.registration.entity.Registration;
import com.volunteer.modules.registration.mapper.RegistrationMapper;
import com.volunteer.modules.user.entity.SysUser;
import com.volunteer.modules.user.mapper.SysUserMapper;
import com.volunteer.security.LoginUser;
import com.volunteer.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {
    private final CheckInRecordMapper checkInRecordMapper;
    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInVO checkIn(Long registrationId, String phone) {
        LoginUser me = SecurityUtils.currentUser();
        Registration r = requireMyRegistration(registrationId, me, phone);
        Activity a = activityMapper.selectById(r.getActivityId());
        if (a == null) throw new BizException("活动不存在");
        if (a.getStatus() == ActivityStatus.CANCELLED) throw new BizException("活动已取消，无法签到");
        // 允许在活动开始时间前后 1 小时内签到
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(a.getStartTime().minusHours(1))) {
            throw new BizException("签到尚未开始，请在活动开始前 1 小时内签到");
        }
        if (now.isAfter(a.getEndTime())) {
            throw new BizException("活动已结束，无法签到");
        }
        CheckInRecord exist = checkInRecordMapper.selectOne(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRegistrationId, r.getId()));
        if (exist != null) {
            throw new BizException("您已签到，请勿重复签到");
        }
        CheckInRecord c = new CheckInRecord();
        c.setRegistrationId(r.getId());
        c.setActivityId(r.getActivityId());
        c.setUserId(me.getUserId());
        c.setCheckInTime(now);
        checkInRecordMapper.insert(c);
        return toVO(c, a, userMapper.selectById(me.getUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInVO checkOut(Long registrationId, String phone) {
        LoginUser me = SecurityUtils.currentUser();
        Registration r = requireMyRegistration(registrationId, me, phone);
        CheckInRecord c = checkInRecordMapper.selectOne(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRegistrationId, r.getId()));
        if (c == null) throw new BizException("请先签到再签退");
        if (c.getCheckOutTime() != null) throw new BizException("您已签退，请勿重复操作");
        LocalDateTime now = LocalDateTime.now();
        if (!now.isAfter(c.getCheckInTime())) {
            throw new BizException("签退时间不能早于签到时间");
        }
        long minutes = Duration.between(c.getCheckInTime(), now).toMinutes();
        c.setCheckOutTime(now);
        c.setDuration((int) Math.max(minutes, 0));
        checkInRecordMapper.updateById(c);
        return toVO(c, activityMapper.selectById(c.getActivityId()), userMapper.selectById(me.getUserId()));
    }

    /** 校验报名归属，并用手机号做二次确认，防止代签 */
    private Registration requireMyRegistration(Long registrationId, LoginUser me, String phone) {
        Registration r = registrationMapper.selectById(registrationId);
        if (r == null || !r.getUserId().equals(me.getUserId())) {
            throw new BizException("报名记录不存在");
        }
        if (r.getStatus() != 1) {
            throw new BizException("报名尚未通过审核，无法签到");
        }
        SysUser u = userMapper.selectById(me.getUserId());
        if (u == null || !StringUtils.hasText(u.getPhone()) || !u.getPhone().equals(phone)) {
            throw new BizException("手机号与报名登记的不一致");
        }
        return r;
    }

    @Override
    public PageResponse<CheckInVO> myRecords(long pageNo, long pageSize) {
        LoginUser me = SecurityUtils.currentUser();
        LambdaQueryWrapper<CheckInRecord> w = new LambdaQueryWrapper<>();
        w.eq(CheckInRecord::getUserId, me.getUserId()).orderByDesc(CheckInRecord::getId);
        return build(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<CheckInVO> byActivity(long pageNo, long pageSize, Long activityId) {
        Activity a = activityMapper.selectById(activityId);
        if (a == null) throw new BizException("活动不存在");
        LoginUser me = SecurityUtils.currentUser();
        boolean owner = a.getOrganizerId() != null && a.getOrganizerId().equals(me.getUserId());
        if (!owner && !Integer.valueOf(3).equals(me.getRole())) {
            throw new BizException("只能查看自己发布活动的签到记录");
        }
        LambdaQueryWrapper<CheckInRecord> w = new LambdaQueryWrapper<>();
        w.eq(CheckInRecord::getActivityId, activityId).orderByDesc(CheckInRecord::getId);
        return build(pageNo, pageSize, w);
    }

    private PageResponse<CheckInVO> build(long pageNo, long pageSize, LambdaQueryWrapper<CheckInRecord> w) {
        Page<CheckInRecord> page = checkInRecordMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<CheckInRecord> rows = page.getRecords();
        if (rows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page.getTotal(), pageNo, pageSize);
        }
        Map<Long, Activity> actMap = activityMapper.selectBatchIds(
                        rows.stream().map(CheckInRecord::getActivityId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Activity::getId, x -> x));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(
                        rows.stream().map(CheckInRecord::getUserId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<CheckInVO> records = rows.stream()
                .map(c -> toVO(c, actMap.get(c.getActivityId()), userMap.get(c.getUserId())))
                .collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }

    private CheckInVO toVO(CheckInRecord c, Activity a, SysUser u) {
        return CheckInVO.builder()
                .id(c.getId())
                .registrationId(c.getRegistrationId())
                .activityId(c.getActivityId())
                .activityTitle(a == null ? "-" : a.getTitle())
                .userId(c.getUserId())
                .realName(u == null ? "-" : (StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername()))
                .checkInTime(c.getCheckInTime())
                .checkOutTime(c.getCheckOutTime())
                .duration(c.getDuration())
                .durationText(formatDuration(c.getDuration()))
                .build();
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null) return "-";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + " 分钟";
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }
}
