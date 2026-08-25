package com.teaching.competition.service;

import com.teaching.competition.domain.SponsoringEnterpriseInfo;

import java.util.List;

/**
 * 赞助企业信息Service接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface ISponsoringEnterpriseInfoService 
{
    /**
     * 查询赞助企业信息
     * 
     * @param enterpriseId 赞助企业信息主键
     * @return 赞助企业信息
     */
    public SponsoringEnterpriseInfo selectSponsoringEnterpriseInfoByEnterpriseId(Long enterpriseId);

    /**
     * 查询赞助企业信息列表
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 赞助企业信息集合
     */
    public List<SponsoringEnterpriseInfo> selectSponsoringEnterpriseInfoList(SponsoringEnterpriseInfo sponsoringEnterpriseInfo);

    /**
     * 新增赞助企业信息
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 结果
     */
    public int insertSponsoringEnterpriseInfo(SponsoringEnterpriseInfo sponsoringEnterpriseInfo);

    /**
     * 修改赞助企业信息
     * 
     * @param sponsoringEnterpriseInfo 赞助企业信息
     * @return 结果
     */
    public int updateSponsoringEnterpriseInfo(SponsoringEnterpriseInfo sponsoringEnterpriseInfo);

    /**
     * 批量删除赞助企业信息
     * 
     * @param enterpriseIds 需要删除的赞助企业信息主键集合
     * @return 结果
     */
    public int deleteSponsoringEnterpriseInfoByEnterpriseIds(String enterpriseIds);

    /**
     * 删除赞助企业信息信息
     * 
     * @param enterpriseId 赞助企业信息主键
     * @return 结果
     */
    public int deleteSponsoringEnterpriseInfoByEnterpriseId(Long enterpriseId);
}
