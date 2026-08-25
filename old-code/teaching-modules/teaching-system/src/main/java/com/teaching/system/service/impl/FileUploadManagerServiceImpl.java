package com.teaching.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisLock;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteOssUploadService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.FileReviewImportMaterial;
import com.teaching.system.api.domain.FileReviewImportSource;
import com.teaching.system.api.domain.PackageFileReq;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.ProcessedRelation;
import com.teaching.system.mapper.AuthInfoMapper;
import com.teaching.system.mapper.FileUploadManagerMapper;
import com.teaching.system.mapper.ProcessedRelationMapper;
import com.teaching.system.mapper.SysUserMapper;
import com.teaching.system.service.IExportManageService;
import com.teaching.system.service.IFileUploadManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.teaching.common.core.constant.DictConstant.EXPORTING;

@Slf4j
@Service
@RefreshScope
public class FileUploadManagerServiceImpl implements IFileUploadManagerService {
    @Autowired
    private FileUploadManagerMapper fileUploadManagerMapper;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IExportManageService exportManageService;

    @Autowired
    private ProcessedRelationMapper processedRelationMapper;

    @Autowired
    private RemoteOssUploadService remoteOssUploadService;

    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private RedisLock redisLock;


    @Value("${process.thread-count:2}")
    private int threadCount;
    @Value("${process.batch-size:50}")
    private int batchSize;

    private final ExecutorService processExecutor = Executors.newFixedThreadPool(10);

    /**
     * 查询文件上传管理
     *
     * @param id 文件上传管理主键
     * @return 文件上传管理
     */
    @Override
    public FileUploadManager selectFileUploadManagerById(Long id) {
        return fileUploadManagerMapper.selectFileUploadManagerById(id);
    }

    /**
     * 查询文件上传管理列表
     *
     * @param fileUploadManager 文件上传管理
     * @return 文件上传管理
     */
    @Override
    public List<FileUploadManager> selectFileUploadManagerList(FileUploadManager fileUploadManager) {
        List<FileUploadManager> fileUploadManagers = fileUploadManagerMapper.selectFileUploadManagerList(fileUploadManager);
        // 带队教师翻译处理
        if (CollectionUtils.isNotEmpty(fileUploadManagers)) {
            fileUploadManagers.stream().forEach(fileUploadRecordRes -> {
                if (StringUtils.isNotEmpty(fileUploadRecordRes.getLeaderTeacherId())) {
                    List<String> leaderTeacherId = Arrays.asList(fileUploadRecordRes.getLeaderTeacherId().split(","));
                    List<Long> leaderTeacherIdLongList = leaderTeacherId.stream().map(Long::parseLong).collect(Collectors.toList());
                    StringBuffer leaderTeacherName = new StringBuffer();
                    leaderTeacherIdLongList.stream().forEach(leaderTeacherIdLong -> {
                        AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(leaderTeacherIdLong);
                        if (Objects.nonNull(authInfo)) {
                            leaderTeacherName.append(authInfo.getRealName() + ",");
                        }
                    });
                    if (StringUtils.isNotEmpty(leaderTeacherName)) {
                        fileUploadRecordRes.setLeaderTeacherName(leaderTeacherName.substring(0, leaderTeacherName.length() - 1).toString());
                    }
                }
                //解析文件，获取文件名称（导出使用）
                if (StringUtils.isNotEmpty(fileUploadRecordRes.getFileInfo())) {
                    StringBuilder builder = new StringBuilder();
                    List<Map> list = JSONUtil.toList(fileUploadRecordRes.getFileInfo(), Map.class);
                    if (CollectionUtils.isNotEmpty(list)) {
                        list.forEach(map -> {
                            String getName = map.get("fileName").toString();
                            builder.append(getName).append(",");
                        });
                    }
                    if (StringUtils.isNotEmpty(builder.toString())) {
                        fileUploadRecordRes.setFileName(builder.substring(0, builder.toString().length() - 1));
                    }
                }
                // 如果是队长或者队员历史数据使用团队编号进行指导教师查询
                if (StringUtils.isEmpty(fileUploadRecordRes.getGuideTeacher())) {
                    R<List<CompetitionApplyInfo>> competitionApplyInfoListR =
                            competitionService.selectCompetitionApplyTeamCode(fileUploadRecordRes.getTeamCode(), SecurityConstants.INNER);
                    if (R.isSuccess(competitionApplyInfoListR) && CollectionUtils.isNotEmpty(competitionApplyInfoListR.getData())) {
                        List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoListR.getData();
                        List<String> guideTeacherNameList = competitionApplyInfoList.stream()
                                .filter(competitionApplyInfo -> "指导教师".equals(competitionApplyInfo.getCompetitionRoleName()))
                                .map(CompetitionApplyInfo::getUserName)
                                .distinct()
                                .toList();
                        if (CollectionUtils.isNotEmpty(guideTeacherNameList)) {
                            String guideTeacher = StringUtils.join(guideTeacherNameList, ",");
                            fileUploadRecordRes.setGuideTeacher(guideTeacher);
                        }
                    }
                }
            });
        }
        return fileUploadManagers;
    }

