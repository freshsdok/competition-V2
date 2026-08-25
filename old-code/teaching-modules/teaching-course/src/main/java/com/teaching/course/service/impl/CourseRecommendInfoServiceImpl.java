package com.teaching.course.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.course.domain.CourseRecommendInfo;
import com.teaching.course.domain.CourseRecommendRela;
import com.teaching.course.mapper.CourseRecommendInfoMapper;
import com.teaching.course.service.ICourseRecommendInfoService;
import com.teaching.system.api.domain.SysUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程推荐信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-23
 */
@Service
public class CourseRecommendInfoServiceImpl implements ICourseRecommendInfoService {
    @Autowired
    private CourseRecommendInfoMapper courseRecommendInfoMapper;

    /**
     * 查询课程推荐信息
     *
     * @param remdId 课程推荐信息主键
     * @return 课程推荐信息
     */
    @Override
    public CourseRecommendInfo selectCourseRecommendInfoByRemdId(Long remdId) {
        return courseRecommendInfoMapper.selectCourseRecommendInfoByRemdId(remdId);
    }

    /**
     * 查询课程推荐信息列表
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 课程推荐信息
     */
    @Override
    public List<CourseRecommendInfo> selectCourseRecommendInfoList(CourseRecommendInfo courseRecommendInfo) {
        return courseRecommendInfoMapper.selectCourseRecommendInfoList(courseRecommendInfo);
    }

