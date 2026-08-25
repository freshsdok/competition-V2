package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Audit Log Mapper接口。
 */
public interface ReviewAuditLogMapper extends ReviewCrudMapper<ReviewAuditLog> {
    @Override
    ReviewAuditLog selectById(Long id);

    @Override
    List<ReviewAuditLog> selectList(ReviewAuditLog query);

    @Override
    int insert(ReviewAuditLog entity);

    @Override
    int update(ReviewAuditLog entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
