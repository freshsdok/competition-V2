package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CertificateImageCacheQuery;
import com.teaching.competition.service.CertificateImageCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competition/certificateImageSync")
public class CompetitionCertificateImageSyncController extends BaseController {
    private final CertificateImageCacheService cacheService;

    public CompetitionCertificateImageSyncController(CertificateImageCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @RequiresPermissions("competition:certificateImageSync:view")
    @GetMapping("/overview")
    public AjaxResult overview() {
        return success(cacheService.getStats());
    }

    @RequiresPermissions("competition:certificateImageSync:view")
    @GetMapping("/list")
    public TableDataInfo list(CertificateImageCacheQuery query) {
        startPage();
        return getDataTable(cacheService.selectAdminPage(query));
    }

    @RequiresPermissions("competition:certificateImageSync:view")
    @GetMapping("/current")
    public AjaxResult current() {
        return success(cacheService.getCurrentRun());
    }

    @RequiresPermissions("competition:certificateImageSync:view")
    @GetMapping("/history")
    public AjaxResult history() {
        return success(cacheService.getRunHistory());
    }

    @RequiresPermissions("competition:certificateImageSync:start")
    @Log(title = "证书图片同步", businessType = BusinessType.OTHER)
    @PostMapping("/start")
    public AjaxResult start() {
        return success(cacheService.startBatch(
                "MANUAL", SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("competition:certificateImageSync:pause")
    @Log(title = "暂停证书图片同步", businessType = BusinessType.UPDATE)
    @PostMapping("/pause")
    public AjaxResult pause() {
        return success(cacheService.pauseBatch());
    }

    @RequiresPermissions("competition:certificateImageSync:pause")
    @Log(title = "继续证书图片同步", businessType = BusinessType.UPDATE)
    @PostMapping("/resume")
    public AjaxResult resume() {
        return success(cacheService.resumeBatch());
    }

    @RequiresPermissions("competition:certificateImageSync:retry")
    @Log(title = "重试证书图片失败记录", businessType = BusinessType.UPDATE)
    @PostMapping("/retry")
    public AjaxResult retry() {
        return success(cacheService.retryFailedRecords());
    }

    @RequiresPermissions("competition:certificateImageSync:retry")
    @Log(title = "重置证书图片缓存", businessType = BusinessType.UPDATE)
    @PostMapping("/reset/{certCode}")
    public AjaxResult reset(@PathVariable String certCode) {
        return success(cacheService.resetCertificate(certCode));
    }
}
