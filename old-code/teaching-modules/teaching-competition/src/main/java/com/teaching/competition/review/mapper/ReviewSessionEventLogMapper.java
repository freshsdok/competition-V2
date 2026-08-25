package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Session Event Log Mapper接口。
 */
public interface ReviewSessionEventLogMapper extends ReviewCrudMapper<ReviewSessionEventLog> {
    @Override
    ReviewSessionEventLog selectById(Long id);

    @Override
    List<ReviewSessionEventLog> selectList(ReviewSessionEventLog query);

    @Override
    int insert(ReviewSessionEventLog entity);

    @Override
    int update(ReviewSessionEventLog entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
