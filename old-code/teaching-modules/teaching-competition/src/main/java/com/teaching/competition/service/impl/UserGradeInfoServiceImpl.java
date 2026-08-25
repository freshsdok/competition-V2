package com.teaching.competition.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.domain.UserGradeInfo;
import com.teaching.competition.mapper.CompetitionAwardsConfigMapper;
import com.teaching.competition.mapper.CompetitionStageConfigMapper;
import com.teaching.competition.mapper.CompetitionWorksMapper;
import com.teaching.competition.mapper.UserGradeInfoMapper;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.competition.service.IUserGradeInfoService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户成绩信息Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class UserGradeInfoServiceImpl implements IUserGradeInfoService
{
    @Autowired
    private UserGradeInfoMapper userGradeInfoMapper;

    @Autowired
    private CompetitionAwardsConfigMapper competitionAwardsConfigMapper;

    @Autowired
    private CompetitionWorksMapper competitionWorksMapper;

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private CompetitionStageConfigMapper competitionStageConfigMapper;

    @Autowired
    private RemoteUserService remoteUserService;

    /**
     * 查询用户成绩信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 用户成绩信息
     */
    @Override
    public UserGradeInfo selectUserGradeInfoByGradeId(Long gradeId)
    {
        return userGradeInfoMapper.selectUserGradeInfoByGradeId(gradeId);
    }

    /**
     * 查询用户成绩信息列表
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 用户成绩信息
     */
    @Override
    public List<UserGradeInfo> selectUserGradeInfoList(UserGradeInfo userGradeInfo) {
        List<UserGradeInfo> gradeInfoList = userGradeInfoMapper.selectUserGradeInfoList(userGradeInfo);
        // 获取每个赛事中所有用户获奖名单
        if(CollectionUtils.isNotEmpty(gradeInfoList)){
            gradeInfoList.stream().forEach(userGradeInfoRes -> {
                UserGradeInfo allUserGradeInfo = new UserGradeInfo();
                allUserGradeInfo.setCompetitionSeriesId(userGradeInfoRes.getCompetitionSeriesId());
                allUserGradeInfo.setIsAward(Constants.IS_YES);
                List<UserGradeInfo> awardInfoList = userGradeInfoMapper.selectUserGradeInfoList(allUserGradeInfo);
                userGradeInfoRes.setUserGradeInfoList(awardInfoList);
            });
        }
        return gradeInfoList;
    }

    /**
     * 新增用户成绩信息
     * 
     * @param userGradeInfoList 用户成绩信息
     * @return 结果
     */
    @Override
    public int insertUserGradeInfo(List<UserGradeInfo> userGradeInfoList) {
        checkAward(userGradeInfoList);
        return userGradeInfoMapper.insertUserGradeInfo(userGradeInfoList);
    }

    @Override
    public int updateUserGradeCompetitionStageConfig(CompetitionStageConfig competitionStageConfig) {
        return competitionStageConfigMapper.updateCompetitionStageConfig(competitionStageConfig);
    }

    @Override
    public int updateCompetitionAwardsConfig(CompetitionAwardsConfig competitionAwardsConfig) {
        return competitionAwardsConfigMapper.updateCompetitionAwardsConfig(competitionAwardsConfig);
    }

    /**
     * 修改用户成绩信息
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    @Override
    public int updateUserGradeInfo(List<UserGradeInfo> userGradeInfoList) {
        // 更新奖项校验
        checkAward(userGradeInfoList);
        return userGradeInfoMapper.batchUpdateUserGradeInfo(userGradeInfoList);
    }

    private void checkAward(List<UserGradeInfo> userGradeInfoList) {
        CompetitionAwardsConfig competitionAwardsConfig = new CompetitionAwardsConfig();
        Map<String, SysDictData> checkStatusMap = new HashMap<>();
        // 获取赛事中奖项设置
        competitionAwardsConfig.setCompetitionSeriesId(userGradeInfoList.get(0).getCompetitionSeriesId());
        competitionAwardsConfig.setStageId(userGradeInfoList.get(0).getStageId());
        List<CompetitionAwardsConfig> competitionAwardsConfigs = competitionAwardsConfigMapper.selectCompetitionAwardsConfigList(competitionAwardsConfig);
        if(CollectionUtils.isEmpty(competitionAwardsConfigs)){
            throw new GlobalException("请先设置赛事奖项设置");
        }
        Map<String,CompetitionAwardsConfig> awardsConfigMap = competitionAwardsConfigs.stream().
                collect(Collectors.toMap(CompetitionAwardsConfig::getAwardsName, Function.identity()));
        // 统计已设定奖项数量
        UserGradeInfo userGradeInfoRes = new UserGradeInfo();
        userGradeInfoRes.setCompetitionSeriesId(userGradeInfoList.get(0).getCompetitionSeriesId());
        userGradeInfoRes.setStageId(userGradeInfoList.get(0).getStageId());
        List<Map<String, Object>> awardsNumList = userGradeInfoMapper.selectUserGradeInfoNum(userGradeInfoRes);
        // 获取奖项设定字典值
        R<List<SysDictData>> checkStatusRes = remoteUserService.dictType("awards_type", SecurityConstants.INNER);
        if(R.isSuccess(checkStatusRes) && CollectionUtils.isNotEmpty(checkStatusRes.getData())){
            List<SysDictData> checkStatusList = checkStatusRes.getData();
            checkStatusMap = checkStatusList.stream().
                    collect(Collectors.toMap(SysDictData::getDictValue, Function.identity()));
        }
        if(CollectionUtils.isNotEmpty(awardsNumList)){
            for (UserGradeInfo userGradeInfo : userGradeInfoList){
                if(Constants.IS_YES.equals(userGradeInfo.getIsAward())){
                    if(checkStatusMap.containsKey(userGradeInfo.getAwardsName())){
                        SysDictData sysDictData = checkStatusMap.get(userGradeInfo.getAwardsName());
                        // 获取已设置奖项数量
                        awardsNumList.stream().forEach(map -> {
                            if(map.get("awardsName").equals(userGradeInfo.getAwardsName())){
                                int count = Integer.valueOf(map.get("num").toString());
                                CompetitionAwardsConfig awardsConfig = awardsConfigMap.get(userGradeInfo.getAwardsName());
                                int awardNum = Integer.valueOf(awardsConfig.getAwardNum());
                                if(count >= awardNum){
                                    throw new GlobalException(sysDictData.getDictLabel()+"奖项分配已满");
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * 批量删除用户成绩信息
     * 
     * @param gradeIds 需要删除的用户成绩信息主键
     * @return 结果
     */
    @Override
    public int deleteUserGradeInfoByGradeIds(Long[] gradeIds)
    {
        return userGradeInfoMapper.deleteUserGradeInfoByGradeIds(gradeIds);
    }

    /**
     * 删除用户成绩信息信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 结果
     */
    @Override
    public int deleteUserGradeInfoByGradeId(Long gradeId)
    {
        return userGradeInfoMapper.deleteUserGradeInfoByGradeId(gradeId);
    }

    // 晋级
    @Override
    public List<CompetitionWorks> createAdvanceUserGradeInfo(UserGradeInfo userGradeInfo) {
        // 生成成绩
        // 晋级人信息
        List<CompetitionWorks> advanceGradeList = new ArrayList<>();
        // 获取赛事获奖规则
        CompetitionMainInfoReq req = new CompetitionMainInfoReq();
        req.setCompetitionSeriesId(userGradeInfo.getCompetitionSeriesId());
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
        CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
        // 赛事进行中
        if(competitionDetailInfo.getCheckStatus().equals(Constants.COMPETITION_RUNNING)){
            // 判断是否晋级，根据当前赛事处于那个阶段，根据分数判定是否晋级
            CompetitionStageConfig competitionStageConfig = competitionStageConfigMapper.selectCompetitionStageConfigByStageId(userGradeInfo.getStageId());
            if(competitionStageConfig.getStageStartTime().getTime() > System.currentTimeMillis()){
                throw new GlobalException("当前赛事:"+competitionStageConfig.getStageName()+"未开始");
            }
            // 获取赛事为评分但未生成成绩的作品
            CompetitionWorks competitionWorks = new CompetitionWorks();
            competitionWorks.setCompetitionSeriesId(userGradeInfo.getCompetitionSeriesId());
            List<CompetitionWorks> worksList = competitionWorksMapper.selectCompetitionWorksScore(competitionWorks);
            if (CollectionUtils.isNotEmpty(worksList)) {
                // 晋级分数,允许有一位小数
                double promoteScore = Double.valueOf(competitionStageConfig.getPromoteScore());
                int promoteNum = Integer.valueOf(competitionStageConfig.getPromoteNum());
                // 已排名过的分数，作为记录，以分辨出分数相同情况排名一致
                Map<Double, Integer> scoreMap = new HashMap<>();
                AtomicInteger workRank = new AtomicInteger();
                for (int i = 0; i < worksList.size(); i++) {
                    CompetitionWorks works = worksList.get(i);
                    double worksScore = Double.valueOf(works.getWorksScore());
                    // 晋级方式按照分数或者按照人数进行
                    if (userGradeInfo.getAdvanceType().equals(Constants.ADVANCE_TYPE_SCORE)) {
                        // 晋级
                        CompetitionWorks promotionWorks = new CompetitionWorks();
                        promotionWorks.setWorksId(works.getWorksId());
                        promotionWorks.setUserId(works.getUserId());
                        if(worksScore >= promoteScore){
                            if(scoreMap.get(worksScore)!=null){
                                promotionWorks.setWorksRank(workRank.get()+"");
                                promotionWorks.setWorksScore(works.getWorksScore());
                                promotionWorks.setIsAdvance(Constants.IS_YES);
                                advanceGradeList.add(promotionWorks);
                            } else {
                                promotionWorks.setWorksRank(workRank.incrementAndGet()+"");
                                promotionWorks.setWorksScore(works.getWorksScore());
                                promotionWorks.setIsAdvance(Constants.IS_YES);
                                advanceGradeList.add(promotionWorks);
                                scoreMap.put(worksScore, workRank.get());
                            }
                        } else {
                            if(scoreMap.get(worksScore)!=null){
                                promotionWorks.setWorksRank(workRank.get()+"");
                                promotionWorks.setIsAdvance(Constants.IS_NO);
                                promotionWorks.setWorksScore(works.getWorksScore());
                                advanceGradeList.add(promotionWorks);
                            } else {
                                promotionWorks.setWorksRank(workRank.incrementAndGet()+"");
                                promotionWorks.setIsAdvance(Constants.IS_NO);
                                promotionWorks.setWorksScore(works.getWorksScore());
                                advanceGradeList.add(promotionWorks);
                                scoreMap.put(worksScore, workRank.get());
                            }
                        }
                    }
                    if(userGradeInfo.getAdvanceType().equals(Constants.ADVANCE_TYPE_PERSON)){
                        // 晋级人数超过设定人数，则分数最高的晋级
                        // 晋级
                        CompetitionWorks promotionWorks = new CompetitionWorks();
                        promotionWorks.setWorksId(works.getWorksId());
                        promotionWorks.setUserId(works.getUserId());
                        if(advanceGradeList.size() <= promoteNum){
                            if(scoreMap.get(worksScore)!=null){
                                promotionWorks.setWorksRank(workRank.get()+"");
                                promotionWorks.setIsAdvance(Constants.IS_YES);
                                advanceGradeList.add(promotionWorks);
                            } else {
                                promotionWorks.setWorksRank(workRank.incrementAndGet()+"");
                                promotionWorks.setIsAdvance(Constants.IS_YES);
                                advanceGradeList.add(promotionWorks);
                                scoreMap.put(worksScore,workRank.get());
                            }
                        }
                        if(advanceGradeList.size() > promoteNum){
                            if(scoreMap.get(worksScore)!=null){
                                promotionWorks.setWorksRank(workRank.get()+"");
                                promotionWorks.setIsAdvance(Constants.IS_YES);
                                advanceGradeList.add(promotionWorks);
                            } else {
                                promotionWorks.setWorksRank(workRank.incrementAndGet()+"");
                                promotionWorks.setIsAdvance(Constants.IS_NO);
                                advanceGradeList.add(promotionWorks);
                                scoreMap.put(worksScore,workRank.get());
                            }
                        }
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(advanceGradeList)){
                // 获取用户姓名
                advanceGradeList.stream().forEach(works -> {
                    // 通过用户中心获取用户真实姓名
                    R<SysUser> userCenterInfo = remoteUserService.getUserCenterInfo(works.getUserId(), SecurityConstants.INNER);
                    if(R.isSuccess(userCenterInfo) && userCenterInfo.getData().getAuthInfo()!=null){
                        works.setUserName(userCenterInfo.getData().getAuthInfo().getRealName());
                    }else {
                        works.setUserName(userCenterInfo.getData().getUserName());
                    }
                });
            }
        }
        return advanceGradeList;
    }

    @Override
    public List<UserGradeInfo> createUserGradeInfo(UserGradeInfo userGradeInfo) {
        List<UserGradeInfo> userGradeInfoList = new ArrayList<>();
        // 获取赛事获奖规则
        CompetitionMainInfoReq req = new CompetitionMainInfoReq();
        req.setCompetitionSeriesId(userGradeInfo.getCompetitionSeriesId());
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
        CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
        if(competitionDetailInfo.getCheckStatus().equals(Constants.COMPETITION_END)){
            // 获取参加赛事所有阶段作品分数
            List<Map<String, Object>> scoreMapList =
                    competitionWorksMapper.selectCompetitionWorksAdvanceScore(userGradeInfo.getCompetitionSeriesId());
            if(CollectionUtils.isNotEmpty(scoreMapList)){
                // 已排名过的分数，作为记录，以分辨出成绩相同情况
                Map<Double, Integer> gradeMap = new HashMap<>();
                AtomicInteger gradeRank = new AtomicInteger();
                scoreMapList.stream().forEach(scoreMap -> {
                    Double score = Double.valueOf(scoreMap.get("worksScore").toString());
                    // 通过用户中心获取用户真实姓名
                    R<SysUser> userCenterInfo = remoteUserService.getUserCenterInfo(Long.valueOf(scoreMap.get("userId").toString()), SecurityConstants.INNER);
                    if(R.isSuccess(userCenterInfo) && null != userCenterInfo.getData() && userCenterInfo.getData().getAuthInfo()!=null){
                        scoreMap.put("userName",userCenterInfo.getData().getAuthInfo().getRealName());
                    }else {
                        scoreMap.put("userName",userCenterInfo.getData().getUserName());
                    }
                    // 根据各个阶段总和分数获取排名
                    UserGradeInfo userGradeInfoRes = new UserGradeInfo();
                    userGradeInfoRes.setCompetitionSeriesId(userGradeInfo.getCompetitionSeriesId());
                    if(gradeMap.containsKey(score)){
                        userGradeInfoRes.setUserName(scoreMap.get("userName").toString());
                        userGradeInfoRes.setUserId(Long.valueOf(scoreMap.get("userId").toString()));
                        userGradeInfoRes.setOrgId(Long.valueOf(scoreMap.get("orgId").toString()));
                        userGradeInfoRes.setScore(scoreMap.get("worksScore").toString());
                        userGradeInfoRes.setRanks(gradeMap.get(score)+"");
                    } else {
                        userGradeInfoRes.setUserName(scoreMap.get("userName").toString());
                        userGradeInfoRes.setUserId(Long.valueOf(scoreMap.get("userId").toString()));
                        userGradeInfoRes.setOrgId(Long.valueOf(scoreMap.get("orgId").toString()));
                        userGradeInfoRes.setScore(scoreMap.get("worksScore").toString());
                        userGradeInfoRes.setRanks(gradeRank.incrementAndGet()+"");
                        gradeMap.put(score, gradeRank.get());
                    }
                    userGradeInfoList.add(userGradeInfoRes);
                });
            }
            // 设置奖项
            // 赛事奖项设置
//            List<CompetitionAwardsConfig> competitionAwardsList = competitionDetailInfo.getCompetitionAwardsList();
            List<CompetitionAwardsConfig> competitionAwardsList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(userGradeInfoList)){
                // 奖项设置排序，从高到低
                // 记录奖项人数
                Map<String, Integer> awardNumMap = new HashMap<>();
                for (CompetitionAwardsConfig awardsConfig : competitionAwardsList){
                    // 获奖奖项人数
                    userGradeInfoList.stream().forEach(userGrade -> {
                        if(userGrade.getRanks().equals(awardsConfig.getAwardsName())){
                            userGrade.setAwardsName(awardsConfig.getAwardsName());
                            userGrade.setIsAward(Constants.IS_YES);
                            if(awardNumMap.containsKey(awardsConfig.getAwardsName())){
                                awardNumMap.put(awardsConfig.getAwardsName(), awardNumMap.get(awardsConfig.getAwardsName())+1);
                            }else {
                                awardNumMap.put(awardsConfig.getAwardsName(), 1);
                            }
                        }
                    });
                }
                for (CompetitionAwardsConfig awardsConfig : competitionAwardsList){
                    userGradeInfoList.stream().forEach(userGrade -> {
                        if(Constants.IS_YES.equals(userGrade.getIsAward()) && userGrade.getAwardsName().equals(awardsConfig.getAwardsName())){
                            //获取获奖奖项人数
                            Integer awardNum = awardNumMap.get(userGrade.getAwardsName());
                            String bonusNum = awardsConfig.getBonusNum();
                            BigDecimal bonusMoney = new BigDecimal(bonusNum);
                            userGrade.setAwardsMoney(new BigDecimal(bonusMoney.doubleValue()/ awardNum));
                        }
                    });
                }
            }
        }
        return userGradeInfoList;
    }

    @Override
    public int saveAdvanceUserGradeInfo(List<CompetitionWorks> competitionWorksList) {

        return competitionWorksMapper.batchUpdateCompetitionWorks(competitionWorksList);
    }
}
