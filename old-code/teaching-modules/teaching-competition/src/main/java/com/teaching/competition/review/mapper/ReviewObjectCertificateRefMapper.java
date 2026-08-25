package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.vo.ReviewCertificateResolveVO;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object Certificate Ref Mapper接口。
 */
public interface ReviewObjectCertificateRefMapper extends ReviewCrudMapper<ReviewObjectCertificateRef> {
    @Override
    ReviewObjectCertificateRef selectById(Long id);

    @Override
    List<ReviewObjectCertificateRef> selectList(ReviewObjectCertificateRef query);

    @Override
    int insert(ReviewObjectCertificateRef entity);

    @Override
    int update(ReviewObjectCertificateRef entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    List<ReviewCertificateResolveVO> selectResolveList(@Param("activityId") Long activityId,
                                                       @Param("certificateCode") String certificateCode);

    int invalidateByObjectId(@Param("activityId") Long activityId,
                             @Param("objectId") Long objectId,
                             @Param("updateBy") String updateBy);
}
