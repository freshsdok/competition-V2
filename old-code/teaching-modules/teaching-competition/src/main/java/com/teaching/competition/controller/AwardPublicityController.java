package com.teaching.competition.controller;

import com.teaching.common.core.exception.ExcelImportRequiredException;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPublicity;
import com.teaching.competition.service.IAwardPublicityService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 获奖公示管理Controller
 *
 * @author teaching
 * @date 2026-05-12
 */
@RestController
@RequestMapping("/publicity")
public class AwardPublicityController extends BaseController {
    @Autowired
    private IAwardPublicityService awardPublicityService;

    /**
     * 查询获奖公示管理列表
     */
//    @RequiresPermissions("competition:publicity:list")
    @GetMapping("/list")
    public TableDataInfo list(AwardPublicity awardPublicity) {
        startPage();
        List<AwardPublicity> list = awardPublicityService.selectAwardPublicityList(awardPublicity);
        return getDataTable(list);
    }


    /**
     * 导入/重导获奖公示数据
     *
     * @param file                要导入的数据 都必传
     * @param importType          导入类型 都必传 addition:追加导入  replace:重导
     * @param competitionSeriesId 赛事系列id 新建导入时必传，重导时可不传
     * @param competitionName     赛事名称  新建导入时必传，重导时可不传
     * @param awardPublicityId    公示管理记录id  新建导入时不传,重导时必传
     * @return
     * @throws Exception
     */
    @Log(title = "获奖公示管理", businessType = BusinessType.IMPORT)
//    @RequiresPermissions("competition:publicity:import")
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, @RequestParam("importType") String importType
            , @RequestParam(value = "competitionSeriesId", required = false) Long competitionSeriesId, @RequestParam(value = "competitionName", required = false) String competitionName
            , @RequestParam(value = "awardPublicityId", required = false) Long awardPublicityId) throws Exception {
        //校验参数
        if (file == null || file.isEmpty()) {
            throw new GlobalException("导入数据文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename) || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new GlobalException("仅支持xlsx或xls格式文件");
        }
        if (StringUtils.isBlank(importType)) {
            throw new GlobalException("导入类型不能为空");
        }
        if (!"addition".equals(importType) && !"replace".equals(importType)) {
            throw new GlobalException("导入类型只能是追加(addition)或替换(replace)");
        }
        if (Objects.isNull(awardPublicityId) && (Objects.isNull(competitionSeriesId) || Objects.isNull(competitionName))) {
            throw new GlobalException("新建导入时，赛事系列id和赛事名称不能为空");
        }
        ExcelUtil<AwardDetails> util = new ExcelUtil<AwardDetails>(AwardDetails.class);
        List<AwardDetails> list;
        try {
            list = util.importExcel(file.getInputStream());
        } catch (ExcelImportRequiredException e) {
            return success(Map.of("msg", e.getMessage(), "importSuccess", false));
        } catch (Exception e) {
            return success(Map.of("msg", "导入数据失败,请检查模板或导入的文件内容", "importSuccess", false));
        }
        return success(awardPublicityService.importData(list, importType, competitionSeriesId, competitionName, awardPublicityId));
    }

    /**
     * 导出获奖公示管理列表
     */
    @RequiresPermissions("competition:publicity:export")
    @Log(title = "获奖公示管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AwardPublicity awardPublicity) {
        List<AwardPublicity> list = awardPublicityService.selectAwardPublicityList(awardPublicity);
        ExcelUtil<AwardPublicity> util = new ExcelUtil<AwardPublicity>(AwardPublicity.class);
        util.exportExcel(response, list, "获奖公示管理数据");
    }

    /**
     * 获取获奖公示管理详细信息
     */
//    @RequiresPermissions("competition:publicity:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(awardPublicityService.selectAwardPublicityById(id));
    }

    /**
     * 新增获奖公示管理
     */
    @RequiresPermissions("competition:publicity:add")
    @Log(title = "获奖公示管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AwardPublicity awardPublicity) {
        return toAjax(awardPublicityService.insertAwardPublicity(awardPublicity));
    }

    /**
     * 修改获奖公示管理
     */
//    @RequiresPermissions("competition:publicity:edit")
    @Log(title = "获奖公示管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AwardPublicity awardPublicity) {
        return toAjax(awardPublicityService.updateAwardPublicity(awardPublicity));
    }

    /**
     * 修改获奖公示管理 仅修改提示信息
     * 提示语是富文本，gateway需设置xss.excludeUrls
     */
//    @RequiresPermissions("competition:publicity:edit")
    @Log(title = "获奖公示管理", businessType = BusinessType.UPDATE)
    @PutMapping("/tipInfo")
    public AjaxResult updateAwardPublicityTipInfo(@RequestBody AwardPublicity awardPublicity) {
        return toAjax(awardPublicityService.updateAwardPublicityTipInfo(awardPublicity));
    }

    /**
     * 删除获奖公示管理
     */
//    @RequiresPermissions("competition:publicity:remove")
    @Log(title = "获奖公示管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(awardPublicityService.deleteAwardPublicityByIds(ids));
    }
}
