package com.teaching.competition.mapper;

import com.teaching.competition.domain.CandidateCertInfo;
import com.teaching.competition.domain.CertCompetitionApplyInfo;
import com.teaching.competition.domain.CertificateImportCompetitionInfo;
import com.teaching.competition.domain.TeamManagerInfoRes;
import com.teaching.competition.domain.UserCompetitionApplyInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.CompetitionApplyInfoVO;
import com.teaching.system.api.domain.RegistrationInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 赛事申请报名信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface CompetitionApplyInfoMapper {
    /**
     * 查询赛事申请报名信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 赛事申请报名信息
     */
    public CompetitionApplyInfo selectCompetitionApplyInfoByMemberId(Long memberId);

    /**
     * 根据用户id查询赛事申请信息
     *
     * @param competitionApplyInfo
     * @return
     */
    public CompetitionApplyInfo selectCompetitionApplyInfoByUserId(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 查询教师赛选手在指定赛道下未取消的报名记录。
     *
     * @param idCard 选手身份证号
     * @param competitionSeriesId 赛事ID
     * @param competitionTrackId 赛道ID
     * @param excludedPayStatus 不参与重复报名判断的支付状态
     * @return 未取消的报名记录
     */
    public CompetitionApplyInfo selectTeacherContestantActiveRegistration(
            @Param("idCard") String idCard,
            @Param("competitionSeriesId") Long competitionSeriesId,
            @Param("competitionTrackId") String competitionTrackId,
            @Param("excludedPayStatus") String excludedPayStatus);

    public List<CompetitionApplyInfo> selectUnboundTeacherContestantsByPhone(@Param("phone") String phone);

    public int bindTeacherContestantUser(@Param("memberId") Long memberId, @Param("userId") Long userId);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoByNullUserId(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 查询赛事申请报名信息列表
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 赛事申请报名信息集合
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByImportIdCard(CompetitionApplyInfo competitionApplyInfo);

    public List<CertCompetitionApplyInfo> selectCertCompetitionApplyInfoList(CertCompetitionApplyInfo certCompetitionApplyInfo);
    public List<CertCompetitionApplyInfo> selectCertCompetitionApplyInfoList2(CertCompetitionApplyInfo certCompetitionApplyInfo);

    public List<CompetitionApplyInfo> selectExportCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo);
    public List<CompetitionApplyInfo> selectExportCompetitionApplyInfoAwardsList(CompetitionApplyInfo competitionApplyInfo);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListPC(CompetitionApplyInfo competitionApplyInfo);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByIdCard(Map<String, Object> params);

    /**
     * 根据团队编码查询赛事申请信息
     *
     * @return
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByTeamCode(Map<String, Object> params);

    public List<CompetitionApplyInfo> selectCompetitionApplyTeamCode(String teamCode);

    public List<CompetitionApplyInfo> selectDefenseScheduleApplyCandidates(
            @Param("competitionSeriesId") Long competitionSeriesId,
            @Param("teamName") String teamName,
            @Param("leaderName") String leaderName,
            @Param("orgName") String orgName);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByUserId(Map<String, Object> params);

    public List<CompetitionApplyInfo>  selectCompetitionApplyInfoListByPCUserId(Map<String, Object> params);

    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByUserTeamCode(Map<String, Object> params);

    public List<CompetitionApplyInfo> selectCertCompetitionApplyInfoListByUserTeamCode(Map<String, Object> params);

    /**
     * 查询团队成员报名信息（不包含指导教师）
     * @param params
     * @return
     */
    public List<CompetitionApplyInfo> selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher(Map<String, Object> params);

    // 获取未支付成功的报名信息
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoByPayStatus();

    public List<CompetitionApplyInfo> queryTeamMemberInvoiceStatus(CompetitionApplyInfo competitionApplyInfo);

    public int selectCompetitionApplyInfoByExitPayStatus(@Param("competitionSeriesId") Long competitionSeriesId, @Param("leaderTeacherId") Long leaderTeacherId);

    public Set<String> selectApplyInfoCompetitionId(@Param("competitionSeriesId") Long competitionSeriesId);
    /**
     * 新增赛事申请报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    public int insertCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 批量新增赛事申请报名信息
     *
     * @param competitionApplyInfoList 列表
     * @return 结果
     */
    public int batchInsertCompetitionApplyInfo(@Param("list") List<CompetitionApplyInfo> competitionApplyInfoList);

    /**
     * 修改赛事申请报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    public int updateCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 批量修改赛事申请报名信息
     *
     * @param competitionApplyInfoList 赛事申请报名信息列表
     * @return 结果
     */
    public int batchUpdateCompetitionApplyInfo(@Param("list") List<CompetitionApplyInfo> competitionApplyInfoList);

    /**
     * 批量修改赛事申请报名信息团队排序
     * @param competitionApplyInfoList
     * @return
     */
    public int batchUpdateCompetitionApplyInfoTeamSort(@Param("list") List<CompetitionApplyInfo> competitionApplyInfoList);

    public int updatePayStatus(@Param("list") List<CompetitionApplyInfo> list);

    public int updateUserId(@Param("list") List<CompetitionApplyInfo> competitionApplyInfoList);

    public int updateSecondLevel(@Param("list") List<CompetitionApplyInfo> competitionApplyInfoList);

    /**
     * 删除赛事申请报名信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 结果
     */
    public int deleteCompetitionApplyInfoByMemberId(Long memberId);

    public int deleteCompetitionApplyInfoByTeamCode(String teamCode);

    // 退赛删除标识为2
    public int deleteRetaCompetitionApplyInfoByTeamCode(String teamCode);

    /**
     * 批量删除赛事申请报名信息
     *
     * @param competitionSeriesId 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionApplyInfoByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事申请报名信息
     *
     * @param memberIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionApplyInfoByMemberIds(Long[] memberIds);

    /**
     * 查询赛事报名人数
     *
     * @return
     */
    public int selectCompetitionApplyNum(Long competitionSeriesId);

    // 获取最大memberId
    public int selectMaxMemberId();

    /**
     * 获取用户参加赛事信息
     *
     * @param userId
     * @return 结果
     */
    public List<UserCompetitionApplyInfo> selectCompetitionApplyByUserId(Long userId);

    /**
     * 获取团队报名信息
     *
     * @return 列表
     */
    public List<CompetitionApplyInfo> selectTeamApplyInfoList(@Param("teamCodeList") List<String> teamCodeList);

    /**
     * 获取团队赛事信息
     *
     * @param registrationInfo
     * @return
     */
    public List<CompetitionApplyInfoVO> selectTeamCompetitionInfo(RegistrationInfo registrationInfo);

    /**
     * 根据团队编码获取团队信息
     *
     * @param teamCodeList
     * @param userId
     * @return
     */
    public List<CompetitionApplyInfoVO> selectTeamCompetitionInfoByTeamCodes(
            @Param("competitionSeriesId") Long competitionSeriesId
            , @Param("teamCodeList") List<String> teamCodeList
            , @Param("leaderTeacherId") Long userId);

    /**
     * 根据团队编码获取信息（订单页面）
     *
     * @param competitionSeriesId
     * @param teamCodeList
     * @param userId
     * @return
     */
    public List<CompetitionApplyInfoVO> selectTeamCompetitionInfoByTeamCodesForOrder(
            @Param("competitionSeriesId") Long competitionSeriesId
            , @Param("teamCodeList") List<String> teamCodeList
            , @Param("leaderTeacherId") Long userId);

    /**
     * 逻辑删除
     *
     * @param teamCode
     * @param userId
     * @return
     */
    public int delApplyInfoByTeamCode(@Param("teamCode") String teamCode, @Param("updateBy") Long userId);

    /**
     * 批量逻辑删除
     *
     * @param teamCodes
     * @param userId
     * @return
     */
    public int delApplyInfoByTeamCodes(@Param("teamCodes") String[] teamCodes, @Param("updateBy") Long userId);

    /**
     * 更新支付状态
     *
     * @param param payStatus支付状态，leaderTeacherId带队老师id（登录人），competitionSeriesId赛事id，teamCodeList团队编码列表
     * @return
     */
    public int updateTeamPayStatusByTeamCodes(Map<String, Object> param);

    /**
     * 查询赛事申请报名信息
     *
     * @param competitionTrackId  赛道
     * @param competitionRoleName 角色=student时查队员 null时查全部
     * @return
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfos(@Param("competitionTrackId") String competitionTrackId, @Param("competitionRoleName") String competitionRoleName);

    public List<TeamManagerInfoRes> selectTeamManagerInfoListForExport(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 查询某userId已缴费的报名信息
     *
     * @param userId
     * @return
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoByPayStatusForUserGroup(Long userId);

    /**
     * 查询匹配规则的报名成功已缴费的学生信息
     * competitionSeriesIds 规则：赛事id
     * competitionTrackIds 规则：赛道id
     * secondLevelCode 规则：二级赛区编码
     *
     * @return
     */
    public Set<Long> selectStudentInfoByCompetitions(Map<String, Object> param);

    /**
     * 查询匹配规则的报名成功已缴费的指导教师信息
     *
     * @param param
     * @return
     */
    public Set<Long> selectTeacherInfoByCompetitions(Map<String, Object> param);

    /**
     * 查询匹配规则的报名成功已缴费的人员信息(包括学生和指导老师)
     *
     * @param param
     * @return
     */
    public Set<Long> selectInfoByCompetitions(Map<String, Object> param);

    /**
     * 获取用户组赛事配置下所有参赛者信息
     *
     * @param param
     * @return
     */
    public List<CompetitionApplyInfo> selectAllUserInfoByCompetitions(Map<String, Object> param);

    /**
     * 根据用户id和赛事id查询团队信息
     * @param userId
     * @param competitionSeriesId
     * @return
     */
    public List<CompetitionApplyInfo> selectApplyInfoByUsrIdAndCompetitionId(@Param("userId") Long userId, @Param("competitionSeriesId") Long competitionSeriesId);

    /**
     * 根据赛事系列id查询报名信息
     * @param seriesId
     * @return
     */
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByCompetitionSeriesId(@Param("seriesId") Long seriesId);

    /**
     * 查询证书导入所需的全部已缴费报名数据，包括指导教师。
     */
    public List<CompetitionApplyInfo> selectCertificateImportApplicants(@Param("seriesId") Long seriesId);

    /**
     * 查询赛事系列与所属大赛，服务端据此校验导入目标。
     */
    public CertificateImportCompetitionInfo selectCertificateImportCompetitionInfo(@Param("seriesId") Long seriesId);

    /**
     * 根据身份证号查询唯一且审核通过的实名认证用户。
     *
     * @param idCards 身份证号列表
     * @return 身份证号和用户ID映射
     */
    public List<CompetitionApplyInfo> selectUniqueVerifiedUsersByIdCards(@Param("idCards") List<String> idCards);

    /**
     * 根据手机号查询唯一且状态正常的用户账号。
     *
     * @param phones 手机号列表
     * @return 手机号和用户ID映射
     */
    public List<CompetitionApplyInfo> selectUniqueActiveUsersByPhones(@Param("phones") List<String> phones);

    /**
     * 查询指定用户ID中的有效账号，用于核验指导教师的带队账号。
     */
    public List<CompetitionApplyInfo> selectActiveUsersByIds(@Param("userIds") List<Long> userIds);

    /**
     * 按真实姓名查询审核通过的实名认证账号，业务层继续按学校做唯一性校验。
     */
    public List<CompetitionApplyInfo> selectVerifiedUsersByRealNames(@Param("realNames") List<String> realNames);

    /**
     * 根据teamCode批量获取团队名称及学校名称
     * @param teamCodes
     * @return
     */
    public List<Map<String,String>> selectTeamNameAndSchoolNameByTeamCodes(@Param("teamCodes") Set<String> teamCodes);

    /**
     * 获取获奖公示团队信息
     * @return
     */
    public List<CompetitionApplyInfo> selectCandidateCertInfoListFromAwards(Map<String, Object> param);
}
