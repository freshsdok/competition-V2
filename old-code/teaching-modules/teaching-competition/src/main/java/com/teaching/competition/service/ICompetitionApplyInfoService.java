package com.teaching.competition.service;

import com.teaching.competition.domain.CertCompetitionApplyInfo;
import com.teaching.competition.domain.TeamManagerInfoRes;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.competition.domain.UserCompetitionApplyInfo;
import com.teaching.system.api.domain.CompetitionApplyInfoVO;
import com.teaching.system.api.domain.RegistrationInfo;
import com.teaching.system.api.domain.TeamManagerInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 赛事申请报名信息Service接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface ICompetitionApplyInfoService {
    /**
     * 查询赛事申请报名信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 赛事申请报名信息
     */
    public CompetitionApplyInfo selectCompetitionApplyInfoByMemberId(Long memberId);

    /**
     * 查询赛事申请报名信息列表
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 赛事申请报名信息集合
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo);

    public List<CertCompetitionApplyInfo> selectCertCompetitionApplyInfoList(CertCompetitionApplyInfo certCompetitionApplyInfo);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoByPayStatus();

    public List<CompetitionApplyInfo> queryTeamMemberInvoiceStatus(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 导出赛事申请报名信息列表
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 赛事申请报名信息集合
     */
    public List<CompetitionApplyInfo> exportCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo);

    public List<CompetitionApplyInfo> exportCompetitionApplyInfoAwardsList(CompetitionApplyInfo competitionApplyInfo);

    public List<TeamManagerInfoRes> exportTeamManagerInfoList(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 新增赛事申请报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    public int insertCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo);

    public int batchInsertCompetitionApplyInfo(List<CompetitionApplyInfo> competitionApplyInfoList);

    // 晋级赛报名
    public int batchInsertAwardsCompetitionApplyInfo(List<CompetitionApplyInfo> competitionApplyInfoList,String nickName,Long competitionSeriesId);

    /**
     * 修改赛事申请报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    public int updateCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo);

    public int updatePayStatus(List<CompetitionApplyInfo> competitionApplyInfoList);

    public int updateCompetitionApplyInfoStatus(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 批量删除赛事申请报名信息
     *
     * @param memberIds 需要删除的赛事申请报名信息主键集合
     * @return 结果
     */
    public int deleteCompetitionApplyInfoByMemberIds(Long[] memberIds);

    public int deleteCompetitionApplyInfoByTeamCode(String teamCode);

    /**
     * 删除赛事申请报名信息信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 结果
     */
    public int deleteCompetitionApplyInfoByMemberId(Long memberId);

    /**
     * 查询用户赛事信息
     *
     * @param
     * @return 用户赛事信息
     */
    public List<UserCompetitionApplyInfo> selectCompetitionApplyInfoByUserId(Long userId);

    /*
     * 获取团队赛事信息
     */
    public List<CompetitionApplyInfoVO> getTeamCompetitionInfo(RegistrationInfo registrationInfo);

    /**
     * 删除团队信息
     *
     * @param teamCode
     * @return
     */
    public int delApplyInfoByTeamCode(String teamCode);

    /**
     * 删除团队s信息
     *
     * @param teamCode
     * @return
     */
    public int delApplyInfoByTeamCodes(String[] teamCode);

    /**
     * 去结算
     *
     * @param teamCodeList
     * @return
     */
    public String settlement(List<String> teamCodeList, Long competitionSeriesId);

    /**
     * 确认订单
     *
     * @return
     */
    public Map<String, Object> confirmOrder(String token, Long competitionSeriesId);

    /**
     * 获取团队信息 给生成订单使用
     *
     * @return
     */
    public Map<String, Object> getTeamInfo(Long competitionSeriesId);

    /**
     * 更新团队支付状态
     *
     * @param map
     * @return
     */
    public int updateTeamPayStatusByTeamCodes(Map<String, Object> map);

    /**
     * 根据团队code获取团队信息
     *
     * @param teamCodes
     * @return
     */
    public List<Map<String, Object>> selectCompetitionApplyInfoListByTeamCode(Map<String, Object> params);

    public List<CompetitionApplyInfo> selectCompetitionApplyTeamCode(String teamCode);

    public List<CompetitionApplyInfo> selectTeamInfoByTeamCode(Map<String, Object> params);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByUserId(Map<String, Object> params);

    public List<CompetitionApplyInfo> getInnerApplyUserInfo(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 获取订单详情
     *
     * @param map
     * @return
     */
    public List<CompetitionApplyInfoVO> getDetailForOrder(Map<String, Object> map);

    //学院校验
    public String collegeVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId,String type);
    public String collegeVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId,String type);

    //跨校组队校验
    public String crossSchoolTeamVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);
    public String crossSchoolTeamVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);

    //重复组队校验
    public String repeatTeamVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);
    public String repeatTeamVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);

    //专业校验
    public String professionalVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);
    public String professionalVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId);

    /**
     * 查询某userId已缴费的报名信息
     * @param userId
     * @return
     */
    public List<CompetitionApplyInfo> getCompetitionApplyInfoByPayStatusForUserGroup(Long userId);

    /**
     * 查询匹配规则的报名成功已缴费的人员信息
     * @param param
     * @return
     */
    public Set<Long> getUserInfoByCompetitions(Map<String,Object> param);

    public List<CompetitionApplyInfo> selectAllUserInfoByCompetitions(Map<String,Object> param);

    /**
     * 根据userId和competitionId查询团队的报名信息
     * @param userId
     * @param competitionId
     * @return
     */
    public List<CompetitionApplyInfo> getApplyInfoByUsrIdAndCompetitionId(Long userId,Long competitionId);

    /**
     * 根据赛事系列id查询报名信息
     * @param seriesId
     * @return
     */
    public List<CompetitionApplyInfo> getCompetitionApplyInfoListByCompetitionSeriesId(Long seriesId);
}
