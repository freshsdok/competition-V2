package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewActivityUserRole;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Activity User Role Mapper接口。
 */
public interface ReviewActivityUserRoleMapper extends ReviewCrudMapper<ReviewActivityUserRole> {
    @Override
    ReviewActivityUserRole selectById(Long id);

    @Override
    List<ReviewActivityUserRole> selectList(ReviewActivityUserRole query);

    @Override
    int insert(ReviewActivityUserRole entity);

    @Override
    int update(ReviewActivityUserRole entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
