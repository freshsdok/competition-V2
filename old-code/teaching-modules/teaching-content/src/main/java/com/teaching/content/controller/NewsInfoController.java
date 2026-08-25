package com.teaching.content.controller;

import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.content.domain.NewsInfo;
import com.teaching.content.domain.query.PublicNewsQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;
import com.teaching.content.service.INewsInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资讯信息Controller
 *
 * @author teaching
 * @date 2025-10-27
 */
@RestController
@RequestMapping("/newsInfo")
public class NewsInfoController extends BaseController {
    @Autowired
    private INewsInfoService newsInfoService;

    /**
     * 查询资讯信息列表
     */
    @RequiresPermissions("content:newsInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(NewsInfo newsInfo) {
        if (newsInfo.getParams() == null) {
            newsInfo.setParams(new java.util.HashMap<>());
        }
        newsInfo.getParams().put("orderScene", "createOnly");
        startPage();
        List<NewsInfo> list = newsInfoService.selectNewsInfoList(newsInfo);
        return getDataTable(list);
    }

    /**
     * 查询资讯信息列表（分页）
     */
    @GetMapping("/getList")
    public TableDataInfo getList(PublicNewsQuery query) {
        startPage();
        List<PublicNewsInfo> list = newsInfoService.selectPublicNewsInfoList(query);
        return getDataTable(list);
    }

    /**
     * 导出资讯信息列表
     */
    @RequiresPermissions("content:newsInfo:export")
    @Log(title = "资讯信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NewsInfo newsInfo) {
        List<NewsInfo> list = newsInfoService.selectNewsInfoList(newsInfo);
        ExcelUtil<NewsInfo> util = new ExcelUtil<NewsInfo>(NewsInfo.class);
        util.exportExcel(response, list, "资讯信息数据");
    }

    /**
     * 获取资讯信息详细信息
     */
    //@RequiresPermissions("content:newsInfo:query")
    @GetMapping(value = "/{newsId}")
    public AjaxResult getInfo(@PathVariable("newsId") Long newsId) {
        return success(newsInfoService.selectNewsInfoByNewsId(newsId));
    }

    /**
     * 公开获取资讯信息详细信息（无需权限验证）
     * 每次调用此接口会自动增加阅读量
     */
    @GetMapping(value = "/public/{newsId}")
    public AjaxResult getPublicInfo(@PathVariable("newsId") Long newsId) {
        PublicNewsInfo newsInfo = newsInfoService.selectPublicNewsInfoByNewsId(newsId);
        if (newsInfo == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "内容不存在或不可访问");
        }
        int updated = newsInfoService.increaseReadingQuantity(newsId);
        if (updated > 0 && newsInfo.getReadingQuantity() != null) {
            newsInfo.setReadingQuantity(newsInfo.getReadingQuantity() + 1);
        }
        return success(newsInfo);
    }

    /**
     * 新增资讯信息
     */
    @RequiresPermissions("content:newsInfo:add")
    @Log(title = "资讯信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NewsInfo newsInfo) {
        return toAjax(newsInfoService.insertNewsInfo(newsInfo));
    }

    /**
     * 修改资讯信息
     * 状态为"审核通过"、"已发布"、"审核中"时不允许修改
     */
    @RequiresPermissions("content:newsInfo:edit")
    @Log(title = "资讯信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NewsInfo newsInfo) {
        // 检查资讯状态，状态为"审核通过"、"已发布"、"审核中"时不允许修改
        if (newsInfo.getNewsId() != null) {
            NewsInfo currentNews = newsInfoService.selectNewsInfoByNewsId(newsInfo.getNewsId());
            if (currentNews != null) {
                String status = currentNews.getNewsStatus();
                if ("审核通过".equals(status) || "已发布".equals(status) || "审核中".equals(status)) {
                    return error("资讯【" + currentNews.getNewsTitle() + "】状态为【" + status + "】，不允许修改");
                }
            }
        }
        return toAjax(newsInfoService.updateNewsInfo(newsInfo));
    }

    /**
     * 删除资讯信息
     * 状态为"审核中"、"已发布"时不允许删除
     */
    @RequiresPermissions("content:newsInfo:remove")
    @Log(title = "资讯信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{newsIds}")
    public AjaxResult remove(@PathVariable Long[] newsIds) {
        // 检查每个资讯的状态，状态为"审核中"、"已发布"时不允许删除
        for (Long newsId : newsIds) {
            NewsInfo newsInfo = newsInfoService.selectNewsInfoByNewsId(newsId);
            if (newsInfo != null) {
                String status = newsInfo.getNewsStatus();
                if ("审核中".equals(status) || "已发布".equals(status)) {
                    return error("资讯【" + newsInfo.getNewsTitle() + "】状态为【" + status + "】，不允许删除");
                }
            }
        }
        return toAjax(newsInfoService.deleteNewsInfoByNewsIds(newsIds));
    }

    /**
     * 发布资讯
     */
    @RequiresPermissions("content:newsInfo:publish")
    @Log(title = "发布资讯", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{newsId}")
    public AjaxResult publish(@PathVariable Long newsId) {
        return toAjax(newsInfoService.publishNews(newsId));
    }

    /**
     * 下架资讯
     */
    @RequiresPermissions("content:newsInfo:offline")
    @Log(title = "下架资讯", businessType = BusinessType.UPDATE)
    @PutMapping("/offline/{newsId}")
    public AjaxResult offline(@PathVariable Long newsId) {
        return toAjax(newsInfoService.offlineNews(newsId));
    }

    /**
     * 提交审核
     * 只有状态为"审核中"时才能提交审核
     */
    @RequiresPermissions("info:task:submit")
    @Log(title = "提交审核", businessType = BusinessType.UPDATE)
    @PutMapping("/submitAudit/{newsId}")
    public AjaxResult submitAudit(@PathVariable Long newsId) {
        // 检查资讯状态，只有"审核中"状态才能提交审核
        NewsInfo newsInfo = newsInfoService.selectNewsInfoByNewsId(newsId);
        if (newsInfo != null) {
            String status = newsInfo.getNewsStatus();
            if (!"审核中".equals(status)) {
                return error("资讯【" + newsInfo.getNewsTitle() + "】状态为【" + status + "】，只有【审核中】状态的资讯才能提交审核");
            }
        }
        return toAjax(newsInfoService.submitAudit(newsId));
    }

    /**
     * 增加阅读量
     */
    @GetMapping("/increaseReading/{newsId}")
    public AjaxResult increaseReading(@PathVariable Long newsId) {
        return toAjax(newsInfoService.increaseReadingQuantity(newsId));
    }

    /**
     * 增加点赞数
     */
    @PostMapping("/increaseLikes/{newsId}")
    public AjaxResult increaseLikes(@PathVariable Long newsId) {
        return toAjax(newsInfoService.increaseLikesNum(newsId));
    }

    /**
     * 跨服务调用：查看资讯详情
     */
    @InnerAuth
    @GetMapping("/inner/detail/{newsId}")
    public AjaxResult getNewsDetail(@PathVariable Long newsId) {
        return success(newsInfoService.selectNewsInfoByNewsId(newsId));
    }

    /**
     * 跨服务调用：修改资讯审核状态
     */
    @RequiresPermissions("info:task:audit")
    @InnerAuth
    @PostMapping("/inner/updateAuditStatus")
    public AjaxResult updateAuditStatus(@RequestBody NewsInfo newsInfo) {
        return success(newsInfoService.updateNewsAuditStatus(newsInfo));
    }
}
