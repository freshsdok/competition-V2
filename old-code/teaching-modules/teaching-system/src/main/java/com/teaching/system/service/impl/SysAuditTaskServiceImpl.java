package com.teaching.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.core.web.page.TableSupport;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteContentService;
import com.teaching.system.api.RemoteCourseService;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import com.teaching.system.api.domain.course.CourseInfo;
import com.teaching.system.domain.SysAuditConfig;
import com.teaching.system.domain.SysAuditMainConfig;
import com.teaching.system.domain.SysAuditTask;
import com.teaching.system.domain.SysAuditTaskSubinfo;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IIdentityInfoService;
import com.teaching.system.service.ISysAuditTaskService;
import com.teaching.system.service.ISysDictTypeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 审核任务Service业务层处理
 *
 * @author teaching
 * @date 2025-10-16
 */
@Service
public class SysAuditTaskServiceImpl implements ISysAuditTaskService {
    private static final Logger log = LoggerFactory.getLogger(SysAuditTaskServiceImpl.class);
    @Autowired
    private SysAuditTaskMapper sysAuditTaskMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysAuditMainConfigMapper sysAuditMainConfigMapper;
    @Autowired
    private SysAuditTaskSubinfoMapper sysAuditTaskSubinfoMapper;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private RemoteContentService remoteContentService;
    @Autowired
    private RemoteCourseService remoteCourseService;
    @Autowired
    private IIdentityInfoService identityInfoService;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private AuthInfoMapper authInfoMapper;

    /**
     * 查询审核任务详情
     *
     * @param taskId 审核任务主键
     * @return 审核任务
     */
    @Override
    public SysAuditTask selectSysAuditTaskByTaskId(Long taskId) {
        //包含审核记录信息
        SysAuditTask sysAuditTask = sysAuditTaskMapper.selectSysAuditTaskAndAuditTypeByTaskId(taskId);
        //课程的章节视频审核
        if (TdConstants.TABLE_NAME_CHAPTERVIDEO.equals(sysAuditTask.getBusinessTable())) {
            String videoIds = sysAuditTask.getVideoIds();
            Long[] array = Arrays.stream(videoIds.split(",")).map(Long::parseLong).toArray(Long[]::new);
            List<CourseChapterVideo> data = remoteCourseService.getChapterVideoDetailInfoByIds(array, SecurityConstants.INNER).getData();
            sysAuditTask.setCourseChapterVideos(data);
        }
        //审核流程图
        sysAuditTask.setReviewProcess(getReviewProcess(sysAuditTask));
        //设置管理员是否介入审核字段
        setAdminIntervention(sysAuditTask);
        sysAuditTask.setSubInfos(null);
        setBusinessDetail(sysAuditTask);
        return sysAuditTask;
    }

    @Override
    public String selectSysAuditTaskPicByTaskId(Long taskId) {
        SysAuditTask sysAuditTask = sysAuditTaskMapper.selectSysAuditTaskByTaskId(taskId);
        String businessDetail = sysAuditTask.getBusinessDetails();
        if (StringUtils.isNotBlank(businessDetail)) {
            JSONObject jsonObject = JSON.parseObject(businessDetail);
            return jsonObject.getString("workCardUrl");
        }
        IdentityInfo identityInfo = identityInfoService.selectIdentityInfoByAuthId(null, sysAuditTask.getBusinessId(), null);
        return identityInfo != null ? identityInfo.getWorkCardUrl() : null;
    }

    /**
     * 查下个节点审核人信息
     *
     * @param sysAuditTask
     */
    private void setNextNodeName(SysAuditTask sysAuditTask) {
        if (TdConstants.CHECK_STATUS_SHZ.equals(sysAuditTask.getCheckStatus())) {
            //审核流程id
            Long auditId = sysAuditTask.getAuditId();
            List<SysAuditTaskSubinfo> subInfos = sysAuditTask.getSubInfos();
            //走过的审核节点id
            List<Long> collect = subInfos.stream().map(SysAuditTaskSubinfo::getAuditConfigId).collect(Collectors.toList());
            String nextPersonName = sysAuditTaskMapper.selectNextPersonName(auditId, collect);
            SysAuditTaskSubinfo nextPersonInfo = new SysAuditTaskSubinfo();
            nextPersonInfo.setCreateBy(nextPersonName);
            nextPersonInfo.setRemark("next");
            subInfos.add(nextPersonInfo);
        }
    }


    /**
     * 审核流程图
     *
     * @param sysAuditTask
     * @return
     */
    private List<Map<String, Object>> getReviewProcess(SysAuditTask sysAuditTask) {
        //已审核的节点信息
        List<SysAuditTaskSubinfo> subInfos = sysAuditTask.getSubInfos();
        //所有是审核节点
        List<Map<String, Object>> allNodeList = sysAuditTaskMapper.selectAllNodeByAuditId(sysAuditTask.getAuditId());
        //一个节点都没审核呢
        if (CollectionUtils.isEmpty(subInfos)) {
            allNodeList.forEach(node -> {
                Long levelSort = MapUtils.getLong(node, "levelSort");
                node.put("color", levelSort == 1 ? "yellow" : "gray");
                setCheckPerson(node);
            });
            return allNodeList;
        }
        //最后的审核节点的id
        Long lastAuditConfigId = subInfos.get(subInfos.size() - 1).getAuditConfigId();
        String lastAuditStatus = subInfos.get(subInfos.size() - 1).getCheckStatus();
        Long allEndConfigId = MapUtils.getLong(allNodeList.get(allNodeList.size() - 1), "configId");
        Long nextConfigId = null;
        //有审核节点
        for (Map<String, Object> node : allNodeList) {
            Long configId = MapUtils.getLong(node, "configId");
            SysAuditTaskSubinfo subInfo = subInfos.stream()
                    .filter(s -> Objects.equals(s.getAuditConfigId(), configId))
                    .findFirst()
                    .orElse(null);
            //审核过的节点
            if (subInfo != null) {
                String checkStatus = subInfo.getCheckStatus();
                node.put("color", TdConstants.CHECK_STATUS_TG.equals(checkStatus) ? "green" : "red");
                node.put("checkStatus", checkStatus);
                node.put("checkPerson", subInfo.getCreateBy());
                node.put("checkTime", subInfo.getCheckTime());
                node.put("checkOpinion", subInfo.getCheckOpinion());
                node.put("adminIntervention", subInfo.getAdminIntervention());
            }
            if (Objects.equals(lastAuditConfigId, configId) && !Objects.equals(lastAuditConfigId, allEndConfigId)) {
                if (TdConstants.CHECK_STATUS_TG.equals(lastAuditStatus)) {
                    nextConfigId = findNextStepConfigId(allNodeList, lastAuditConfigId);
                }
            }
        }

        if (nextConfigId != null) {
            Long finalNextConfigId = nextConfigId;
            allNodeList.forEach(node -> {
                Long currentConfigId = MapUtils.getLong(node, "configId");
                if (Objects.equals(currentConfigId, finalNextConfigId)) {
                    node.put("color", "yellow");
                    setCheckPerson(node);
                }
            });
        }
        return allNodeList;
    }


