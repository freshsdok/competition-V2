package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewBaseEntity;
import com.teaching.competition.review.service.IReviewCrudService;
import com.teaching.competition.review.support.ReviewCrudMapper;

import java.util.Date;
import java.util.List;

/**
 * 通用评审模块基础Service实现。
 */
public abstract class AbstractReviewCrudService<T extends ReviewBaseEntity> implements IReviewCrudService<T> {

    protected abstract ReviewCrudMapper<T> mapper();

    @Override
    public T selectById(Long id) {
        return mapper().selectById(id);
    }

    @Override
    public List<T> selectList(T query) {
        return mapper().selectList(query);
    }

    @Override
    public int insert(T entity) {
        if (entity == null) {
            throw new ServiceException("保存对象不能为空");
        }
        fillCreate(entity);
        return mapper().insert(entity);
    }

    @Override
    public int update(T entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新对象ID不能为空");
        }
        fillUpdate(entity);
        return mapper().update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除对象ID不能为空");
        }
        return mapper().deleteByIds(ids, currentUsername());
    }

        protected void fillCreate(T entity) {
            fillCreateBase(entity);
        }

        protected void fillCreateBase(ReviewBaseEntity entity) {
            Date now = DateUtils.getNowDate();
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setCreateBy(currentUsername());
            entity.setUpdateBy(currentUsername());
        if (StringUtils.isEmpty(entity.getDelFlag())) {
            entity.setDelFlag(ReviewConstants.DEL_FLAG_NORMAL);
        }
        }

        protected void fillUpdate(T entity) {
            fillUpdateBase(entity);
        }

        protected void fillUpdateBase(ReviewBaseEntity entity) {
            entity.setUpdateTime(DateUtils.getNowDate());
            entity.setUpdateBy(currentUsername());
        }

    protected String currentUsername() {
        try {
            String username = SecurityUtils.getUsername();
            return StringUtils.isNotEmpty(username) ? username : ReviewConstants.SYSTEM_OPERATOR;
        } catch (Exception ex) {
            return ReviewConstants.SYSTEM_OPERATOR;
        }
    }

    protected Long currentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception ex) {
            return null;
        }
    }
}
