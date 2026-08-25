package com.teaching.competition.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionCertExchangeRule;
import com.teaching.competition.service.ICompetitionCertExchangeRuleService;

/**
 * 赛证互通规则Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/competitionCertExchangeRule")
public class CompetitionCertExchangeRuleController extends BaseController {
    @Autowired
    private ICompetitionCertExchangeRuleService competitionCertExchangeRuleService;

    /**
     * 查询赛证互通规则列表
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionCertExchangeRule competitionCertExchangeRule) {
        startPage();
        List<CompetitionCertExchangeRule> list = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleList(competitionCertExchangeRule);
        return getDataTable(list);
    }

     /**
      * 查询赛证互通规则列表（PC端使用，无权限校验）
      */
     @GetMapping("/getList")
     public TableDataInfo getList(CompetitionCertExchangeRule competitionCertExchangeRule) {
         startPage();
         competitionCertExchangeRule.setRulerStatus("1");
         List<CompetitionCertExchangeRule> list = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleList(competitionCertExchangeRule);
         return getDataTable(list);
     }

    /**
     * 查询赛证互通规则列表（首页使用，只查询启用且置顶的数据）
     */
    @GetMapping("/getHomeList")
    public TableDataInfo getHomeList(CompetitionCertExchangeRule competitionCertExchangeRule) {
        startPage();
        competitionCertExchangeRule.setRulerStatus("1");
        competitionCertExchangeRule.setIsTope("1");
        List<CompetitionCertExchangeRule> list = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleList(competitionCertExchangeRule);
        return getDataTable(list);
    }

    /**
     * 跨服务调用：查询赛证互通规则最小字段列表
     */
    @InnerAuth
    @PostMapping("/inner/list")
    public AjaxResult innerList(@RequestBody(required = false) Map<String, Object> param) {
        CompetitionCertExchangeRule query = new CompetitionCertExchangeRule();
        if (param != null) {
            Object competitionSeriesId = param.get("competitionSeriesId");
            if (competitionSeriesId != null && String.valueOf(competitionSeriesId).length() > 0) {
                try {
                    query.setCompetitionSeriesId(Long.valueOf(String.valueOf(competitionSeriesId)));
                } catch (NumberFormatException ignored) {
                }
            }
            Object competitionTrackId = param.get("competitionTrackId");
            if (competitionTrackId != null) {
                query.setCompetitionTrackId(String.valueOf(competitionTrackId));
            }
            Object secondLevelCode = param.get("secondLevelCode");
            if (secondLevelCode != null) {
                query.setSecondLevelCode(String.valueOf(secondLevelCode));
            }
            Object delFlag = param.get("delFlag");
            if (delFlag != null) {
                query.setDelFlag(String.valueOf(delFlag));
            }
        }
        query.setDelFlag("0");
        query.setIsTope("1");
        query.setRulerStatus("1");

        List<CompetitionCertExchangeRule> list = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleListSimple(query);
        if (list != null) {
            list.sort(Comparator.comparing(CompetitionCertExchangeRule::getSort, Comparator.nullsLast(Long::compareTo)));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (CompetitionCertExchangeRule rule : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("ruleId", rule.getRuleId());
                item.put("ruleName", rule.getRulerName());
                item.put("rulerName", rule.getRulerName());
                item.put("applyDesc", rule.getApplyDesc());
                item.put("certConditions", rule.getCertConditions());
                item.put("icon", rule.getIcon());
                item.put("sort", rule.getSort());
                item.put("isTope", rule.getIsTope());
                item.put("createTime", rule.getCreateTime());
                item.put("type", "cert");
                result.add(item);
            }
        }
        return success(result);
    }

    /**
     * 导出赛证互通规则列表
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:export")
    @Log(title = "赛证互通规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionCertExchangeRule competitionCertExchangeRule) {
        List<CompetitionCertExchangeRule> list = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleList(competitionCertExchangeRule);
        ExcelUtil<CompetitionCertExchangeRule> util = new ExcelUtil<CompetitionCertExchangeRule>(CompetitionCertExchangeRule.class);
        util.exportExcel(response, list, "赛证互通规则数据");
    }

    /**
     * 获取赛证互通规则详细信息
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:query")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId) {
        return success(competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleById(ruleId));
    }

    /**
     * 新增赛证互通规则
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:add")
    @Log(title = "赛证互通规则", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionCertExchangeRule")
    public AjaxResult add(@RequestBody CompetitionCertExchangeRule competitionCertExchangeRule) {
        return toAjax(competitionCertExchangeRuleService.insertCompetitionCertExchangeRule(competitionCertExchangeRule));
    }

    /**
     * 修改赛证互通规则
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:edit")
    @Log(title = "赛证互通规则", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionCertExchangeRule")
    public AjaxResult edit(@RequestBody CompetitionCertExchangeRule competitionCertExchangeRule) {
        return toAjax(competitionCertExchangeRuleService.updateCompetitionCertExchangeRule(competitionCertExchangeRule));
    }

    /**
     * 修改赛证互通规则
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:edit")
    @Log(title = "赛证互通规则主信息修改", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionCertExchangeRuleMain")
    public AjaxResult updateCompetitionCertExchangeRuleMain(@RequestBody CompetitionCertExchangeRule competitionCertExchangeRule) {
        if(competitionCertExchangeRule.getSort()==null){
            competitionCertExchangeRule.setSort(-1L);
        }
        return toAjax(competitionCertExchangeRuleService.updateCompetitionCertExchangeRuleMain(competitionCertExchangeRule));
    }

    /**
     * 删除赛证互通规则
     */
    @RequiresPermissions("competition:competitionCertExchangeRule:remove")
    @Log(title = "赛证互通规则", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{ruleId}")
    public AjaxResult remove(@PathVariable Long ruleId) {
        return toAjax(competitionCertExchangeRuleService.deleteCompetitionCertExchangeRuleById(ruleId));
    }
}
