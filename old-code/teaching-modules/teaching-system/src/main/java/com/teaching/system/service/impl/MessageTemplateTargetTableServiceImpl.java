package com.teaching.system.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.MessageTemplateTargetTableMapper;
import com.teaching.system.domain.MessageTemplateTargetTable;
import com.teaching.system.service.IMessageTemplateTargetTableService;

/**
 * 短信模板目标Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-22
 */
@Service
public class MessageTemplateTargetTableServiceImpl implements IMessageTemplateTargetTableService 
{
    @Autowired
    private MessageTemplateTargetTableMapper messageTemplateTargetTableMapper;

    /**
     * 查询短信模板目标
     * 
     * @param id 短信模板目标主键
     * @return 短信模板目标
     */
    @Override
    public MessageTemplateTargetTable selectMessageTemplateTargetTableById(Long id)
    {
        return messageTemplateTargetTableMapper.selectMessageTemplateTargetTableById(id);
    }

    /**
     * 查询短信模板目标列表
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 短信模板目标
     */
    @Override
    public List<MessageTemplateTargetTable> selectMessageTemplateTargetTableList(MessageTemplateTargetTable messageTemplateTargetTable)
    {
        return messageTemplateTargetTableMapper.selectMessageTemplateTargetTableList(messageTemplateTargetTable);
    }

    /**
     * 新增短信模板目标
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 结果
     */
    @Override
    public int insertMessageTemplateTargetTable(MessageTemplateTargetTable messageTemplateTargetTable)
    {
        messageTemplateTargetTable.setCreateTime(DateUtils.getNowDate());
        return messageTemplateTargetTableMapper.insertMessageTemplateTargetTable(messageTemplateTargetTable);
    }

    /**
     * 修改短信模板目标
     * 
     * @param messageTemplateTargetTable 短信模板目标
     * @return 结果
     */
    @Override
    public int updateMessageTemplateTargetTable(MessageTemplateTargetTable messageTemplateTargetTable)
    {
        messageTemplateTargetTable.setUpdateTime(DateUtils.getNowDate());
        return messageTemplateTargetTableMapper.updateMessageTemplateTargetTable(messageTemplateTargetTable);
    }

    /**
     * 批量删除短信模板目标
     * 
     * @param ids 需要删除的短信模板目标主键
     * @return 结果
     */
    @Override
    public int deleteMessageTemplateTargetTableByIds(Long[] ids)
    {
        return messageTemplateTargetTableMapper.deleteMessageTemplateTargetTableByIds(ids);
    }

    /**
     * 删除短信模板目标信息
     * 
     * @param id 短信模板目标主键
     * @return 结果
     */
    @Override
    public int deleteMessageTemplateTargetTableById(Long id)
    {
        return messageTemplateTargetTableMapper.deleteMessageTemplateTargetTableById(id);
    }
}
