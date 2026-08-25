package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Round Mapper接口。
 */
public interface ReviewRoundMapper extends ReviewCrudMapper<ReviewRound> {
    @Override
    ReviewRound selectById(Long id);

    @Override
    List<ReviewRound> selectList(ReviewRound query);

    @Override
    int insert(ReviewRound entity);

    @Override
    int update(ReviewRound entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
