package com.teaching.content.controller;

import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableSupport;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.content.domain.NoticeInfo;
import com.teaching.content.domain.query.PublicNoticeQuery;
import com.teaching.content.domain.vo.PublicNoticeInfo;
import com.teaching.content.service.INoticeInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告信息Controller
 *
 * @author teaching
 * @date 2025-10-27
 */
@RestController
@RequestMapping("/noticeInfo")
public class NoticeInfoController extends BaseController {
    @Autowired
    private INoticeInfoService noticeInfoService;

    /**
     * 查询通知公告信息列表
     */
    @RequiresPermissions("content:noticeInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(NoticeInfo noticeInfo, @RequestParam(required = false) Boolean rule) {
        if (noticeInfo.getParams() == null) {
            noticeInfo.setParams(new java.util.HashMap<>());
        }
        if (rule != null) {
            noticeInfo.getParams().put("rule", String.valueOf(rule));
        }
        Object ruleParam = noticeInfo.getParams().get("rule");
        boolean ruleEnabled = ruleParam != null && "true".equalsIgnoreCase(String.valueOf(ruleParam));
        if (!ruleEnabled) {
            noticeInfo.getParams().put("orderScene", "createOnly");
        }
        startPage();
        List<NoticeInfo> list = noticeInfoService.selectNoticeInfoList(noticeInfo);
        if (!ruleEnabled) {
            return getDataTable(list);
        }
        clearPage();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        List<NoticeInfo> pageList = PageUtils.paginate(list, pageDomain.getPageNum(), pageDomain.getPageSize());
        TableDataInfo rspData = new TableDataInfo(pageList, list != null ? list.size() : 0);
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        return rspData;
    }

    /**
     * 查询通知公告信息列表（分页）
     */
    //@RequiresPermissions("content:noticeInfo:list")
    @GetMapping("/getList")
    public TableDataInfo getList(PublicNoticeQuery query) {
        List<PublicNoticeInfo> list = noticeInfoService.selectPublicNoticeInfoList(query);
        PageDomain pageDomain = TableSupport.buildPageRequest();
        List<PublicNoticeInfo> pageList = PageUtils.paginate(
                list,
                pageDomain.getPageNum(),
                pageDomain.getPageSize());
        TableDataInfo response = new TableDataInfo(pageList, list != null ? list.size() : 0);
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询成功");
        return response;
    }

    /**
     * 导出通知公告信息列表
     */
    @RequiresPermissions("content:noticeInfo:export")
    @Log(title = "通知公告信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NoticeInfo noticeInfo) {
        List<NoticeInfo> list = noticeInfoService.selectNoticeInfoList(noticeInfo);
        ExcelUtil<NoticeInfo> util = new ExcelUtil<NoticeInfo>(NoticeInfo.class);
        util.exportExcel(response, list, "通知公告信息数据");
    }