    /**
     * 新增文件上传管理
     *
     * @param fileUploadManager 文件上传管理
     * @return 结果
     */
    @Override
    public int insertFileUploadManager(FileUploadManager fileUploadManager) {
        fileUploadManager.setCreateTime(DateUtils.getNowDate());
        return fileUploadManagerMapper.insertFileUploadManager(fileUploadManager);
    }

    /**
     * 修改文件上传管理
     *
     * @param fileUploadManager 文件上传管理
     * @return 结果
     */
    @Override
    public int updateFileUploadManager(FileUploadManager fileUploadManager) {
        fileUploadManager.setUpdateTime(DateUtils.getNowDate());
        fileUploadManager.setSubmitStatus(null);
        return fileUploadManagerMapper.updateFileUploadManager(fileUploadManager);
    }

    @Override
    public int updateFileUploadManagerByTaskId(FileUploadManager fileUploadManager) {
        return fileUploadManagerMapper.updateFileUploadManagerByTaskId(fileUploadManager);
    }

    /**
     * 批量删除文件上传管理
     *
     * @param ids 需要删除的文件上传管理主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadManagerByIds(Long[] ids) {
        return fileUploadManagerMapper.deleteFileUploadManagerByIds(ids);
    }

    /**
     * 删除文件上传管理信息
     *
     * @param id 文件上传管理主键
     * @return 结果
     */
    @Override
    public int deleteFileUploadManagerById(Long id) {
        return fileUploadManagerMapper.deleteFileUploadManagerById(id);
    }

    @Override
    public AjaxResult exportZipFile(FileUploadManager fileUploadManager) {
        List<FileUploadManager> fileUploadManagers = selectFileUploadManagerList(fileUploadManager);
        return commonExportFile(fileUploadManagers);
    }

