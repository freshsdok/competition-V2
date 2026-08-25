package com.teaching.course.service.impl;

import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.course.mapper.CourseChapterVideoMapper;
import com.teaching.course.mapper.CourseInfoMapper;
import com.teaching.course.service.ICourseChapterVideoService;
import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 章节视频信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-23
 */
@Service
public class CourseChapterVideoServiceImpl implements ICourseChapterVideoService {
    @Autowired
    private CourseChapterVideoMapper courseChapterVideoMapper;
    @Autowired
    private CourseInfoMapper courseInfoMapper;

    /**
     * 查询章节视频信息
     *
     * @param videoId 章节视频信息主键
     * @return 章节视频信息
     */
    @Override
    public CourseChapterVideo selectCourseChapterVideoByVideoId(Long videoId) {
        return courseChapterVideoMapper.selectCourseChapterVideoByVideoId(videoId);
    }

    @Override
    public List<CourseChapterVideo> selectCourseChapterVideoByVideoIds(Long[] videoIds) {
        return courseChapterVideoMapper.selectCourseChapterVideoByVideoIds(videoIds);
    }

    /**
     * 查询章节视频信息列表
     *
     * @param courseChapterVideo 章节视频信息
     * @return 章节视频信息
     */
    @Override
    public List<CourseChapterVideo> selectCourseChapterVideoList(CourseChapterVideo courseChapterVideo) {
        return courseChapterVideoMapper.selectCourseChapterVideoList(courseChapterVideo);
    }

    /**
     * 根据章节ID查询视频列表
     *
     * @param chapterId
     * @return
     */
    @Override
    public List<CourseChapterVideo> selectCourseChapterVideoListByChapterId(Long chapterId) {
        CourseChapterVideo courseChapterVideo = new CourseChapterVideo(chapterId);
        return courseChapterVideoMapper.selectCourseChapterVideoList(courseChapterVideo);
    }

    /**
     * 新增章节视频信息
     *
     * @param courseChapterVideo 章节视频信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCourseChapterVideo(CourseChapterVideo courseChapterVideo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseChapterVideo.setCreateTime(DateUtils.getNowDate());
        courseChapterVideo.setCreateBy(sysUser.getNickName());
        if (UserConstants.YES.equals(courseChapterVideo.getIsFree())) {
            courseChapterVideo.setExpenses(BigDecimal.ZERO);
        }
        int i = courseChapterVideoMapper.insertCourseChapterVideo(courseChapterVideo);
        updateCreditHour(courseChapterVideo.getChapterId());
        return i;
    }

    /**
     * 修改章节视频信息
     *
     * @param courseChapterVideo 章节视频信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCourseChapterVideo(CourseChapterVideo courseChapterVideo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseChapterVideo.setUpdateTime(DateUtils.getNowDate());
        courseChapterVideo.setUpdateBy(sysUser.getNickName());
        if (UserConstants.YES.equals(courseChapterVideo.getIsFree())) {
            courseChapterVideo.setExpenses(BigDecimal.ZERO);
        }
        int i = courseChapterVideoMapper.updateCourseChapterVideo(courseChapterVideo);
        updateCreditHour(courseChapterVideo.getChapterId());
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCourseChapterVideo(CourseChapterInfo courseChapterInfo) {
        //章节id
        Long chapterId = courseChapterInfo.getChapterId();
        //章节视频
        List<CourseChapterVideo> chapterVideoList = courseChapterInfo.getChapterVideoList();
        if (chapterId == null || CollectionUtils.isEmpty(chapterVideoList)) {
            throw new RuntimeException("章节id或视频列表为空");
        }
        // 传来的视频列表 不是修改就是新增，删除的在页面单独调接口删除
        for (CourseChapterVideo video : chapterVideoList) {
            if (video.getVideoId() != null) {
                updateCourseChapterVideo(video);
            } else {
                insertCourseChapterVideo(video);
            }
        }
        return 1;
    }

    /**
     * 更新章节视频信息审核状态和发布状态  按video id来
     *
     * @param pageInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int updateCourseChapterVideoStatus(PageInfo pageInfo) {
        //章节id
        Long pageId = pageInfo.getPageId();
        //审核状态
        String checkStatus = pageInfo.getCheckStatus();
        CourseChapterVideo courseChapterVideo = new CourseChapterVideo(pageId, checkStatus, pageInfo.getApplyReason());
        //根据视频查询课程的审核状态
        String coursePublishStatus = courseChapterVideoMapper.selectCoursePublishStatusByVideoId(pageId);
        courseChapterVideo.setPublishStatus(coursePublishStatus);
        return courseChapterVideoMapper.updateCourseChapterVideo(courseChapterVideo);
    }

    /**
     * 更新章节视频信息审核状态和发布状态  按章节id来
     *
     * @param auditResult
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int updateCourseChapterVideoStatusByChapter(ChapterAuditResult auditResult) {
        //章节id
        Long chapterId = auditResult.getChapterId();
        //各个视频的审核信息
        List<PageInfo> pageInfo = auditResult.getPageInfo();
        //根据章节id查询课程的审核状态
        String coursePublishStatus = courseChapterVideoMapper.selectCoursePublishStatusByChapterId(chapterId);
        for (PageInfo info : pageInfo) {
            CourseChapterVideo courseChapterVideo = new CourseChapterVideo(info.getPageId(), info.getCheckStatus(), info.getApplyReason());
            courseChapterVideo.setPublishStatus(coursePublishStatus);
            courseChapterVideoMapper.updateCourseChapterVideo(courseChapterVideo);
        }
        return 1;
    }

    /**
     * 批量删除章节视频信息
     *
     * @param videoIds 需要删除的章节视频信息主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCourseChapterVideoByVideoIds(Long[] videoIds, Long chapterId) {
        int i = courseChapterVideoMapper.updateFlagChapterVideoByVideoIds(videoIds);
        updateCreditHour(chapterId);
        return i;
    }

    /**
     * 删除章节视频信息信息
     *
     * @param videoId 章节视频信息主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCourseChapterVideoByVideoId(Long videoId, Long chapterId) {
        int i = courseChapterVideoMapper.updateFlagChapterVideoByVideoId(videoId);
        updateCreditHour(chapterId);
        return i;
    }


    /**
     * 通过章节id，更新章节学时和课程学时
     *
     * @param chapterId
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCreditHour(Long chapterId) {
        //更新章节学时
        courseInfoMapper.updateChapterCreditHourByChapterId(chapterId);
        //更新课程学时
        courseInfoMapper.updateCourseCreditHourByChapterId(chapterId);
    }
}
