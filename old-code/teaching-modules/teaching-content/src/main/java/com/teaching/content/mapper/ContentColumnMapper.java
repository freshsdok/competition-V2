package com.teaching.content.mapper;

import com.teaching.content.domain.ContentColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容栏目Mapper接口
 *
 * @author teaching
 * @date 2025-11-21
 */
@Mapper
public interface ContentColumnMapper {
    /**
     * 查询内容栏目
     *
     * @param columnId 内容栏目主键
     * @return 内容栏目
     */
    public ContentColumn selectContentColumnByColumnId(Long columnId);

    /**
     * 查询内容栏目列表
     *
     * @param contentColumn 内容栏目
     * @return 内容栏目集合
     */
    public List<ContentColumn> selectContentColumnList(ContentColumn contentColumn);

    /**
     * 根据菜单ID查询栏目
     *
     * @param menuId 菜单ID
     * @return 内容栏目
     */
    public ContentColumn selectContentColumnByMenuId(Long menuId);

    /**
     * 检查是否存在相同类型的栏目
     *
     * @param menuId 菜单ID
     * @param columnType 栏目类型
     * @return 存在返回1，不存在返回0
     */
    public Integer checkContentColumnByMenuId(@Param("menuId") Long menuId,@Param("columnType") String columnType);

    /**
     * 根据父ID查询子栏目列表
     *
     * @param parentId 父栏目ID
     * @return 内容栏目集合
     */
    public List<ContentColumn> selectContentColumnListByParentId(Long parentId);

    /**
     * 查询栏目树形结构
     *
     * @param contentColumn 内容栏目
     * @return 内容栏目集合
     */
    public List<ContentColumn> selectContentColumnTree(ContentColumn contentColumn);

    /**
     * 新增内容栏目
     *
     * @param contentColumn 内容栏目
     * @return 结果
     */
    public int insertContentColumn(ContentColumn contentColumn);

    /**
     * 修改内容栏目
     *
     * @param contentColumn 内容栏目
     * @return 结果
     */
    public int updateContentColumn(ContentColumn contentColumn);

    /**
     * 删除内容栏目
     *
     * @param columnId 内容栏目主键
     * @return 结果
     */
    public int deleteContentColumnByColumnId(Long columnId);

    /**
     * 批量删除内容栏目
     *
     * @param columnIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteContentColumnByColumnIds(Long[] columnIds);

    /**
     * 检查是否存在子栏目
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    public int hasChildByColumnId(Long columnId);
}


