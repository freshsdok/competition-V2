package com.teaching.system.service;

import java.util.List;
import com.teaching.system.api.domain.AuthInfo;

/**
 * 实名认证Service接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface IAuthInfoService 
{
    /**
     * 查询实名认证
     * 
     * @param authId 实名认证主键
     * @return 实名认证
     */
    public AuthInfo selectAuthInfoByAuthId(Long userId);

    public AuthInfo selectAuthInfoByUserId(Long userId);

    /**
     * 查询实名认证列表
     * 
     * @param authInfo 实名认证
     * @return 实名认证集合
     */
    public List<AuthInfo> selectAuthInfoList(AuthInfo authInfo);

    public List<AuthInfo> selectAuthInfoByUserName(String realName);

    public List<AuthInfo> selectAuthInfoByIdCard(AuthInfo authInfo);

    /**
     * 新增实名认证
     * 
     * @param authInfo 实名认证
     * @return 结果
     */
    public int insertAuthInfo(AuthInfo authInfo) throws Exception;

    /**
     * 修改实名认证
     * 
     * @param authInfo 实名认证
     * @return 结果
     */
    public int updateAuthInfo(AuthInfo authInfo);

    /**
     * 批量删除实名认证
     * 
     * @param authIds 需要删除的实名认证主键集合
     * @return 结果
     */
    public int deleteAuthInfoByAuthIds(String[] authIds);

    /**
     * 删除实名认证信息
     * 
     * @param authId 实名认证主键
     * @return 结果
     */
    public int deleteAuthInfoByAuthId(String authId);
}
