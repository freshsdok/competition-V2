package com.teaching.competition.service.impl;

import java.util.List;
import java.util.Objects;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionWorkLinkInfo;
import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionStageConfigMapper;
import com.teaching.competition.mapper.CompetitionWorkLinkInfoMapper;
import com.teaching.competition.mapper.CompetitionWorksMapper;
import com.teaching.competition.service.ICompetitionWorksService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionStageConfig;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事作品Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class CompetitionWorksServiceImpl implements ICompetitionWorksService
{
    @Autowired
    private CompetitionWorksMapper competitionWorksMapper;

    @Autowired
    private CompetitionStageConfigMapper competitionStageConfigMapper;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CompetitionWorkLinkInfoMapper competitionWorkLinkInfoMapper;

    /**
     * 查询赛事作品
     * 
     * @param worksId 赛事作品主键
     * @return 赛事作品
     */
    @Override
    public CompetitionWorks selectCompetitionWorksByWorksId(Long worksId) {
        return competitionWorksMapper.selectCompetitionWorksByWorksId(worksId);
    }

    @Override
    public List<CompetitionWorks> selectCompetitionWorksByUserId(CompetitionWorks competitionWorks) {
        return competitionWorksMapper.selectCompetitionList(competitionWorks);
    }

    /**
     * 查询赛事作品列表
     * 
     * @param competitionWorks 赛事作品
     * @return 赛事作品
     */
    @Override
    @DataScope(orgAlias = "a", userAlias = "a")
    public List<CompetitionWorks> selectCompetitionWorksList(CompetitionWorks competitionWorks) {
        List<CompetitionWorks> worksList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
        for (CompetitionWorks works : worksList) {
            CompetitionStageConfig competitionStageConfig = competitionStageConfigMapper.selectCompetitionStageConfigByStageId(works.getStageId());
            works.setStageName(competitionStageConfig.getStageName());
            // 通过用户中心获取用户真实姓名
            R<SysUser> userCenterInfo = remoteUserService.getUserCenterInfo(works.getUserId(), SecurityConstants.INNER);
            if(R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData()) && Objects.nonNull(userCenterInfo.getData().getAuthInfo())){
                works.setUserName(userCenterInfo.getData().getAuthInfo().getRealName() == null? null: userCenterInfo.getData().getAuthInfo().getRealName());
            }
        }
        return worksList;
    }

    @Override
    public List<CompetitionWorks> selectSpecialistCompetitionWorksList(CompetitionWorks competitionWorks) {
        // 校验提取码是否正确，是否失效
        if (!redisService.hasKey(competitionWorks.getExtractionCode())) {
            throw new RuntimeException("提取码错误");
        }
        // 校验提取码有效期
        CompetitionWorkLinkInfo competitionWorkLinkInfo = competitionWorkLinkInfoMapper.selectCompetitionWorkLinkInfoByExtractionCode(competitionWorks.getExtractionCode());
        if (Objects.isNull(competitionWorkLinkInfo)) {
            throw new RuntimeException("该链接不存在");
        }
        if (competitionWorkLinkInfo.getExtractionCodeTime().getTime() < System.currentTimeMillis()) {
            throw new RuntimeException("提取码已失效");
        }
        competitionWorks.setWorksIds(competitionWorkLinkInfo.getWorksId());
        List<CompetitionWorks> worksList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
        for (CompetitionWorks works : worksList) {
            CompetitionStageConfig competitionStageConfig = competitionStageConfigMapper.selectCompetitionStageConfigByStageId(works.getStageId());
            works.setStageName(competitionStageConfig.getStageName());
//            // 通过用户中心获取用户真实姓名
//            R<SysUser> userCenterInfo = remoteUserService.getUserCenterInfo(competitionWorks.getUserId(), SecurityConstants.INNER);
//            if(R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData()) && Objects.nonNull(userCenterInfo.getData().getAuthInfo())){
//                works.setUserName(userCenterInfo.getData().getAuthInfo().getRealName() == null? null: userCenterInfo.getData().getAuthInfo().getRealName());
//            }
        }
        return worksList;
    }

    @Override
    public List<CompetitionWorks> selectCompetitionWorksListByUserId(CompetitionWorks competitionWorks) {
        List<CompetitionWorks> worksList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
        for (CompetitionWorks works : worksList) {
            CompetitionStageConfig competitionStageConfig = competitionStageConfigMapper.selectCompetitionStageConfigByStageId(works.getStageId());
            works.setStageName(competitionStageConfig.getStageName());
            // 通过用户中心获取用户真实姓名
            R<SysUser> userCenterInfo = remoteUserService.getUserCenterInfo(competitionWorks.getUserId(), SecurityConstants.INNER);
            if(R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData()) && Objects.nonNull(userCenterInfo.getData().getAuthInfo())){
                works.setUserName(userCenterInfo.getData().getAuthInfo().getRealName() == null? null: userCenterInfo.getData().getAuthInfo().getRealName());
            }
        }
        return worksList;
    }

    /**
     * 新增赛事作品
     * 
     * @param competitionWorks 赛事作品
     * @return 结果
     */
    @Override
    public int insertCompetitionWorks(CompetitionWorks competitionWorks) {
        if(competitionWorks.getWorksId() == null){
            competitionWorks.setWorksStatus(Constants.WORKS_NO_JUDGE);
            competitionWorks.setCreateTime(DateUtils.getNowDate());
            competitionWorks.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
            competitionWorks.setOrgId(SecurityUtils.getLoginUser().getSysUser().getOrgId());
            competitionWorks.setCreateBy(SecurityUtils.getLoginUser().getUserid()+ "");
        }
        return competitionWorksMapper.insertCompetitionWorks(competitionWorks);
    }

    /**
     * 修改赛事作品
     * 
     * @param competitionWorks 赛事作品
     * @return 结果
     */
    @Override
    public int updateCompetitionWorks(CompetitionWorks competitionWorks) {
        competitionWorks.setUpdateTime(DateUtils.getNowDate());
        return competitionWorksMapper.updateCompetitionWorks(competitionWorks);
    }

    @Override
    public int updateLinkCompetitionWorks(CompetitionWorks competitionWorks) {
        // 校验提取码是否正确，是否失效
        if (!redisService.hasKey(competitionWorks.getExtractionCode())) {
            throw new RuntimeException("提取码错误");
        }
        // 校验提取码有效期
        CompetitionWorkLinkInfo competitionWorkLinkInfo = competitionWorkLinkInfoMapper.selectCompetitionWorkLinkInfoByExtractionCode(competitionWorks.getExtractionCode());
        if (Objects.isNull(competitionWorkLinkInfo)) {
            throw new RuntimeException("该链接不存在");
        }
        if (competitionWorkLinkInfo.getExtractionCodeTime().getTime() < System.currentTimeMillis()) {
            throw new RuntimeException("提取码已失效");
        }
        competitionWorks.setUpdateTime(DateUtils.getNowDate());
        return competitionWorksMapper.updateCompetitionWorks(competitionWorks);
    }

    @Override
    public CompetitionWorks selectLinkCompetitionWorksByWorksId(Long worksId) {
        return competitionWorksMapper.selectCompetitionWorksByWorksId(worksId);
    }

    /**
     * 批量删除赛事作品
     * 
     * @param worksIds 需要删除的赛事作品主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorksByWorksIds(Long[] worksIds)
    {
        return competitionWorksMapper.deleteCompetitionWorksByWorksIds(worksIds);
    }

    /**
     * 删除赛事作品信息
     * 
     * @param worksId 赛事作品主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorksByWorksId(Long worksId)
    {
        return competitionWorksMapper.deleteCompetitionWorksByWorksId(worksId);
    }
}
