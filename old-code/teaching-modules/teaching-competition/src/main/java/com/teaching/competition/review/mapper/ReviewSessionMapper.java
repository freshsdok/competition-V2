package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Session Mapper接口。
 */
public interface ReviewSessionMapper extends ReviewCrudMapper<ReviewSession> {
    @Override
    ReviewSession selectById(Long id);

    @Override
    List<ReviewSession> selectList(ReviewSession query);

    @Override
    int insert(ReviewSession entity);

    @Override
    int update(ReviewSession entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
