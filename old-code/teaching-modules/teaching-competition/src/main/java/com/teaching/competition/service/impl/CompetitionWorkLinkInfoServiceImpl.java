package com.teaching.competition.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.competition.domain.CompetitionWorkLinkInfo;
import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.mapper.CompetitionWorkLinkInfoMapper;
import com.teaching.competition.mapper.CompetitionWorksMapper;
import com.teaching.competition.service.ICompetitionWorkLinkInfoService;
import com.teaching.competition.util.ExtractionCodeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作品打分链接信息Service业务层处理
 * 
 * @author teaching
 * @date 2025-11-19
 */
@Service
public class CompetitionWorkLinkInfoServiceImpl implements ICompetitionWorkLinkInfoService
{
    @Autowired
    private CompetitionWorkLinkInfoMapper competitionWorkLinkInfoMapper;

    @Autowired
    private CompetitionWorksMapper competitionWorksMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 查询作品打分链接信息
     * 
     * @param linkId 作品打分链接信息主键
     * @return 作品打分链接信息
     */
    @Override
    public CompetitionWorkLinkInfo selectCompetitionWorkLinkInfoByLinkId(Long linkId) {
        CompetitionWorkLinkInfo competitionWorkLinkInfo = competitionWorkLinkInfoMapper.selectCompetitionWorkLinkInfoByLinkId(linkId);
        if(Objects.nonNull(competitionWorkLinkInfo)){
            CompetitionWorks competitionWorks = new CompetitionWorks();
            competitionWorks.setWorksIds(competitionWorkLinkInfo.getWorksId());
            List<CompetitionWorks> worksList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
            competitionWorkLinkInfo.setCompetitionWorksList(worksList);
        }
        return competitionWorkLinkInfo;
    }

    /**
     * 查询作品打分链接信息列表
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 作品打分链接信息
     */
    @Override
    public List<CompetitionWorkLinkInfo> selectCompetitionWorkLinkInfoList(CompetitionWorkLinkInfo competitionWorkLinkInfo) {
        // 作品列表
        List<CompetitionWorkLinkInfo> competitionWorkLinkInfos = competitionWorkLinkInfoMapper.selectCompetitionWorkLinkInfoList(competitionWorkLinkInfo);
        return competitionWorkLinkInfos;
    }

    /**
     * 新增作品打分链接信息
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 结果
     */
    @Override
    public String insertCompetitionWorkLinkInfo(CompetitionWorkLinkInfo competitionWorkLinkInfo) {
        competitionWorkLinkInfo.setCreateTime(DateUtils.getNowDate());
        String extractionCode = ExtractionCodeUtil.getExtractionCode();
        // 加密
        // 每次放redis用于后续校验
        redisService.setCacheObject(extractionCode, competitionWorkLinkInfo.getWorksId());
        competitionWorkLinkInfo.setExtractionCode(extractionCode);
        LocalDate now = LocalDate.now();
        competitionWorkLinkInfo.setExtractionCodeTime(DateUtils.toDate(now.plusDays(7)));
        competitionWorkLinkInfoMapper.insertCompetitionWorkLinkInfo(competitionWorkLinkInfo);
        return extractionCode;
    }

    /**
     * 修改作品打分链接信息
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 结果
     */
    @Override
    public int updateCompetitionWorkLinkInfo(CompetitionWorkLinkInfo competitionWorkLinkInfo) {
        competitionWorkLinkInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionWorkLinkInfoMapper.updateCompetitionWorkLinkInfo(competitionWorkLinkInfo);
    }

    /**
     * 批量删除作品打分链接信息
     * 
     * @param linkIds 需要删除的作品打分链接信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorkLinkInfoByLinkIds(Long[] linkIds)
    {
        return competitionWorkLinkInfoMapper.deleteCompetitionWorkLinkInfoByLinkIds(linkIds);
    }

    /**
     * 删除作品打分链接信息信息
     * 
     * @param linkId 作品打分链接信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorkLinkInfoByLinkId(Long linkId)
    {
        return competitionWorkLinkInfoMapper.deleteCompetitionWorkLinkInfoByLinkId(linkId);
    }
}