    /**
     * 新增课程推荐信息
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertCourseRecommendInfo(CourseRecommendInfo courseRecommendInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        courseRecommendInfo.setCreateTime(DateUtils.getNowDate());
        courseRecommendInfo.setCreateBy(sysUser.getNickName());
        List<CourseRecommendRela> courseRecommendRelaList = courseRecommendInfo.getCourseRecommendRelaList();
        courseRecommendInfo.setCourseNum(CollectionUtils.isNotEmpty(courseRecommendRelaList) ? courseRecommendRelaList.size() : 0L);
        int rows = courseRecommendInfoMapper.insertCourseRecommendInfo(courseRecommendInfo);
        insertCourseRecommendRela(courseRecommendInfo);
        return rows;
    }

    /**
     * 修改课程推荐信息
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateCourseRecommendInfo(CourseRecommendInfo courseRecommendInfo) {
        courseRecommendInfo.setUpdateTime(DateUtils.getNowDate());
        /*courseRecommendInfoMapper.deleteCourseRecommendRelaByRemdId(courseRecommendInfo.getRemdId());
        insertCourseRecommendRela(courseRecommendInfo);*/
        insertOrUpdateCourseRecommendRela(courseRecommendInfo);
        return courseRecommendInfoMapper.updateCourseRecommendInfo(courseRecommendInfo);
    }

    /**
     * 批量删除课程推荐信息
     *
     * @param remdIds 需要删除的课程推荐信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCourseRecommendInfoByRemdIds(Long[] remdIds) {
        courseRecommendInfoMapper.deleteCourseRecommendRelaByRemdIds(remdIds);
        return courseRecommendInfoMapper.deleteCourseRecommendInfoByRemdIds(remdIds);
    }

    /**
     * 删除课程推荐信息信息
     *
     * @param remdId 课程推荐信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCourseRecommendInfoByRemdId(Long remdId) {
        courseRecommendInfoMapper.deleteCourseRecommendRelaByRemdId(remdId);
        upateCourseNumByRemId(remdId,0L);
        return courseRecommendInfoMapper.deleteCourseRecommendInfoByRemdId(remdId);
    }

    /**
     * 新增课程推荐关联关系信息
     *
     * @param courseRecommendInfo 课程推荐信息对象
     */
    public void insertCourseRecommendRela(CourseRecommendInfo courseRecommendInfo) {
        List<CourseRecommendRela> courseRecommendRelaList = courseRecommendInfo.getCourseRecommendRelaList();
        if (CollectionUtils.isNotEmpty(courseRecommendRelaList)) {
            Long remdId = courseRecommendInfo.getRemdId();
            String createBy = courseRecommendInfo.getCreateBy();
            Date createTime = courseRecommendInfo.getCreateTime();
            List<CourseRecommendRela> list = new ArrayList<CourseRecommendRela>();
            for (CourseRecommendRela courseRecommendRela : courseRecommendRelaList) {
                courseRecommendRela.setRemdId(remdId);
                courseRecommendRela.setCreateBy(createBy);
                courseRecommendRela.setCreateTime(createTime);
                list.add(courseRecommendRela);
            }
            if (!list.isEmpty()) {
                courseRecommendInfoMapper.batchCourseRecommendRela(list);
            }
        }
    }

    /**
     * 新增或更新课程推荐关联关系信息
     * @param courseRecommendInfo
     */
    @Transactional
    public void insertOrUpdateCourseRecommendRela(CourseRecommendInfo courseRecommendInfo) {
        if (courseRecommendInfo == null) {
            return;
        }
        List<CourseRecommendRela> courseRecommendRelaList = courseRecommendInfo.getCourseRecommendRelaList();
        Long remdId = courseRecommendInfo.getRemdId();

        if (CollectionUtils.isEmpty(courseRecommendRelaList)) {
            // 如果传入列表为空，则删除所有关联记录
            courseRecommendInfoMapper.deleteCourseRecommendRelaByRemdId(remdId);
            upateCourseNumByRemId(remdId,0L);
            return;
        }
        // 1. 查询数据库中已有的关联记录ID集合
        List<CourseRecommendRela> existingRelations = courseRecommendInfoMapper.selectCourseRecommendRelaByRemdId(remdId);
        Set<Long> existingIds = existingRelations.stream()
                .map(CourseRecommendRela::getId)
                .collect(Collectors.toSet());

        String createBy = courseRecommendInfo.getCreateBy();
        Date createTime = courseRecommendInfo.getCreateTime() != null ? courseRecommendInfo.getCreateTime() : new Date();
        List<CourseRecommendRela> insertList = new ArrayList<>();
        List<CourseRecommendRela> updateList = new ArrayList<>();
        // 2. 遍历传入列表，分类处理
        for (CourseRecommendRela newRela : courseRecommendRelaList) {
            newRela.setRemdId(remdId);
            newRela.setCreateBy(createBy);
            newRela.setCreateTime(createTime);
            Long newId = newRela.getId();
            if (newId != null) {
                // 如果传入记录有ID，检查是否存在于数据库中
                if (existingIds.contains(newId)) {
                    // 存在于数据库中，标记为更新
                    newRela.setUpdateBy(createBy);
                    updateList.add(newRela);
                    // 从现有ID集合中移除，剩下的就是需要删除的
                    existingIds.remove(newId);
                }
               /* else {
                    // ID不存在于数据库中，但传入有ID，可能是错误情况
                    // 根据业务需求处理，这里选择忽略或作为新增
                    insertList.add(newRela);
                }*/
            } else {
                // 传入记录没有ID，标记为新增
                insertList.add(newRela);
            }
        }
        // 3. 剩下的existingIds就是需要删除的记录ID
        List<Long> deleteIds = new ArrayList<>(existingIds);
        // 4. 执行数据库操作
        if (!insertList.isEmpty()) {
            courseRecommendInfoMapper.batchCourseRecommendRela(insertList);
        }
        if (!updateList.isEmpty()) {
            batchUpdateWithTransaction(updateList, updateList.size());
        }
        if (!deleteIds.isEmpty()) {
            courseRecommendInfoMapper.batchDeleteCourseRecommendRelaByIds(deleteIds);
        }
        upateCourseNumByRemId(remdId,(long)courseRecommendRelaList.size());
    }


    /**
     * 分批更新课程推荐关系
     *
     * @param list      待更新的课程推荐关系列表
     * @param batchSize 每批处理的数量（默认500）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateWithTransaction(List<CourseRecommendRela> list, int batchSize) {
        if (list == null || list.isEmpty()) {
            return;
        }
        // 默认每批500条
        int effectiveBatchSize = batchSize > 0 ? batchSize : 500;
        int total = list.size();
        for (int i = 0; i < total; i += effectiveBatchSize) {
            int end = Math.min(i + effectiveBatchSize, total);
            List<CourseRecommendRela> subList = list.subList(i, end);
            courseRecommendInfoMapper.batchUpdateCourseRecommendRela(subList);
        }
    }

    /**
     * 通过主键修改推荐课程数量
     * @param remdId
     * @param courseNum
     * @return
     */
    public int upateCourseNumByRemId(Long remdId,Long courseNum){
        return courseRecommendInfoMapper.updateCourseRecommendInfo(new CourseRecommendInfo(remdId,courseNum));
    }
}
