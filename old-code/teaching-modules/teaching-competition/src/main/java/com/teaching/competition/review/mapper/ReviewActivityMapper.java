package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Activity Mapper接口。
 */
public interface ReviewActivityMapper extends ReviewCrudMapper<ReviewActivity> {
    @Override
    ReviewActivity selectById(Long id);

    @Override
    List<ReviewActivity> selectList(ReviewActivity query);

    @Override
    int insert(ReviewActivity entity);

    @Override
    int update(ReviewActivity entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
