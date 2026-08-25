package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionCertificateQueryRequest;
import com.teaching.competition.domain.CertificateExportRequest;
import com.teaching.competition.domain.CertificateFallbackRequest;
import com.teaching.competition.domain.GuidedCertificateQuery;
import com.teaching.competition.service.CertificateExportService;
import com.teaching.competition.service.CertificateImageCacheService;
import com.teaching.competition.service.ICertificateDownloadService;
import com.teaching.system.api.domain.UserCertificate;
import com.teaching.competition.service.IUserCertificateService;
import com.teaching.system.api.domain.CompetitionCertExchangeApply;
import com.teaching.competition.domain.CompetitionCertExchangeRuleUserApply;
import com.teaching.competition.service.ICompetitionCertExchangeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 赛证互通用户端访问接口
 *
 * @author teaching
 */
@RestController
@RequestMapping("/user/competitionCertExchangeRule")
public class CompetitionCertExchangeUserController extends BaseController {

    @Autowired
    private ICompetitionCertExchangeUserService competitionCertExchangeUserService;

    @Autowired
    private IUserCertificateService userCertificateService;

    @Autowired
    private ICertificateDownloadService certificateDownloadService;

    @Autowired
    private CertificateImageCacheService certificateImageCacheService;

    @Autowired
    private CertificateExportService certificateExportService;

    /**
     * 赛证互通申请明细接口
     * @param rulerId
     * @return
     */
    @GetMapping("/queryUserCertExchangeApplyDetail/{rulerId}")
    public AjaxResult queryUserCertExchangeApplyDetail(@PathVariable Long rulerId) throws Exception {
        return success(competitionCertExchangeUserService.queryUserCertExchangeApplyDetail(rulerId));
    }

    // 无权限认证条用接口
    @GetMapping("/queryUserCertExchangeApplyDetailNoAuth/{rulerId}")
    public AjaxResult queryUserCertExchangeApplyDetailNoAuth(@PathVariable Long rulerId) {
        return success(competitionCertExchangeUserService.queryUserCertExchangeApplyDetailNoAuth(rulerId));
    }

    /**
     * 赛证互通申请计算下单金额
     *
     */
    @PostMapping("/queryUserCertExchangeApplyDetail")
    public AjaxResult queryUserCertExchangeApply(@RequestBody CompetitionCertExchangeRuleUserApply apply) {
        return success(competitionCertExchangeUserService.queryUserCertExchangeApply(apply));
    }

    /**
     * 赛证互通申请接口包含下单
     *
     */
    @PostMapping("/saveUserCertExchangeApply")
    public AjaxResult saveUserCertExchangeApply(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        return competitionCertExchangeUserService.saveUserCertExchangeApply(competitionCertExchangeApply);
    }

    /**
     * saveUserCertExchangeApply 前check
     * @param competitionCertExchangeApply
     * @return
     */
    @PostMapping("/saveUserCertExchangeApplyCheck")
    public AjaxResult saveUserCertExchangeApplyCheck(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        return success(competitionCertExchangeUserService.saveUserCertExchangeApplyBeforeCheck(competitionCertExchangeApply));
    }

    // 赛证互通申请接口包含下单支付成功后回调修改状态及订单id
    @InnerAuth
    @PostMapping("/updateUserCertExchangeApply")
    public AjaxResult updateUserCertExchangeApply(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        return success(competitionCertExchangeUserService.updateUserCertExchangeApply(competitionCertExchangeApply));
    }

    // 修改申请发票状态
    @InnerAuth
    @PostMapping("/updateUserCertExchangeApplyInvoiceStatus")
    public AjaxResult updateUserCertExchangeApplyInvoiceStatus(@RequestBody List<CompetitionCertExchangeApply> competitionCertExchangeApplyList) {
        return success(competitionCertExchangeUserService.updateCompetitionCertExchangeApplyInvoiceStatus(competitionCertExchangeApplyList));
    }

    /**
     * 查询用户证书列表
     */
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody UserCertificate userCertificate) throws Exception {
        startPage();
        List<UserCertificate> list = competitionCertExchangeUserService.selectUserCertificateByUserId(userCertificate);
        return getDataTable(list);
    }

    /**
     * 大赛查询证书列表
     */
    @PostMapping("/getCompetitionCertificateList")
    public AjaxResult getCompetitionCertificateList(
            @RequestBody CompetitionCertificateQueryRequest queryRequest) {
        List<UserCertificate> list = userCertificateService.selectCompetitionCertificateList(queryRequest);
        return success(list);
    }

    /**
     * 查询当前团队报名负责人可打包的学生证书数量。
     */
    @GetMapping("/certificate/guidedSummary")
    public AjaxResult getGuidedCertificateSummary() {
        return success(certificateDownloadService.getGuidedCertificateSummary(currentUserId()));
    }

    /**
     * 从证书查询平台换取图片地址，交由前端生成ZIP。
     */
    @PostMapping("/certificate/guidedPackage")
    public AjaxResult getGuidedCertificatePictures() {
        return error("该接口已废弃，请使用负责人证书分页和异步导出接口");
    }

    /** 服务端分页、全局筛选负责人名下证书。 */
    @PostMapping("/certificate/guidedPage")
    public TableDataInfo getGuidedCertificatePage(@RequestBody(required = false) GuidedCertificateQuery query) {
        startPage();
        return getDataTable(certificateImageCacheService.selectGuidedPage(currentUserId(), query));
    }

    @GetMapping("/certificate/guidedFilterOptions")
    public AjaxResult getGuidedCertificateFilterOptions() {
        return success(certificateImageCacheService.selectGuidedFilterOptions(currentUserId()));
    }

    /** 当前页缺图异步兜底；立即返回入队状态。 */
    @PostMapping("/certificate/fallback")
    public AjaxResult fallbackCertificatePictures(@RequestBody CertificateFallbackRequest request) {
        return success(certificateImageCacheService.enqueueFallback(
                currentUserId(), request == null ? null : request.getCertCodes()));
    }

    /** 授权后为单张缓存图片生成短时预览地址。 */
    @GetMapping("/certificate/preview/{certCode}")
    public AjaxResult previewCertificatePicture(@PathVariable String certCode) {
        return success(certificateImageCacheService.getPreviewUrl(currentUserId(), certCode));
    }

    @PostMapping("/certificate/exportTask")
    public AjaxResult createCertificateExportTask(@RequestBody CertificateExportRequest request) {
        return success(certificateExportService.createTask(currentUserId(), request));
    }

    @GetMapping("/certificate/exportTask/{taskId}")
    public AjaxResult getCertificateExportTask(@PathVariable String taskId) {
        return success(certificateExportService.getTask(currentUserId(), taskId));
    }

    @GetMapping("/certificate/exportTask/{taskId}/download")
    public AjaxResult downloadCertificateExportTask(@PathVariable String taskId) {
        return success(certificateExportService.getDownload(currentUserId(), taskId));
    }

    private Long currentUserId() {
        if (SecurityUtils.getLoginUser() == null
                || SecurityUtils.getLoginUser().getSysUser() == null) {
            return null;
        }
        return SecurityUtils.getLoginUser().getSysUser().getUserId();
    }
}
