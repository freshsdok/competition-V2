package com.teaching.system.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.domain.ReviewTaskInfoReq;
import com.teaching.system.domain.vo.ExpertReviewInfo;
import com.teaching.system.service.IReviewTaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评审任务分配信息接口
 *
 * @author teaching
 * @date 2026-04-09
 */
@RestController
@RequestMapping("/reviewTaskInfo")
public class ReviewTaskInfoController extends BaseController {
    @Autowired
    private IReviewTaskInfoService reviewTaskInfoService;

    /**
     * 查询评审任务分配信息列表
     */
//    @RequiresPermissions("system:reviewTask:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewTaskInfo reviewTaskInfo) {
        startPage();
        List<ReviewTaskInfo> list = reviewTaskInfoService.selectReviewTaskInfoList(reviewTaskInfo);
        return getDataTable(list);
    }

    /**
     * 导出评审任务分配信息列表
     */
//    @RequiresPermissions("system:reviewTask:export")
//    @Log(title = "评审任务分配信息", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, ReviewTaskInfo reviewTaskInfo) {
//        List<ReviewTaskInfo> list = reviewTaskInfoService.selectReviewTaskInfoList(reviewTaskInfo);
//        ExcelUtil<ReviewTaskInfo> util = new ExcelUtil<ReviewTaskInfo>(ReviewTaskInfo.class);
//        util.exportExcel(response, list, "评审任务分配信息数据");
//    }

    /**
     * 获取评审任务分配信息详细信息
     */
//    @RequiresPermissions("system:reviewTask:query")
//    @GetMapping(value = "/{reviewId}")
//    public AjaxResult getInfo(@PathVariable("reviewId") Long reviewId) {
//        return success(reviewTaskInfoService.selectReviewTaskInfoByReviewId(reviewId));
//    }

    /**
     * 批量新增或更新评审任务分配信息
     */
//    @RequiresPermissions("system:reviewTask:add")
    @Log(title = "新增及修改评审任务分配信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveReviewTaskInfo")
    public AjaxResult batchInsertReviewTaskInfo(@RequestBody ReviewTaskInfoReq reviewTaskInfoReq) {
        return toAjax(reviewTaskInfoService.batchInsertReviewTaskInfo(reviewTaskInfoReq));
    }

    /**
     * 评审任务分配专家
     */
//    @RequiresPermissions("system:reviewTask:add")
    @Log(title = "评审任务分配专家", businessType = BusinessType.INSERT)
    @PostMapping("/saveSpecialistReviewTaskInfo")
    public AjaxResult batchInsertSpecialistReviewTaskInfo(@RequestBody Map<String, Object> param) {
        if (Objects.isNull(param.get("reviewIdList")) && Objects.isNull(param.get("userIdList"))) {
            return success();
        }
        List<Long> reviewIdList = Convert.toList(Long.class, param.get("reviewIdList"));
        List<Long> userIdList = Convert.toList(Long.class, param.get("userIdList"));
        return toAjax(reviewTaskInfoService.batchInsertSpecialistReviewTaskInfo(reviewIdList, userIdList));
    }

    /**
     * 评审任务分配分配专家组
     */
//    @RequiresPermissions("system:reviewTask:add")
    @Log(title = "评审任务分配分配专家组", businessType = BusinessType.INSERT)
    @PostMapping("/saveSpecialistGroupReviewTaskInfo")
    public AjaxResult saveSpecialistGroupReviewTaskInfo(@RequestBody Map<String, Object> param) {
        if (Objects.isNull(param.get("reviewIdList")) && Objects.isNull(param.get("groupIdList"))) {
            return success();
        }
        List<Long> reviewIdList = Convert.toList(Long.class, param.get("reviewIdList"));
        List<Long> groupIdList = Convert.toList(Long.class, param.get("groupIdList"));
        return toAjax(reviewTaskInfoService.saveSpecialistGroupReviewTaskInfo(reviewIdList, groupIdList));
    }

    /**
     * 修改评审任务分配信息
     */
//    @RequiresPermissions("system:reviewTask:edit")
//    @Log(title = "评审任务分配信息", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@RequestBody ReviewTaskInfo reviewTaskInfo) {
//        return toAjax(reviewTaskInfoService.updateReviewTaskInfo(reviewTaskInfo));
//    }

    /**
     * 删除评审任务分配信息
     */
//    @RequiresPermissions("system:reviewTask:remove")
//    @Log(title = "评审任务分配信息", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{reviewIds}")
//    public AjaxResult remove(@PathVariable Long[] reviewIds) {
//        return toAjax(reviewTaskInfoService.deleteReviewTaskInfoByReviewIds(reviewIds));
//    }

    /**
     * 获取专家评审列表
     *
     * @param param
     * @return
     */
    @GetMapping("/getExpertList")
    public TableDataInfo getExpertList(ExpertReviewInfo param) {
        startPage();
        String processedStr = param.getProcessedStr();
        if (StringUtils.isNotBlank(processedStr) && !NumberUtil.isNumber(processedStr)) {
            param.setProcessedStr(null);
        }
        List<ExpertReviewInfo> expertList = reviewTaskInfoService.getExpertList(param);
        return getDataTable(expertList);
    }

    /**
     * 获取评审任务信息 点击审阅
     *
     * @param processedId
     * @return
     */
    @GetMapping("/getTaskInfoByProcessedId")
    public AjaxResult getTaskInfoByProcessedId(Long processedId) {
        return success(reviewTaskInfoService.getTaskInfoByProcessedId(processedId));
    }
}
