package com.teaching.competition.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanValidators;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.CompetitionGradeInfo;
import com.teaching.competition.domain.CompetitionGradeInfoImport;
import com.teaching.competition.domain.CompetitionGradeInfoImportReq;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionGradeInfoMapper;
import com.teaching.competition.service.ICompetitionGradeInfoService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.SysUser;
import jakarta.validation.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 成绩Service业务层处理
 *
 * @author teaching
 */
@Service
public class CompetitionGradeInfoServiceImpl implements ICompetitionGradeInfoService {
    private static final Logger log = LoggerFactory.getLogger(CompetitionGradeInfoServiceImpl.class);
    @Autowired
    private CompetitionGradeInfoMapper competitionGradeInfoMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RemoteUserService userService;

    /**
     * 查询成绩
     *
     * @param gradeId 成绩主键
     * @return 成绩
     */
    @Override
    public CompetitionGradeInfo selectCompetitionGradeInfoById(Long gradeId) {
        CompetitionGradeInfo competitionGradeInfo = competitionGradeInfoMapper.selectCompetitionGradeInfoById(gradeId);
        if(Objects.nonNull(competitionGradeInfo)){
            if(Objects.nonNull(competitionGradeInfo.getLeaderTeacherId())){
                R<SysUser> sysUserR = userService.getUserCenterInfo(competitionGradeInfo.getLeaderTeacherId(), SecurityConstants.INNER);
                if(R.isSuccess(sysUserR) && Objects.nonNull(sysUserR.getData())){
                    AuthInfo authInfo = sysUserR.getData().getAuthInfo();
                    if(authInfo != null){
                        competitionGradeInfo.setLeaderTeacherName(authInfo.getRealName());
                    }
                }
            }
        }
        return competitionGradeInfo;
    }

    /**
     * 查询成绩列表
     *
     * @param competitionGradeInfo 成绩
     * @return 成绩集合
     */
    @Override
    public List<CompetitionGradeInfo> selectCompetitionGradeInfoList(CompetitionGradeInfo competitionGradeInfo) {
        List<CompetitionGradeInfo> competitionGradeInfos = competitionGradeInfoMapper.selectCompetitionGradeInfoList(competitionGradeInfo);
        if(CollectionUtils.isNotEmpty(competitionGradeInfos)){
            competitionGradeInfos.stream().forEach(competitionGradeInfoRes -> {
                if(Objects.nonNull(competitionGradeInfoRes.getLeaderTeacherId())){
                    R<SysUser> sysUserR = userService.getUserCenterInfo(competitionGradeInfoRes.getLeaderTeacherId(), SecurityConstants.INNER);
                    if(R.isSuccess(sysUserR) && Objects.nonNull(sysUserR.getData())){
                        AuthInfo authInfo = sysUserR.getData().getAuthInfo();
                        if(authInfo != null){
                            competitionGradeInfoRes.setLeaderTeacherName(authInfo.getRealName());
                        }
                    }
                }
            });
        }
        return competitionGradeInfos;
    }

