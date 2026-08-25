package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewPanelMember;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Panel Member Mapper接口。
 */
public interface ReviewPanelMemberMapper extends ReviewCrudMapper<ReviewPanelMember> {
    @Override
    ReviewPanelMember selectById(Long id);

    @Override
    List<ReviewPanelMember> selectList(ReviewPanelMember query);

    @Override
    int insert(ReviewPanelMember entity);

    @Override
    int update(ReviewPanelMember entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
