package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.core.JsonUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionGradeInfoImportReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionGradeInfo;
import com.teaching.competition.domain.CompetitionGradeInfoImport;
import com.teaching.competition.service.ICompetitionGradeInfoService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 成绩Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/competitionGradeInfo")
public class CompetitionGradeInfoController extends BaseController {
    @Autowired
    private ICompetitionGradeInfoService competitionGradeInfoService;

    /**
     * 查询成绩列表
     */
    @RequiresPermissions("competition:competitionGradeInfo:list")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody CompetitionGradeInfo competitionGradeInfo) {
        startPage();
        List<CompetitionGradeInfo> list = competitionGradeInfoService.selectCompetitionGradeInfoList(competitionGradeInfo);
        return getDataTable(list);
    }

    /**
     * 导出成绩列表
     */
    @RequiresPermissions("competition:competitionGradeInfo:export")
    @Log(title = "成绩", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionGradeInfo competitionGradeInfo) {
        List<CompetitionGradeInfo> list = competitionGradeInfoService.selectCompetitionGradeInfoList(competitionGradeInfo);
        ExcelUtil<CompetitionGradeInfo> util = new ExcelUtil<CompetitionGradeInfo>(CompetitionGradeInfo.class);
        util.exportExcel(response, list, "成绩数据");
    }

    /**
     * 获取成绩详细信息
     */
    @RequiresPermissions("competition:competitionGradeInfo:query")
    @GetMapping("/{gradeId}")
    public AjaxResult getInfo(@PathVariable("gradeId") Long gradeId) {
        return success(competitionGradeInfoService.selectCompetitionGradeInfoById(gradeId));
    }

    /**
     * 导入成绩数据
     */
    @Log(title = "成绩", businessType = BusinessType.IMPORT)
    @RequiresPermissions("competition:competitionGradeInfo:import")
    @PostMapping("/importGradeInfo")
    public AjaxResult importData(@RequestPart("file")MultipartFile file,@RequestParam("reqJson") String jsonParams) throws Exception {
        CompetitionGradeInfoImportReq req = JsonUtils.parseObject(jsonParams, CompetitionGradeInfoImportReq.class);
        ExcelUtil<CompetitionGradeInfoImport> util = new ExcelUtil<>(CompetitionGradeInfoImport.class);
        List<CompetitionGradeInfoImport> gradeList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getLoginUser().getUsername();
        List<CompetitionGradeInfo> updateCompetitionGradeInfoList = competitionGradeInfoService.importGradeInfo(gradeList, false, operName,req);
        return success(updateCompetitionGradeInfoList);
    }

    // 导入后成绩更新
    @RequiresPermissions("competition:competitionGradeInfo:import")
    @PostMapping("/updateGradeInfo")
    public AjaxResult updateGradeInfo(@RequestBody List<CompetitionGradeInfo> gradeList) throws Exception {

        return success( competitionGradeInfoService.updateGradeInfo(gradeList));
    }
//    /**
//     * 获取导入模板
//     */
//    @PostMapping("/importTemplate")
//    public void importTemplate(HttpServletResponse response) throws Exception {
//        ExcelUtil<CompetitionGradeInfoImport> util = new ExcelUtil<CompetitionGradeInfoImport>(CompetitionGradeInfoImport.class);
//        util.importTemplateExcel(response, "成绩数据");
//    }

    /**
     * 新增成绩
     */
    @RequiresPermissions("competition:competitionGradeInfo:add")
    @Log(title = "成绩", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionGradeInfo competitionGradeInfo) {
        return toAjax(competitionGradeInfoService.insertCompetitionGradeInfo(competitionGradeInfo));
    }

    /**
     * 修改成绩
     */
    @RequiresPermissions("competition:competitionGradeInfo:edit")
    @Log(title = "成绩", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionGradeInfo")
    public AjaxResult edit(@RequestBody CompetitionGradeInfo competitionGradeInfo) {
        return toAjax(competitionGradeInfoService.updateCompetitionGradeInfo(competitionGradeInfo));
    }

    /**
     * 删除成绩
     */
    @RequiresPermissions("competition:competitionGradeInfo:remove")
    @Log(title = "成绩", businessType = BusinessType.DELETE)
    @GetMapping("/removeCompetitionGradeInfo/{gradeId}")
    public AjaxResult remove(@PathVariable Long gradeId) {
        return toAjax(competitionGradeInfoService.deleteCompetitionGradeInfoById(gradeId));
    }
}
