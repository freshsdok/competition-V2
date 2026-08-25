package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object Mapper接口。
 */
public interface ReviewObjectMapper extends ReviewCrudMapper<ReviewObject> {
    @Override
    ReviewObject selectById(Long id);

    @Override
    List<ReviewObject> selectList(ReviewObject query);

    @Override
    int insert(ReviewObject entity);

    @Override
    int update(ReviewObject entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    ReviewObject selectBySourceRef(@Param("activityId") Long activityId,
                                   @Param("sourceModule") String sourceModule,
                                   @Param("sourceBizType") String sourceBizType,
                                   @Param("sourceBizId") String sourceBizId);
}
