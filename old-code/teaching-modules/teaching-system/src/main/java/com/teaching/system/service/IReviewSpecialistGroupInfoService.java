package com.teaching.system.service;

import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.ReviewSpecialistGroupInfo;
import com.teaching.system.domain.SpecialistSysUser;

import java.util.List;

/**
 * 专家组Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewSpecialistGroupInfoService {
    /**
     * 查询专家组
     *
     * @param groupId 专家组主键
     * @return 专家组
     */
    public ReviewSpecialistGroupInfo selectReviewSpecialistGroupInfoByGroupId(Long groupId);

    /**
     * 查询专家组列表
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 专家组集合
     */
    public List<ReviewSpecialistGroupInfo> selectReviewSpecialistGroupInfoList(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 新增专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    public int insertReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 修改专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    public int updateReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 批量删除专家组
     *
     * @param groupIds 需要删除的专家组主键集合
     * @return 结果
     */
    public int deleteReviewSpecialistGroupInfoByGroupIds(Long[] groupIds);

    /**
     * 删除专家组信息
     *
     * @param groupId 专家组主键
     * @return 结果
     */
    public int deleteReviewSpecialistGroupInfoByGroupId(Long groupId);

    // 获取专家组信息
    public List<SpecialistSysUser> getSpecialistInfo(SpecialistSysUser specialistSysUser);
}
