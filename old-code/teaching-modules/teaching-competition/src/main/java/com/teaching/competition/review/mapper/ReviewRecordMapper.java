package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Record Mapper接口。
 */
public interface ReviewRecordMapper extends ReviewCrudMapper<ReviewRecord> {
    @Override
    ReviewRecord selectById(Long id);

    @Override
    List<ReviewRecord> selectList(ReviewRecord query);

    @Override
    int insert(ReviewRecord entity);

    @Override
    int update(ReviewRecord entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
