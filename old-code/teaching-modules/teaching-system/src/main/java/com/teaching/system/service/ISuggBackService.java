package com.teaching.system.service;

import java.util.List;
import com.teaching.system.domain.SuggBack;

/**
 * 意见反馈服务层
 * 
 * @author teaching
 */
public interface ISuggBackService
{
    /**
     * 查询意见反馈信息
     * 
     * @param suggBackId 意见反馈ID
     * @return 意见反馈信息
     */
    public SuggBack selectSuggBackBySuggBackId(Long suggBackId);

    /**
     * 查询意见反馈列表
     * 
     * @param suggBack 意见反馈信息
     * @return 意见反馈集合
     */
    public List<SuggBack> selectSuggBackList(SuggBack suggBack);

    /**
     * 新增意见反馈
     * 
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    public int insertSuggBack(SuggBack suggBack);

    /**
     * 修改意见反馈
     * 
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    public int updateSuggBack(SuggBack suggBack);

    /**
     * 删除意见反馈信息
     * 
     * @param suggBackId 意见反馈ID
     * @return 结果
     */
    public int deleteSuggBackBySuggBackId(Long suggBackId);
    
    /**
     * 批量删除意见反馈信息
     * 
     * @param suggBackIds 需要删除的意见反馈ID
     * @return 结果
     */
    public int deleteSuggBackBySuggBackIds(Long[] suggBackIds);

    /**
     * 回复意见反馈
     * 
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    public int replySuggBack(SuggBack suggBack);

    /**
     * 转交意见反馈
     * 
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    public int transferSuggBack(SuggBack suggBack);
}
