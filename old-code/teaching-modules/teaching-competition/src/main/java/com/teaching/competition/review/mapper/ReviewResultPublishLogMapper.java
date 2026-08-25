package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewResultPublishLog;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Result Publish Log Mapper接口。
 */
public interface ReviewResultPublishLogMapper extends ReviewCrudMapper<ReviewResultPublishLog> {
    @Override
    ReviewResultPublishLog selectById(Long id);

    @Override
    List<ReviewResultPublishLog> selectList(ReviewResultPublishLog query);

    @Override
    int insert(ReviewResultPublishLog entity);

    @Override
    int update(ReviewResultPublishLog entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
