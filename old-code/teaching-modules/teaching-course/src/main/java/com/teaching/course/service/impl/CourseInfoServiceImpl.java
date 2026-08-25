package com.teaching.course.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.course.mapper.CourseChapterVideoMapper;
import com.teaching.course.mapper.CourseInfoMapper;
import com.teaching.course.service.ICourseInfoService;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class CourseInfoServiceImpl implements ICourseInfoService {
    private static final Logger log = LoggerFactory.getLogger(CourseInfoServiceImpl.class);
    @Autowired
    private CourseInfoMapper courseInfoMapper;
    @Autowired
    private CourseChapterVideoMapper courseChapterVideoMapper;

    /**
     * 查询课程信息，包括章节和视频详情信息
     *
     * @param courseId 课程信息主键
     * @return 课程信息
     */
    @Override
    public CourseInfo selectCourseInfoByCourseId(Long courseId) {
        // 根据课程ID查询课程信息，包括章节和视频详情
        CourseInfo courseInfo = courseInfoMapper.selectCourseInfoByCourseId(courseId);
        if (courseInfo == null) {
            return null;
        }
        // 计算总时长
        List<CourseChapterInfo> chapters = courseInfo.getCourseChapterInfoList();
        if (chapters != null && !chapters.isEmpty()) {
            long totalDuration = chapters.stream()
                    .mapToLong(this::calculateChapterVideoDuration)
                    .sum();
            courseInfo.setVideoTotalDuration(totalDuration);
        }
        return courseInfo;
    }

    /**
     * 根据章节id获取课程信息，包括章节和视频详情信息
     * @param chapterId 章节id
     * @return
     */
    @Override
    public CourseChapterInfo getChapterAndVideoInfoByChapter(Long chapterId) {
        return courseInfoMapper.selectChapterAndVideoInfoByChapterId(chapterId);
    }

    /**
     * 计算某章节下所有视频时长之和
     *
     * @param chapter 章节
     * @return 章节下视频总时长
     */
    private long calculateChapterVideoDuration(CourseChapterInfo chapter) {
        if (chapter == null || chapter.getChapterVideoList() == null) {
            return 0L;
        }
        return chapter.getChapterVideoList().stream()
                .mapToLong(video -> video.getVideoDuration() != null ? video.getVideoDuration() : 0L)
                .sum();
    }


    /**
     * 查询课程信息列表，包括章节不包含视频信息
     *
     * @param courseInfo 课程信息
     * @return 课程信息
     */
    @Override
    public List<CourseInfo> selectCourseInfoList(CourseInfo courseInfo) {
        return courseInfoMapper.selectCourseInfoList(courseInfo);
    }

    /**
     * 新增课程信息
     *
     * @param courseInfo 课程信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertCourseInfo(CourseInfo courseInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseInfo.setCreateBy(sysUser.getUserName());
        courseInfo.setCreateTime(DateUtils.getNowDate());
        courseInfo.setUserId(sysUser.getUserId());
        courseInfo.setOrgId(sysUser.getOrgId());
        courseInfo.setCourseCode("K" + DateUtils.dateTimeNow("yyyyMMddHHmmssSSS"));
        int rows = courseInfoMapper.insertCourseInfo(courseInfo);
        insertCourseChapterInfo(courseInfo);
        return rows;
    }

    /**
     * 修改课程信息
     *
     * @param courseInfo 课程信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateCourseInfo(CourseInfo courseInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseInfo.setUpdateTime(DateUtils.getNowDate());
        courseInfo.setUpdateBy(sysUser.getNickName());
        updateOrInsertCourseChapterInfo(courseInfo);
        return courseInfoMapper.updateCourseInfo(courseInfo);
    }

    /**
     * 修改审核状态
     *
     * @param pageInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int updateStatus(PageInfo pageInfo) {
        CourseInfo courseInfo = new CourseInfo(pageInfo.getPageId(), pageInfo.getCheckStatus(),pageInfo.getApplyReason());
        return courseInfoMapper.updateCourseInfo(courseInfo);
    }

    /**
     * 批量删除课程信息
     *
     * @param courseIds 需要删除的课程信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCourseInfoByCourseIds(Long[] courseIds) {
        courseInfoMapper.deleteCourseChapterInfoByCourseIds(courseIds);
        return courseInfoMapper.deleteCourseInfoByCourseIds(courseIds);
    }

    /**
     * 删除课程信息信息
     *
     * @param courseId 课程信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCourseInfoByCourseId(Long courseId) {
        deleteCourseChapterInfoByCourseId(courseId);
        return courseInfoMapper.deleteCourseInfoByCourseId(courseId);
    }

    /**
     * 修改或新增章节信息信息
     *
     * @param courseInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrInsertCourseChapterInfo(CourseInfo courseInfo) {
        List<CourseChapterInfo> courseChapterInfoList = courseInfo.getCourseChapterInfoList();
        Long courseId = courseInfo.getCourseId();
        if (CollectionUtils.isEmpty(courseChapterInfoList)) {
            //删除全部
            courseInfoMapper.deleteCourseChapterInfoByCourseId(courseId);
            deleteCourseChapterInfoByCourseId(courseId);
            return;
        }
        //章节信息包含视频信息
        List<CourseChapterInfo> courseChapterInfos = courseInfoMapper.selectCourseChapterInfoList(courseId);
        Set<Long> existingIds = courseChapterInfos.stream().map(CourseChapterInfo::getChapterId).collect(Collectors.toSet());
        courseChapterInfoList.forEach(courseChapterInfo -> {
            String createBy = courseInfo.getCreateBy();
            Date createTime = courseInfo.getCreateTime();
            Long userId = courseInfo.getUserId();
            Long orgId = courseInfo.getOrgId();
            courseChapterInfo.setCreateTime(createTime);
            courseChapterInfo.setUserId(userId);
            courseChapterInfo.setOrgId(orgId);
            courseChapterInfo.setCourseId(courseId);
            Long chapterId = courseChapterInfo.getChapterId();
            if (StringUtils.isNull(chapterId)) {
                // 新增章节信息信息
                courseInfoMapper.insertCourseChapterInfo(courseChapterInfo);
            } else {
                // 如果传入记录有ID，检查是否存在于数据库中
                if (existingIds.contains(chapterId)) {
                    // 存在于数据库中，标记为更新
                    courseChapterInfo.setUpdateBy(createBy);
                    courseInfoMapper.updateCourseChapterInfo(courseChapterInfo);
                    // 从现有ID集合中移除，剩下的就是需要删除的
                    existingIds.remove(chapterId);
                } /*else {
                    // 不在库中，传来的有id，正常不会出现这情况
                    courseInfoMapper.insertCourseChapterInfo(courseChapterInfo);
                }*/
            }
        });
        // 剩下的existingIds就是需要删除的记录ID
        List<Long> deleteIds = new ArrayList<>(existingIds);
        if (!deleteIds.isEmpty()) {
            courseInfoMapper.batchDeleteCourseChapterInfoByIds(deleteIds);
            courseChapterVideoMapper.deleteCourseChapterVideoByVideoByChapterIds(deleteIds);
        }
    }

    /**
     * 根据课程ID删除章节信息和视频信息
     *
     * @param courseId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseChapterInfoByCourseId(Long courseId) {
        CourseInfo courseInfo = courseInfoMapper.selectCourseInfoByCourseId(courseId);
        if (Objects.isNull(courseInfo)) {
            throw new ServiceException("课程信息不存在");
        }
        //根据课程id删除章节信息信息
        courseInfoMapper.deleteCourseChapterInfoByCourseId(courseId);
        //删除章节下的视频信息
        List<CourseChapterInfo> courseChapterInfoList = courseInfo.getCourseChapterInfoList();
        List<Long> collect = courseChapterInfoList.stream().map(CourseChapterInfo::getChapterId).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            courseChapterVideoMapper.deleteCourseChapterVideoByVideoByChapterIds(collect);
        }
    }

    /**
     * 新增章节信息信息
     *
     * @param courseInfo 课程信息对象
     */
    public void insertCourseChapterInfo(CourseInfo courseInfo) {
        List<CourseChapterInfo> courseChapterInfoList = courseInfo.getCourseChapterInfoList();
        Long courseId = courseInfo.getCourseId();
        if (StringUtils.isNotNull(courseChapterInfoList)) {
            Date createTime = courseInfo.getCreateTime();
            Long userId = courseInfo.getUserId();
            Long orgId = courseInfo.getOrgId();
            List<CourseChapterInfo> list = new ArrayList<CourseChapterInfo>();
            for (CourseChapterInfo courseChapterInfo : courseChapterInfoList) {
                courseChapterInfo.setCourseId(courseId);
                courseChapterInfo.setCreateTime(createTime);
                courseChapterInfo.setUserId(userId);
                courseChapterInfo.setOrgId(orgId);
                list.add(courseChapterInfo);
            }
            if (!list.isEmpty()) {
                courseInfoMapper.batchCourseChapterInfo(list);
            }
        }
    }

    /**
     * 课程分类是否被引用
     * @param classifyId
     * @return
     */
    @Override
    public boolean hasUsedByClassifyId(Long classifyId) {
        return courseInfoMapper.hasUsedByClassifyId(classifyId) > 0;
    }
}
