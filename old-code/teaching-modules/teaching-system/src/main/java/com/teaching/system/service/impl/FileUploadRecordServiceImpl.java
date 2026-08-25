package com.teaching.system.service.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.poi.CustomMultipartFile;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisService;
import com.teaching.file.utils.FileSizeUtil;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteOssUploadService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.domain.*;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IExportManageService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.teaching.system.service.IFileUploadRecordService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传管理Service业务层处理
 * 
 * @author teaching
 * @date 2026-01-09
 */
@Service
public class FileUploadRecordServiceImpl implements IFileUploadRecordService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadRecordServiceImpl.class);

    @Autowired
    private FileUploadRecordMapper fileUploadRecordMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private FileTaskMapper fileTaskMapper;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private RemoteOssUploadService remoteOssUploadService;

    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    @Lazy
    private IExportManageService exportManageService;

    @Autowired
    private FileUploadManagerMapper fileUploadManagerMapper;

    /**
     * 查询文件上传管理
     * 
     * @param id 文件上传管理主键
     * @return 文件上传管理
     */
    @Override
    public FileUploadRecord selectFileUploadRecordById(Long id)
    {
        return fileUploadRecordMapper.selectFileUploadRecordById(id);
    }

    /**
     * 查询文件上传管理列表
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 文件上传管理
     */
    @Override
    public List<FileUploadRecord> selectFileUploadRecordList(FileUploadRecord fileUploadRecord) {
        List<FileUploadRecord> fileUploadRecords = fileUploadRecordMapper.selectFileUploadRecordList(fileUploadRecord);
        // 带队教师翻译处理
        if(CollectionUtils.isNotEmpty(fileUploadRecords)){
            fileUploadRecords.stream().forEach(fileUploadRecordRes -> {
                if(StringUtils.isNotEmpty(fileUploadRecordRes.getLeaderTeacherId())){
                    List<String> leaderTeacherId = Arrays.asList(fileUploadRecordRes.getLeaderTeacherId().split( ","));
                    List<Long> leaderTeacherIdLongList = leaderTeacherId.stream().map(Long::parseLong).toList();
                    StringBuffer leaderTeacherName = new StringBuffer();
                    leaderTeacherIdLongList.stream().forEach(leaderTeacherIdLong -> {
                        AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(leaderTeacherIdLong);
                        if(Objects.nonNull(authInfo)){
                            leaderTeacherName.append(authInfo.getRealName()+",");
                        }
                    });
                    if(StringUtils.isNotEmpty(leaderTeacherName)){
                        fileUploadRecordRes.setLeaderTeacherName(leaderTeacherName.substring(0, leaderTeacherName.length()-1).toString());
                    }
                }
                //解析文件，获取文件名称（导出使用）
                if (StringUtils.isNotEmpty(fileUploadRecordRes.getFileInfo())) {
                    StringBuilder builder = new StringBuilder();
                    List<Map> list = JSONUtil.toList(fileUploadRecordRes.getFileInfo(), Map.class);
                    if (CollUtil.isNotEmpty(list)) {
                        list.forEach(map->{
                            String getName = map.get("fileName").toString();
                            builder.append(getName).append(",");
                        });
                    }
                    if (StringUtils.isNotEmpty(builder.toString())) {
                        fileUploadRecordRes.setFileName(builder.substring(0,builder.toString().length()-1));
                    }
                }
            });
        }
        return fileUploadRecords;
    }

    /**
     * 新增文件上传管理
     * 
     * @param fileUploadReq 文件上传管理
     * @return 结果
     */
    @Override
    public int insertFileUploadRecord(FileUploadRecord fileUploadRecord) {
        Set<Long> taskIdListRedis = redisService.getCacheObject("userGroup:info:" + fileUploadRecord.getUserId());
        // 获取任务用户组id
        FileTask fileTask = fileTaskMapper.selectFileTaskById(fileUploadRecord.getFileTaskId());
        if (!"2".equals(fileTask.getTaskStatus())) {
            throw new GlobalException("当前任务未发布");
        }
        List<String> taskIdList = List.of(fileTask.getUserGroupIds().split(","));
        List<Long> taskIdLongList = taskIdList.stream().map(Long::parseLong).toList();
        logger.info("获取reids中任务用户组id：{}", JSONObject.toJSONString(taskIdListRedis));
        logger.info("获取库任务用户组id：{}", JSONObject.toJSONString(taskIdList));
        if(CollectionUtils.isEmpty(taskIdListRedis) || !taskIdListRedis.stream().anyMatch(taskIdLongList::contains)){
            throw new GlobalException("当前用户不所属用户组，请联系管理员");
        }
        setCompetitionData(fileUploadRecord);
        if(StringUtils.isEmpty(fileUploadRecord.getUploadOperationType())){
            if(Objects.isNull(fileUploadRecord.getId())){
                fileUploadRecord.setUploadOperationType("add");
            } else {
                fileUploadRecord.setUploadOperationType("update");
            }
        }
        fileUploadRecord.setCreateTime(DateUtils.getNowDate());
        fileUploadRecord.setUserGroupIds(fileTask.getUserGroupIds());
        fileUploadRecord.setUploadTime(DateUtils.getNowDate());
        fileUploadRecordMapper.insertFileUploadRecord(fileUploadRecord);
        return 1;
    }

    @Override
    public int insertFileUploadManager(FileUploadReq fileUploadReq) {
        Set<Long> taskIdListRedis = redisService.getCacheObject("userGroup:info:" + fileUploadReq.getUserId());
        // 获取任务用户组id
        FileTask fileTask = fileTaskMapper.selectFileTaskByTaskId(fileUploadReq.getId());
        if (!"2".equals(fileTask.getTaskStatus())) {
            throw new GlobalException("当前任务未发布");
        }
        List<String> taskIdList = List.of(fileTask.getUserGroupIds().split(","));
        List<Long> taskIdLongList = taskIdList.stream().map(Long::parseLong).toList();
        if(CollectionUtils.isEmpty(taskIdListRedis) || !taskIdListRedis.stream().anyMatch(taskIdLongList::contains)){
            throw new GlobalException("当前用户不所属用户组，请联系管理员");
        }
        FileUploadRecord fileUploadRecord = new FileUploadRecord();
        fileUploadRecord.setSysUserGroupCompetitionRelationList(fileUploadReq.getSysUserGroupCompetitionRelationList());
        fileUploadRecord.setUserId(fileUploadReq.getUserId());
        setCompetitionData(fileUploadRecord);
        // 文件管理任务
        // 记录文件上传操作日志记录
        FileUploadManager fileUploadManager = new FileUploadManager();
        BeanUtils.copyProperties(fileUploadRecord, fileUploadManager);
        fileUploadManager.setFileTaskId(fileUploadReq.getId());
        fileUploadManager.setFileTaskName(fileUploadReq.getTaskName());
        fileUploadManager.setSubmitStatus(fileUploadReq.getSubmitStatus());
        List<Map> fileInfoList = new ArrayList<>();
        AtomicReference<BigDecimal> allTotalSize = new AtomicReference<>(new BigDecimal(0));
        if(CollectionUtils.isNotEmpty(fileUploadReq.getFileUploadManagerList())){
            fileUploadReq.getFileUploadManagerList().stream().forEach(fileUploadManagerRes -> {
                BigDecimal totalSize = new BigDecimal(fileUploadManagerRes.getTotalSize());
                allTotalSize.set(allTotalSize.get().add(totalSize));
                String fileInfo = fileUploadManagerRes.getFileInfo();
                List<Map> list = JSONUtil.toList(fileInfo, Map.class);
                fileInfoList.addAll(list);
            });
            fileUploadManager.setTotalSize(allTotalSize.get().toString());
            fileUploadManager.setFileInfo(JSONUtil.toJsonStr(fileInfoList));
        }
        FileUploadManager dataFileUploadManager= new FileUploadManager();
        dataFileUploadManager.setFileTaskId(fileUploadManager.getFileTaskId());
        dataFileUploadManager.setUserId(fileUploadManager.getUserId());
        List<FileUploadManager> fileUploadManagers = fileUploadManagerMapper.selectFileUploadManagerList(dataFileUploadManager);
        if(CollectionUtils.isNotEmpty(fileUploadManagers)){
            fileUploadManager.setId(fileUploadManagers.get(0).getId());
            fileUploadManager.setUploadTime(DateUtils.getNowDate());
            fileUploadManager.setUpdateTime(DateUtils.getNowDate());
            if(CollectionUtils.isEmpty(fileUploadReq.getFileUploadManagerList())){
                fileUploadManager.setDelFlag("1");
            }
            fileUploadManagerMapper.updateFileUploadManager(fileUploadManager);
        } else {
            fileUploadManager.setUploadTime(DateUtils.getNowDate());
            fileUploadManager.setCreateTime(DateUtils.getNowDate());
            fileUploadManagerMapper.insertFileUploadManager(fileUploadManager);
        }
        return 1;
    }

    private void setCompetitionData(FileUploadRecord fileUploadRecord) {
        // 赛事信息处理
        List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList = fileUploadRecord.getSysUserGroupCompetitionRelationList();
        if(CollectionUtils.isNotEmpty(sysUserGroupCompetitionRelationList)){
            StringBuffer competitionSeriesIdSb = new StringBuffer();
            StringBuffer competitionNameSb = new StringBuffer();
            StringBuffer competitionStageIdSb = new StringBuffer();
            StringBuffer competitionStageNameSb = new StringBuffer();
            StringBuffer competitionTrackIdSb = new StringBuffer();
            StringBuffer competitionTrackNameSb = new StringBuffer();
            StringBuffer secondLevelCodeSb = new StringBuffer();
            StringBuffer secondLevelNameSb = new StringBuffer();
            StringBuffer teamCodeSb = new StringBuffer();
            StringBuffer teamNameSb = new StringBuffer();
            StringBuffer guideTeacherSb = new StringBuffer();
            StringBuffer leaderTeacherSb = new StringBuffer();
            Set<String> competitionSeriesIds = new HashSet<>();
            Set<String> competitionStageIds = new HashSet<>();
            Set<String> competitionStageNames = new HashSet<>();
            Set<String> competitionTrackIds = new HashSet<>();
            Set<String> secondLevelCodes = new HashSet<>();
            Set<String> competitionSeriesNameList = new HashSet<>();
            Set<String> competitionTrackNameList = new HashSet<>();
            Set<String> secondLevelNameList = new HashSet<>();
            sysUserGroupCompetitionRelationList.stream().forEach(sysUserGroupCompetitionRelations -> {
                if(CollectionUtils.isNotEmpty(sysUserGroupCompetitionRelations)){
                    Map<Long, List<SysUserGroupCompetitionRelation>> groupBySort = sysUserGroupCompetitionRelations.stream().collect(Collectors.groupingBy(SysUserGroupCompetitionRelation::getSort));
                    groupBySort.forEach((key, relationList) -> {
                        for (SysUserGroupCompetitionRelation relation : relationList) {
                            switch (key.toString()) {
                                case "1":
                                    competitionSeriesIds.add(relation.getCode());
                                    competitionSeriesNameList.add(relation.getName());
                                    break;
                                case "2":
                                    competitionStageIds.add(relation.getCode());
                                    competitionStageNames.add(relation.getName());
                                    break;
                                case "3":
                                    competitionTrackIds.add(relation.getCode());
                                    competitionTrackNameList.add(relation.getName());
                                    break;
                                case "4":
                                    secondLevelCodes.add(relation.getCode());
                                    secondLevelNameList.add(relation.getName());
                                    break;
                            }
                        }
                    });
                }
            });
            Map<String,Object> param = new HashMap<>();
            param.put("userId",fileUploadRecord.getUserId());
            //competitionSeriesIds 规则：赛事id
            param.put("competitionSeriesIds", competitionSeriesIds);
            //competitionTrackIds 规则：赛道id
            param.put("competitionTrackIds", competitionTrackIds);
            //secondLevelCode 规则：二级赛区编码
            param.put("secondLevelCode", secondLevelCodes);
            param.put("hasValidCondition", !competitionSeriesIds.isEmpty() ||  !competitionTrackIds.isEmpty() || !secondLevelCodes.isEmpty());
            R<List<CompetitionApplyInfo>> competitionApplyInfolistR = competitionService.selectCompetitionApplyInfoListByUserId(param, SecurityConstants.INNER);
            if(R.isSuccess(competitionApplyInfolistR)){
                List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfolistR.getData();
                competitionStageIdSb.append(String.join(",", competitionStageIds));
                competitionStageNameSb.append(String.join(",", competitionStageNames));
                if(CollectionUtils.isNotEmpty(competitionApplyInfoList)){
                    Map<Long,List<CompetitionApplyInfo>> groupByCompetitionSeriesId = competitionApplyInfoList.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getCompetitionSeriesId));
                    Set<String> competitionSeriesNameAppList = new HashSet<>(competitionSeriesNameList);
                    Set<String> competitionTrackNameAppList = new HashSet<>(competitionTrackNameList);
                    Set<String> secondLevelNameAppList = new HashSet<>(secondLevelNameList);
                    Set<String> competitionSeriesIdList = new HashSet<>();
                    Set<String> competitionTrackIdList = new HashSet<>();
                    Set<String> secondLevelCodeList = new HashSet<>();
                    Set<String> leaderTeacherIdList = new HashSet<>();
                    Set<String> guideTeacherList = new HashSet<>();
                    Set<String> teamCodeList = new HashSet<>();
                    Set<String> teamNameList = new HashSet<>();
                    for (Long competitionSeriesIdApplyInfo : groupByCompetitionSeriesId.keySet()){
                        List<CompetitionApplyInfo> competitionApplyInfoListByCompetitionSeriesId = groupByCompetitionSeriesId.get(competitionSeriesIdApplyInfo);
                        competitionApplyInfoListByCompetitionSeriesId.stream().forEach(applyInfo -> {
                            competitionSeriesIdList.add(applyInfo.getCompetitionSeriesId().toString());
                            competitionSeriesNameAppList.add(applyInfo.getCompetitionName());
                            if(applyInfo.getCompetitionRoleName().equals("指导教师")){
                                guideTeacherList.add(applyInfo.getGuideTeacher());
                            } else {
                                competitionSeriesIdList.add(applyInfo.getCompetitionSeriesId().toString());
                                competitionSeriesNameAppList.add(applyInfo.getCompetitionName());
                                competitionTrackIdList.add(applyInfo.getCompetitionTrackId());
                                competitionTrackNameAppList.add(applyInfo.getCompetitionTrackName());
                                secondLevelCodeList.add(applyInfo.getSecondLevelCode());
                                secondLevelNameAppList.add(applyInfo.getSecondLevelName());
                                leaderTeacherIdList.add(applyInfo.getLeaderTeacherId().toString());
                                teamCodeList.add(applyInfo.getTeamCode());
                                teamNameList.add(applyInfo.getTeamName());
                            }
                        });
                    }
                    competitionTrackIdSb.append(String.join(",", competitionTrackIdList));
                    competitionTrackNameSb.append(String.join(",", competitionTrackNameAppList));
                    secondLevelCodeSb.append(String.join(",", secondLevelCodeList));
                    secondLevelNameSb.append(String.join(",", secondLevelNameAppList));
                    teamCodeSb.append(String.join(",", teamCodeList));
                    teamNameSb.append(String.join(",", teamNameList));
                    leaderTeacherSb.append(String.join(",", leaderTeacherIdList));
                    guideTeacherSb.append(String.join(",", guideTeacherList));
                    competitionSeriesIdSb.append(String.join(",", competitionSeriesIdList));
                    competitionNameSb.append(String.join(",", competitionSeriesNameAppList));
                } else {
                    Set<String> competitionSeriesNameAppList = new HashSet<>(competitionSeriesNameList);
                    Set<String> competitionTrackNameAppList = new HashSet<>(competitionTrackNameList);
                    Set<String> secondLevelNameAppList = new HashSet<>(secondLevelNameList);
                    Set<String> competitionSeriesIdList = new HashSet<>(competitionSeriesIds);
                    Set<String> competitionTrackIdList = new HashSet<>(competitionTrackIds);
                    Set<String> secondLevelCodeList = new HashSet<>(secondLevelCodes);
                    competitionSeriesIdSb.append(String.join(",", competitionSeriesIdList));
                    competitionNameSb.append(String.join(",", competitionSeriesNameAppList));
                    competitionTrackIdSb.append(String.join(",", competitionTrackIdList));
                    competitionTrackNameSb.append(String.join(",", competitionTrackNameAppList));
                    secondLevelCodeSb.append(String.join(",", secondLevelCodeList));
                    secondLevelNameSb.append(String.join(",", secondLevelNameAppList));
                }
            }
            fileUploadRecord.setCompetitionSeriesId(competitionSeriesIdSb.toString());
            fileUploadRecord.setCompetitionName(competitionNameSb.toString());
            fileUploadRecord.setCompetitionStageId(competitionStageIdSb.toString());
            fileUploadRecord.setCompetitionStageName(competitionStageNameSb.toString());
            fileUploadRecord.setCompetitionTrackCode(competitionTrackIdSb.toString());
            fileUploadRecord.setCompetitionTrackName(competitionTrackNameSb.toString());
            fileUploadRecord.setSecondLevelCode(secondLevelCodeSb.toString());
            fileUploadRecord.setSecondLevelName(secondLevelNameSb.toString());
            fileUploadRecord.setTeamCode(teamCodeSb.toString());
            fileUploadRecord.setTeamName(teamNameSb.toString());
            fileUploadRecord.setGuideTeacher(guideTeacherSb.toString());
            fileUploadRecord.setLeaderTeacherId(leaderTeacherSb.toString());
        }
    }

    /**
     * 修改文件上传管理
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 结果
     */
    @Override
    public int updateFileUploadRecord(FileUploadRecord fileUploadRecord)
    {
        fileUploadRecord.setUpdateTime(DateUtils.getNowDate());
        fileUploadRecord.setUploadTime(DateUtils.getNowDate());
        return fileUploadRecordMapper.updateFileUploadRecord(fileUploadRecord);
    }

    /**
     * 批量删除文件上传管理
     * 
     * @param ids 需要删除的文件上传管理主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadRecordByIds(Long[] ids)
    {
        return fileUploadRecordMapper.deleteFileUploadRecordByIds(ids);
    }

    /**
     * 删除文件上传管理信息
     * 
     * @param id 文件上传管理主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadRecordById(Long id)
    {
        return fileUploadRecordMapper.deleteFileUploadRecordById(id);
    }


    public Map<String,Object> exportZipFile(List<String> urls,Long id,Map<String,String> urlMap) {

        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 1. 创建临时目录
            File tempDir = new File(remoteFileService.getFilePath().getData() + "/exportFileDownload");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            // 2. 下载所有文件到临时目录
            List<File> downloadedFiles = new ArrayList<>();
            for (String url : urls) {
                File downloadedFile = downloadFile(url, tempDir,urlMap);
                if (downloadedFile != null) {
                    downloadedFiles.add(downloadedFile);
                }
            }
            if (downloadedFiles.isEmpty()) {
                resultMap.put("code", "fail");
                resultMap.put("failReason", "文件下载失败，请检查文件是否是有效的链接！");
                return resultMap;
            }
            // 3. 打包文件
            String zipFileName = "文件导出_" + System.currentTimeMillis() + ".zip";
            File zipFile = new File(tempDir, zipFileName);
            createZipFile(downloadedFiles, zipFile);
            MultipartFile customMultipartFile = null;
            try {
                customMultipartFile = new CustomMultipartFile(zipFile);
            } catch (IOException e) {
                resultMap.put("code", "fail");
                resultMap.put("failReason", "文件打包失败：！" + e.getMessage());
                return resultMap;
            }
            // 4. 上传到服务器
            R<String> upload = remoteOssUploadService.ossUpload(customMultipartFile, "fileExport", null);
            System.out.println("文件上传成功：" + upload.getData());
            if (upload.getCode() != 200) {
                resultMap.put("code", "fail");
                resultMap.put("failReason", "文件上传到服务器失败：" + upload.getMsg());
                return resultMap;
            }
            // 6. 返回下载URL（根据实际情况构造）
            resultMap.put("code", "success");
            resultMap.put("url", upload.getData());
            resultMap.put("size", FileSizeUtil.formatDecimalFileSize(customMultipartFile.getSize()));
            resultMap.put("fileName", customMultipartFile.getOriginalFilename());
            // 5. 清理临时文件
            cleanupTempFiles(downloadedFiles);
            System.out.println("文件已清理！");
            if (zipFile.exists()) {
                zipFile.delete();
            }
            return resultMap;
        } catch (Exception e) {
            resultMap.put("code", "fail");
            resultMap.put("failReason", "导出功能异常：" + e.getMessage());
            return resultMap;
        }
    }

    /*@Override
    public AjaxResult exportZipFile(FileUploadRecord fileUploadRecord) {
        List<FileUploadRecord> fileUploadRecords = fileUploadRecordMapper.selectFileUploadRecordList(fileUploadRecord);
        if (CollUtil.isEmpty(fileUploadRecords)) {
            throw new ServiceException("没有需要导出的文件");
        }
        Map<String, Object> resultMap = new HashMap<>();
//        List<Map<String,String>> urlMaps = new ArrayList<>();
        Map<String, String> urlMap = new HashMap<>();
        fileUploadRecords.forEach(record->{
            String fileInfo = record.getFileInfo();
            if (StringUtils.isNotEmpty(fileInfo)) {
                List<Map> list = JSONUtil.toList(record.getFileInfo(), Map.class);
                if (CollUtil.isNotEmpty(list)) {
                    list.forEach(map->{
                        String url = map.get("downloadLink").toString();
                        String fileName = map.get("fileName").toString();
                        urlMap.put(url, fileName);
                    });
                }
            }
        });
        BigDecimal totalSize = fileUploadRecords.stream().map(FileUploadRecord::getTotalSize).filter(Objects::nonNull).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalSize.compareTo(new BigDecimal("1000")) > 0) {
            resultMap.put("totalSize", totalSize.divide(new BigDecimal("1000")).setScale(2,RoundingMode.HALF_UP) + "GB");
        } else {
            resultMap.put("totalSize", totalSize + "MB");
        }
        if (urlMap.isEmpty()) {
            throw new ServiceException("没有可以下载的文件");
        }
        if (totalSize.compareTo(new BigDecimal("0")) == 0) {
            throw new ServiceException("文件总大小为0，无法导出");
        }
        resultMap.put("count", urlMap.size());
        BigDecimal bigDecimal = totalSize.divide(new BigDecimal("1.2"),0,RoundingMode.HALF_UP);
        resultMap.put("time",bigDecimal.toString());
        exportManageService.exportFiles(urlMap);
        return AjaxResult.success(resultMap);
    }
*/
    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(List<File> files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 下载并返回单个文件
     */
    private File downloadFile(String fileUrl, File targetDir,Map<String,String> urlMap) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpGet httpGet = new HttpGet(fileUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();

                    // 从URL提取文件名
//                    String fileName = extractFileName(fileUrl);
                    String fileName = urlMap.get(fileUrl);
                    File targetFile = new File(targetDir, fileName);

                    // 确保文件名唯一
                    targetFile = getUniqueFile(targetFile);

                    try (InputStream inputStream = entity.getContent();
                         FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    System.out.println("文件下载成功: " + targetFile.getAbsolutePath());
                    return targetFile;
                }
            }
        } catch (Exception e) {
            System.err.println("下载文件失败: " + fileUrl + ", 错误: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileName(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            // 如果URL解析失败，使用时间戳作为文件名
            return "file_" + System.currentTimeMillis() + ".dat";
        }
    }

    /**
     * 获取唯一的文件名（避免重复）
     */
    private File getUniqueFile(File file) {
        if (!file.exists()) {
            return file;
        }

        String name = file.getName();
        String baseName = name.substring(0, name.lastIndexOf('.'));
        String extension = name.substring(name.lastIndexOf('.'));

        int counter = 1;
        File newFile;
        do {
            newFile = new File(file.getParent(), baseName + "_" + counter + extension);
            counter++;
        } while (newFile.exists());

        return newFile;
    }

    /**
     * 创建ZIP文件
     */
    private void createZipFile(List<File> files, File zipFile) {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : files) {
                if (file.exists() && file.isFile()) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }

                    zos.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new ServiceException("压缩文件失败：" + e.getMessage());
        }

        System.out.println("ZIP文件创建成功: " + zipFile.getAbsolutePath());
    }
}
