package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Rule Mapper接口。
 */
public interface ReviewRuleMapper extends ReviewCrudMapper<ReviewRule> {
    @Override
    ReviewRule selectById(Long id);

    @Override
    List<ReviewRule> selectList(ReviewRule query);

    @Override
    int insert(ReviewRule entity);

    @Override
    int update(ReviewRule entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    int countSubmittedRecordsByRuleId(@Param("ruleId") Long ruleId);
}
