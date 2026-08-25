package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewerProfile;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Reviewer Profile Mapper接口。
 */
public interface ReviewerProfileMapper extends ReviewCrudMapper<ReviewerProfile> {
    @Override
    ReviewerProfile selectById(Long id);

    @Override
    List<ReviewerProfile> selectList(ReviewerProfile query);

    @Override
    int insert(ReviewerProfile entity);

    @Override
    int update(ReviewerProfile entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
