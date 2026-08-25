package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object Member Mapper接口。
 */
public interface ReviewObjectMemberMapper extends ReviewCrudMapper<ReviewObjectMember> {
    @Override
    ReviewObjectMember selectById(Long id);

    @Override
    List<ReviewObjectMember> selectList(ReviewObjectMember query);

    @Override
    int insert(ReviewObjectMember entity);

    @Override
    int update(ReviewObjectMember entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    int deleteByObjectId(@Param("activityId") Long activityId,
                         @Param("objectId") Long objectId,
                         @Param("updateBy") String updateBy);
}
