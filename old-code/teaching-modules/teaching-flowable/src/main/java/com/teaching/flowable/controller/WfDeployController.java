package com.teaching.flowable.controller;

import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.vo.WfDeployVo;
import com.teaching.flowable.domain.vo.WfFormVo;
import com.teaching.flowable.service.IWfDeployFormService;
import com.teaching.flowable.service.IWfDeployService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * 流程部署
 *
 * @author KonBAI
 * @createTime 2022/3/24 20:57
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/deploy")
public class WfDeployController extends BaseController {

    private final IWfDeployService deployService;
    private final IWfDeployFormService deployFormService;

    /**
     * 查询流程部署列表
     */
    @RequiresPermissions("workflow:deploy:list")
    @GetMapping("/list")
    public TableDataInfo<WfDeployVo> list(ProcessQuery processQuery, PageQuery pageQuery) {
        return deployService.queryPageList(processQuery, pageQuery);
    }

    /**
     * 查询流程部署版本列表
     */
    @RequiresPermissions("workflow:deploy:list")
    @GetMapping("/publishList")
    public TableDataInfo<WfDeployVo> publishList(@RequestParam String processKey, PageQuery pageQuery) {
        return deployService.queryPublishList(processKey, pageQuery);
    }

    /**
     * 激活或挂起流程
     *
     * @param state        状态（active:激活 suspended:挂起）
     * @param definitionId 流程定义ID
     */
    @RequiresPermissions("workflow:deploy:state")
    @PutMapping(value = "/changeState")
    public R<Void> changeState(@RequestParam String state, @RequestParam String definitionId) {
        deployService.updateState(definitionId, state);
        return R.ok(null,"操作成功");
    }

    /**
     * 读取xml文件
     *
     * @param definitionId 流程定义ID
     * @return
     */
    @RequiresPermissions("workflow:deploy:query")
    @GetMapping("/bpmnXml/{definitionId}")
    public R<String> getBpmnXml(@PathVariable(value = "definitionId") String definitionId) {
        return R.ok(null, deployService.queryBpmnXmlById(definitionId));
    }

    /**
     * 删除流程模型
     *
     * @param deployIds 流程部署ids
     */
    @RequiresPermissions("workflow:deploy:remove")
    @Log(title = "删除流程部署", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deployIds}")
    public R<String> remove(@NotEmpty(message = "主键不能为空") @PathVariable String[] deployIds) {
        deployService.deleteByIds(Arrays.asList(deployIds));
        return R.ok();
    }

    /**
     * 上线流程部署
     *
     * @param deployIds
     * @return
     */
    @RequiresPermissions("workflow:deploy:remove")
    @Log(title = "上线流程部署", businessType = BusinessType.UPDATE)
    @PostMapping("/makeItOnline/{deployIds}/{status}/{category}")
    public R<String> makeItOnline(@NotEmpty(message = "主键不能为空") @PathVariable String[] deployIds, @PathVariable String status, @PathVariable String category) {
        deployService.makeItOnline(Arrays.asList(deployIds), status,category);
        return R.ok();
    }

    /**
     * 查询流程部署关联表单信息
     *
     * @param deployId 流程部署id
     */
    @GetMapping("/form/{deployId}")
    public R<?> start(@PathVariable(value = "deployId") String deployId) {
        WfFormVo formVo = deployFormService.selectDeployFormByDeployId(deployId);
        if (Objects.isNull(formVo)) {
            return R.fail("请先配置流程表单");
        }
        return R.ok(JsonUtils.parseObject(formVo.getContent(), Map.class));
    }
}
