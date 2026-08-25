package com.teaching.competition.service.impl;

import java.util.Arrays;
import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.SponsoringEnterpriseInfo;
import com.teaching.competition.mapper.SponsoringEnterpriseInfoMapper;
import com.teaching.competition.service.ISponsoringEnterpriseInfoService;
import com.teaching.system.api.domain.SysUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赞助企业信息Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class SponsoringEnterpriseInfoServiceImpl implements ISponsoringEnterpriseInfoService
{
    @Autowired
    private SponsoringEnterpriseInfoMapper sponsoringEnterpriseInfoMapper;

    /**
     * 查询赞助企业信息
     * 
     * @param enterpriseId 赞助企业信息主键
     * @return 赞助企业信息
     */
    @Override
    public SponsoringEnterpriseInfo selectSponsoringEnterpriseInfoByEnterpriseId(Long enterpriseId)
    {
        return sponsoringEnterpriseInfoMapper.selectSponsoringEnterpriseInfoByEnterpriseId(enterpriseId);
    }

    /**
     * 查询赞助企业信息列表
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 赞助企业信息
     */
    @Override
    @DataScope(orgAlias = "a", userAlias = "a")
    public List<SponsoringEnterpriseInfo> selectSponsoringEnterpriseInfoList(SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        return sponsoringEnterpriseInfoMapper.selectSponsoringEnterpriseInfoList(sponsoringEnterpriseInfo);
    }

    /**
     * 新增赞助企业信息
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 结果
     */
    @Override
    public int insertSponsoringEnterpriseInfo(SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        sponsoringEnterpriseInfo.setUserId(sysUserInfo.getUserId());
        sponsoringEnterpriseInfo.setOrgId(sysUserInfo.getOrgId());
        sponsoringEnterpriseInfo.setCreateBy(sysUserInfo.getUserId()+"");
        sponsoringEnterpriseInfo.setCreateTime(DateUtils.getNowDate());
        return sponsoringEnterpriseInfoMapper.insertSponsoringEnterpriseInfo(sponsoringEnterpriseInfo);
    }

    /**
     * 修改赞助企业信息
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 结果
     */
    @Override
    public int updateSponsoringEnterpriseInfo(SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        sponsoringEnterpriseInfo.setUpdateBy(sysUserInfo.getUserId()+"");
        sponsoringEnterpriseInfo.setUpdateTime(DateUtils.getNowDate());
        return sponsoringEnterpriseInfoMapper.updateSponsoringEnterpriseInfo(sponsoringEnterpriseInfo);
    }

    /**
     * 批量删除赞助企业信息
     * 
     * @param enterpriseIdStr 需要删除的赞助企业信息主键
     * @return 结果
     */
    @Override
    public int deleteSponsoringEnterpriseInfoByEnterpriseIds(String enterpriseIdStr)
    {
        if(StringUtils.isNotBlank(enterpriseIdStr)){
            Long[] enterpriseIds = Arrays.stream(enterpriseIdStr.split(","))
                    .map(Long::valueOf)
                    .toArray(Long[]::new);
            return sponsoringEnterpriseInfoMapper.deleteSponsoringEnterpriseInfoByEnterpriseIds(enterpriseIds);
        }
        return 0;
    }

    /**
     * 删除赞助企业信息信息
     * 
     * @param enterpriseId 赞助企业信息主键
     * @return 结果
     */
    @Override
    public int deleteSponsoringEnterpriseInfoByEnterpriseId(Long enterpriseId)
    {
        return sponsoringEnterpriseInfoMapper.deleteSponsoringEnterpriseInfoByEnterpriseId(enterpriseId);
    }
}
