package com.volunteer.modules.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.activity.ActivityStatus;
import com.volunteer.modules.activity.entity.Activity;
import com.volunteer.modules.activity.mapper.ActivityMapper;
import com.volunteer.modules.checkin.entity.CheckInRecord;
import com.volunteer.modules.checkin.mapper.CheckInRecordMapper;
import com.volunteer.modules.evaluation.dto.EvaluationRequest;
import com.volunteer.modules.evaluation.dto.EvaluationVO;
import com.volunteer.modules.evaluation.entity.Evaluation;
import com.volunteer.modules.evaluation.mapper.EvaluationMapper;
import com.volunteer.modules.user.entity.SysUser;
import com.volunteer.modules.user.mapper.SysUserMapper;
import com.volunteer.security.LoginUser;
import com.volunteer.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {
    private final EvaluationMapper evaluationMapper;
    private final ActivityMapper activityMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(EvaluationRequest request) {
        LoginUser me = SecurityUtils.currentUser();
        Activity a = activityMapper.selectById(request.getActivityId());
        if (a == null) throw new BizException("活动不存在");
        if (a.getStatus() != ActivityStatus.FINISHED) {
            throw new BizException("活动结束后才能评价");
        }
        // 必须实际参加过（存在签到记录）才能评价
        if (checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getActivityId, request.getActivityId())
                .eq(CheckInRecord::getUserId, me.getUserId())) == 0) {
            throw new BizException("只有参加过该活动的志愿者才能评价");
        }
        if (evaluationMapper.selectCount(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getActivityId, request.getActivityId())
                .eq(Evaluation::getUserId, me.getUserId())) > 0) {
            throw new BizException("您已评价过该活动");
        }
        Evaluation e = new Evaluation();
        e.setActivityId(request.getActivityId());
        e.setUserId(me.getUserId());
        e.setScore(request.getScore());
        e.setContent(request.getContent());
        evaluationMapper.insert(e);
    }

    @Override
    public PageResponse<EvaluationVO> byActivity(long pageNo, long pageSize, Long activityId) {
        LambdaQueryWrapper<Evaluation> w = new LambdaQueryWrapper<>();
        w.eq(Evaluation::getActivityId, activityId).orderByDesc(Evaluation::getId);
        return build(pageNo, pageSize, w);
    }

    @Override
    public PageResponse<EvaluationVO> organizerReceived(long pageNo, long pageSize, Long activityId) {
        LoginUser me = SecurityUtils.currentUser();
        List<Long> myActivityIds;
        if (activityId != null) {
            Activity a = activityMapper.selectById(activityId);
            if (a == null) throw new BizException("活动不存在");
            boolean owner = a.getOrganizerId() != null && a.getOrganizerId().equals(me.getUserId());
            if (!owner && !Integer.valueOf(3).equals(me.getRole())) {
                throw new BizException("只能查看自己发布活动的评价");
            }
            myActivityIds = List.of(activityId);
        } else {
            myActivityIds = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getOrganizerId, me.getUserId()).select(Activity::getId))
                    .stream().map(Activity::getId).collect(Collectors.toList());
        }
        if (myActivityIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, pageNo, pageSize);
        }
        LambdaQueryWrapper<Evaluation> w = new LambdaQueryWrapper<>();
        w.in(Evaluation::getActivityId, myActivityIds).orderByDesc(Evaluation::getId);
        return build(pageNo, pageSize, w);
    }

    private PageResponse<EvaluationVO> build(long pageNo, long pageSize, LambdaQueryWrapper<Evaluation> w) {
        Page<Evaluation> page = evaluationMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<Evaluation> rows = page.getRecords();
        if (rows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page.getTotal(), pageNo, pageSize);
        }
        Map<Long, Activity> actMap = activityMapper.selectBatchIds(
                        rows.stream().map(Evaluation::getActivityId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Activity::getId, x -> x));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(
                        rows.stream().map(Evaluation::getUserId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<EvaluationVO> records = rows.stream().map(e -> {
            Activity a = actMap.get(e.getActivityId());
            SysUser u = userMap.get(e.getUserId());
            return EvaluationVO.builder()
                    .id(e.getId())
                    .activityId(e.getActivityId())
                    .activityTitle(a == null ? "-" : a.getTitle())
                    .userId(e.getUserId())
                    .realName(u == null ? "-" : (StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername()))
                    .score(e.getScore())
                    .content(e.getContent())
                    .createTime(e.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }
}
