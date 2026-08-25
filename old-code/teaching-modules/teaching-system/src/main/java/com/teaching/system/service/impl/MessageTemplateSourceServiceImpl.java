package com.teaching.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.sms.SmsUtil;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.MessageTemplateTargetTable;
import com.teaching.system.mapper.SysUserMapper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.MessageTemplateSourceMapper;
import com.teaching.system.domain.MessageTemplateSource;
import com.teaching.system.service.IMessageTemplateSourceService;

/**
 * 短信模板Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-22
 */
@Service
public class MessageTemplateSourceServiceImpl implements IMessageTemplateSourceService 
{
    @Autowired
    private MessageTemplateSourceMapper messageTemplateSourceMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 查询短信模板
     * 
     * @param id 短信模板主键
     * @return 短信模板
     */
    @Override
    public MessageTemplateSource selectMessageTemplateSourceById(Long id)
    {
        return messageTemplateSourceMapper.selectMessageTemplateSourceById(id);
    }

    /**
     * 查询短信模板列表
     * 
     * @param messageTemplateSource 短信模板
     * @return 短信模板
     */
    @Override
    public List<MessageTemplateSource> selectMessageTemplateSourceList(MessageTemplateSource messageTemplateSource)
    {
        return messageTemplateSourceMapper.selectMessageTemplateSourceList(messageTemplateSource);
    }

    /**
     * 新增短信模板
     * 
     * @param messageTemplateSource 短信模板
     * @return 结果
     */
    @Override
    public int insertMessageTemplateSource(MessageTemplateSource messageTemplateSource)
    {
        messageTemplateSource.setCreateTime(DateUtils.getNowDate());
        return messageTemplateSourceMapper.insertMessageTemplateSource(messageTemplateSource);
    }

    /**
     * 修改短信模板
     * 
     * @param messageTemplateSource 短信模板
     * @return 结果
     */
    @Override
    public int updateMessageTemplateSource(MessageTemplateSource messageTemplateSource)
    {
        return messageTemplateSourceMapper.updateMessageTemplateSource(messageTemplateSource);
    }

    /**
     * 批量删除短信模板
     * 
     * @param ids 需要删除的短信模板主键
     * @return 结果
     */
    @Override
    public int deleteMessageTemplateSourceByIds(Long[] ids)
    {
        return messageTemplateSourceMapper.deleteMessageTemplateSourceByIds(ids);
    }

    /**
     * 删除短信模板信息
     * 
     * @param id 短信模板主键
     * @return 结果
     */
    @Override
    public int deleteMessageTemplateSourceById(Long id)
    {
        return messageTemplateSourceMapper.deleteMessageTemplateSourceById(id);
    }

    @Override
    public List<MessageTemplateSource> getTemplateSource() {
        return messageTemplateSourceMapper.selectDistinctMessageTemplateSourceList(null);
    }

    // 发短信调用
    @Override
    public Map<String, Object> getTemplateAttributeValue(String templateCode,Long userId) {
        Map<String, Object> templateAttributeParams = new HashMap<>();
        // 获取短信模板属性配置信息
        List<MessageTemplateSource> messageTemplateSourceList =
                messageTemplateSourceMapper.selectMessageTemplateAllInfo(templateCode);
        if (CollectionUtils.isNotEmpty(messageTemplateSourceList)) {
            messageTemplateSourceList.stream().forEach(messageTemplateSource -> {
                List<MessageTemplateTargetTable> messageTemplateTargetTableList =
                        messageTemplateSource.getMessageTemplateTargetTableList();
                if (CollectionUtils.isNotEmpty(messageTemplateTargetTableList)) {
                    StringBuffer sb = new StringBuffer();
                    messageTemplateTargetTableList.stream().forEach(messageTemplateTargetTable -> {
                        // 获取字段值
                        Map<String,Object> params = new HashMap<>(Map.of("targetColumn", messageTemplateTargetTable.getTargetColumn(),
                                "targetTable", messageTemplateTargetTable.getTargetTable(),
                                "targetConditionColumn", messageTemplateTargetTable.getConditionColumn(),
                                "targetConditionName", userId
                        ));
                        if(StringUtils.isNotEmpty(messageTemplateTargetTable.getConditionColumn2())){
                            params.put("conditionColumn2", messageTemplateTargetTable.getConditionColumn2());
                        }
                        Map<String, Object> resMap = messageTemplateSourceMapper.selectAttributeValue(params);
                        if(MapUtils.isNotEmpty(resMap)){
                            sb.append(resMap.get(messageTemplateTargetTable.getTargetColumn()));
                            sb.append("-");
                        }
                    });
                    templateAttributeParams.put(messageTemplateSource.getAttributeName(), sb.substring(0, sb.length() - 1).toString());
                }
            });
        }
        SysUser sysUser = sysUserMapper.selectUserById(userId);
        SmsUtil.sendNoticeCode(templateCode,sysUser.getPhonenumber(),templateAttributeParams);
        return templateAttributeParams;
    }
}