    /**
     * 设置当前节点审核人姓名 多个话使用逗号分隔(没有权限过滤)
     *
     * @param node
     */
    public void setCheckPerson(Map<String, Object> node) {
        // role:check_person_role、dept:check_person_org、deptRole:check_person_org && check_person_role
        if ("yellow".equals(MapUtils.getString(node, "color"))) {
            String type = MapUtils.getString(node, "checkPersonType");
            String checkPerson = switch (type) {
                case "role" -> sysUserMapper.selectAuditUserNamesByRoleKey(MapUtils.getString(node, "checkPersonRole"));
                case "dept" -> sysUserMapper.selectAuditUserNamesByOrgId(MapUtils.getString(node, "checkPersonOrg"));
                case "deptRole" ->
                        sysUserMapper.selectAuditUserNamesByOrgAndRoleKey(MapUtils.getLong(node, "checkPersonOrg"), MapUtils.getString(node, "checkPersonRole"));
                // user 直接去node查出来的nickName
                default -> MapUtils.getString(node, "nodeName");
                //check_person_role字段 当check_person_type是user时存的是roleId,其他是roleKey
            };
            node.put("checkPerson", checkPerson);
        }
    }


    /**
     * 设置管理员是否在介入审核当前流程
     *
     * @param sysAuditTask
     */
    private void setAdminIntervention(SysAuditTask sysAuditTask) {
        boolean admin = SecurityUtils.getLoginUser().getSysUser().isAdmin();
        //仅是超级管理员时进行判断
        if (admin) {
            Long userid = SecurityUtils.getLoginUser().getUserid();
            //可以审核的审核的审核节点
            List<Map<String, Long>> cacheMapList = redisService.getCacheObject("audit:info:" + userid);
            String isIntervention = "Y";
            if (CollectionUtils.isNotEmpty(cacheMapList)) {
                Long auditId = sysAuditTask.getAuditId();
                Long nowCheckStep = sysAuditTask.getNowCheckStep();
                for (Map<String, Long> map : cacheMapList) {
                    Long tempAuditId = MapUtils.getLong(map, "auditId");
                    Long tempConfigId = MapUtils.getLong(map, "configId");
                    if (Objects.equals(auditId, tempAuditId) && Objects.equals(nowCheckStep, tempConfigId)) {
                        isIntervention = "N";
                        break;
                    }
                }
            }
            sysAuditTask.setAdminAccessing(isIntervention);
        }
    }

    /**
     * 查询审核任务列表 进行中的
     *
     * @param sysAuditTask 审核任务
     * @return 审核任务
     */
    @Override
    public List<SysAuditTask> selectSysAuditTaskList(SysAuditTask sysAuditTask) {
        Long userid = SecurityUtils.getLoginUser().getUserid();
        List<Map<String, Long>> cacheMapList = redisService.getCacheObject("audit:info:" + userid);
        boolean admin = SecurityUtils.getLoginUser().getSysUser().isAdmin();
        if (!admin && CollectionUtils.isEmpty(cacheMapList)) {
            return Collections.emptyList();
        }
        PageUtils.startPage();
        List<SysAuditTask> sysAuditTasks = sysAuditTaskMapper.selectSysAuditTaskListByUserCache(cacheMapList, admin, sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer());
        setBusinessName(sysAuditTasks);
        if (CollectionUtils.isNotEmpty(sysAuditTasks)) {
            sysAuditTasks.forEach(this::setAdminIntervention);
        }
        return sysAuditTasks;
    }

    @Override
    public TableDataInfo selectSysAuditTaskListPage(SysAuditTask sysAuditTask) {
        TableDataInfo rspData = new TableDataInfo();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Long userid = SecurityUtils.getLoginUser().getUserid();
        List<Map<String, Long>> cacheMapList = redisService.getCacheObject("audit:info:" + userid);
        boolean admin = SecurityUtils.getLoginUser().getSysUser().isAdmin();
        if (!admin && CollectionUtils.isEmpty(cacheMapList)) {
            return rspData;
        }
        List<SysAuditTask> sysAuditTasks = sysAuditTaskMapper.selectSysAuditTaskListByUserCache(cacheMapList, admin, sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer());
        sysAuditTasks = filterTeacherSchool(sysAuditTasks, sysAuditTask.getTeacherSchoolName());
        List<SysAuditTask> paginate = PageUtils.paginate(sysAuditTasks, pageNum, pageSize);
        if (CollectionUtils.isNotEmpty(paginate)) {
            paginate.forEach(this::setAdminIntervention);
            if (Constants.IDENTITY_TYPE_TEACHER.equals(sysAuditTask.getAuditType())) {
                paginate.forEach(this::setTeacherNamesBySchoolId);
            }
        }
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(paginate);
        rspData.setMsg("查询成功");
        rspData.setTotal(new com.github.pagehelper.PageInfo(sysAuditTasks).getTotal());
        return rspData;
    }

