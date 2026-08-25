package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysUser;

/**
 * 身份认证信息Service接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface IIdentityInfoService
{
    /**
     * 查询身份认证信息
     *
     * @param userId 身份认证信息主键
     * @return 身份认证信息
     */
    public IdentityInfo selectIdentityInfoByAuthId(Long userId,Long authId,String certificationType);

    /**
     * 查询身份认证信息列表
     *
     * @param identityInfo 身份认证信息
     * @return 身份认证信息集合
     */
    public List<IdentityInfo> selectIdentityInfoList(IdentityInfo identityInfo);

    /**
     * 查询身份认证信息列表(用户端)
     *
     * @param identityInfo 身份认证信息
     * @return 身份认证信息集合
     */
    public List<IdentityInfo> selectUserIdentityInfoList(IdentityInfo identityInfo);

    /**
     * 新增身份认证信息
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    public int insertIdentityInfo(IdentityInfo identityInfo);

    /**
     * 修改身份认证信息
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    public int updateIdentityInfo(IdentityInfo identityInfo);

    /**
     * 修改身份认证信息状态
     * @param identityInfo
     * @return
     */
    public int updateIdentityInfoStatus(IdentityInfo identityInfo);

    /**
     * 批量删除身份认证信息
     *
     * @param authIds 需要删除的身份认证信息主键集合
     * @return 结果
     */
    public int deleteIdentityInfoByAuthIds(Long[] authIds);

    /**
     * 删除身份认证信息信息
     *
     * @param authId 身份认证信息主键
     * @return 结果
     */
    public int deleteIdentityInfoByAuthId(Long authId);

    /**
     * 根据学校id查询教师姓名列表
     * @param schoolId
     * @return
     */
    public List<Map<String,Object>>getTeacherNameBySchoolId(String schoolId);
}
