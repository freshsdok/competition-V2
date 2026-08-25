package com.teaching.common.core.utils;

import com.github.pagehelper.PageHelper;
import com.teaching.common.core.utils.sql.SqlUtil;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableSupport;

import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 *
 * @author teaching
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }


    /**
     * 列表数据分页返回
     *
     * @param list     总数据
     * @param pageNum  当前页
     * @param pageSize 每页数量
     * @param <T>
     * @return
     */
    public static <T> List<T> paginate(List<T> list, int pageNum, int pageSize) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        int safePageNum = Math.max(1, pageNum);
        int safePageSize = Math.max(1, pageSize);
        long offset = (long) (safePageNum - 1) * safePageSize;
        if (offset >= list.size()) {
            return Collections.emptyList();
        }
        int fromIndex = (int) offset;
        int toIndex = (int) Math.min(offset + (long) safePageSize, list.size());
        // 使用subList方法获取分页数据
        return list.subList(fromIndex, toIndex);
    }
}
