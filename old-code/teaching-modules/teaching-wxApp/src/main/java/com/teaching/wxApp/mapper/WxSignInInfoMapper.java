package com.teaching.wxApp.mapper;

import com.teaching.wxApp.domain.WxSignInInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 签到信息Mapper接口
 *
 * @author teaching
 * @date 2026-04-08
 */
public interface WxSignInInfoMapper {

    /**
     * 查询签到信息
     *
     * @param signId 签到信息主键
     * @return 签到信息
     */
    public WxSignInInfo selectWxSignInInfoBySignId(Long signId);

    /**
     * 查询签到信息列表
     *
     * @param wxSignInInfo 签到信息
     * @return 签到信息集合
     */
    public List<WxSignInInfo> selectWxSignInInfoList(WxSignInInfo wxSignInInfo);

    /**
     * 新增签到信息
     *
     * @param wxSignInInfo 签到信息
     * @return 结果
     */
    public int insertWxSignInInfo(WxSignInInfo wxSignInInfo);

    /**
     * 修改签到信息
     *
     * @param wxSignInInfo 签到信息
     * @return 结果
     */
    public int updateWxSignInInfo(WxSignInInfo wxSignInInfo);

    /**
     * 删除签到信息
     *
     * @param signId 签到信息主键
     * @return 结果
     */
    public int deleteWxSignInInfoBySignId(Long signId);

    /**
     * 批量删除签到信息
     *
     * @param signIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWxSignInInfoBySignIds(Long[] signIds);
}
