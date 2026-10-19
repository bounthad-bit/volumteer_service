package com.volunteer.modules.notice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.common.BizException;
import com.volunteer.common.PageResponse;
import com.volunteer.modules.notice.dto.NoticeRequest;
import com.volunteer.modules.notice.dto.NoticeVO;
import com.volunteer.modules.notice.entity.Notice;
import com.volunteer.modules.notice.mapper.NoticeMapper;
import com.volunteer.modules.user.entity.SysUser;
import com.volunteer.modules.user.mapper.SysUserMapper;
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
public class NoticeServiceImpl implements NoticeService {
    private final NoticeMapper noticeMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResponse<NoticeVO> page(long pageNo, long pageSize, String keyword) {
        LambdaQueryWrapper<Notice> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(keyword), Notice::getTitle, keyword).orderByDesc(Notice::getId);
        Page<Notice> page = noticeMapper.selectPage(new Page<>(pageNo, pageSize), w);
        List<Notice> rows = page.getRecords();
        if (rows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page.getTotal(), pageNo, pageSize);
        }
        Map<Long, SysUser> adminMap = userMapper.selectBatchIds(
                        rows.stream().map(Notice::getAdminId).filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<NoticeVO> records = rows.stream().map(n -> toVO(n, adminMap.get(n.getAdminId()))).collect(Collectors.toList());
        return new PageResponse<>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public NoticeVO detail(Long id) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) throw new BizException("公告不存在");
        return toVO(n, n.getAdminId() == null ? null : userMapper.selectById(n.getAdminId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO create(NoticeRequest request) {
        Notice n = new Notice();
        n.setTitle(request.getTitle().trim());
        n.setContent(request.getContent());
        n.setAdminId(SecurityUtils.currentUserId());
        noticeMapper.insert(n);
        return toVO(n, userMapper.selectById(n.getAdminId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO update(Long id, NoticeRequest request) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) throw new BizException("公告不存在");
        n.setTitle(request.getTitle().trim());
        n.setContent(request.getContent());
        noticeMapper.updateById(n);
        return toVO(n, n.getAdminId() == null ? null : userMapper.selectById(n.getAdminId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (noticeMapper.selectById(id) == null) throw new BizException("公告不存在");
        noticeMapper.deleteById(id);
    }

    private NoticeVO toVO(Notice n, SysUser admin) {
        return NoticeVO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .adminId(n.getAdminId())
                .adminName(admin == null ? "-" : (StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername()))
                .createTime(n.getCreateTime())
                .build();
    }
}
