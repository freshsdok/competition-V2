package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.Questions;
import com.teaching.content.service.IQuestionsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 常见问题Controller
 *
 * @author teaching
 * @date 2025-11-06
 */
@RestController
@RequestMapping("/questions")
public class QuestionsController extends BaseController {
    @Autowired
    private IQuestionsService questionsService;

    /**
     * 查询常见问题列表
     */
    @RequiresPermissions("content:questions:list")
    @GetMapping("/list")
    public TableDataInfo list(Questions questions) {
        startPage();
        List<Questions> list = questionsService.selectQuestionsList(questions);
        return getDataTable(list);
    }

    @GetMapping("/pc/list")
    public TableDataInfo pcList(Questions questions) {
        startPage();
        List<Questions> list = questionsService.selectQuestionsList(questions);
        return getDataTable(list);
    }

    /**
     * 导出常见问题列表
     */
    @RequiresPermissions("content:questions:export")
    @Log(title = "常见问题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Questions questions) {
        List<Questions> list = questionsService.selectQuestionsList(questions);
        ExcelUtil<Questions> util = new ExcelUtil<Questions>(Questions.class);
        util.exportExcel(response, list, "常见问题数据");
    }

    /**
     * 获取常见问题详细信息
     */
    @RequiresPermissions("content:questions:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(questionsService.selectQuestionsById(id));
    }

    /**
     * 新增常见问题
     */
    @RequiresPermissions("content:questions:add")
    @Log(title = "常见问题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Questions questions) {
        return toAjax(questionsService.insertQuestions(questions));
    }

    /**
     * 修改常见问题
     */
    @RequiresPermissions("content:questions:edit")
    @Log(title = "常见问题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Questions questions) {
        questions.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(questionsService.updateQuestions(questions));
    }

    /**
     * 删除常见问题
     */
    @RequiresPermissions("content:questions:remove")
    @Log(title = "常见问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(questionsService.deleteQuestionsByIds(ids));
    }
}
