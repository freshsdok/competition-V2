package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Assignment Mapper接口。
 */
public interface ReviewAssignmentMapper extends ReviewCrudMapper<ReviewAssignment> {
    @Override
    ReviewAssignment selectById(Long id);

    @Override
    List<ReviewAssignment> selectList(ReviewAssignment query);

    @Override
    int insert(ReviewAssignment entity);

    @Override
    int update(ReviewAssignment entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
