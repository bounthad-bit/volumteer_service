package com.volunteer.modules.registration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.ActivityStatus;
import com.volunteer.modules.activity.entity.Activity;
import com.volunteer.modules.activity.mapper.ActivityMapper;
import com.volunteer.modules.checkin.entity.CheckInRecord;
import com.volunteer.modules.checkin.mapper.CheckInRecordMapper;
import com.volunteer.modules.registration.dto.RegistrationVO;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final Map<Integer, String> STATUS_LABEL = Map.of(
            0, "待审核", 1, "已通过", 2, "已拒绝", 3, "已取消");

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final SysUserMapper userMapper;
    private final CheckInRecordMapper checkInRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long activityId) {
        LoginUser me = SecurityUtils.currentUser();
        Activity a = activityMapper.selectById(activityId);
        if (a == null) throw new BizException("活动不存在");
        if (a.getStatus() != ActivityStatus.RECRUITING) {
            throw new BizException("该活动当前不可报名");
        }
        Registration exists = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId).eq(Registration::getUserId, me.getUserId()));
        if (exists != null) {
            if (exists.getStatus() == 3 || exists.getStatus() == 2) {
                // 曾经取消或被拒绝，允许重新报名
                exists.setStatus(0);
                exists.setReviewRemark(null);
                exists.setRegisterTime(LocalDateTime.now());
                registrationMapper.updateById(exists);
                return;
            }
            throw new BizException("您已报名该活动，请勿重复报名");
        }
        long approved = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId).eq(Registration::getStatus, 1));
        if (approved >= a.getMaxNum()) {
            throw new BizException("报名人数已满");
        }
        Registration r = new Registration();
        r.setActivityId(activityId);
        r.setUserId(me.getUserId());
        r.setStatus(0);
        registrationMapper.insert(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMine(Long registrationId) {
        LoginUser me = SecurityUtils.currentUser();
        Registration r = registrationMapper.selectById(registrationId);
        if (r == null || !r.getUserId().equals(me.getUserId())) {
            throw new BizException("报名记录不存在");
        }
        if (r.getStatus() == 3) throw new BizException("该报名已取消");
        Activity a = activityMapper.selectById(r.getActivityId());
        if (a != null && a.getStatus() == ActivityStatus.FINISHED) {
            throw new BizException("活动已结束，不能取消报名");
        }
        // 已签到的不能再取消
        if (checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRegistrationId, registrationId)) > 0) {
            throw new BizException("已签到的报名无法取消");
        }
        r.setStatus(3);
        registrationMapper.updateById(r);
    }

    @Override
    public PageResponse<RegistrationVO> myRegistrations(long pageNo, long pageSize, Integer status) {
        LoginUser me = SecurityUtils.currentUser();
        LambdaQueryWrapper<Registration> w = new LambdaQueryWrapper<>();
        w.eq(Registration::getUserId, me.getUserId());
        w.eq(status != null, Registration::getStatus, status);
        w.orderByDesc(Registration::getId);
        return build(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<RegistrationVO> byActivity(long pageNo, long pageSize, Long activityId, Integer status) {
        Activity a = activityMapper.selectById(activityId);
        assertOwnActivity(a);
        LambdaQueryWrapper<Registration> w = new LambdaQueryWrapper<>();
        w.eq(Registration::getActivityId, activityId);
        w.eq(status != null, Registration::getStatus, status);
        w.orderByDesc(Registration::getId);
        return build(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<RegistrationVO> myActivitiesRegistrations(long pageNo, long pageSize, Long activityId, Integer status) {
        LoginUser me = SecurityUtils.currentUser();
        List<Long> myActivityIds;
        if (activityId != null) {
            Activity a = activityMapper.selectById(activityId);
            assertOwnActivity(a);
            myActivityIds = List.of(activityId);
        } else {
            myActivityIds = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getOrganizerId, me.getUserId())
                            .select(Activity::getId))
                    .stream().map(Activity::getId).collect(Collectors.toList());
        }
        if (myActivityIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, pageNo, pageSize);
        }
        LambdaQueryWrapper<Registration> w = new LambdaQueryWrapper<>();
        w.in(Registration::getActivityId, myActivityIds);
        w.eq(status != null, Registration::getStatus, status);
        w.orderByDesc(Registration::getId);
        return build(pageNo, pageSize, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long registrationId, boolean pass, String remark) {
        Registration r = registrationMapper.selectById(registrationId);
        if (r == null) throw new BizException("报名记录不存在");
        Activity a = activityMapper.selectById(r.getActivityId());
        assertOwnActivity(a);
        if (r.getStatus() != 0) {
            throw new BizException("该报名已审核，无需重复操作");
        }
        if (pass) {
            long approved = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getActivityId, r.getActivityId()).eq(Registration::getStatus, 1));
            if (approved >= a.getMaxNum()) {
                throw new BizException("通过人数已达活动招募上限");
            }
            r.setStatus(1);
        } else {
            r.setStatus(2);
        }
        r.setReviewRemark(remark);
        registrationMapper.updateById(r);
    }

    private void assertOwnActivity(Activity a) {
        if (a == null) throw new BizException("活动不存在");
        LoginUser me = SecurityUtils.currentUser();
        boolean owner = a.getOrganizerId() != null && a.getOrganizerId().equals(me.getUserId());
        if (!owner && !Integer.valueOf(3).equals(me.getRole())) {
            throw new BizException("只能管理自己发布的活动");
        }
    }

    private PageResponse<RegistrationVO> build(long pageNo, long pageSize, LambdaQueryWrapper<Registration> w) {
        Page<Registration> page = registrationMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<Registration> rows = page.getRecords();
        if (rows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page.getTotal(), pageNo, pageSize);
        }
        List<Long> activityIds = rows.stream().map(Registration::getActivityId).distinct().collect(Collectors.toList());
        List<Long> userIds = rows.stream().map(Registration::getUserId).distinct().collect(Collectors.toList());
        Map<Long, Activity> activityMap = activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, x -> x));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, x -> x));
        List<CheckInRecord> checks = checkInRecordMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                .in(CheckInRecord::getRegistrationId, rows.stream().map(Registration::getId).collect(Collectors.toList())));
        Map<Long, CheckInRecord> checkMap = checks.stream()
                .collect(Collectors.toMap(CheckInRecord::getRegistrationId, x -> x, (x, y) -> y));

        List<RegistrationVO> records = rows.stream().map(r -> {
            Activity a = activityMap.get(r.getActivityId());
            SysUser u = userMap.get(r.getUserId());
            CheckInRecord c = checkMap.get(r.getId());
            return RegistrationVO.builder()
                    .id(r.getId())
                    .activityId(r.getActivityId())
                    .activityTitle(a == null ? "-" : a.getTitle())
                    .activityStartTime(a == null ? null : a.getStartTime())
                    .activityEndTime(a == null ? null : a.getEndTime())
                    .activityLocation(a == null ? null : a.getLocation())
                    .activityStatus(a == null ? null : a.getStatus())
                    .activityStatusLabel(a == null ? "-" : ActivityStatus.label(a.getStatus()))
                    .userId(r.getUserId())
                    .realName(u == null ? "-" : (StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername()))
                    .username(u == null ? "-" : u.getUsername())
                    .phone(u == null ? "-" : u.getPhone())
                    .studentNo(u == null ? null : u.getStudentNo())
                    .status(r.getStatus())
                    .statusLabel(STATUS_LABEL.getOrDefault(r.getStatus(), "-"))
                    .reviewRemark(r.getReviewRemark())
                    .registerTime(r.getRegisterTime())
                    .checkInState(c == null ? null : (c.getCheckOutTime() == null ? 0 : 1))
                    .duration(c == null ? null : c.getDuration())
                    .build();
        }).collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }
}
