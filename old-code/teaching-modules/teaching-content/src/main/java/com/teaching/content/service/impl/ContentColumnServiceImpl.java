package com.teaching.content.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ContentColumn;
import com.teaching.content.mapper.ContentColumnMapper;
import com.teaching.content.service.IContentColumnService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 内容栏目Service业务层处理
 *
 * @author teaching
 * @date 2025-12-10
 */
@Service
public class ContentColumnServiceImpl implements IContentColumnService {
    @Autowired
    private ContentColumnMapper contentColumnMapper;

    /**
     * 查询内容栏目
     *
     * @param columnId 内容栏目主键
     * @return 内容栏目
     */
    @Override
    public ContentColumn selectContentColumnByColumnId(Long columnId) {
        return contentColumnMapper.selectContentColumnByColumnId(columnId);
    }

    /**
     * 查询内容栏目列表
     *
     * @param contentColumn 内容栏目
     * @return 内容栏目集合
     */
    @Override
    public List<ContentColumn> selectContentColumnList(ContentColumn contentColumn) {
        return contentColumnMapper.selectContentColumnList(contentColumn);
    }

    /**
     * 根据菜单ID查询栏目
     *
     * @param menuId 菜单ID
     * @return 内容栏目
     */
    @Override
    public ContentColumn selectContentColumnByMenuId(Long menuId) {
        return contentColumnMapper.selectContentColumnByMenuId(menuId);
    }

    /**
     * 查询栏目树形结构
     *
     * @param contentColumn 内容栏目
     * @return 内容栏目集合
     */
    @Override
    public List<ContentColumn> selectContentColumnTree(ContentColumn contentColumn) {
        List<ContentColumn> columns = contentColumnMapper.selectContentColumnTree(contentColumn);
        return buildColumnTree(columns);
    }

    /**
     * 新增内容栏目
     *
     * @param contentColumn 内容栏目
     * @return 结果
     */
    @Override
    public int insertContentColumn(ContentColumn contentColumn) {
        // 判断菜单是否已经绑定栏目
        int count = contentColumnMapper.checkContentColumnByMenuId(contentColumn.getMenuId(),contentColumn.getColumnType());
        if (count > 0) {
            throw new GlobalException("菜单已经绑定栏目，请勿重复绑定！");
        }
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentColumn.setCreateBy(sysUser.getNickName());
        }
        contentColumn.setCreateTime(DateUtils.getNowDate());
        // 默认删除标志为0（未删除）
        if (contentColumn.getDelFlag() == null || contentColumn.getDelFlag().isEmpty()) {
            contentColumn.setDelFlag("0");
        }
        // 默认状态为正常（0）
        if (contentColumn.getStatus() == null || contentColumn.getStatus().isEmpty()) {
            contentColumn.setStatus("0");
        }
        // 默认不置顶
        if (contentColumn.getIsTop() == null || contentColumn.getIsTop().isEmpty()) {
            contentColumn.setIsTop("0");
        }
        // 默认排序为0
        if (contentColumn.getOrderNum() == null) {
            contentColumn.setOrderNum(0);
        }
        return contentColumnMapper.insertContentColumn(contentColumn);
    }

    /**
     * 修改内容栏目
     *
     * @param contentColumn 内容栏目
     * @return 结果
     */
    @Override
    public int updateContentColumn(ContentColumn contentColumn) {
        // 判断菜单是否已经绑定栏目
        int count = contentColumnMapper.checkContentColumnByMenuId(contentColumn.getMenuId(),contentColumn.getColumnType());
        if (count > 0) {
            throw new GlobalException("菜单已经绑定栏目，请勿重复绑定！");
        }
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentColumn.setUpdateBy(sysUser.getNickName());
        }
        contentColumn.setUpdateTime(DateUtils.getNowDate());
        return contentColumnMapper.updateContentColumn(contentColumn);
    }

    /**
     * 删除内容栏目
     *
     * @param columnId 内容栏目主键
     * @return 结果
     */
    @Override
    public int deleteContentColumnByColumnId(Long columnId) {
        // 检查是否存在子栏目
        if (contentColumnMapper.hasChildByColumnId(columnId) > 0) {
            return 0;  // 存在子栏目，不允许删除
        }
        return contentColumnMapper.deleteContentColumnByColumnId(columnId);
    }

    /**
     * 批量删除内容栏目
     *
     * @param columnIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteContentColumnByColumnIds(Long[] columnIds) {
        return contentColumnMapper.deleteContentColumnByColumnIds(columnIds);
    }

    /**
     * 检查是否存在子栏目
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    @Override
    public int hasChildByColumnId(Long columnId) {
        return contentColumnMapper.hasChildByColumnId(columnId);
    }

    /**
     * 构建栏目树形结构
     *
     * @param columns 栏目列表
     * @return 栏目树形结构
     */
    private List<ContentColumn> buildColumnTree(List<ContentColumn> columns) {
        List<ContentColumn> returnList = new ArrayList<ContentColumn>();
        List<Long> tempList = new ArrayList<Long>();
        for (ContentColumn column : columns) {
            tempList.add(column.getColumnId());
        }

        for (ContentColumn column : columns) {
            // 如果是顶级节点, 根据父节点id为空来判断
            if (!tempList.contains(column.getParentId())) {
                recursionFn(columns, column);
                returnList.add(column);
            }
        }
        if (returnList.isEmpty()) {
            returnList = columns;
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list 分类列表
     * @param t    分类
     */
    private void recursionFn(List<ContentColumn> list, ContentColumn t) {
        // 得到子节点列表
        List<ContentColumn> childList = getChildList(list, t);
        t.setChildren(childList);
        for (ContentColumn tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     *
     * @param list 分类列表
     * @param t    分类
     */
    private List<ContentColumn> getChildList(List<ContentColumn> list, ContentColumn t) {
        List<ContentColumn> tlist = new ArrayList<ContentColumn>();
        Iterator<ContentColumn> it = list.iterator();
        while (it.hasNext()) {
            ContentColumn n = (ContentColumn) it.next();
            if (n.getParentId().longValue() == t.getColumnId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     *
     * @param list 分类列表
     * @param t    分类
     */
    private boolean hasChild(List<ContentColumn> list, ContentColumn t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }
}
