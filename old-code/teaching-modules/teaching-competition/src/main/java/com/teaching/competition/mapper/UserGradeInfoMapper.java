package com.teaching.competition.mapper;

import com.teaching.competition.domain.UserGradeInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户成绩信息Mapper接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface UserGradeInfoMapper 
{
    /**
     * 查询用户成绩信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 用户成绩信息
     */
    public UserGradeInfo selectUserGradeInfoByGradeId(Long gradeId);

    public List<Map<String, Object>> selectUserGradeInfoNum(UserGradeInfo userGradeInfo);

    /**
     * 查询用户成绩信息列表
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 用户成绩信息集合
     */
    public List<UserGradeInfo> selectUserGradeInfoList(UserGradeInfo userGradeInfo);

    /**
     * 新增用户成绩信息
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    public int insertUserGradeInfo(@Param("list") List<UserGradeInfo> userGradeInfoList);

    /**
     * 修改用户成绩信息
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    public int batchUpdateUserGradeInfo(@Param("list")List<UserGradeInfo> userGradeInfoList);

    /**
     * 删除用户成绩信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 结果
     */
    public int deleteUserGradeInfoByGradeId(Long gradeId);

    /**
     * 批量删除用户成绩信息
     * 
     * @param gradeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserGradeInfoByGradeIds(Long[] gradeIds);
}
