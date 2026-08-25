package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Criteria Mapper接口。
 */
public interface ReviewCriteriaMapper extends ReviewCrudMapper<ReviewCriteria> {
    @Override
    ReviewCriteria selectById(Long id);

    @Override
    List<ReviewCriteria> selectList(ReviewCriteria query);

    @Override
    int insert(ReviewCriteria entity);

    @Override
    int update(ReviewCriteria entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
