package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家文件阅读状态记录Mapper接口
 *
 * @author teaching
 * @date 2026-04-27
 */
public interface ReviewRecordMapper {
    /**
     * 查询专家文件阅读状态记录
     *
     * @param id 专家文件阅读状态记录主键
     * @return 专家文件阅读状态记录
     */
    public ReviewRecord selectReviewRecordById(Long id);

    /**
     * 查询专家文件阅读状态记录列表
     *
     * @param reviewRecord 专家文件阅读状态记录
     * @return 专家文件阅读状态记录集合
     */
    public List<ReviewRecord> selectReviewRecordList(ReviewRecord reviewRecord);

    /**
     * 根据专家和文件查询记录
     * @param expertId
     * @param fileId
     * @return
     */
    public ReviewRecord selectRecordByExpertAndFile(@Param("expertId") Long expertId, @Param("fileId") Long fileId);

    /**
     * 新增专家文件阅读状态记录
     *
     * @param reviewRecord 专家文件阅读状态记录
     * @return 结果
     */
    public int insertReviewRecord(ReviewRecord reviewRecord);

    /**
     * 修改专家文件阅读状态记录
     *
     * @param reviewRecord 专家文件阅读状态记录
     * @return 结果
     */
    public int updateReviewRecord(ReviewRecord reviewRecord);

    /**
     * 删除专家文件阅读状态记录
     *
     * @param id 专家文件阅读状态记录主键
     * @return 结果
     */
    public int deleteReviewRecordById(Long id);

    /**
     * 批量删除专家文件阅读状态记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewRecordByIds(Long[] ids);
}
