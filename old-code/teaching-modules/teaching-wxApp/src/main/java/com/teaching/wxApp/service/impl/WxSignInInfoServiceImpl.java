package com.teaching.wxApp.service.impl;

import com.github.pagehelper.PageInfo;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.core.web.page.TableSupport;
import com.teaching.wxApp.domain.WxSignInInfo;
import com.teaching.wxApp.mapper.WxSignInInfoMapper;
import com.teaching.wxApp.service.IWxSignInInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到信息Service业务层处理
 *
 * @author teaching
 * @date 2026-04-08
 */
@Service
public class WxSignInInfoServiceImpl implements IWxSignInInfoService {

    @Autowired
    private WxSignInInfoMapper wxSignInInfoMapper;

    /**
     * 查询签到信息
     *
     * @param signId 签到信息主键
     * @return 签到信息
     */
    @Override
    public WxSignInInfo selectWxSignInInfoBySignId(Long signId) {
        return wxSignInInfoMapper.selectWxSignInInfoBySignId(signId);
    }

    /**
     * 查询签到信息列表
     *
     * @param wxSignInInfo 签到信息
     * @return 签到信息
     */
    @Override
    public List<WxSignInInfo> selectWxSignInInfoList(WxSignInInfo wxSignInInfo) {
        return wxSignInInfoMapper.selectWxSignInInfoList(wxSignInInfo);
    }


    @Override
    public TableDataInfo getWxSignInInfoList(WxSignInInfo wxSignInInfo) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        List<WxSignInInfo> wxSignInInfos = wxSignInInfoMapper.selectWxSignInInfoList(wxSignInInfo);
        List<WxSignInInfo> wxSignInInfos1 = filter(wxSignInInfos, wxSignInInfo);
        List<WxSignInInfo> paginate = PageUtils.paginate(wxSignInInfos1, pageNum, pageSize);
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(paginate);
        rspData.setMsg("查询成功");
        rspData.setTotal(new PageInfo(wxSignInInfos1).getTotal());
        return rspData;
    }


    @Override
    public List<WxSignInInfo> getEndList(WxSignInInfo wxSignInInfo) {
        WxSignInInfo temp = new WxSignInInfo();
        if (!"all".equals(wxSignInInfo.getExportType())) {
            BeanUtils.copyProperties(wxSignInInfo, temp);
        }
        List<WxSignInInfo> wxSignInInfos = wxSignInInfoMapper.selectWxSignInInfoList(temp);
        return filter(wxSignInInfos, temp);
    }


    /**
     * realName为空时，用nickName代替,realName过滤
     *
     * @param wxSignInInfos
     * @param wxSignInInfo
     * @return
     */
    private List<WxSignInInfo> filter(List<WxSignInInfo> wxSignInInfos, WxSignInInfo wxSignInInfo) {
        if (!CollectionUtils.isEmpty(wxSignInInfos)) {
            wxSignInInfos.forEach(sysUser -> {
                sysUser.setRealName(StringUtils.isNotBlank(sysUser.getRealName()) ? sysUser.getRealName() : sysUser.getNickName());
                sysUser.setSignName(StringUtils.isNotBlank(sysUser.getSignName()) ? sysUser.getSignName() : sysUser.getSignNick());
            });
        }
        if (StringUtils.isNotBlank(wxSignInInfo.getRealName())) {
            wxSignInInfos = wxSignInInfos.stream()
                    .filter(u -> StringUtils.isNotBlank(u.getRealName()) && u.getRealName().contains(wxSignInInfo.getRealName().trim()))
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(wxSignInInfo.getSignName())) {
            wxSignInInfos = wxSignInInfos.stream()
                    .filter(u -> StringUtils.isNotBlank(u.getSignName()) && u.getSignName().contains(wxSignInInfo.getSignName().trim()))
                    .collect(Collectors.toList());
        }
        return wxSignInInfos;
    }

    /**
     * 新增签到信息
     *
     * @param wxSignInInfo 签到信息
     * @return 结果
     */
    @Override
    public int insertWxSignInInfo(WxSignInInfo wxSignInInfo) {
        return wxSignInInfoMapper.insertWxSignInInfo(wxSignInInfo);
    }

    /**
     * 修改签到信息
     *
     * @param wxSignInInfo 签到信息
     * @return 结果
     */
    @Override
    public int updateWxSignInInfo(WxSignInInfo wxSignInInfo) {
        return wxSignInInfoMapper.updateWxSignInInfo(wxSignInInfo);
    }

    /**
     * 批量删除签到信息
     *
     * @param signIds 需要删除的签到信息主键
     * @return 结果
     */
    @Override
    public int deleteWxSignInInfoBySignIds(Long[] signIds) {
        return wxSignInInfoMapper.deleteWxSignInInfoBySignIds(signIds);
    }

    /**
     * 删除签到信息
     *
     * @param signId 签到信息主键
     * @return 结果
     */
    @Override
    public int deleteWxSignInInfoBySignId(Long signId) {
        return wxSignInInfoMapper.deleteWxSignInInfoBySignId(signId);
    }
}
