package com.teaching.course.service.impl;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.course.domain.CourseClassifyInfo;
import com.teaching.course.mapper.CourseClassifyInfoMapper;
import com.teaching.course.service.ICourseClassifyInfoService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程分类Service业务层处理
 *
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class CourseClassifyInfoServiceImpl implements ICourseClassifyInfoService {
    @Autowired
    private CourseClassifyInfoMapper courseClassifyInfoMapper;

    /**
     * 查询课程分类
     *
     * @param classifyId 课程分类主键
     * @return 课程分类
     */
    @Override
    public CourseClassifyInfo selectCourseClassifyInfoByClassifyId(Long classifyId) {
        return courseClassifyInfoMapper.selectCourseClassifyInfoByClassifyId(classifyId);
    }

    /**
     * 查询课程分类列表
     *
     * @param courseClassifyInfo 课程分类
     * @return 课程分类
     */
    @Override
    public List<CourseClassifyInfo> selectCourseClassifyInfoList(CourseClassifyInfo courseClassifyInfo) {
        courseClassifyInfo.setClassifyCode(StringUtils.isNotBlank(courseClassifyInfo.getClassifyCode()) ? courseClassifyInfo.getClassifyCode().toUpperCase() : null);
        return courseClassifyInfoMapper.selectCourseClassifyInfoList(courseClassifyInfo);
    }

    /**
     * 新增课程分类
     *
     * @param courseClassifyInfo 课程分类
     * @return 结果
     */
    @Override
    public int insertCourseClassifyInfo(CourseClassifyInfo courseClassifyInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseClassifyInfo.setCreateBy(sysUser.getNickName());
        courseClassifyInfo.setCreateTime(DateUtils.getNowDate());
        String pinYin = PinyinUtil.getFirstLetter(courseClassifyInfo.getClassifyName(), "").toUpperCase();
        courseClassifyInfo.setClassifyCode(pinYin);
        return courseClassifyInfoMapper.insertCourseClassifyInfo(courseClassifyInfo);
    }

    /**
     * 修改课程分类
     *
     * @param courseClassifyInfo 课程分类
     * @return 结果
     */
    @Override
    public int updateCourseClassifyInfo(CourseClassifyInfo courseClassifyInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseClassifyInfo.setUpdateBy(sysUser.getNickName());
        courseClassifyInfo.setUpdateTime(DateUtils.getNowDate());
        return courseClassifyInfoMapper.updateCourseClassifyInfo(courseClassifyInfo);
    }

    /**
     * 批量删除课程分类
     *
     * @param classifyIds 需要删除的课程分类主键
     * @return 结果
     */
    @Override
    public int deleteCourseClassifyInfoByClassifyIds(Long[] classifyIds) {
        return courseClassifyInfoMapper.deleteCourseClassifyInfoByClassifyIds(classifyIds);
    }

    /**
     * 删除课程分类信息
     *
     * @param classifyId 课程分类主键
     * @return 结果
     */
    @Override
    public int deleteCourseClassifyInfoByClassifyId(Long classifyId) {
        return courseClassifyInfoMapper.deleteCourseClassifyInfoByClassifyId(classifyId);
    }

    /**
     * 是否有子分类信息
     *
     * @param classifyId
     * @return
     */
    @Override
    public boolean hasChildByClassifyId(Long classifyId) {
        int result = courseClassifyInfoMapper.hasChildByClassifyId(classifyId);
        return result > 0;
    }
}
