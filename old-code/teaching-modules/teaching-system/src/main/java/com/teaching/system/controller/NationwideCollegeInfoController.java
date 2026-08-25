package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.service.INationwideCollegeInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全国院校信息Controller
 *
 * @author teaching
 * @date 2025-12-03
 */
@RestController
@RequestMapping("/school")
public class NationwideCollegeInfoController extends BaseController {
    @Autowired
    private INationwideCollegeInfoService nationwideCollegeInfoService;

    /**
     * 查询全国院校信息列表
     */
    @RequiresPermissions("system:school:list")
    @GetMapping("/list")
    public TableDataInfo list(NationwideCollegeInfo nationwideCollegeInfo) {
        startPage();
        List<NationwideCollegeInfo> list = nationwideCollegeInfoService.selectNationwideCollegeInfoList(nationwideCollegeInfo);
        return getDataTable(list);
    }

    /**
     * 导出全国院校信息列表
     */
    @RequiresPermissions("system:school:export")
    @Log(title = "全国院校信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NationwideCollegeInfo nationwideCollegeInfo) {
        List<NationwideCollegeInfo> list = nationwideCollegeInfoService.selectNationwideCollegeInfoList(nationwideCollegeInfo);
        ExcelUtil<NationwideCollegeInfo> util = new ExcelUtil<NationwideCollegeInfo>(NationwideCollegeInfo.class);
        util.exportExcel(response, list, "全国院校信息数据");
    }

    /**
     * 获取全国院校信息详细信息
     */
    @GetMapping(value = "/pc/list")
    public AjaxResult getNationwideCollegeInfoByNameLimit10(NationwideCollegeInfo nationwideCollegeInfo) {
        return success(nationwideCollegeInfoService.getNationwideCollegeInfoByNameLimit10(nationwideCollegeInfo));
    }

    /**
     * 获取全国院校信息详细信息
     */
    @RequiresPermissions("system:school:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(nationwideCollegeInfoService.selectNationwideCollegeInfoById(id));
    }

    @InnerAuth
    @GetMapping(value = "/getNationwideCollegeInfoInfo/{id}")
    public AjaxResult getNationwideCollegeInfoInfo(@PathVariable("id") String id) {
        return success(nationwideCollegeInfoService.selectNationwideCollegeInfoById(id));
    }


    @InnerAuth
    @GetMapping(value = "/getNationwideCollegeInfoInfoByName/{name}")
    public AjaxResult getNationwideCollegeInfoInfoByName(@PathVariable("name") String name) {
        return success(nationwideCollegeInfoService.selectNationwideCollegeInfoByName(name));
    }

    /**
     * 获取全国院校省份信息
     */
    @GetMapping(value = "/selectAllProvince")
    public AjaxResult selectAllNationwideCollegeProvince() {
        return success(nationwideCollegeInfoService.selectAllNationwideCollegeProvince());
    }

    /**
     * 新增全国院校信息
     */
    @RequiresPermissions("system:school:add")
    @Log(title = "全国院校信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NationwideCollegeInfo nationwideCollegeInfo) {
        return toAjax(nationwideCollegeInfoService.insertNationwideCollegeInfo(nationwideCollegeInfo));
    }

    /**
     * 修改全国院校信息
     */
    @RequiresPermissions("system:school:edit")
    @Log(title = "全国院校信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NationwideCollegeInfo nationwideCollegeInfo) {
        return toAjax(nationwideCollegeInfoService.updateNationwideCollegeInfo(nationwideCollegeInfo));
    }

    /**
     * 删除全国院校信息
     */
    @RequiresPermissions("system:school:remove")
    @Log(title = "全国院校信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(nationwideCollegeInfoService.deleteNationwideCollegeInfoByIds(ids));
    }

    /**
     * 获取双一流院校信息
     *
     * @param nationwideCollegeInfo
     * @return
     */
    @InnerAuth
    @GetMapping("/getDoubleFirstClassUniversityPlan")
    public AjaxResult getDoubleFirstClassUniversityPlan(NationwideCollegeInfo nationwideCollegeInfo) {
        nationwideCollegeInfo.setDoubleFirstClassUniversityPlan("是");
        return success(nationwideCollegeInfoService.selectNationwideCollegeInfoList(nationwideCollegeInfo));
    }
}
