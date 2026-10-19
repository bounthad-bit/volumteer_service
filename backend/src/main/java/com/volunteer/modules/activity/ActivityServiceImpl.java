package com.volunteer.modules.activity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.dto.ActivityRequest;
import com.volunteer.modules.activity.dto.ActivityVO;
import com.volunteer.modules.activity.entity.Activity;
import com.volunteer.modules.activity.mapper.ActivityMapper;
import com.volunteer.modules.checkin.entity.CheckInRecord;
import com.volunteer.modules.checkin.mapper.CheckInRecordMapper;
import com.volunteer.modules.evaluation.entity.Evaluation;
import com.volunteer.modules.evaluation.mapper.EvaluationMapper;
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
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final EvaluationMapper evaluationMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResponse<ActivityVO> publicPage(long pageNo, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        w.in(Activity::getStatus, ActivityStatus.RECRUITING, ActivityStatus.ONGOING, ActivityStatus.FINISHED);
        w.eq(status != null
                        && (status == ActivityStatus.RECRUITING || status == ActivityStatus.ONGOING || status == ActivityStatus.FINISHED),
                Activity::getStatus, status);
        w.like(StringUtils.hasText(keyword), Activity::getTitle, keyword);
        w.orderByDesc(Activity::getId);
        return buildPage(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<ActivityVO> myPage(long pageNo, long pageSize, String keyword, Integer status) {
        LoginUser me = SecurityUtils.currentUser();
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        w.eq(Activity::getOrganizerId, me.getUserId());
        w.eq(status != null, Activity::getStatus, status);
        w.like(StringUtils.hasText(keyword), Activity::getTitle, keyword);
        w.orderByDesc(Activity::getId);
        return buildPage(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<ActivityVO> adminPage(long pageNo, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        w.eq(status != null, Activity::getStatus, status);
        w.like(StringUtils.hasText(keyword), Activity::getTitle, keyword);
        w.orderByDesc(Activity::getId);
        return buildPage(pageNo, pageSize, w);
    }

    private PageResponse<ActivityVO> buildPage(long pageNo, long pageSize, LambdaQueryWrapper<Activity> w) {
        Page<Activity> page = activityMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<Activity> rows = page.getRecords();
        if (rows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page.getTotal(), pageNo, pageSize);
        }
        List<Long> ids = rows.stream().map(Activity::getId).collect(Collectors.toList());
        List<Long> organizerIds = rows.stream().map(Activity::getOrganizerId).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        Map<Long, Long> approvedMap = countApprovedByActivity(ids);
        Map<Long, String> organizerMap = loadOrganizerNames(organizerIds);
        Map<Long, Double> avgScoreMap = avgScoreByActivity(ids);

        // 当前登录用户在这些活动里的报名与签到状态
        LoginUser me = SecurityUtils.currentUserOrNull();
        Map<Long, Integer> myRegMap = Collections.emptyMap();
        Map<Long, Integer> myCheckMap = Collections.emptyMap();
        if (me != null) {
            List<Registration> myRegs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getUserId, me.getUserId())
                    .in(Registration::getActivityId, ids));
            myRegMap = myRegs.stream().collect(Collectors.toMap(
                    Registration::getActivityId, Registration::getStatus, (a, b) -> b));
            List<CheckInRecord> myChecks = checkInRecordMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                    .eq(CheckInRecord::getUserId, me.getUserId())
                    .in(CheckInRecord::getActivityId, ids));
            myCheckMap = myChecks.stream().collect(Collectors.toMap(
                    CheckInRecord::getActivityId,
                    c -> c.getCheckOutTime() == null ? 0 : 1,
                    (a, b) -> b));
        }

        final Map<Long, Integer> regMap = myRegMap;
        final Map<Long, Integer> checkMap = myCheckMap;
        List<ActivityVO> records = rows.stream().map(a -> {
            ActivityVO vo = toVO(a);
            vo.setOrganizerName(organizerMap.getOrDefault(a.getOrganizerId(), "-"));
            vo.setApprovedCount(approvedMap.getOrDefault(a.getId(), 0L));
            vo.setAvgScore(avgScoreMap.get(a.getId()));
            vo.setMyRegistrationStatus(regMap.get(a.getId()));
            vo.setMyCheckInState(checkMap.get(a.getId()));
            return vo;
        }).collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }

    private Map<Long, Long> countApprovedByActivity(List<Long> ids) {
        List<Registration> list = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getActivityId, ids)
                .eq(Registration::getStatus, 1));
        return list.stream().collect(Collectors.groupingBy(Registration::getActivityId, Collectors.counting()));
    }

    private Map<Long, String> loadOrganizerNames(List<Long> organizerIds) {
        if (organizerIds.isEmpty()) return Collections.emptyMap();
        List<SysUser> users = userMapper.selectBatchIds(organizerIds);
        return users.stream().collect(Collectors.toMap(SysUser::getId,
                u -> StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername(), (a, b) -> a));
    }

    private Map<Long, Double> avgScoreByActivity(List<Long> ids) {
        List<Evaluation> list = evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .in(Evaluation::getActivityId, ids));
        return list.stream().collect(Collectors.groupingBy(Evaluation::getActivityId,
                Collectors.averagingInt(Evaluation::getScore)));
    }

    @Override
    public ActivityVO detail(Long id, boolean forAdminView) {
        Activity a = activityMapper.selectById(id);
        if (a == null) throw new BizException("活动不存在");
        LoginUser me = SecurityUtils.currentUserOrNull();
        // 非公开状态的活动仅管理员与发布者可查看
        if (a.getStatus() == ActivityStatus.PENDING || a.getStatus() == ActivityStatus.CANCELLED) {
            boolean privileged = forAdminView
                    || (me != null && (Integer.valueOf(3).equals(me.getRole())
                    || (a.getOrganizerId() != null && a.getOrganizerId().equals(me.getUserId()))));
            if (!privileged) {
                throw new BizException("该活动尚未发布或已取消");
            }
        }
        ActivityVO vo = toVO(a);
        vo.setOrganizerName(loadOrganizerNames(
                a.getOrganizerId() == null ? Collections.emptyList() : List.of(a.getOrganizerId())).get(a.getOrganizerId()));
        vo.setApprovedCount(countApprovedByActivity(List.of(id)).getOrDefault(id, 0L));
        vo.setAvgScore(avgScoreByActivity(List.of(id)).get(id));
        if (me != null) {
            Registration reg = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getActivityId, id).eq(Registration::getUserId, me.getUserId()));
            if (reg != null) vo.setMyRegistrationStatus(reg.getStatus());
            CheckInRecord c = checkInRecordMapper.selectOne(new LambdaQueryWrapper<CheckInRecord>()
                    .eq(CheckInRecord::getActivityId, id).eq(CheckInRecord::getUserId, me.getUserId()));
            if (c != null) vo.setMyCheckInState(c.getCheckOutTime() == null ? 0 : 1);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO create(ActivityRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        Activity a = new Activity();
        a.setTitle(request.getTitle().trim());
        a.setDescription(request.getDescription());
        a.setCoverUrl(request.getCoverUrl());
        a.setLocation(request.getLocation().trim());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setMaxNum(request.getMaxNum());
        a.setStatus(ActivityStatus.PENDING);
        a.setOrganizerId(SecurityUtils.currentUserId());
        activityMapper.insert(a);
        return toVO(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO update(Long id, ActivityRequest request) {
        Activity a = loadOwnedOrAdmin(id);
        if (a.getStatus() == ActivityStatus.FINISHED || a.getStatus() == ActivityStatus.CANCELLED) {
            throw new BizException("已结束或已取消的活动不能修改");
        }
        validateTimes(request.getStartTime(), request.getEndTime());
        a.setTitle(request.getTitle().trim());
        a.setDescription(request.getDescription());
        a.setCoverUrl(request.getCoverUrl());
        a.setLocation(request.getLocation().trim());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setMaxNum(request.getMaxNum());
        activityMapper.updateById(a);
        return toVO(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Activity a = loadOwnedOrAdmin(id);
        if (a.getStatus() == ActivityStatus.FINISHED) {
            throw new BizException("已结束的活动不能取消");
        }
        a.setStatus(ActivityStatus.CANCELLED);
        activityMapper.updateById(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean pass, String remark) {
        Activity a = activityMapper.selectById(id);
        if (a == null) throw new BizException("活动不存在");
        if (a.getStatus() != ActivityStatus.PENDING) {
            throw new BizException("该活动不处于待审核状态");
        }
        if (pass) {
            a.setStatus(LocalDateTime.now().isBefore(a.getStartTime()) ? ActivityStatus.RECRUITING : ActivityStatus.ONGOING); // 未开始则进入招募中，已开始则直接进行中
        } else {
            a.setStatus(ActivityStatus.CANCELLED);
        }
        a.setAuditRemark(remark);
        activityMapper.updateById(a);
    }

    /** 校验操作者对该活动的权限：发布者本人或管理员 */
    private Activity loadOwnedOrAdmin(Long id) {
        Activity a = activityMapper.selectById(id);
        if (a == null) throw new BizException("活动不存在");
        LoginUser me = SecurityUtils.currentUser();
        boolean owner = a.getOrganizerId() != null && a.getOrganizerId().equals(me.getUserId());
        if (!owner && !Integer.valueOf(3).equals(me.getRole())) {
            throw new BizException("只能管理自己发布的活动");
        }
        return a;
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new BizException("活动时间不能为空");
        if (!start.isBefore(end)) throw new BizException("活动开始时间必须早于结束时间");
    }

    private ActivityVO toVO(Activity a) {
        return ActivityVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .coverUrl(a.getCoverUrl())
                .location(a.getLocation())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .maxNum(a.getMaxNum())
                .status(a.getStatus())
                .statusLabel(ActivityStatus.label(a.getStatus()))
                .organizerId(a.getOrganizerId())
                .auditRemark(a.getAuditRemark())
                .createTime(a.getCreateTime())
                .build();
    }
}
