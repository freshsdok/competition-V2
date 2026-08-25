package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.TeacherTmpInfo;

/**
 * 教师导入临时Mapper接口
 * 
 * @author teaching
 * @date 2025-12-19
 */
public interface TeacherTmpInfoMapper 
{
    /**
     * 查询教师导入临时
     * 
     * @param id 教师导入临时主键
     * @return 教师导入临时
     */
    public TeacherTmpInfo selectTeacherTmpInfoById(String id);

    /**
     * 查询教师导入临时列表
     * 
     * @param teacherTmpInfo 教师导入临时
     * @return 教师导入临时集合
     */
    public List<TeacherTmpInfo> selectTeacherTmpInfoList(TeacherTmpInfo teacherTmpInfo);

    /**
     * 新增教师导入临时
     * 
     * @param teacherTmpInfo 教师导入临时
     * @return 结果
     */
    public int insertTeacherTmpInfo(TeacherTmpInfo teacherTmpInfo);

    /**
     * 修改教师导入临时
     * 
     * @param teacherTmpInfo 教师导入临时
     * @return 结果
     */
    public int updateTeacherTmpInfo(TeacherTmpInfo teacherTmpInfo);

    /**
     * 删除教师导入临时
     * 
     * @param id 教师导入临时主键
     * @return 结果
     */
    public int deleteTeacherTmpInfoById(String id);

    /**
     * 批量删除教师导入临时
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTeacherTmpInfoByIds(String[] ids);
}
