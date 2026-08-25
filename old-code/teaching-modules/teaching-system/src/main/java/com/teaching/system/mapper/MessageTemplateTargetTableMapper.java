package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.MessageTemplateTargetTable;

/**
 * 短信模板目标Mapper接口
 * 
 * @author teaching
 * @date 2025-12-22
 */
public interface MessageTemplateTargetTableMapper 
{
    /**
     * 查询短信模板目标
     * 
     * @param id 短信模板目标主键
     * @return 短信模板目标
     */
    public MessageTemplateTargetTable selectMessageTemplateTargetTableById(Long id);

    /**
     * 查询短信模板目标列表
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 短信模板目标集合
     */
    public List<MessageTemplateTargetTable> selectMessageTemplateTargetTableList(MessageTemplateTargetTable messageTemplateTargetTable);

    /**
     * 新增短信模板目标
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 结果
     */
    public int insertMessageTemplateTargetTable(MessageTemplateTargetTable messageTemplateTargetTable);

    /**
     * 修改短信模板目标
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 结果
     */
    public int updateMessageTemplateTargetTable(MessageTemplateTargetTable messageTemplateTargetTable);

    /**
     * 删除短信模板目标
     * 
     * @param id 短信模板目标主键
     * @return 结果
     */
    public int deleteMessageTemplateTargetTableById(Long id);

    /**
     * 批量删除短信模板目标
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMessageTemplateTargetTableByIds(Long[] ids);
}
