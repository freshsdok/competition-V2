package com.teaching.wxApp.service;

import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.wxApp.domain.WxSignInInfo;

import java.util.List;

/**
 * 签到信息Service接口
 *
 * @author teaching
 * @date 2026-04-08
 */
public interface IWxSignInInfoService {

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
     * 获取签到信息列表
     * @param wxSignInInfo
     * @return
     */
    public TableDataInfo getWxSignInInfoList(WxSignInInfo wxSignInInfo);

    /**
     * 获取导出数据
     * @param wxSignInInfo
     * @return
     */
    public List<WxSignInInfo> getEndList(WxSignInInfo wxSignInInfo);
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
     * 批量删除签到信息
     *
     * @param signIds 需要删除的签到信息主键集合
     * @return 结果
     */
    public int deleteWxSignInInfoBySignIds(Long[] signIds);

    /**
     * 删除签到信息
     *
     * @param signId 签到信息主键
     * @return 结果
     */
    public int deleteWxSignInInfoBySignId(Long signId);
}
