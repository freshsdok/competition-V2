package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewPanel;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Panel Mapper接口。
 */
public interface ReviewPanelMapper extends ReviewCrudMapper<ReviewPanel> {
    @Override
    ReviewPanel selectById(Long id);

    @Override
    List<ReviewPanel> selectList(ReviewPanel query);

    @Override
    int insert(ReviewPanel entity);

    @Override
    int update(ReviewPanel entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
