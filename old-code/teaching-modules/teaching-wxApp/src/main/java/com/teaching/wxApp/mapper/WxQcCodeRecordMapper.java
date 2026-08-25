package com.teaching.wxApp.mapper;

import com.teaching.wxApp.domain.WxQcCodeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 二维码生成记录Mapper接口
 *
 * @author teaching
 * @date 2026-04-08
 */
public interface WxQcCodeRecordMapper {

    /**
     * 查询二维码生成记录
     *
     * @param recordId 二维码生成记录主键
     * @return 二维码生成记录
     */
    public WxQcCodeRecord selectWxQcCodeRecordByRecordId(Long recordId);

    /**
     * 查询二维码生成记录-只包含base64字段
     * @param recordId
     * @return
     */
    public Map<String,Object> selectWxQcCodeBaseByRecordId(Long recordId);

    /**
     * 查询二维码生成记录列表
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 二维码生成记录集合
     */
    public List<WxQcCodeRecord> selectWxQcCodeRecordList(WxQcCodeRecord wxQcCodeRecord);

    /**
     * 查询二维码生成记录-含base64字段
     * @param wxQcCodeRecord
     * @return
     */
    public List<WxQcCodeRecord> selectWxQcCodeRecordInfosList(WxQcCodeRecord wxQcCodeRecord);

    /**
     * 新增二维码生成记录
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 结果
     */
    public int insertWxQcCodeRecord(WxQcCodeRecord wxQcCodeRecord);

    /**
     * 修改二维码生成记录
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 结果
     */
    public int updateWxQcCodeRecord(WxQcCodeRecord wxQcCodeRecord);

    /**
     * 删除二维码生成记录
     *
     * @param recordId 二维码生成记录主键
     * @return 结果
     */
    public int deleteWxQcCodeRecordByRecordId(Long recordId);

    /**
     * 批量删除二维码生成记录
     *
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWxQcCodeRecordByRecordIds(Long[] recordIds);

    /**
     * 根据配置ID查询二维码生成记录列表
     *
     * @param codeConfigId 配置ID
     * @return 二维码生成记录集合
     */
    public List<WxQcCodeRecord> selectWxQcCodeRecordListByCodeConfigId(Long codeConfigId);

    /**
     * 查询二维码生成记录及其关联的配置信息
     * @param id 二维码生成记录ID
     * @return 包含二维码生成记录和配置信息的Map
     */
    public Map<String,Object> selectRecordAndConfig(Long id);
}
