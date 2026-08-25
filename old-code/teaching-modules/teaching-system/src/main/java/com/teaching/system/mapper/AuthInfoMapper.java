package com.teaching.system.mapper;

import java.util.List;

import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.teaching.system.api.domain.AuthInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 实名认证Mapper接口
 *
 * @author teaching
 * @date 2025-10-13
 */
@Mapper
public interface AuthInfoMapper
{
    /**
     * 查询实名认证
     *
     * @param authId 实名认证主键
     * @return 实名认证
     */
    public AuthInfo selectAuthInfoByAuthId(Long userId);

    /**
     * 主键查询
     * @param authId
     * @return
     */
    public AuthInfo selectAuthInfoById(Long authId);

    /**
     * 根据用户id查询实名认证
     *
     * @param userId 用户id
     * @return 实名认证
     */
    public AuthInfo selectAuthInfoByUserId(Long userId);

    public List<AuthInfo> selectAuthInfoByUserName(String realName);

    /**
     * 根据类型及证件编号查询实名认证信息
     * @param authInfo
     * @return
     */
    public AuthInfo selectIdentityInfoByType(AuthInfo authInfo);
    /**
     * 查询实名认证列表
     *
     * @param authInfo 实名认证
     * @return 实名认证集合
     */
    public List<AuthInfo> selectAuthInfoList(AuthInfo authInfo);

    public List<AuthInfo> selectAuthInfoListByUserInfo(@Param("idCartList") List<String> idCartList);

    public List<AuthInfo> selectAuthInfoListByIdCard(AuthInfo authInfo);

    /**
     * 新增实名认证
     *
     * @param authInfo 实名认证
     * @return 结果
     */
    public int insertAuthInfo(AuthInfo authInfo);

    public int batchInsertAuthInfo(@Param("authInfoList") List<AuthInfo> authInfoList);

    /**
     * 修改实名认证
     *
     * @param authInfo 实名认证
     * @return 结果
     */
    public int updateAuthInfo(AuthInfo authInfo);

    /**
     * 删除实名认证
     *
     * @param authId 实名认证主键
     * @return 结果
     */
    public int deleteAuthInfoByAuthId(String authId);

    /**
     * 批量删除实名认证
     *
     * @param authIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAuthInfoByAuthIds(String[] authIds);
}
