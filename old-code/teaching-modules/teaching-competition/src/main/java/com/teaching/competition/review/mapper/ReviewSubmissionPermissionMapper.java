package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Submission Permission Mapper接口。
 */
public interface ReviewSubmissionPermissionMapper extends ReviewCrudMapper<ReviewSubmissionPermission> {
    @Override
    ReviewSubmissionPermission selectById(Long id);

    @Override
    List<ReviewSubmissionPermission> selectList(ReviewSubmissionPermission query);

    @Override
    int insert(ReviewSubmissionPermission entity);

    @Override
    int update(ReviewSubmissionPermission entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    int deleteByObjectId(@Param("activityId") Long activityId,
                         @Param("objectId") Long objectId,
                         @Param("updateBy") String updateBy);
}
