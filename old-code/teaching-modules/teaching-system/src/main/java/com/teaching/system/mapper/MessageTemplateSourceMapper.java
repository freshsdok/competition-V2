package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.MessageTemplateSource;

/**
 * 短信模板Mapper接口
 * 
 * @author teaching
 * @date 2025-12-22
 */
public interface MessageTemplateSourceMapper 
{
    /**
     * 查询短信模板
     * 
     * @param id 短信模板主键
     * @return 短信模板
     */
    public MessageTemplateSource selectMessageTemplateSourceById(Long id);

    /**
     * 获取短信模板配置
     * @return
     */
    public List<MessageTemplateSource> selectMessageTemplateAllInfo(String msgCode);

    /**
     * 查询短信模板列表
     * 
     * @param messageTemplateSource 短信模板
     * @return 短信模板集合
     */
    public List<MessageTemplateSource> selectMessageTemplateSourceList(MessageTemplateSource messageTemplateSource);

    /**
     * 获取短信模板列表
     * @param messageTemplateSource
     * @return
     */
    public List<MessageTemplateSource> selectDistinctMessageTemplateSourceList(MessageTemplateSource messageTemplateSource);

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
     * 删除短信模板
     * 
     * @param id 短信模板主键
     * @return 结果
     */
    public int deleteMessageTemplateSourceById(Long id);

    /**
     * 批量删除短信模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMessageTemplateSourceByIds(Long[] ids);

    /**
     * 获取短信模板属性值
     * @param params
     * @return
     */
    public Map<String,Object> selectAttributeValue(Map<String,Object> params);
}
