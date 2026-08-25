package com.teaching.competition.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.UserCertificate;
import com.teaching.competition.service.IUserCertificateService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户证书Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/userCertificate")
public class UserCertificateController extends BaseController {

    private static ThreadPoolExecutor exportCertThreadPool = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    @Autowired
    private IUserCertificateService userCertificateService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    /**
     * 查询用户证书列表
     */
    @RequiresPermissions("competition:userCertificate:list")
    @GetMapping("/list")
    public TableDataInfo list(UserCertificate userCertificate) {
        startPage();
        List<UserCertificate> list = userCertificateService.selectUserCertificateList(userCertificate);
//        return userCertificateService.getUserCertificateList(userCertificate);
        return getDataTable(list);
    }

    /**
     * 导出用户证书列表
     */
    @RequiresPermissions("competition:userCertificate:export")
    @Log(title = "用户证书", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response,@RequestBody UserCertificate userCertificate) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String userName = SecurityUtils.getUsername();
        Map<String,Object> fileParam = new HashMap<>();
        fileParam.put("userId", userId);
        fileParam.put("userName", userName);
        R<Long> longR = remoteUserService.saveOssExportFile(fileParam, SecurityConstants.INNER);
        Long fileMangerId;
        if (R.isSuccess(longR)) {
            fileMangerId = longR.getData();
        } else {
            fileMangerId = null;
        }
        exportCertThreadPool.execute(() -> {
            uploadFileAndUpdateUserCertificateExportInfo(response, userCertificate,userName,userId,fileMangerId);
        });
        return AjaxResult.success("用户证书信息导出成功，请稍后在'导出管理'列表查看文件");
    }

    private void uploadFileAndUpdateUserCertificateExportInfo(HttpServletResponse response, UserCertificate userCertificate,
                                                              String userName, Long userId, Long fileMangerId) {
        Map<String, Object> fileParam = new HashMap<>();
        try {
            List<UserCertificate> list = userCertificateService.selectUserCertificateList(userCertificate);
//        ExcelUtil<UserCertificate> util = new ExcelUtil<UserCertificate>(UserCertificate.class);
//        util.exportExcel(response, list, "用户证书数据");
//        MultipartFile multipartFile = util.transToMultipartFile(response, list, "用户证书");
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("用户证书", list);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap, "用户证书信息导出.xlsx");
            //调用上传附件接口上传文件
            R<String> upload = remoteFileService.ossUpload(multipartFile, "fileExport", null);
            //上传成功后，更新导出文件url、状态、结束时间等信息
            if (upload.getCode() == 200) {
                String url = upload.getData();
                fileParam.put("fileName", multipartFile.getOriginalFilename());
                fileParam.put("fileUrl", url);
                fileParam.put("fileSize", multipartFile.getSize());
                fileParam.put("userId", userId);
                fileParam.put("userName", userName);
                fileParam.put("id", fileMangerId);
                fileParam.put("status", "1");
                remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            }else {
                fileParam.put("userId", userId);
                fileParam.put("userName", userName);
                fileParam.put("id", fileMangerId);
                fileParam.put("status", "2");
                remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            }
        }catch (Exception e){
            fileParam.put("userId", userId);
            fileParam.put("userName", userName);
            fileParam.put("id", fileMangerId);
            fileParam.put("status", "2");
            remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            logger.error("导出文件失败", e);
        }
    }

    /**
     * 获取用户证书详细信息
     */
    @RequiresPermissions("competition:userCertificate:query")
    @PostMapping("/getCertInfo")
    public AjaxResult getInfo(@RequestBody UserCertificate userCertificate) {
        return success(userCertificateService.selectUserCertificateById(userCertificate));
    }

    /**
     * 新增用户证书
     */
    @RequiresPermissions("competition:userCertificate:add")
    @Log(title = "用户证书", businessType = BusinessType.INSERT)
    @PostMapping("/saveUserCertificate")
    public AjaxResult add(@RequestBody UserCertificate userCertificate) {
        return toAjax(userCertificateService.insertUserCertificate(userCertificate));
    }

    /**
     * 批量新增用户证书
     */
    @RequiresPermissions("competition:userCertificate:add")
    @Log(title = "用户证书", businessType = BusinessType.INSERT)
    @PostMapping("/batchSaveUserCertificate")
    public AjaxResult batchAdd(@RequestBody Map<String, Object> param) throws ParseException {
        if(MapUtils.isEmpty(param) || Objects.isNull(param.get("userCertificateList"))){
            return success();
        }
        List<UserCertificate> userCertificateList;
        try {
            Object userCertificateObj = param.get("userCertificateList");
            userCertificateList = JSONArray.parseArray(
                    JSONArray.toJSONString(userCertificateObj),
                    UserCertificate.class
            );
        } catch (Exception e) {
            logger.error("解析用户证书列表失败", e);
            return error("用户证书数据格式不正确");
        }
//        List<UserCertificate> userCertificateList =
//                JsonUtils.parseArray(param.get("userCertificateList").toString(), UserCertificate.class);
        Date issuanceDate = null;
        if(Objects.nonNull(param.get("issuanceDate"))){
            issuanceDate = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(param.get("issuanceDate").toString());
        }
        return toAjax(userCertificateService.batchInsertUserCertificate(userCertificateList,issuanceDate));
    }

    /**
     * 修改用户证书
     */
    @RequiresPermissions("competition:userCertificate:edit")
    @Log(title = "用户证书", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserCertificate userCertificate) {
        return toAjax(userCertificateService.updateUserCertificate(userCertificate));
    }

    /**
     * 删除用户证书
     */
    @RequiresPermissions("competition:userCertificate:remove")
    @Log(title = "用户证书", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody UserCertificate userCertificate) {
        return toAjax(userCertificateService.deleteUserCertificateById(userCertificate));
    }
}
