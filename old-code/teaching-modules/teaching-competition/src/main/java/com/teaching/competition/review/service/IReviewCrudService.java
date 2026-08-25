package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewBaseEntity;

import java.util.List;

/**
 * 通用评审模块基础Service契约。
 */
public interface IReviewCrudService<T extends ReviewBaseEntity> {
    T selectById(Long id);

    List<T> selectList(T query);

    int insert(T entity);

    int update(T entity);

    int deleteByIds(Long[] ids);
}
