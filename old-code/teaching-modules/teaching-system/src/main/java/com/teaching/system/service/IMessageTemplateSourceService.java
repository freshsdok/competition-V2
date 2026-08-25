package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.MessageTemplateSource;

/**
 * 短信模板Service接口
 * 
 * @author teaching
 * @date 2025-12-22
 */
public interface IMessageTemplateSourceService 
{
    /**
     * 查询短信模板
     * 
     * @param id 短信模板主键
     * @return 短信模板
     */
    public MessageTemplateSource selectMessageTemplateSourceById(Long id);

    /**
     * 查询短信模板列表
     * 
     * @param messageTemplateSource 短信模板
     * @return 短信模板集合
     */
    public List<MessageTemplateSource> selectMessageTemplateSourceList(MessageTemplateSource messageTemplateSource);

    /**
     * 新增短信模板
     * 
     * @param messageTemplateSource 短信模板
     * @return 结果
     */
    public int insertMessageTemplateSource(MessageTemplateSource messageTemplateSource);

    /**
     * 修改短信模板
     * 
     * @param messageTemplateSource 短信模板
     * @return 结果
     */
    public int updateMessageTemplateSource(MessageTemplateSource messageTemplateSource);

    /**
     * 批量删除短信模板
     * 
     * @param ids 需要删除的短信模板主键集合
     * @return 结果
     */
    public int deleteMessageTemplateSourceByIds(Long[] ids);

    /**
     * 删除短信模板信息
     * 
     * @param id 短信模板主键
     * @return 结果
     */
    public int deleteMessageTemplateSourceById(Long id);

    /**
     * 获取短信模板配置
     * @return
     */
    public List<MessageTemplateSource> getTemplateSource();

    /**
     * 获取短信模板属性值
     * @param templateCode
     * @return
     */
    public Map<String,Object> getTemplateAttributeValue(String templateCode,Long userId);
}
