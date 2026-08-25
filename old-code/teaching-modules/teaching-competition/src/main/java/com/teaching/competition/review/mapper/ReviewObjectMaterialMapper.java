package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Object Material Mapper接口。
 */
public interface ReviewObjectMaterialMapper extends ReviewCrudMapper<ReviewObjectMaterial> {
    @Override
    ReviewObjectMaterial selectById(Long id);

    @Override
    List<ReviewObjectMaterial> selectList(ReviewObjectMaterial query);

    @Override
    int insert(ReviewObjectMaterial entity);

    @Override
    int update(ReviewObjectMaterial entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
