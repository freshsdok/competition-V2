package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;

import com.teaching.system.api.domain.IdentityInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 身份认证信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface IdentityInfoMapper
{
    /**
     * 查询身份认证信息
     *
     * @param userId 身份认证信息主键
     * @return 身份认证信息
     */
    public IdentityInfo selectIdentityInfoByAuthId(@Param("userId") Long userId,@Param("authId") Long authId,@Param("certificationType") String certificationType);

    /**
     * 校验用户是否进行身份认证
     * @param
     * @return
     */
    public Integer checkIdentityInfo(@Param("userId") Long userId);

    /**
     * 查询身份认证信息列表
     *
     * @param identityInfo 身份认证信息
     * @return 身份认证信息集合
     */
    public List<IdentityInfo> selectIdentityInfoList(IdentityInfo identityInfo);

    /**
     * 新增身份认证信息
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    public int insertIdentityInfo(IdentityInfo identityInfo);

    public int batchInsertIdentityInfo(@Param("identityInfoList") List<IdentityInfo> identityInfoList);
    /**
     * 修改身份认证信息
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    public int updateIdentityInfo(IdentityInfo identityInfo);

    /**
     * 删除身份认证信息
     *
     * @param authId 身份认证信息主键
     * @return 结果
     */
    public int deleteIdentityInfoByAuthId(Long authId);

    /**
     * 批量删除身份认证信息
     *
     * @param authIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteIdentityInfoByAuthIds(Long[] authIds);

    /**
     * 根据用户id和身份认证类型查询审核中的信息
     * @param userId
     * @param certificationType
     * @return
     */
    public IdentityInfo selectInfoByUserAndType(@Param("userId") Long userId, @Param("certificationType") String certificationType);

    /**
     * 根据学校id查询教师姓名列表
     * @param schoolId
     * @return
     */
    public List<Map<String,Object>>selectTeacherNameBySchoolId(String schoolId);
}
