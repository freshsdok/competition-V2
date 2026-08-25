package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObjectExternalRef;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object External Ref Mapper接口。
 */
public interface ReviewObjectExternalRefMapper extends ReviewCrudMapper<ReviewObjectExternalRef> {
    @Override
    ReviewObjectExternalRef selectById(Long id);

    @Override
    List<ReviewObjectExternalRef> selectList(ReviewObjectExternalRef query);

    @Override
    int insert(ReviewObjectExternalRef entity);

    @Override
    int update(ReviewObjectExternalRef entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
