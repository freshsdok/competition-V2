package com.teaching.competition.review.support;

import com.teaching.competition.review.domain.ReviewBaseEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用评审模块基础Mapper契约。
 */
public interface ReviewCrudMapper<T extends ReviewBaseEntity> {
    T selectById(Long id);

    List<T> selectList(T query);

    int insert(T entity);

    int update(T entity);

    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
