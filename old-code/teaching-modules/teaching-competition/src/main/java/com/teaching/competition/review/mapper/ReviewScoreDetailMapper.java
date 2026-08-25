package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewScoreDetail;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Score Detail Mapper接口。
 */
public interface ReviewScoreDetailMapper extends ReviewCrudMapper<ReviewScoreDetail> {
    @Override
    ReviewScoreDetail selectById(Long id);

    @Override
    List<ReviewScoreDetail> selectList(ReviewScoreDetail query);

    @Override
    int insert(ReviewScoreDetail entity);

    @Override
    int update(ReviewScoreDetail entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    int deleteByRecordId(@Param("recordId") Long recordId, @Param("updateBy") String updateBy);
}