    /**
     * 获取通知公告信息详细信息
     */
    @RequiresPermissions("content:noticeInfo:query")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return success(noticeInfoService.selectNoticeInfoByNoticeId(noticeId));
    }

    /**
     * 公开获取通知公告信息详细信息（无需权限验证）
     */
    @GetMapping(value = "/public/{noticeId}")
    public AjaxResult getPublicInfo(@PathVariable("noticeId") Long noticeId) {
        PublicNoticeInfo noticeInfo = noticeInfoService.selectPublicNoticeInfoByNoticeId(noticeId);
        if (noticeInfo == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "内容不存在或不可访问");
        }
        return success(noticeInfo);
    }

    /**
     * 新增通知公告信息
     */
    @RequiresPermissions("content:noticeInfo:add")
    @Log(title = "通知公告信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NoticeInfo noticeInfo) {
        return toAjax(noticeInfoService.insertNoticeInfo(noticeInfo));
    }

    /**
     * 修改通知公告信息
     * 状态为"审核通过"、"已发布"、"审核中"时不允许修改
     */
    @RequiresPermissions("content:noticeInfo:edit")
    @Log(title = "通知公告信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NoticeInfo noticeInfo) {
        // 检查通知公告状态，状态为"审核通过"、"已发布"、"审核中"时不允许修改
        if (noticeInfo.getNoticeId() != null) {
            NoticeInfo currentNotice = noticeInfoService.selectNoticeInfoByNoticeId(noticeInfo.getNoticeId());
            if (currentNotice != null) {
                String status = currentNotice.getNoticeStatus();
                if ("审核通过".equals(status) || "已发布".equals(status) || "审核中".equals(status)) {
                    return error("通知公告【" + currentNotice.getNoticeTitle() + "】状态为【" + status + "】，不允许修改");
                }
            }
        }
        return toAjax(noticeInfoService.updateNoticeInfo(noticeInfo));
    }

    /**
     * 删除通知公告信息
     * 状态为"审核通过"、"已发布"、"审核中"时不允许删除
     */
    @RequiresPermissions("content:noticeInfo:remove")
    @Log(title = "通知公告信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        // 检查每个通知公告的状态，状态为"审核通过"、"已发布"、"审核中"时不允许删除
        for (Long noticeId : noticeIds) {
            NoticeInfo noticeInfo = noticeInfoService.selectNoticeInfoByNoticeId(noticeId);
            if (noticeInfo != null) {
                String status = noticeInfo.getNoticeStatus();
                if ("审核通过".equals(status) || "已发布".equals(status) || "审核中".equals(status)) {
                    return error("通知公告【" + noticeInfo.getNoticeTitle() + "】状态为【" + status + "】，不允许删除");
                }
            }
        }
        return toAjax(noticeInfoService.deleteNoticeInfoByNoticeIds(noticeIds));
    }

    /**
     * 发布通知公告
     */
    @RequiresPermissions("content:noticeInfo:publish")
    @Log(title = "发布通知公告", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{noticeId}")
    public AjaxResult publish(@PathVariable Long noticeId) {
        return toAjax(noticeInfoService.publishNotice(noticeId));
    }

    /**
     * 下架通知公告
     */
    @RequiresPermissions("content:noticeInfo:offline")
    @Log(title = "下架通知公告", businessType = BusinessType.UPDATE)
    @PutMapping("/offline/{noticeId}")
    public AjaxResult offline(@PathVariable Long noticeId) {
        return toAjax(noticeInfoService.offlineNotice(noticeId));
    }

    /**
     * 提交审核
     * 只有状态为"审核中"时才能提交审核
     */
    @RequiresPermissions("notice:tsak:submit")
    @Log(title = "提交审核", businessType = BusinessType.UPDATE)
    @PutMapping("/submitAudit/{noticeId}")
    public AjaxResult submitAudit(@PathVariable Long noticeId) {
        // 检查通知公告状态，只有"审核中"状态才能提交审核
        NoticeInfo noticeInfo = noticeInfoService.selectNoticeInfoByNoticeId(noticeId);
        if (noticeInfo != null) {
            String status = noticeInfo.getNoticeStatus();
            if (!"审核中".equals(status)) {
                return error("通知公告【" + noticeInfo.getNoticeTitle() + "】状态为【" + status + "】，只有【审核中】状态的通知公告才能提交审核");
            }
        }
        return toAjax(noticeInfoService.submitAudit(noticeId));
    }

    /**
     * 跨服务调用：查看通知公告详情
     */
    @InnerAuth
    @GetMapping("/inner/detail/{noticeId}")
    public AjaxResult getNoticeDetail(@PathVariable Long noticeId) {
        return success(noticeInfoService.selectNoticeInfoByNoticeId(noticeId));
    }

    /**
     * 跨服务调用：修改通知公告审核状态
     */
    @RequiresPermissions("notice:task:audit")
    @InnerAuth
    @PostMapping("/inner/updateAuditStatus")
    public AjaxResult updateAuditStatus(@RequestBody NoticeInfo noticeInfo) {
        return success(noticeInfoService.updateNoticeAuditStatus(noticeInfo));
    }
}
