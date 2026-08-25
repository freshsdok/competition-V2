package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObjectSubmitLog;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object Submit Log Mapper接口。
 */
public interface ReviewObjectSubmitLogMapper extends ReviewCrudMapper<ReviewObjectSubmitLog> {
    @Override
    ReviewObjectSubmitLog selectById(Long id);

    @Override
    List<ReviewObjectSubmitLog> selectList(ReviewObjectSubmitLog query);

    @Override
    int insert(ReviewObjectSubmitLog entity);

    @Override
    int update(ReviewObjectSubmitLog entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
