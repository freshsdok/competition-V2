package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Session Object Mapper接口。
 */
public interface ReviewSessionObjectMapper extends ReviewCrudMapper<ReviewSessionObject> {
    @Override
    ReviewSessionObject selectById(Long id);

    @Override
    List<ReviewSessionObject> selectList(ReviewSessionObject query);

    @Override
    int insert(ReviewSessionObject entity);

    @Override
    int update(ReviewSessionObject entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