    /**
     * 查询审核任务列表 已完成
     *
     * @param sysAuditTask
     * @return
     */
    @Override
    public List<SysAuditTask> selectSysAuditTaskFinishList(SysAuditTask sysAuditTask) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        List<SysAuditTask> sysAuditTasks = new ArrayList<>();
        if (sysUser.isAdmin()) {
            sysAuditTasks = sysAuditTaskMapper.selectSysAuditTaskFinishedListAdmin(sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer(), sysAuditTask.getCheckStatus());
        } else {
            sysAuditTasks = sysAuditTaskMapper.selectFinishedList(sysUser.getUserId(), sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer(), sysAuditTask.getCheckStatus());
        }
        setBusinessName(sysAuditTasks);
        return sysAuditTasks;
    }

    @Override
    public TableDataInfo selectSysAuditTaskFinishListPage(SysAuditTask sysAuditTask) {
        TableDataInfo rspData = new TableDataInfo();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        List<SysAuditTask> sysAuditTasks = new ArrayList<>();
        if (sysUser.isAdmin()) {
            sysAuditTasks = sysAuditTaskMapper.selectSysAuditTaskFinishedListAdmin(sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer(), sysAuditTask.getCheckStatus());
        } else {
            sysAuditTasks = sysAuditTaskMapper.selectFinishedList(sysUser.getUserId(), sysAuditTask.getAuditType(), sysAuditTask.getAuditTitle(), sysAuditTask.getSubPer(), sysAuditTask.getCheckStatus());
        }
        sysAuditTasks = filterTeacherSchool(sysAuditTasks, sysAuditTask.getTeacherSchoolName());
        List<SysAuditTask> paginate = PageUtils.paginate(sysAuditTasks, pageNum, pageSize);
        if (Constants.IDENTITY_TYPE_TEACHER.equals(sysAuditTask.getAuditType())) {
            paginate.forEach(this::setTeacherNamesBySchoolId);
        }
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(paginate);
        rspData.setMsg("查询成功");
        rspData.setTotal(new com.github.pagehelper.PageInfo(sysAuditTasks).getTotal());
        return rspData;
    }

    /**
     * 设置学校名称/事项标题，筛选老师学校
     *
     * @param sysAuditTasks
     * @param teacherSchoolFilter
     * @return
     */
    private List<SysAuditTask> filterTeacherSchool(List<SysAuditTask> sysAuditTasks, String teacherSchoolFilter) {
        if (CollectionUtils.isNotEmpty(sysAuditTasks)) {
            setBusinessName(sysAuditTasks);
            if (StringUtils.isNotBlank(teacherSchoolFilter)) {
                //筛选老师学校
                return sysAuditTasks.stream()
                        .filter(u -> {
                            String teacherSchool = u.getTeacherSchoolName();
                            if (StringUtils.isBlank(teacherSchool)) {
                                return false;
                            }
                            return teacherSchool.contains(teacherSchoolFilter.trim());
                        })
                        .collect(Collectors.toList());
            }
        }
        return sysAuditTasks;
    }

    /**
     * 统计同一个学校老师数量及姓名
     *
     * @param sysAuditTask
     */
    private void setTeacherNamesBySchoolId(SysAuditTask sysAuditTask) {
        String teacherSchool = sysAuditTask.getTeacherSchool();
        if (StringUtils.isNotBlank(teacherSchool)) {
            List<Map<String, Object>> teacherNameBySchoolId = identityInfoService.getTeacherNameBySchoolId(teacherSchool);
            sysAuditTask.setSchoolTeacherNames(teacherNameBySchoolId);
            sysAuditTask.setSchoolTeacherCount(CollectionUtils.isNotEmpty(teacherNameBySchoolId) ? teacherNameBySchoolId.size() + "" : "0");
        }
    }


    /**
     * 设置业务名称（页面/赛事/课程）
     *
     * @param sysAuditTasks
     */
    public void setBusinessName(List<SysAuditTask> sysAuditTasks) {
        sysAuditTasks.forEach(item -> {
            Long businessId = item.getBusinessId();
            String businessDetail = item.getBusinessDetails();
            switch (returnType(item.getAuditType())) {
                case TdConstants.AUDIT_FLOW_TYPE_PAGE:
                    String platform = null, url = null, version = null;
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        platform = jsonObject.getString("displayPlatform");
                        url = jsonObject.getString("url");
                        version = jsonObject.getString("version");
                    } else {
                        PageManagerInfo pageInfo = remoteContentService.getContentDetailInfoById(businessId, SecurityConstants.INNER).getData();
                        if (!Objects.isNull(pageInfo)) {
                            platform = pageInfo.getDisplayPlatform();
                            url = pageInfo.getUrl();
                            version = pageInfo.getVersion() + "";
                        }
                    }
                    SysDictData displayPlatform = dictTypeService.selectDictDataByTypeAndValue("display_platform", platform);
                    String platformLabel = Objects.isNull(displayPlatform) ? "" : displayPlatform.getDictLabel();
                    SysDictData cmsPagePath = dictTypeService.selectDictDataByTypeAndValue("cms_page_path", url);
                    String pagePathLabel = Objects.isNull(cmsPagePath) ? "" : cmsPagePath.getDictLabel();
                    item.setBusinessName(pagePathLabel + "-" + platformLabel + "-V" + version);
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_RACE:
                    //赛事详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("competitionName"));
                        break;
                    }
                    CompetitionDetailInfo raceInfo = competitionService.getCompetitionDetailInfoById(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(raceInfo) ? null : raceInfo.getCompetitionName());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_RACETRACK:
                    //赛道详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("competitionSeriesName") + "-" + jsonObject.getString("competitionName") + "-" + jsonObject.getString("competitionTrackName"));
                        break;
                    }
                    CompetitionTrackInfo data1 = competitionService.getInnerCompetitionTrackDetail(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(data1) ? null : data1.getCompetitionSeriesName() + data1.getCompetitionName() + data1.getCompetitionTrackName());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_COURSE:
                    //课程详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("name"));
                        break;
                    }
                    CourseInfo courseInfo = remoteCourseService.getCourseDetailInfoById(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(courseInfo) ? null : courseInfo.getName());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_TEAM:
                    //团队
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("teamName"));
                        break;
                    }
                    TeamManagerInfo team = competitionService.getInnerTeamDetailInfo(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(team) ? null : team.getTeamName());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_USER:
                    // 身份认证
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("realName"));
                        item.setTeacherSchoolName(jsonObject.getString("schoolName"));
                        item.setTeacherSchool(jsonObject.getString("school"));
                        break;
                    }
                    IdentityInfo identityInfo = identityInfoService.selectIdentityInfoByAuthId(null, businessId, null);
                    item.setBusinessName(Objects.isNull(identityInfo) ? null : identityInfo.getRealName());
                    item.setTeacherSchoolName(Objects.isNull(identityInfo) ? null : identityInfo.getSchoolName());
                    item.setTeacherSchool(Objects.isNull(identityInfo) ? null : identityInfo.getSchool());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_INFO:
                    // 资讯
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("newsTitle"));
                        break;
                    }
                    NewsInfo newsInfo = remoteContentService.getNewsDetailInfoById(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(newsInfo) ? null : newsInfo.getNewsTitle());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_APPLY:
                    //  报名详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName("1".equals(jsonObject.getString("joinType")) ? jsonObject.getString("userName") : jsonObject.getString("teamName"));
                        break;
                    }
                    CompetitionApplyInfo data = competitionService.getInnerApplyDetailInfo(businessId, SecurityConstants.INNER).getData();
                    //joinType 1单人2团队
                    item.setBusinessName(Objects.isNull(data) ? null : ("1".equals(data.getJoinType()) ? data.getUserName() : data.getTeamName()));
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_NOTICE:
                    //  公告详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("noticeTitle"));
                        break;
                    }
                    NoticeInfo notice = remoteContentService.getNoticeDetailInfoById(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(notice) ? null : notice.getNoticeTitle());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO:
                    // 章节视频详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("courseName") + " " + jsonObject.getString("chapterName"));
                        break;
                    }
                    CourseChapterInfo chapterInfo = remoteCourseService.getChapterVideoDetailInfoById(businessId, SecurityConstants.INNER).getData();
                    item.setBusinessName(Objects.isNull(chapterInfo) ? null : chapterInfo.getCourseName() + " " + chapterInfo.getChapterName());
                    break;
                case TdConstants.AUDIT_FLOW_TYPE_REALNAME:
                    // 实名认证详情
                    if (StringUtils.isNotBlank(businessDetail)) {
                        JSONObject jsonObject = JSONObject.parseObject(businessDetail);
                        item.setBusinessName(jsonObject.getString("realName") + "-" + jsonObject.getString("idCardTypeName") + "-" + jsonObject.getString("idCard"));
                    }
                    AuthInfo authInfo = authInfoMapper.selectAuthInfoById(businessId);
                    item.setBusinessName(Objects.isNull(authInfo) ? null : authInfo.getRealName() + "-" + authInfo.getIdCardTypeName() + "-" + authInfo.getIdCard());
                    break;
            }
        });
    }

    /**
     * 新增审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
//    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public int insertSysAuditTask(SysAuditTask sysAuditTask) {
        //匹配到审核流程  前端传值auditType和businessId  章节视频时不传值
        String auditType = StringUtils.isBlank(sysAuditTask.getAuditType()) ? TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO : sysAuditTask.getAuditType();
        /*if (StringUtils.isBlank(auditType) || Objects.isNull(sysAuditTask.getBusinessId())) {
            throw new ServiceException("参数错误");
        }*/
        SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectNewVersionAuditMainConfigByType(auditType);
        if (Objects.isNull(sysAuditMainConfig)) {
            throw new ServiceException("对应类型的审核流程不存在");
        }
        if ("0".equals(sysAuditMainConfig.getIsEnable())) {
            //有流程没启用 直接通过 通过table,businessId修改check_status  整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
            if (TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO.equals(auditType)) {
                return updateChapterVideoStatus(sysAuditTask.getChapterAuditResult(), TdConstants.CHECK_STATUS_TG);
            }
            return updateBusinessStatus(auditType, sysAuditTask.getBusinessId(), TdConstants.CHECK_STATUS_TG, null);
        }
        //过滤开启状态的 isEnable=1的
        List<SysAuditConfig> list = sysAuditMainConfig.getSysAuditConfigList().stream().filter(item -> "1".equals(item.getIsEnable())).toList();
        if (CollectionUtils.isEmpty(list)) {
            if (TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO.equals(auditType)) {
                return updateChapterVideoStatus(sysAuditTask.getChapterAuditResult(), TdConstants.CHECK_STATUS_TG);
            }
            return updateBusinessStatus(auditType, sysAuditTask.getBusinessId(), TdConstants.CHECK_STATUS_TG, null);
        }
        //需要走流程的 修改业务表审核状态为审核中
        //sysAuditTaskMapper.updateAuditStatusByTableAndId(table, sysAuditTask.getBusinessId(), "3");
        if (TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO.equals(auditType)) {
            ChapterAuditResult chapterAuditResult = sysAuditTask.getChapterAuditResult();
            sysAuditTask.setBusinessId(chapterAuditResult.getChapterId());
            Set<Long> collect = chapterAuditResult.getPageInfo().stream().map(PageInfo::getPageId).collect(Collectors.toSet());
            String videoIds = collect.stream().map(String::valueOf).collect(Collectors.joining(","));
            sysAuditTask.setVideoIds(videoIds);
            updateChapterVideoStatus(sysAuditTask.getChapterAuditResult(), TdConstants.CHECK_STATUS_SHZ);
        } else {
            int i = updateBusinessStatus(auditType, sysAuditTask.getBusinessId(), TdConstants.CHECK_STATUS_SHZ, null);
        }
        //业务表名  业务id传过来的
        sysAuditTask.setBusinessTable(getTableName(auditType));
        //审核流程id
        sysAuditTask.setAuditId(sysAuditMainConfig.getAuditId());
        //当前审核环节（节点id） 默认第一个节点s
        List<SysAuditConfig> listSorted = list.stream().sorted(Comparator.comparing(SysAuditConfig::getLevelSort)).toList();
        sysAuditTask.setNowCheckStep(listSorted.get(0).getConfigId());
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        //提交人
        sysAuditTask.setSubPer(sysUser.getUserId() + "");
        //创建人
        sysAuditTask.setCreateBy(sysUser.getNickName());
        Date nowDate = DateUtils.getNowDate();
        //提交时间
        sysAuditTask.setSubTime(nowDate);
        //创建时间
        sysAuditTask.setCreateTime(nowDate);
        //整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
        sysAuditTask.setCheckStatus(TdConstants.CHECK_STATUS_SHZ);
        setBusinessDetail(sysAuditTask);
        return sysAuditTaskMapper.insertSysAuditTask(sysAuditTask);
    }

    /**
     * 身份认证  新增审核
     *
     * @param sysAuditTask
     * @return
     */
    @Override
    public String authUserInsertSysAuditTask(SysAuditTask sysAuditTask) {
        String auditType = sysAuditTask.getAuditType();
        SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectNewVersionAuditMainConfigByType(auditType);
        if (Objects.isNull(sysAuditMainConfig)) {
            throw new ServiceException("对应类型的审核流程不存在");
        }
        if ("0".equals(sysAuditMainConfig.getIsEnable())) {
            //有流程没启用 直接通过 通过table,businessId修改check_status  整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
            return TdConstants.CHECK_STATUS_TG;
        }
        //过滤开启状态的 isEnable=1的
        List<SysAuditConfig> list = sysAuditMainConfig.getSysAuditConfigList().stream().filter(item -> "1".equals(item.getIsEnable())).toList();
        if (CollectionUtils.isEmpty(list)) {
            return TdConstants.CHECK_STATUS_TG;
        }
        //需要走流程的 修改业务表审核状态为审核中
        //sysAuditTaskMapper.updateAuditStatusByTableAndId(table, sysAuditTask.getBusinessId(), "3");
        //业务表名  业务id传过来的
        sysAuditTask.setBusinessTable(getTableName(auditType));
        //审核流程id
        sysAuditTask.setAuditId(sysAuditMainConfig.getAuditId());
        //当前审核环节（节点id） 默认第一个节点s
        List<SysAuditConfig> listSorted = list.stream().sorted(Comparator.comparing(SysAuditConfig::getLevelSort)).toList();
        sysAuditTask.setNowCheckStep(listSorted.get(0).getConfigId());
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        //提交人
        sysAuditTask.setSubPer(sysUser.getUserId() + "");
        //创建人
        sysAuditTask.setCreateBy(sysUser.getNickName());
        Date nowDate = DateUtils.getNowDate();
        //提交时间
        sysAuditTask.setSubTime(nowDate);
        //创建时间
        sysAuditTask.setCreateTime(nowDate);
        //整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
        sysAuditTask.setCheckStatus(TdConstants.CHECK_STATUS_SHZ);
        setBusinessDetail(sysAuditTask);
        sysAuditTaskMapper.insertSysAuditTask(sysAuditTask);
        return TdConstants.CHECK_STATUS_SHZ;
    }

    /**
     * 实名认证审核
     * 实名认证审核，不包含身份证认证
     *
     * @param authInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int realNameAuthAuditTask(AuthInfo authInfo) throws Exception {
        if (StringUtils.isBlank(authInfo.getRealName()) || StringUtils.isBlank(authInfo.getIdCard()) || StringUtils.isBlank(authInfo.getIdCardType()) || StringUtils.isBlank(authInfo.getIdCardFront())) {
            throw new ServiceException("参数错误");
        }
        authInfo.setIdCard(RsaUtils.decryptByPrivateKey(authInfo.getIdCard()));
        //同一个类型同一个编号只能认证成功一次
        AuthInfo authInfoTemp = authInfoMapper.selectIdentityInfoByType(authInfo);
        if (Objects.nonNull(authInfoTemp)) {
            throw new ServiceException("此类型证件号：" + authInfo.getIdCard() + "已认证,不能重复使用");
        }
        SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectNewVersionAuditMainConfigByType("realName");
        if (Objects.isNull(sysAuditMainConfig)) {
            throw new ServiceException("对应类型的审核流程不存在");
        }
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Date nowDate = DateUtils.getNowDate();
        authInfo.setAuthTime(nowDate);
        authInfo.setCreateTime(nowDate);
        authInfo.setCreateBy(sysUser.getUserId() + "");
        authInfo.setUserId(sysUser.getUserId());
        authInfo.setIdCard(authInfo.getIdCard());
        SysUser user = new SysUser();
        user.setUserId(sysUser.getUserId());
        if ("0".equals(sysAuditMainConfig.getIsEnable())) {
            //有流程没启用 直接通过   整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
            authInfo.setCheckStatus(TdConstants.CHECK_STATUS_TG);
            authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
            user.setAuthStatus(Constants.AUTH_STATUS_PASS);
            sysUserMapper.updateUser(user);
            return authInfoMapper.insertAuthInfo(authInfo);
        }
        //过滤开启状态的 isEnable=1的
        List<SysAuditConfig> list = sysAuditMainConfig.getSysAuditConfigList().stream().filter(item -> "1".equals(item.getIsEnable())).toList();
        if (CollectionUtils.isEmpty(list)) {
            authInfo.setCheckStatus(TdConstants.CHECK_STATUS_TG);
            authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
            user.setAuthStatus(Constants.AUTH_STATUS_PASS);
            sysUserMapper.updateUser(user);
            return authInfoMapper.insertAuthInfo(authInfo);
        }
        //需要走流程的 修改业务表审核状态为审核中
        authInfo.setCheckStatus(TdConstants.CHECK_STATUS_SHZ);
        authInfo.setAuthStatus(Constants.AUTH_STATUS_CHECKING);
        authInfoMapper.insertAuthInfo(authInfo);
        user.setAuthStatus(Constants.AUTH_STATUS_CHECKING);
        sysUserMapper.updateUser(user);
        //业务表名
        SysAuditTask sysAuditTask = new SysAuditTask();
        sysAuditTask.setAuditType(TdConstants.AUDIT_FLOW_TYPE_REALNAME);
        sysAuditTask.setBusinessTable(getTableName("realName"));
        sysAuditTask.setBusinessId(authInfo.getAuthId());
        //审核流程id
        sysAuditTask.setAuditId(sysAuditMainConfig.getAuditId());
        //当前审核环节（节点id） 默认第一个节点s
        List<SysAuditConfig> listSorted = list.stream().sorted(Comparator.comparing(SysAuditConfig::getLevelSort)).toList();
        sysAuditTask.setNowCheckStep(listSorted.get(0).getConfigId());
        //提交人
        sysAuditTask.setSubPer(sysUser.getUserId() + "");
        //创建人
        sysAuditTask.setCreateBy(sysUser.getNickName());
        //提交时间
        sysAuditTask.setSubTime(nowDate);
        //创建时间
        sysAuditTask.setCreateTime(nowDate);
        //整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
        sysAuditTask.setCheckStatus(TdConstants.CHECK_STATUS_SHZ);
        setBusinessDetail(sysAuditTask);
        return sysAuditTaskMapper.insertSysAuditTask(sysAuditTask);
    }

    /**
     * 根据类型获取表名
     *
     * @param auditType 类型
     * @return 表名
     */
    private String getTableName(String auditType) {
        return switch (returnType(auditType)) {
            case TdConstants.AUDIT_FLOW_TYPE_PAGE -> TdConstants.TABLE_NAME_PAGE;
            case TdConstants.AUDIT_FLOW_TYPE_RACE -> TdConstants.TABLE_NAME_RACE;
            case TdConstants.AUDIT_FLOW_TYPE_COURSE -> TdConstants.TABLE_NAME_COURSE;
            case TdConstants.AUDIT_FLOW_TYPE_USER -> TdConstants.TABLE_NAME_IDENTITY;
            case TdConstants.AUDIT_FLOW_TYPE_TEAM -> TdConstants.TABLE_NAME_TEAM;
            case TdConstants.AUDIT_FLOW_TYPE_INFO -> TdConstants.TABLE_NAME_INFO;
            case TdConstants.AUDIT_FLOW_TYPE_APPLY -> TdConstants.TABLE_NAME_APPLY;
            case TdConstants.AUDIT_FLOW_TYPE_NOTICE -> TdConstants.TABLE_NAME_NOTICE;
            case TdConstants.AUDIT_FLOW_TYPE_CHAPTERVIDEO -> TdConstants.TABLE_NAME_CHAPTERVIDEO;
            case TdConstants.AUDIT_FLOW_TYPE_REALNAME -> TdConstants.TABLE_NAME_REALNAME;
            case TdConstants.AUDIT_FLOW_TYPE_RACETRACK -> TdConstants.TABLE_NAME_RACETRACK;
            default -> "";
        };
    }

    /**
     * 学生认证、教师认证、学校认证、企业认证都走用户审核流程
     *
     * @param auditType
     * @return
     */
    private String returnType(String auditType) {
        if (TdConstants.AUDIT_FLOW_TYPE_STUDENT.equals(auditType) ||
                TdConstants.AUDIT_FLOW_TYPE_TEACHER.equals(auditType) ||
                TdConstants.AUDIT_FLOW_TYPE_SCHOOL.equals(auditType) ||
                TdConstants.AUDIT_FLOW_TYPE_ENTERPRISE.equals(auditType)) {
            return TdConstants.AUDIT_FLOW_TYPE_USER;
        }
        return auditType;
    }

    /**
     * 修改业务表审核状态
     *
     * @param type         类型 page页面，race赛事，course课程,team团队,user用户
     * @param businessId   主键
     * @param status       状态  整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     * @param checkOpinion 审核意见
     * @return 结果
     */
    private int updateBusinessStatus(String type, Long businessId, String status, String checkOpinion) {
        int result = 0;
        switch (returnType(type)) {
            case TdConstants.AUDIT_FLOW_TYPE_PAGE:
                //页面
                result = remoteContentService.updateContentInfoStatus(new PageInfo(businessId, status, checkOpinion), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_RACE:
                // 赛事
                result = competitionService.updateCompetitionInfoStatus(new CompetitionSeriesInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_COURSE:
                //课程
                result = remoteCourseService.updateCourseInfoStatus(new PageInfo(businessId, status, checkOpinion), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_TEAM:
                //团队
                result = competitionService.updateTeamManagerStatus(new TeamManagerInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_USER:
                //身份认证改成本地
                result = identityInfoService.updateIdentityInfoStatus(new IdentityInfo(businessId, status));
                break;
            case TdConstants.AUDIT_FLOW_TYPE_INFO:
                // 资讯改状态
                result = remoteContentService.updateNewsInfoStatus(new NewsInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_APPLY:
                // 修改报名状态
                result = competitionService.updateCompetitionApplyInfoStatus(new CompetitionApplyInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_NOTICE:
                // 公告改状态
                result = remoteContentService.updateNoticeInfoStatus(new NoticeInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_REALNAME:
                // 实名认证改状态
                result = authInfoMapper.updateAuthInfo(new AuthInfo(businessId, status, checkOpinion));
                AuthInfo authInfo = authInfoMapper.selectAuthInfoById(businessId);
                SysUser user = new SysUser();
                user.setUserId(authInfo.getUserId());
                user.setAuthStatus(TdConstants.CHECK_STATUS_TG.equals(status) ? Constants.AUTH_STATUS_PASS : Constants.AUTH_STATUS_FAIL);
                sysUserMapper.updateUser(user);
                break;
            case TdConstants.AUDIT_FLOW_TYPE_RACETRACK:
                // 赛道改状态
                result = competitionService.updateCompetitionTrackStatus(new CompetitionTrackInfo(businessId, status), SecurityConstants.INNER).getData();
                break;
        }
        return result;
    }

    /**
     * 更新章节视频信息审核状态和发布状态  按章节id来
     *
     * @param auditResult 审核信息
     * @param status      状态
     * @return
     */
    public int updateChapterVideoStatus(ChapterAuditResult auditResult, String status) {
        if (StringUtils.isNotBlank(status)) {
            auditResult.getPageInfo().forEach(item -> {
                item.setCheckStatus(status);
            });
        }
        return remoteCourseService.updateChapterVideoStatus(auditResult, SecurityConstants.INNER).getData();
    }

    /**
     * 审核
     *
     * @param subInfo 审核任务
     * @return
     */
    @Override
    public int sysAuditTaskDoAudit(SysAuditTaskSubinfo subInfo) {
        //整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Date nowDate = DateUtils.getNowDate();
        subInfo.setCheckPer(sysUser.getUserId());
        subInfo.setCheckTime(nowDate);
        subInfo.setCreateBy(sysUser.getNickName());
        subInfo.setUpdateBy(sysUser.getNickName());
        subInfo.setCreateTime(nowDate);
        subInfo.setUpdateTime(nowDate);
        subInfo.setUserId(sysUser.getUserId());
        subInfo.setOrgId(sysUser.getOrgId());
        //新增审核信息
        int insertResult = sysAuditTaskSubinfoMapper.insertSysAuditTaskSubinfo(subInfo);
        //获取任务信息及对应审核流程类型
        SysAuditTask sysAuditTask = sysAuditTaskMapper.selectSysAuditTaskAndAuditTypeByTaskId(subInfo.getTaskId());
        String checkStatus = subInfo.getCheckStatus();
        String checkOpinion = subInfo.getCheckOpinion();
        String auditType = sysAuditTask.getAuditType();
        //如果是拒绝 直接修改业务表状态 并更新任务信息审核状态和当前环节id为null 结束流程
        if (TdConstants.CHECK_STATUS_JJ.equals(checkStatus)) {
            //修改业务表状态
            updateBusinessStatus(auditType, sysAuditTask.getBusinessId(), checkStatus, checkOpinion);
            return updateTaskStatus(sysAuditTask.getTaskId(), checkStatus, null, sysUser, nowDate);
        }
        //如果是通过 判断下一个审核环节是否存在 没有则修改业务表状态 并更新任务信息审核状态和当前环节id为null 结束流程 有则更新任务信息审核状态和当前环节id
        if (TdConstants.CHECK_STATUS_TG.equals(checkStatus)) {
            //获取对应审核的详细配置包括节点配置
            SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectSysAuditMainConfigByAuditId(sysAuditTask.getAuditId());
            //启用的环节
            List<SysAuditConfig> enabledConfigs = sysAuditMainConfig.getSysAuditConfigList()
                    .stream()
                    .filter(item -> "1".equals(item.getIsEnable()))
                    .collect(Collectors.toList());
            //当前环节id
            Long currentStep = sysAuditTask.getNowCheckStep();
            //下个环节id
            Long nextStep = findNextStep(enabledConfigs, currentStep);
            if (nextStep == null) {
                //没有下个环节表示结束 更新业务表状态 并更新任务信息审核状态和当前环节id为null 结束流程
                updateBusinessStatus(auditType, sysAuditTask.getBusinessId(), checkStatus, checkOpinion);
                return updateTaskStatus(sysAuditTask.getTaskId(), checkStatus, null, sysUser, nowDate);
            } else {
                //有下个环节，修改任务表当前环节id为下个环节id
                return updateTaskStatus(sysAuditTask.getTaskId(), null, nextStep, sysUser, null);
            }
        }
        return insertResult;
    }

    @Override
    public int sysAuditTaskDoAudits(List<SysAuditTaskSubinfo> subInfos) {
        subInfos.forEach(this::sysAuditTaskDoAudit);
        return 1;
    }

    /**
     * 章节视频审核
     *
     * @param subInfo 审核任务
     * @return
     */
    @Override
    public int sysAuditTaskVideoDoAudit(SysAuditTaskSubinfo subInfo) {
        //审核信息 包括章节id和各个视频的id、审核状态、审核意见
        ChapterAuditResult chapterAuditResult = subInfo.getChapterAuditResult();
        Date nowDate = DateUtils.getNowDate();
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        chapterAuditResult.getPageInfo().forEach(item -> {
            SysAuditTaskSubinfo info = new SysAuditTaskSubinfo();
            info.setTaskId(subInfo.getTaskId());
            info.setAuditConfigId(subInfo.getAuditConfigId());
            //整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
            info.setCheckPer(sysUser.getUserId());
            info.setCheckTime(nowDate);
            info.setCreateBy(sysUser.getNickName());
            info.setUpdateBy(sysUser.getNickName());
            info.setCreateTime(nowDate);
            info.setUpdateTime(nowDate);
            info.setUserId(sysUser.getUserId());
            info.setOrgId(sysUser.getOrgId());
            info.setVideoId(item.getPageId());
            info.setCheckStatus(item.getCheckStatus());
            info.setCheckOpinion(item.getApplyReason());
            info.setAdminIntervention(subInfo.getAdminIntervention());
            //新增审核信息
            sysAuditTaskSubinfoMapper.insertSysAuditTaskSubinfo(info);
        });
        //修改视频的审核状态
        updateChapterVideoStatus(chapterAuditResult, null);
        //chapterAuditResult.getPageInfo()中筛选审核状态是通过的
        List<PageInfo> passList = chapterAuditResult.getPageInfo().stream()
                .filter(item -> TdConstants.CHECK_STATUS_TG.equals(item.getCheckStatus()))
                .toList();
        //还有需要继续审核的
        if (CollectionUtils.isNotEmpty(passList)) {
            SysAuditTask sysAuditTask = sysAuditTaskMapper.selectSysAuditTaskAndAuditTypeByTaskId(subInfo.getTaskId());
            //获取对应审核的详细配置包括节点配置
            SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectSysAuditMainConfigByAuditId(sysAuditTask.getAuditId());
            //启用的环节
            List<SysAuditConfig> enabledConfigs = sysAuditMainConfig.getSysAuditConfigList()
                    .stream()
                    .filter(item -> "1".equals(item.getIsEnable()))
                    .collect(Collectors.toList());
            //当前环节id
            Long currentStep = sysAuditTask.getNowCheckStep();
            //下个环节id
            Long nextStep = findNextStep(enabledConfigs, currentStep);
            if (nextStep == null) {
                //没有下个环节表示结束 更新业务表状态 并更新任务信息审核状态和当前环节id为null 结束流程
                return updateTaskStatus(sysAuditTask.getTaskId(), null, null, sysUser, nowDate);
            } else {
                //有下个环节，修改任务表当前环节id为下个环节id
                return updateTaskStatus(sysAuditTask.getTaskId(), null, nextStep, sysUser, null);
            }
        } else {
            //审核结束，更新审核任务最终状态
            return updateTaskStatus(subInfo.getTaskId(), TdConstants.CHECK_STATUS_JJ, null, sysUser, nowDate);
        }
    }

    /**
     * 查找下一个审核环节id
     *
     * @param configs     启用的审核节点
     * @param currentStep 当前环节id
     * @return
     */
    private Long findNextStep(List<SysAuditConfig> configs, Long currentStep) {
        if (currentStep == null) {
            return null;
        }
        for (int i = 0; i < configs.size(); i++) {
            if (currentStep.equals(configs.get(i).getConfigId())) {
                return i + 1 < configs.size() ? configs.get(i + 1).getConfigId() : null;
            }
        }
        return null;
    }

    private Long findNextStepConfigId(List<Map<String, Object>> configs, Long currentStep) {
        if (currentStep == null) {
            return null;
        }
        for (int i = 0; i < configs.size(); i++) {
            if (currentStep.equals(MapUtils.getLong(configs.get(i), "configId"))) {
                return i + 1 < configs.size() ? MapUtils.getLong(configs.get(i + 1), "configId") : null;
            }
        }
        return null;
    }

    /**
     * 更新任务信息审核状态和当前环节id
     *
     * @param taskId      任务id
     * @param checkStatus 最终审核状态
     * @param nextStep    下个节点id
     * @param sysUser     用户信息
     * @param nowDate     当前时间
     * @return
     */
    private int updateTaskStatus(Long taskId, String checkStatus, Long nextStep, SysUser sysUser, Date nowDate) {
        SysAuditTask task = new SysAuditTask();
        task.setTaskId(taskId);
        task.setCheckStatus(checkStatus);
        task.setNowCheckStep(nextStep);
        task.setCheckTime(nowDate);
        task.setUpdateTime(nowDate);
        task.setCreateBy(sysUser.getNickName());
        return sysAuditTaskMapper.updateSysAuditTask(task);
    }


    /**
     * 修改审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
    @Override
    public int updateSysAuditTask(SysAuditTask sysAuditTask) {
        sysAuditTask.setUpdateTime(DateUtils.getNowDate());
        return sysAuditTaskMapper.updateSysAuditTask(sysAuditTask);
    }

    /**
     * 批量删除审核任务
     *
     * @param taskIds 需要删除的审核任务主键
     * @return 结果
     */
    @Override
    public int deleteSysAuditTaskByTaskIds(Long[] taskIds) {
        return sysAuditTaskMapper.deleteSysAuditTaskByTaskIds(taskIds);
    }

    /**
     * 删除审核任务信息
     *
     * @param taskId 审核任务主键
     * @return 结果
     */
    @Override
    public int deleteSysAuditTaskByTaskId(Long taskId) {
        return sysAuditTaskMapper.deleteSysAuditTaskByTaskId(taskId);
    }

    /**
     * 根据审核类型获取审核意见
     *
     * @param auditType  审核类型
     * @param businessId 业务id
     * @return 拒绝意见
     */
    @Override
    public String getCheckOpinion(String auditType, Long businessId) {
        String tableName = getTableName(auditType);
        if (StringUtils.isBlank(tableName)) {
            throw new ServiceException("参数错误");
        }
        return sysAuditTaskMapper.selectCheckOpinionByTaskId(tableName, businessId);
    }

    /**
     * 设置业务详情信息
     *
     * @param sysAuditTask
     */
    private void setBusinessDetail(SysAuditTask sysAuditTask) {
        if (StringUtils.isNotBlank(sysAuditTask.getBusinessDetails())) {
            //json字符串转map
            Map map = JSONUtil.toBean(sysAuditTask.getBusinessDetails(), Map.class);
            sysAuditTask.setBusinessDetail(map);
            return;
        }
        Object obj = null;
        Long businessId = sysAuditTask.getBusinessId();
        switch (returnType(sysAuditTask.getAuditType())) {
            case TdConstants.AUDIT_FLOW_TYPE_PAGE:
                obj = remoteContentService.getContentDetailInfoById(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_RACE:
                //赛事详情
                obj = competitionService.getCompetitionDetailInfoById(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_COURSE:
                obj = remoteCourseService.getCourseDetailInfoById(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_TEAM:
                obj = competitionService.getInnerTeamDetailInfo(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_USER:
                obj = identityInfoService.selectIdentityInfoByAuthId(null, businessId, null);
                break;
            case TdConstants.AUDIT_FLOW_TYPE_INFO:
                // 资讯详情
                obj = remoteContentService.getNewsDetailInfoById(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_APPLY:
                // 报名详情
                obj = competitionService.getInnerApplyDetailInfo(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_NOTICE:
                //公告详情
                obj = remoteContentService.getNoticeDetailInfoById(businessId, SecurityConstants.INNER).getData();
                break;
            case TdConstants.AUDIT_FLOW_TYPE_REALNAME:
                //实名认证详情
                try {
                    AuthInfo info = authInfoMapper.selectAuthInfoById(businessId);
                    info.setIdCard(RsaUtils.encryptByPublicKey(info.getIdCard()));
                    obj = info;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case TdConstants.AUDIT_FLOW_TYPE_RACETRACK:
                //赛道详情
                obj = competitionService.getInnerCompetitionTrackDetail(businessId, SecurityConstants.INNER).getData();
                break;
        }
        if (obj != null) {
            sysAuditTask.setBusinessDetail(BeanUtils.beanToMapIgnoreNullValue(obj));
            sysAuditTask.setBusinessDetails(JSONUtil.toJsonStr(obj));
        }
    }
}
