package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.competition.service.ICompetitionWorksService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionMainInfoReq;
import com.teaching.system.api.domain.SysFile;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 赛事作品Controller
 * 
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/competitionWorks")
public class CompetitionWorksController extends BaseController
{
    @Autowired
    private ICompetitionWorksService competitionWorksService;

    @Autowired
    private RemoteFileService sysFileService;

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private RedisService redisService;

    private static final Logger log = LoggerFactory.getLogger(CompetitionWorksController.class);

    /**
     * 查询赛事作品列表
     */
    @RequiresPermissions("competition:competitionWorks:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionWorks competitionWorks) {
        startPage();
        List<CompetitionWorks> list = competitionWorksService.selectCompetitionWorksList(competitionWorks);
        return getDataTable(list);
    }

    /**
     * 导出赛事作品列表
     */
    @RequiresPermissions("competition:competitionWorks:export")
    @Log(title = "赛事作品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionWorks competitionWorks) {
        List<CompetitionWorks> list = competitionWorksService.selectCompetitionWorksList(competitionWorks);
        ExcelUtil<CompetitionWorks> util = new ExcelUtil<CompetitionWorks>(CompetitionWorks.class);
        util.exportExcel(response, list, "赛事作品数据");
    }

    /**
     * 获取赛事作品详细信息
     */
    @RequiresPermissions("competition:competitionWorks:list")
    @GetMapping(value = "/{worksId}")
    public AjaxResult getInfo(@PathVariable("worksId") Long worksId) {
        return success(competitionWorksService.selectCompetitionWorksByWorksId(worksId));
    }

    // 用户端查询赛事上传作品
    @GetMapping("/getUserList")
    public AjaxResult getUserList(CompetitionWorks competitionWorks) {
        competitionWorks.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        List<CompetitionWorks> list = competitionWorksService.selectCompetitionWorksListByUserId(competitionWorks);
        return success(list);
    }

    @GetMapping(value = "/queryUserCompetitionWorks")
    public AjaxResult selectUserCompetitionWorksByWorksId(@RequestParam CompetitionWorks competitionWorks) {
        competitionWorks.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return success(competitionWorksService.selectCompetitionWorksByUserId(competitionWorks));
    }

    /**
     * 新增赛事作品并上传（用户端）
     */
//    @RequiresPermissions("work:user:add")
    @Log(title = "赛事作品", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionWorks")
    public AjaxResult insertCompetitionWorks(@RequestBody CompetitionWorks competitionWorks) {
        return toAjax(competitionWorksService.insertCompetitionWorks(competitionWorks));
    }

    /**
     * 赛事作品上传请求（用户端）
     */
//    @RequiresPermissions("work:user:add")
    @Log(title = "赛事作品上传请求", businessType = BusinessType.INSERT)
    @PostMapping("/uploadCompetitionWorks/{competitionSeriesId}")
    public R<SysFile> uploadCompetitionWorks(@PathVariable Long competitionSeriesId, MultipartFile file) {
        try {
            // 校验文件格式大小
            CompetitionMainInfoReq req = new CompetitionMainInfoReq();
            req.setCompetitionSeriesId(competitionSeriesId);
            List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
            if(CollectionUtils.isEmpty(competitionDetailInfoList)){
                throw new GlobalException("未查询到赛事信息");
            }
            CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
//            if(StringUtils.isNotEmpty(competitionDetailInfo.getWorksFormat())){
//                List<String> worksFormatList = Arrays.asList(competitionDetailInfo.getWorksFormat().split(","));
//                String originalFilename = file.getOriginalFilename();
//                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
//                if (CollectionUtils.isNotEmpty(worksFormatList)) {
//                    boolean isContains = worksFormatList.stream()
//                            .map(String::toLowerCase)
//                            .anyMatch(item -> item.equals(fileExtension.toLowerCase()));
//                    if (!isContains) {
//                        return R.fail("文件格式错误");
//                    }
//                }
//            }
            // 上传作品截至时间校验
//            if (Objects.nonNull(competitionDetailInfo.getWorksSubmitDate())) {
//                if (competitionDetailInfo.getWorksSubmitDate().getTime() < System.currentTimeMillis()) {
//                    throw new GlobalException("上传作品已截止");
//                }
//            }
//            if(StringUtils.isNotEmpty(competitionDetailInfo.getWorksFormatSize())){
//                // 单位MB
//                long worksFormatSize = Long.parseLong(competitionDetailInfo.getWorksFormatSize());
//                long size = file.getSize()/(1024 * 1024);
//                if (size > worksFormatSize) {
//                    return R.fail("文件大小超出限制");
//                }
//            }
            // 上传并返回访问地址
            return sysFileService.upload(file);
        }
        catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 生成链接获取专家打分赛事作品列表
     */
    @GetMapping("/getSpecialistList")
    public AjaxResult getSpecialistList(CompetitionWorks competitionWorks) {
        List<CompetitionWorks> list = competitionWorksService.selectSpecialistCompetitionWorksList(competitionWorks);
        return success(list);
    }

    /**
     * 生成链接修改赛事作品打分
     */
    @PostMapping("/updateCompetitionWorksScore")
    public AjaxResult updateScore(@RequestBody CompetitionWorks competitionWorks) {
        return toAjax(competitionWorksService.updateLinkCompetitionWorks(competitionWorks));
    }

    @GetMapping(value = "/getLinkCompetitionWorksInfo/{worksId}")
    public AjaxResult getLinkCompetitionWorksInfo(@PathVariable("worksId") Long worksId) {
        return success(competitionWorksService.selectLinkCompetitionWorksByWorksId(worksId));
    }

    /**
     * 赛事作品打分
     */
    @RequiresPermissions("competition:competitionWorks:edit")
    @Log(title = "赛事作品打分", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionWorks")
    public AjaxResult edit(@RequestBody CompetitionWorks competitionWorks)
    {
        return toAjax(competitionWorksService.updateCompetitionWorks(competitionWorks));
    }

    /**
     * 删除赛事作品
     */
    @RequiresPermissions("competition:competitionWorks:remove")
    @Log(title = "赛事作品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{worksIds}")
    public AjaxResult remove(@PathVariable Long[] worksIds)
    {
        return toAjax(competitionWorksService.deleteCompetitionWorksByWorksIds(worksIds));
    }
}
