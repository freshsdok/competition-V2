package com.teaching.system.mapper;

import java.util.List;

import com.teaching.system.domain.ProcessedRelation;
import com.teaching.system.domain.ReviewTaskSpecialistRelation;
import org.apache.ibatis.annotations.Param;

/**
 * 评审文件处理前后对应关系Mapper接口
 *
 * @author teaching
 * @date 2026-04-23
 */
public interface ProcessedRelationMapper {
    /**
     * 查询评审文件处理前后对应关系
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 评审文件处理前后对应关系
     */
    public ProcessedRelation selectProcessedRelationById(Long id);

    /**
     * 查询评审文件处理前后对应关系列表
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 评审文件处理前后对应关系集合
     */
    public List<ProcessedRelation> selectProcessedRelationList(ProcessedRelation processedRelation);

    /**
     * 新增评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    public int insertProcessedRelation(ProcessedRelation processedRelation);

    /**
     * 修改评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    public int updateProcessedRelation(ProcessedRelation processedRelation);

    /**
     * 删除评审文件处理前后对应关系
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 结果
     */
    public int deleteProcessedRelationById(Long id);

    /**
     * 批量删除评审文件处理前后对应关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProcessedRelationByIds(Long[] ids);

    /**
     * 根据managerIds删除评审文件处理前后对应关系
     * @param ids
     * @return
     */
    public int updateProcessedRelationByManagerIds(Long[] ids);

    /**
     * 根据managerIds查询评审文件处理前后对应关系
     * @param list
     * @return
     */
    public List<ProcessedRelation> selectProcessedRelationByManagerId(List<Long> list);

    /**
     * 根据relaId修改审阅状态
     * @param reviewTaskSpecialistRelation
     * @return
     */
    public int updateReviewStatusByRelaId(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation);

}
