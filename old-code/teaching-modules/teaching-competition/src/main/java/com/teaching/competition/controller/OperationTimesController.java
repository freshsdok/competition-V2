package com.teaching.competition.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.OperationTimes;
import com.teaching.competition.domain.UserCompetitionApplyInfoDTO;
import com.teaching.competition.service.IOperationConfigService;
import com.teaching.competition.service.IOperationTimesService;
import com.teaching.system.api.domain.OperationConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 队伍操作次数Controller
 *
 * @author teaching
 * @date 2026-01-24
 */
@RestController
@RequestMapping("/times")
public class OperationTimesController extends BaseController {
    @Autowired
    private IOperationTimesService operationTimesService;
    @Autowired
    private IOperationConfigService operationConfigService;

    /**
     * 查询队伍操作次数列表
     */
    @RequiresPermissions("system:times:list")
    @GetMapping("/list")
    public TableDataInfo list(OperationTimes operationTimes) {
        startPage();
        List<OperationTimes> list = operationTimesService.selectOperationTimesList(operationTimes);
        return getDataTable(list);
    }

    /**
     * 导出队伍操作次数列表
     */
    @RequiresPermissions("system:times:export")
    @Log(title = "队伍操作次数", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperationTimes operationTimes) {
        List<OperationTimes> list = operationTimesService.selectOperationTimesList(operationTimes);
        ExcelUtil<OperationTimes> util = new ExcelUtil<OperationTimes>(OperationTimes.class);
        util.exportExcel(response, list, "队伍操作次数数据");
    }

    /**
     * 获取队伍操作次数详细信息
     */
    @RequiresPermissions("system:times:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(operationTimesService.selectOperationTimesById(id));
    }

    /**
     * 新增队伍操作次数
     */
    @RequiresPermissions("system:times:add")
    @Log(title = "队伍操作次数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OperationTimes operationTimes) {
        return toAjax(operationTimesService.insertOperationTimes(operationTimes));
    }

    /**
     * 修改队伍操作次数
     */
    @RequiresPermissions("system:times:edit")
    @Log(title = "队伍操作次数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OperationTimes operationTimes) {
        return toAjax(operationTimesService.updateOperationTimes(operationTimes));
    }

    /**
     * 取消队伍退费重缴操作次数
     */
    @InnerAuth
    @GetMapping("/cancelRepaymentOperationTimes/{teamCode}")
    public AjaxResult cancelRepaymentOperationTimes(@PathVariable String teamCode) {
        return toAjax(operationTimesService.cancelRepaymentOperationTimes(teamCode));
    }

    /**
     * 删除队伍操作次数
     */
    @RequiresPermissions("system:times:remove")
    @Log(title = "队伍操作次数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(operationTimesService.deleteOperationTimesByIds(ids));
    }

    /**
     * 校验退费重缴剩余次数
     *
     * @param competitionSeriesId 比赛code
     * @param teamCode            团队code
     * @return
     */
    @InnerAuth
    @GetMapping("/checkRepaymentTimes/{competitionSeriesId}/{teamCode}")
    public AjaxResult checkRepaymentTimes(@PathVariable Long competitionSeriesId, @PathVariable String teamCode) {
        UserCompetitionApplyInfoDTO applyInfoDTO = new UserCompetitionApplyInfoDTO();
        applyInfoDTO.setCompetitionSeriesId(competitionSeriesId);
        applyInfoDTO.setTeamCode(teamCode);
        return toAjax(operationTimesService.checkRepaymentTimes(applyInfoDTO, null));
    }

    /**
     * 记录退费重缴使用次数
     *
     * @param teamCode 团队code
     * @return
     */
    @InnerAuth
    @GetMapping("/recordUsedTimes/{competitionSeriesId}/{teamCode}")
    public AjaxResult recordUsedTimes(@PathVariable String teamCode, @PathVariable Long competitionSeriesId) {
        UserCompetitionApplyInfoDTO applyInfoDTO = new UserCompetitionApplyInfoDTO();
        applyInfoDTO.setTeamCode(teamCode);
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setCompetitionSeriesId(competitionSeriesId);
        operationConfig.setOperationType("3");
        List<OperationConfig> operationConfigs = operationConfigService.selectOperationConfigList(operationConfig);
        if(CollectionUtils.isEmpty(operationConfigs)){
            throw new RuntimeException("未找到退费重缴配置");
        }
        List<Map<String, String>> operationMap = new ArrayList<>();
        operationMap.add(Map.of("operationType", "repayment", "configId", operationConfigs.get(0).getId().toString()));
        return toAjax(operationTimesService.recordUsedTimes(applyInfoDTO, operationMap));
    }
}