    /**
     * 新增成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    @Override
    public int insertCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo) {
        return competitionGradeInfoMapper.insertCompetitionGradeInfo(competitionGradeInfo);
    }

    /**
     * 修改成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    @Override
    public int updateCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo) {
        String score = competitionGradeInfo.getScore();
        //判断score是否是数字
        if(StringUtils.isNotBlank(score) && !NumberUtil.isNumber(score)){
            throw new GlobalException("请输入正确的成绩！");
        }
        return competitionGradeInfoMapper.updateCompetitionGradeInfo(competitionGradeInfo);
    }

    /**
     * 删除成绩
     *
     * @param gradeId 成绩主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionGradeInfoById(Long gradeId) {
        return competitionGradeInfoMapper.deleteCompetitionGradeInfoById(gradeId);
    }

    /**
     * 批量删除成绩
     *
     * @param gradeIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCompetitionGradeInfoByIds(Long[] gradeIds) {
        return competitionGradeInfoMapper.deleteCompetitionGradeInfoByIds(gradeIds);
    }

    /**
     * 导入成绩数据
     *
     * @param gradeList 成绩数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public List<CompetitionGradeInfo> importGradeInfo(List<CompetitionGradeInfoImport> gradeList, Boolean isUpdateSupport,
                                  String operName, CompetitionGradeInfoImportReq req) {
        if (CollectionUtils.isEmpty(gradeList)) {
            throw new GlobalException("导入成绩数据不能为空！");
        }
//        int successNum = 0;
//        int failureNum = 0;
//        StringBuilder successMsg = new StringBuilder();
//        StringBuilder failureMsg = new StringBuilder();
        List<CompetitionGradeInfo> updateGradeList = new ArrayList<>();
        for (CompetitionGradeInfoImport gradeImport : gradeList) {
            try {
                // 验证姓名和身份证号不能为空
                if (StringUtils.isEmpty(gradeImport.getUserName())) {
//                    failureNum++;
//                    failureMsg.append(failureNum + "、姓名不能为空");
                    continue;
                }
                // 去除姓名中的换行符
                gradeImport.setUserName(gradeImport.getUserName().replaceAll("\\s+", " "));

                if (StringUtils.isEmpty(gradeImport.getIdCard())) {
//                    failureNum++;
//                    failureMsg.append(failureNum + "、身份证号不能为空");
                    continue;
                }
                // 根据身份证号查询是否已存在成绩记录
                CompetitionGradeInfo competitionGradeInfo = new CompetitionGradeInfo();
                BeanUtils.copyProperties(req, competitionGradeInfo);
                competitionGradeInfo.setIdCard(gradeImport.getIdCard());
                List<CompetitionGradeInfo> existGrade = competitionGradeInfoMapper.selectCompetitionGradeInfoByIdCard(competitionGradeInfo);
                List<CompetitionGradeInfo> addGradeList = new ArrayList<>();
                if(CollectionUtils.isEmpty(existGrade)){
                    // 根据身份证号去报名表匹配参赛选手信息
                    List<CompetitionApplyInfo> applyInfoList = new ArrayList<>();
                    if(Objects.nonNull(req.getCompetitionSeriesId())){
                        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                        BeanUtils.copyProperties(req, competitionApplyInfo);
                        competitionApplyInfo.setIdCard(gradeImport.getIdCard());
                        applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByImportIdCard(competitionApplyInfo);
                        log.info("applyInfoList:{}", JSONObject.toJSONString(applyInfoList));
                    }
                    if (CollectionUtils.isNotEmpty(applyInfoList)) {
                        applyInfoList.stream().forEach(applyInfo -> {
                            CompetitionGradeInfo gradeInfo = new CompetitionGradeInfo();
                            gradeInfo.setUserName(gradeImport.getUserName());
                            gradeInfo.setIdCard(gradeImport.getIdCard());
                            gradeInfo.setScore(gradeImport.getScore());
                            gradeInfo.setCompetitionSeriesId(applyInfo.getCompetitionSeriesId());
                            gradeInfo.setCompetitionTrackId(applyInfo.getCompetitionTrackId());
                            gradeInfo.setSecondLevelCode(applyInfo.getSecondLevelCode());
                            gradeInfo.setCompetitionStageId(req.getCompetitionStageId());
                            gradeInfo.setTeamCode(applyInfo.getTeamCode());
                            gradeInfo.setMemberId(applyInfo.getMemberId());
                            gradeInfo.setUserId(applyInfo.getUserId());
                            gradeInfo.setGradeSource(ApplyConstants.PLAYER_SOURCE_IMPORT);
                            gradeInfo.setCreateBy(operName);
                            addGradeList.add(gradeInfo);
                        });
                    } else {
                        CompetitionGradeInfo gradeInfo = new CompetitionGradeInfo();
                        gradeInfo.setUserName(gradeImport.getUserName());
                        gradeInfo.setCompetitionSeriesId(req.getCompetitionSeriesId());
                        gradeInfo.setCompetitionTrackId(req.getCompetitionTrackId());
                        gradeInfo.setSecondLevelCode(req.getSecondLevelCode());
                        gradeInfo.setCompetitionStageId(req.getCompetitionStageId());
                        gradeInfo.setIdCard(gradeImport.getIdCard());
                        gradeInfo.setScore(gradeImport.getScore());
                        gradeInfo.setCreateBy(operName);
                        gradeInfo.setGradeSource(ApplyConstants.PLAYER_SOURCE_IMPORT);
                        addGradeList.add(gradeInfo);
                    }
                } else {
                    CompetitionGradeInfo gradeInfo = new CompetitionGradeInfo();
                    gradeInfo.setUserName(gradeImport.getUserName());
                    gradeInfo.setCompetitionSeriesId(req.getCompetitionSeriesId());
                    gradeInfo.setCompetitionTrackId(req.getCompetitionTrackId());
                    gradeInfo.setSecondLevelCode(req.getSecondLevelCode());
                    gradeInfo.setCompetitionStageId(req.getCompetitionStageId());
                    gradeInfo.setIdCard(gradeImport.getIdCard());
                    gradeInfo.setScore(gradeImport.getScore());
                    gradeInfo.setCreateBy(operName);
                    gradeInfo.setGradeSource(ApplyConstants.PLAYER_SOURCE_IMPORT);
                    gradeInfo.setGradeId(existGrade.get(0).getGradeId());
                    updateGradeList.add(gradeInfo);
                }
                if (CollectionUtils.isNotEmpty(addGradeList)) {
                    // 新增成绩
                    competitionGradeInfoMapper.batchInsertCompetitionGradeInfo(addGradeList);
//                    successNum++;
//                    successMsg.append(successNum + "、姓名 " + gradeImport.getUserName() + " 导入成功");
                }
//                else if (CollectionUtils.isNotEmpty(updateGradeList) && isUpdateSupport) {
//                    // 更新成绩
//                    competitionGradeInfoMapper.batchUpdateCompetitionGradeInfo(updateGradeList);
//                    successNum++;
//                    successMsg.append(successNum + "、姓名 " + gradeImport.getUserName() + " 更新成功");
//                } else {
//                    failureNum++;
//                    failureMsg.append(failureNum + "、姓名 " + gradeImport.getUserName() + " 已存在");
//                }
            } catch (Exception e) {
//                failureNum++;
//                String msg = failureNum + "、姓名 " + gradeImport.getUserName() + " 导入失败：";
//                failureMsg.append(msg + e.getMessage());
                log.info(e.getMessage());
            }
        }
//        if (failureNum > 0) {
//            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据错误，错误如下：");
//            throw new GlobalException(failureMsg.toString());
//        } else {
//            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
//        }
        if(CollectionUtils.isNotEmpty(updateGradeList)){
            return updateGradeList;
        }
        return new ArrayList<>();
    }

    @Override
    public int updateGradeInfo(List<CompetitionGradeInfo> gradeList) {
        return competitionGradeInfoMapper.batchUpdateCompetitionGradeInfo(gradeList);
    }
}
