package com.teaching.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.system.domain.SuggBack;

/**
 * 意见反馈Mapper接口
 * 
 * @author teaching
 */
@Mapper
public interface SuggBackMapper
{
    /**
     * 查询意见反馈
     * 
     * @param suggBackId 意见反馈主键
     * @return 意见反馈
     */
    public SuggBack selectSuggBackBySuggBackId(Long suggBackId);

    /**
     * 查询意见反馈列表
     * 
     * @param suggBack 意见反馈
     * @return 意见反馈集合
     */
    public List<SuggBack> selectSuggBackList(SuggBack suggBack);

    /**
     * 新增意见反馈
     * 
     * @param suggBack 意见反馈
     * @return 结果
     */
    public int insertSuggBack(SuggBack suggBack);

    /**
     * 修改意见反馈
     * 
     * @param suggBack 意见反馈
     * @return 结果
     */
    public int updateSuggBack(SuggBack suggBack);

    /**
     * 删除意见反馈
     * 
     * @param suggBackId 意见反馈主键
     * @return 结果
     */
    public int deleteSuggBackBySuggBackId(Long suggBackId);

    /**
     * 批量删除意见反馈
     * 
     * @param suggBackIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSuggBackBySuggBackIds(Long[] suggBackIds);

    /**
     * 回复意见反馈
     * 
     * @param suggBack 意见反馈
     * @return 结果
     */
    public int replySuggBack(SuggBack suggBack);

    /**
     * 转交意见反馈
     * 
     * @param suggBack 意见反馈
     * @return 结果
     */
    public int transferSuggBack(SuggBack suggBack);

    /**
     * 查询指定日期前缀的最大反馈编码
     * 
     * @param datePrefix 日期前缀（格式：yyyyMMdd-YJ-）
     * @return 最大编码
     */
    public String selectMaxBackCodeByDatePrefix(String datePrefix);
}