    @NotNull
    private AjaxResult commonExportFile(List<FileUploadManager> fileUploadManagers) {
        if (CollUtil.isEmpty(fileUploadManagers)) {
            throw new ServiceException("没有需要导出的文件");
        }
        //查询是否有正在导出中的任务，有的话不能再次导出
        ExportManage manager = new ExportManage();
        manager.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        manager.setStatus(EXPORTING);
        List<ExportManage> exportManages = exportManageService.selectExportManageList(manager);
        if (CollUtil.isNotEmpty(exportManages)) {
            throw new ServiceException("有正在导出的文件任务，不能继续导出，请稍后再试！");
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<PackageFileReq> fileList = new ArrayList<>();
        AtomicReference<Integer> count = new AtomicReference<>(0);

        Map<Long, String> userIdSchoolNameMap = new HashMap<>();
        try {
            List<Long> userIds = fileUploadManagers.stream()
                    .map(FileUploadManager::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(userIds)) {
                List<SysUser> users = sysUserMapper.selectUserByIds(userIds);
                if (CollUtil.isNotEmpty(users)) {
                    for (SysUser u : users) {
                        if (u != null && u.getUserId() != null) {
                            userIdSchoolNameMap.put(u.getUserId(), u.getSchoolName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("批量查询学校名称失败：{}", e.getMessage());
        }

        for (FileUploadManager record : fileUploadManagers) {
            PackageFileReq req = new PackageFileReq();
            //用户id
            req.setUserId(record.getUserId());
            //用户名称
            req.setUserName(record.getUserName());
            //用户赛事、赛道、赛项
            String schoolName = record.getUserId() != null ? userIdSchoolNameMap.get(record.getUserId()) : null;
            req.setFileDir(joinZipPath(
                    record.getSecondLevelName(),
                    record.getCompetitionTrackName(),
                    record.getCompetitionName(),
                    schoolName,
                    record.getUserName()
            ));
            String fileInfo = record.getFileInfo();
            Map<String, String> urlMap = new HashMap<>();
            //获取文件信息，解析json
            if (StringUtils.isNotEmpty(fileInfo)) {
                List<Map> list = JSONUtil.toList(record.getFileInfo(), Map.class);
                //附件列表解析
                if (CollUtil.isNotEmpty(list)) {
                    //循环获取文件名称和url
                    for (Map map : list) {
                        //文件名称
                        String fileName = map.get("fileName").toString();
                        //文件链接
                        String url = map.get("downloadLink").toString();
                        //链接不为空，处理信息并添加到入参列表中
                        if (StringUtils.isNotEmpty(url)) {
                            urlMap.put(url, fileName);
                            count.getAndSet(count.get() + 1);
                            req.setUrlMap(urlMap);
                        } else {
                            log.info("用户" + record.getUserName() + "(" + record.getUserId() + ")" + "上传文件：【" + fileName + "】链接为空");
                        }
                    }
                } else {
                    log.info("用户" + record.getUserName() + "(" + record.getUserId() + ")" + "附件列表为空");
                }
            } else {
                log.info("用户" + record.getUserName() + "(" + record.getUserId() + ")" + "附件列表为空");
            }
            if (req.getUrlMap() != null) {
                fileList.add(req);
            }
        }
        ;
        BigDecimal totalSize = fileUploadManagers.stream().map(FileUploadManager::getTotalSize).filter(Objects::nonNull).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalSize.compareTo(new BigDecimal("1000")) > 0) {
            resultMap.put("totalSize", totalSize.divide(new BigDecimal("1000")).setScale(2, RoundingMode.HALF_UP) + "GB");
        } else {
            resultMap.put("totalSize", totalSize.setScale(2, RoundingMode.HALF_UP) + "MB");
        }
        if (CollUtil.isEmpty(fileList)) {
            throw new ServiceException("没有可以下载的文件");
        }
        if (totalSize.compareTo(new BigDecimal("0")) == 0) {
            throw new ServiceException("文件总大小为0，无法导出");
        }
        resultMap.put("count", count.get());
        BigDecimal bigDecimal = totalSize.divide(new BigDecimal("5"), 0, RoundingMode.HALF_UP);
        resultMap.put("time", bigDecimal.compareTo(BigDecimal.ZERO) == 0 ? "1" : bigDecimal.toString());
        exportManageService.exportFiles(fileList);
        return AjaxResult.success(resultMap);
    }

    private String joinZipPath(String... segments) {
        if (segments == null || segments.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String seg : segments) {
            String cleaned = sanitizeZipSegment(seg);
            if (!StringUtils.isNotEmpty(cleaned)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(cleaned);
        }
        return sb.toString();
    }

    private String sanitizeZipSegment(String segment) {
        if (!StringUtils.isNotEmpty(segment)) {
            return null;
        }
        String s = segment.trim();
        if (!StringUtils.isNotEmpty(s)) {
            return null;
        }
        s = s.replaceAll("[\\\\/:*?\"<>|]", "_");
        s = s.replace("\r", " ").replace("\n", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return StringUtils.isNotEmpty(s) ? s : null;
    }

    @Override
    public AjaxResult selectExportFiles(List<String> ids) {
        FileUploadManager fileUploadManager = new FileUploadManager();
        fileUploadManager.setIds(ids);
        List<FileUploadManager> fileUploadManagers = selectFileUploadManagerList(fileUploadManager);
        return commonExportFile(fileUploadManagers);
    }

    @Override
    public List<FileReviewImportSource> listReviewImportSourcesByTaskId(Long fileTaskId, Boolean submittedOnly) {
        if (fileTaskId == null) {
            throw new ServiceException("文件任务ID不能为空");
        }
        FileUploadManager query = new FileUploadManager();
        query.setFileTaskId(fileTaskId);
        List<FileUploadManager> records = selectFileUploadManagerList(query);
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream()
                .filter(record -> !Boolean.TRUE.equals(submittedOnly) || Boolean.TRUE.equals(record.getSubmitStatus()))
                .map(this::toReviewImportSource)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileReviewImportSource> listReviewImportSourcesByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        FileUploadManager query = new FileUploadManager();
        query.setIds(ids.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList()));
        if (CollUtil.isEmpty(query.getIds())) {
            return Collections.emptyList();
        }
        List<FileUploadManager> records = selectFileUploadManagerList(query);
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream().map(this::toReviewImportSource).collect(Collectors.toList());
    }

    private FileReviewImportSource toReviewImportSource(FileUploadManager record) {
        FileReviewImportSource source = new FileReviewImportSource();
        source.setId(record.getId());
        source.setFileTaskId(record.getFileTaskId());
        source.setFileTaskName(record.getFileTaskName());
        source.setUserId(record.getUserId());
        source.setUserName(record.getUserName());
        source.setUserGroupIds(record.getUserGroupIds());
        source.setCompetitionSeriesId(record.getCompetitionSeriesId());
        source.setCompetitionName(record.getCompetitionName());
        source.setCompetitionStageId(record.getCompetitionStageId());
        source.setCompetitionStageName(record.getCompetitionStageName());
        source.setCompetitionTrackName(record.getCompetitionTrackName());
        source.setCompetitionTrackCode(record.getCompetitionTrackCode());
        source.setSecondLevelCode(record.getSecondLevelCode());
        source.setSecondLevelName(record.getSecondLevelName());
        source.setLeaderTeacherId(record.getLeaderTeacherId());
        source.setLeaderTeacherName(record.getLeaderTeacherName());
        source.setTeamCode(record.getTeamCode());
        source.setTeamName(record.getTeamName());
        source.setGuideTeacher(record.getGuideTeacher());
        source.setUploadTime(record.getUploadTime());
        source.setTotalSize(record.getTotalSize());
        source.setFileInfo(record.getFileInfo());
        source.setOrgId(record.getOrgId());
        source.setSubmitStatus(record.getSubmitStatus());
        source.setMaterials(parseReviewImportMaterials(record.getFileInfo()));
        return source;
    }

    private List<FileReviewImportMaterial> parseReviewImportMaterials(String fileInfo) {
        if (StringUtils.isEmpty(fileInfo)) {
            return Collections.emptyList();
        }
        List<FileReviewImportMaterial> materials = new ArrayList<>();
        List<Map> list = JSONUtil.toList(fileInfo, Map.class);
        if (CollUtil.isEmpty(list)) {
            return materials;
        }
        for (Map map : list) {
            if (map == null) {
                continue;
            }
            FileReviewImportMaterial material = new FileReviewImportMaterial();
            material.setFileName(MapUtil.getStr(map, "fileName"));
            material.setDownloadLink(MapUtil.getStr(map, "downloadLink"));
            material.setMimeType(MapUtil.getStr(map, "mimeType"));
            material.setFileSize(firstLong(map, "fileSize", "size"));
            materials.add(material);
        }
        return materials;
    }

    private Long firstLong(Map map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Long value = MapUtil.getLong(map, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据文件任务id获取文件上传管理列表
     * <p>
     * 功能说明：
     * 1. 查询任务下所有上传记录，解析文件信息
     * 2. 检查文件是否已处理过（通过ProcessedRelation表对比）
     * 3. 对于需要重新处理的文件，调用processFile方法进行处理
     * </p>
     *
     * @param fileTaskId 文件任务id
     */
    @Override
    public void getFileUploadManagerByFileTaskId(Long fileTaskId) {
        String key = "file:pdf:process:task:" + fileTaskId;
        RLock rLock = redisLock.getRLock(key);
        try {
            boolean b = rLock.tryLock(0, 2, TimeUnit.HOURS);
            if (!b) {
                log.info("获取锁失败，任务id：{}正在处理中", fileTaskId);
                return;
            }
            log.info("获取锁成功，任务id：{}开始处理", fileTaskId);
            try {
                doProcessFile(fileTaskId);
            } finally {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.info("释放锁，任务id：{}", fileTaskId);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁失败", e);
        }

    }

    private void doProcessFile(Long fileTaskId) {
        // 1. 查询任务下所有上传记录
        List<FileUploadManager> fileUploadManagers = fileUploadManagerMapper.selectFileUploadManagerByFileTaskId(fileTaskId);
        if (CollUtil.isEmpty(fileUploadManagers)) {
            return;
        }

        // 2. 解析文件信息，构建数据结构
        // managerFileMap: managerId -> 该记录下的文件列表，用于快速查找
        // allFileInfos: 所有文件信息列表，用于后续处理
        Map<Long, List<Map>> managerFileMap = new HashMap<>();
        List<Map> allFileInfos = new ArrayList<>();

        for (FileUploadManager item : fileUploadManagers) {
            String fileInfo = item.getFileInfo();
            if (StringUtils.isNotEmpty(fileInfo)) {
                List<Map> list = JSONUtil.toList(fileInfo, Map.class);
                //list中去掉fileName是null或不是.pdf结尾的
                list.removeIf(e -> {
                    Object fileName = e.get("fileName");
                    return fileName == null || !fileName.toString().toLowerCase().endsWith(".pdf");
                });
                if (CollUtil.isEmpty(list)) {
                    continue;
                }
                // 为每个文件添加managerId标识，便于后续关联
                for (Map e : list) {
                    e.put("managerId", item.getId());
                }
                managerFileMap.put(item.getId(), list);
                allFileInfos.addAll(list);
            }
        }

        // 3. 收集需要处理的文件对应的managerId
        List<Long> needProcessed = new ArrayList<>();
        List<Long> allManagerIds = new ArrayList<>(managerFileMap.keySet());

        // 4. 分批查询处理关系（每批100条），避免一次性查询大量数据
        for (int i = 0; i < allManagerIds.size(); i += 100) {
            int endIndex = Math.min(i + 100, allManagerIds.size());
            List<Long> batchIds = allManagerIds.subList(i, endIndex);

            // 查询这批managerId对应的处理前后关系记录
            List<ProcessedRelation> processedRelations = processedRelationMapper.selectProcessedRelationByManagerId(batchIds);

            if (CollUtil.isNotEmpty(processedRelations)) {
                // 按managerId分组，便于逐个对比
                Map<Long, List<ProcessedRelation>> groupByManagerId = processedRelations.stream()
                        .collect(Collectors.groupingBy(ProcessedRelation::getManagerId));

                // 5. 对比当前文件URL与已记录的URL，判断是否需要重新处理
                for (Map.Entry<Long, List<ProcessedRelation>> entry : groupByManagerId.entrySet()) {
                    Long managerId = entry.getKey();
                    List<ProcessedRelation> relations = entry.getValue();
                    List<Map> fileMaps = managerFileMap.get(managerId);

                    if (fileMaps == null) continue;

                    // 收集当前文件的URL集合
                    Set<String> currentUrls = new HashSet<>();
                    for (Map m : fileMaps) {
                        Object url = m.get("downloadLink");
                        if (url != null) {
                            currentUrls.add(url.toString());
                        }
                    }

                    // 收集已记录的旧URL集合
                    Set<String> oldUrls = new HashSet<>();
                    for (ProcessedRelation r : relations) {
                        if (r.getOldUrl() != null) {
                            oldUrls.add(r.getOldUrl());
                        }
                    }

                    // 6. 如果URL不一致，标记旧记录为无效，需要重新处理 （文件数量不同或文件路径不同）
                    if (!currentUrls.equals(oldUrls)) {
                        Long[] ids = relations.stream()
                                .map(ProcessedRelation::getId)
                                .toArray(Long[]::new);
                        processedRelationMapper.updateProcessedRelationByManagerIds(ids);
                        needProcessed.add(managerId);
                    }
                }

                // 7. 对于有记录但不在groupByManagerId中的，说明是新文件，需要处理
                for (Long id : batchIds) {
                    if (!groupByManagerId.containsKey(id)) {
                        needProcessed.add(id);
                    }
                }
            } else {
                // 8. 无处理记录，说明全部需要处理
                needProcessed.addAll(batchIds);
            }
        }

        // 9. 筛选出需要处理的文件，调用处理方法
        if (CollUtil.isNotEmpty(needProcessed)) {
            List<Map> listToProcess = new ArrayList<>();
            for (Map fileInfo : allFileInfos) {
                Object managerId = fileInfo.get("managerId");
                if (managerId != null && needProcessed.contains(((Number) managerId).longValue())) {
                    listToProcess.add(fileInfo);
                }
            }
            processFile(listToProcess);
        }
    }

    /**
     * 处理文件（调用file模块进行PDF处理）
     * <p>
     * 功能说明：
     * 1. 使用线程池并发处理多个文件，提高处理效率
     * 2. 每个文件调用file模块的processPdfFile接口完成：下载 -> PDF处理(裁剪+水印) -> 上传
     * 3. 处理完成后批量插入数据库记录
     * </p>
     *
     * @param list 待处理的文件信息列表
     */
    public void processFile(List<Map> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int totalFiles = list.size();
        log.info("开始处理 {} 个文件", totalFiles);

        // 1. 初始化计数器和结果列表（线程安全）
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<ProcessedRelation> relationList = Collections.synchronizedList(new ArrayList<>());

        // 2. 创建线程池，线程数根据文件数量动态调整
        int actualThreadCount = Math.min(threadCount, Math.max(1, totalFiles / 5));
        ExecutorService executor = Executors.newFixedThreadPool(actualThreadCount);
        List<Future<?>> futures = new ArrayList<>();

        // 3. 提交所有处理任务到线程池
        for (Map fileInfo : list) {
            Future<?> future = executor.submit(() -> {
                Long managerId = MapUtil.getLong(fileInfo, "managerId");
                String originalFileName = MapUtil.getStr(fileInfo, "fileName");
                String ossUrl = MapUtil.getStr(fileInfo, "downloadLink");

                try {
                    // 3.1 校验URL是否为空
                    if (StringUtils.isEmpty(ossUrl)) {
                        log.warn("文件URL为空, 跳过处理: managerId={}, fileName={}", managerId, originalFileName);
                        failCount.incrementAndGet();
                        return;
                    }

                    // 3.2 调用file模块处理PDF文件（下载、裁剪、水印、上传一步完成）
                    R<String> processResult = remoteOssUploadService.processPdfFile(ossUrl);

                    if (processResult == null || StringUtils.isEmpty(processResult.getData())) {
                        log.error("处理PDF文件失败: {}", ossUrl);
                        failCount.incrementAndGet();
                        return;
                    }

                    // 3.3 构建处理关系记录
                    String newOssUrl = processResult.getData();
                    String newFileName = extractFileNameFromUrl(newOssUrl);

                    ProcessedRelation processedRelation = new ProcessedRelation();
                    processedRelation.setManagerId(managerId);
                    processedRelation.setOldFileName(originalFileName);
                    processedRelation.setOldUrl(ossUrl);
                    processedRelation.setNewFileName(newFileName);
                    processedRelation.setNewUrl(newOssUrl);
                    processedRelation.setCreateTime(new Date());
                    relationList.add(processedRelation);

                    successCount.incrementAndGet();
                    log.info("文件处理完成({}/{}): {} -> {}", successCount.get(), totalFiles, originalFileName, newFileName);

                } catch (Exception e) {
                    log.error("处理文件失败: managerId={}, fileName={}", managerId, originalFileName, e);
                    failCount.incrementAndGet();
                }
            });
            futures.add(future);
        }

        // 4. 关闭线程池，等待所有任务完成（最长等待2小时）
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 5. 批量插入处理关系到数据库
        if (!relationList.isEmpty()) {
            batchInsertProcessedRelations(relationList);
        }

        log.info("文件处理完成: 总计 {}, 成功 {}, 失败 {}", totalFiles, successCount.get(), failCount.get());
    }

    private String extractFileNameFromUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }
        String path = url;
        if (url.contains("?")) {
            path = url.substring(0, url.indexOf("?"));
        }
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * 批量插入处理关系记录
     * <p>
     * 将处理结果分批插入数据库，避免一次性插入大量数据导致性能问题
     * </p>
     *
     * @param relations 处理关系列表
     */
    private void batchInsertProcessedRelations(List<ProcessedRelation> relations) {
        if (CollUtil.isEmpty(relations)) {
            return;
        }
        int batch = Math.max(1, batchSize);
        // 分批插入，每批batchSize条记录
        for (int i = 0; i < relations.size(); i += batch) {
            int endIndex = Math.min(i + batch, relations.size());
            List<ProcessedRelation> subList = relations.subList(i, endIndex);
            for (ProcessedRelation relation : subList) {
                try {
                    processedRelationMapper.insertProcessedRelation(relation);
                } catch (Exception e) {
                    log.error("批量插入处理关系失败: managerId={}", relation.getManagerId(), e);
                }
            }
        }
        log.info("批量插入处理关系完成: {} 条", relations.size());
    }


}
