package com.teaching.competition.service.impl;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.*;
import com.teaching.competition.util.UUIDUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import com.teaching.common.core.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 赛事主数据Service业务层处理
 *
 * @author teaching
 * @date 2025-10-10
 */
@Service
public class CompetitionMainInfoServiceImpl implements ICompetitionMainInfoService
{
    private static final Logger log = LoggerFactory.getLogger(CompetitionMainInfoServiceImpl.class);
    @Autowired
    private CompetitionMainInfoMapper competitionMainInfoMapper;

    @Autowired
    private CompetitionStageConfigMapper stageConfigMapper;

    @Autowired
    private CompetitionCourseConfigMapper courseConfigMapper;

    @Autowired
    private CompetitionEnterpriseRelaMapper enterpriseRelaMapper;

    @Autowired
    private CompetitionAwardsConfigMapper awardsConfigMapper;

    @Autowired
    private CompetitionSeriesInfoMapper seriesInfoMapper;

    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderInfoService;

    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private OperationConfigMapper operationConfigMapper;

    /**
     * 查询赛事主数据详情
     *
     * @param req 赛事主数据主键
     * @return 赛事主数据
     */
    @Override
    public List<CompetitionDetailInfo> selectCompetitionDetailInfoByCompetitionId(CompetitionMainInfoReq req)
    {
        // 赛事基本信息
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoMapper.selectCompetitionDetailInfoByCompetitionId(req);
        if(CollectionUtils.isNotEmpty(competitionDetailInfoList)){
            competitionDetailInfoList.stream().forEach(competitionDetailInfo -> {
                if(null != competitionDetailInfo){
                    // 获取赛事系列配置
                    List<CompetitionStageConfig> stageConfigList = stageConfigMapper.selectCompetitionStageConfigList(competitionDetailInfo.getCompetitionSeriesId());
                    competitionDetailInfo.setCompetitionStageList(stageConfigList);
                    // 获取赛事关联课程配置
                    List<CompetitionCourseConfig> competitionCourseConfigs = courseConfigMapper.selectCompetitionCourseConfigList(competitionDetailInfo.getCompetitionSeriesId());
                    competitionDetailInfo.setCompetitionCourseConfigList(competitionCourseConfigs);
                    // 获取赛事赞助企业配置
                    List<CompetitionEnterpriseRela> competitionEnterpriseRelas = enterpriseRelaMapper.selectCompetitionEnterpriseRelaList(competitionDetailInfo.getCompetitionSeriesId());
                    competitionDetailInfo.setCompetitionEnterpriseRelaList(competitionEnterpriseRelas);
                    // 获取赛事报名修改信息权限配置
                    List<OperationConfig> operationConfigs = operationConfigMapper.selectOperationConfigByCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
                    competitionDetailInfo.setOperationConfigList(operationConfigs);
                }
                // 翻译
                // 赛事类型
                Map<String, SysDictData> competitionTypeMap = new HashMap<>();
                R<List<SysDictData>> competitionType = userService.dictType("competition_type", SecurityConstants.INNER);
                List<SysDictData> competitionTypeList = competitionType.getData();
                if (CollectionUtils.isNotEmpty(competitionTypeList) && StringUtils.isNotEmpty(competitionDetailInfo.getCompetitionType())) {
                    competitionTypeMap = competitionTypeList.stream().
                            collect(Collectors.toMap(SysDictData::getDictValue, SysDictData -> SysDictData));
                    competitionDetailInfo.setCompetitionTypeCn(competitionTypeMap.get(competitionDetailInfo.getCompetitionType()).getDictLabel());
                }
                // 赛事收藏数量
                Map<String,Object> params = new HashMap<>();
                params.put("competitionId",competitionDetailInfo.getCompetitionId());
                params.put("competitionSeriesId", competitionDetailInfo.getCompetitionSeriesId());
                Integer competitionCount = userCollectMapper.selectCollectCompetitionCount(params);
                competitionDetailInfo.setCompetitionCollectNum(
                        competitionCount ==null?0:competitionCount);
                // 赛事分享数量
                Integer shareNum = redisService.getCacheObject(competitionDetailInfo.getCompetitionId() + "");
                competitionDetailInfo.setCompetitionShareNum(shareNum==null?0:shareNum);
            });
        }
        if (req.getCompetitionNum() != null && competitionDetailInfoList.size() > req.getCompetitionNum()) {
            return competitionDetailInfoList.subList(0, req.getCompetitionNum());
        }
        return competitionDetailInfoList;
    }

    @Override
    public List<CompetitionDetailInfo> selectAllCompetitionDetailInfo(CompetitionMainInfoReq req) {
        // 赛事基本信息
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoMapper.selectCompetitionDetailInfoByCompetitionId(req);
        if(CollectionUtils.isNotEmpty(competitionDetailInfoList)){
            competitionDetailInfoList.stream().forEach(competitionDetailInfo -> {
                // 获取赛事系列配置
                List<CompetitionStageConfig> stageConfigList = stageConfigMapper.selectCompetitionStageConfigList(competitionDetailInfo.getCompetitionSeriesId());
                competitionDetailInfo.setCompetitionStageList(stageConfigList);
                // 获取赛事赛道等配置信息
                CompetitionTrackInfo competitionTrackInfo = new CompetitionTrackInfo();
                competitionTrackInfo.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
                competitionTrackInfo.setCheckStatus(Constants.CHECK_PASS);
                List<CompetitionTrackInfo> competitionTrackInfos = competitionTrackInfoMapper.selectCompetitionTrackInfoList((competitionTrackInfo));
                competitionDetailInfo.setCompetitionTrackList(competitionTrackInfos);
            });
        }
        return competitionDetailInfoList;
    }

    /**
     * 查询赛事主数据列表
     *
     * @param req 赛事主数据
     * @return 赛事主数据
     */
    @Override
    @DataScope(orgAlias = "a", userAlias = "a")
    public List<CompetitionMainInfo> selectCompetitionMainInfoList(CompetitionMainInfoReq req) {
        List<CompetitionMainInfo> competitionMainInfos = competitionMainInfoMapper.selectCompetitionMainInfoList(req);
        // 获取审核原因
        if (CollectionUtils.isNotEmpty(competitionMainInfos)) {
            competitionMainInfos.stream().forEach(competitionMainInfo -> {
                R<String> dataResult = orderInfoService.innerGetCheckOpinion
                        (TdConstants.AUDIT_FLOW_TYPE_RACE, competitionMainInfo.getCompetitionId(), SecurityConstants.INNER);
                if (R.isSuccess(dataResult)) {
                    competitionMainInfo.setApplyReason(dataResult.getData());
                }
            });
        }
        return competitionMainInfos;
    }

    @Override
    public List<Map<String, Object>> selectCompetitionMainInfoListInner(CompetitionMainInfoReq req) {
        List<Map<String, Object>> competitionMainInfoList = new ArrayList<>();
        req.setCheckStatus(Constants.COMPETITION_PUBLISH+","+Constants.COMPETITION_RUNNING);
        List<CompetitionMainInfo> competitionMainInfos = competitionMainInfoMapper.selectCompetitionMainInfoList(req);
        if(CollectionUtils.isNotEmpty(competitionMainInfos)){
            competitionMainInfos.stream().forEach(competitionMainInfo -> {
                Map<String, Object> competitionMainInfoMap = Map.of(
                        "competitionId", competitionMainInfo.getCompetitionId(),
                        "competitionSeriesId", competitionMainInfo.getCompetitionSeriesId(),
                        "competitionName", competitionMainInfo.getCompetitionSeriesName()+competitionMainInfo.getCompetitionName()
                );
                competitionMainInfoList.add(competitionMainInfoMap);
            });
        }
        return competitionMainInfoList;
    }

    @Override
    public List<CompetitionMainInfo> selectCompetitionSeriesInfoByCompetitionName(String competitionName) {
        return competitionMainInfoMapper.selectCompetitionSeriesInfoByCompetitionName(competitionName);
    }

    @Override
    public List<CompetitionMainInfo> selectCompetitionMainInfoPullDownList(CompetitionMainInfoReq req) {
        return competitionMainInfoMapper.selectCompetitionMainInfoList(req);
    }

    /**
     * 保存赛事主数据
     *
     * @param competitionDetailInfo 赛事主数据
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionDetailInfo insertCompetitionMainInfo(CompetitionDetailInfo competitionDetailInfo) {
        // 权限用户信息设置
        SysUser userInfo = SecurityUtils.getLoginUser().getSysUser();
        competitionDetailInfo.setUserId(userInfo.getUserId());
        competitionDetailInfo.setOrgId(userInfo.getOrgId());
        CompetitionMainInfo competitionMainInfo = new CompetitionMainInfo();
        BeanUtils.copyProperties(competitionDetailInfo, competitionMainInfo);
        if(null == competitionDetailInfo.getCompetitionId() && null == competitionDetailInfo.getCompetitionSeriesId()){
            int checkCount = competitionMainInfoMapper.checkCompetitionName(competitionDetailInfo.getCompetitionName(),competitionDetailInfo.getCompetitionSeriesName());
            // 赛事名称及赛事界数都重复校验
            if (checkCount > 0) {
                throw new GlobalException("该赛事名称已存在");
            }
//            List<CompetitionMainInfo> competitionMainInfos =
//                    competitionMainInfoMapper.selectCompetitionInfoByCompetitionName(competitionDetailInfo.getCompetitionName());
//            if(CollectionUtils.isEmpty(competitionMainInfos)){
                // 设置赛事唯一编号
                competitionMainInfo.setCompetitionCode("TJDX_"+ UUIDUtils.getRandomCode());
                competitionMainInfo.setCreateTime(DateUtils.getNowDate());
                // 新增赛事主数据
                competitionMainInfoMapper.insertCompetitionMainInfo(competitionMainInfo);
                competitionDetailInfo.setCompetitionId(competitionMainInfo.getCompetitionId());
//            } else {
//                CompetitionMainInfo competitionMainInfoInner = competitionMainInfos.get(0);
//                competitionDetailInfo.setCompetitionId(competitionMainInfoInner.getCompetitionId());
//            }
            // 新增赛事界数据
            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
            BeanUtils.copyProperties(competitionDetailInfo, seriesInfo);
            seriesInfo.setCreateTime(DateUtils.getNowDate());
            seriesInfo.setPublishPerson(userInfo.getUserId());
            seriesInfoMapper.insertCompetitionSeriesInfo(seriesInfo);
            competitionDetailInfo.setCompetitionSeriesId(seriesInfo.getCompetitionSeriesId());
        }
        if(null != competitionDetailInfo.getCompetitionId() && null == competitionDetailInfo.getCompetitionSeriesId()){
            // 新增赛事界数据
            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
            BeanUtils.copyProperties(competitionDetailInfo, seriesInfo);
            seriesInfoMapper.insertCompetitionSeriesInfo(seriesInfo);
            competitionDetailInfo.setCompetitionSeriesId(seriesInfo.getCompetitionSeriesId());
        }
        if(null != competitionDetailInfo.getCompetitionId() && null != competitionDetailInfo.getCompetitionSeriesId()){
            // 修改赛事界数据
            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
            BeanUtils.copyProperties(competitionDetailInfo, seriesInfo);
            seriesInfo.setUpdateTime(DateUtils.getNowDate());
            seriesInfoMapper.updateCompetitionSeriesInfo(seriesInfo);
            competitionDetailInfo.setCompetitionSeriesId(seriesInfo.getCompetitionSeriesId());
        }
        // 新增赛事阶段配置
        List<CompetitionStageConfig> stageConfigs = stageConfigMapper.selectCompetitionStageConfigList(competitionDetailInfo.getCompetitionSeriesId());
        if(CollectionUtils.isEmpty(stageConfigs) && CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionStageList())){
            competitionDetailInfo.getCompetitionStageList().forEach(stageConfig -> {
                stageConfig.setStageId(UUIDUtils.getUUID());
                stageConfig.setCreateTime(DateUtils.getNowDate());
                stageConfig.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
            });
            stageConfigMapper.insertCompetitionStageConfig(competitionDetailInfo.getCompetitionStageList());
        }
        // 新增赛事课程配置
        List<CompetitionCourseConfig> courseConfigs = courseConfigMapper.selectCompetitionCourseConfigList(competitionDetailInfo.getCompetitionSeriesId());
        if(CollectionUtils.isEmpty(courseConfigs) && CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionCourseConfigList())){
            competitionDetailInfo.getCompetitionCourseConfigList().forEach(courseConfig -> {
                courseConfig.setCourseConfigId(UUIDUtils.getUUID());
                courseConfig.setCreateTime(DateUtils.getNowDate());
                courseConfig.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
            });
            courseConfigMapper.insertCompetitionCourseConfig(competitionDetailInfo.getCompetitionCourseConfigList());
        }
        // 新增赛事赞助企业配置
        List<CompetitionEnterpriseRela> competitionEnterpriseRelas = enterpriseRelaMapper.selectCompetitionEnterpriseRelaList(competitionDetailInfo.getCompetitionSeriesId());
        if(CollectionUtils.isEmpty(competitionEnterpriseRelas) && CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionEnterpriseRelaList())){
            competitionDetailInfo.getCompetitionEnterpriseRelaList().forEach(enterpriseRela -> {
                enterpriseRela.setRelaId(UUIDUtils.getUUID());
                enterpriseRela.setCreateTime(DateUtils.getNowDate());
                enterpriseRela.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
            });
            enterpriseRelaMapper.insertCompetitionEnterpriseRela(competitionDetailInfo.getCompetitionEnterpriseRelaList());
        }
//        // 新增赛事奖项配置
//        List<CompetitionAwardsConfig> competitionAwardsConfigs = awardsConfigMapper.selectCompetitionAwardsConfigList(competitionDetailInfo.getCompetitionSeriesId(),null);
//        if(CollectionUtils.isEmpty(competitionAwardsConfigs) && CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionAwardsList())){
//            competitionDetailInfo.getCompetitionAwardsList().forEach(awardsConfig -> {
//                awardsConfig.setAwardsId(UUIDUtils.getUUID());
//                awardsConfig.setCreateTime(DateUtils.getNowDate());
//                awardsConfig.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
//            });
//            awardsConfigMapper.insertCompetitionAwardsConfig(competitionDetailInfo.getCompetitionAwardsList());
//        }
        return competitionDetailInfo;
    }

    /**
     * 修改赛事主数据
     *
     * @param competitionDetailInfo 赛事主数据
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitionMainInfo(CompetitionDetailInfo competitionDetailInfo)
    {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        competitionDetailInfo.setUpdateBy(sysUserInfo.getUserId()+"");
        // 修改赛事状态校验，草稿、审核驳回、已撤销发布可修改
        CompetitionSeriesInfo competitionSeriesInfo =
                seriesInfoMapper.selectCompetitionSeriesInfoByCompetitionSeriesId(competitionDetailInfo.getCompetitionId(), competitionDetailInfo.getCompetitionSeriesId());
        boolean checkFlag = false;
        if(competitionSeriesInfo!=null){
            checkFlag = competitionSeriesInfo.getCheckStatus().equals(Constants.DRAFT) || competitionSeriesInfo.getCheckStatus().equals(Constants.CHECK_REJECT);
        }
        if(checkFlag || competitionSeriesInfo.getCheckStatus().equals(Constants.COMPETITION_REPEAL_PUBLISH) || competitionSeriesInfo.getCheckStatus().equals(Constants.CHECK_PASS)){
//            int checkCount = competitionMainInfoMapper.checkCompetitionName(competitionDetailInfo.getCompetitionName(),competitionDetailInfo.getCompetitionSeriesName());
//            if (checkCount > 0) {
//                throw new GlobalException("该赛事名称已存在");
//            }
            CompetitionMainInfo competitionMainInfo = new CompetitionMainInfo();
            BeanUtils.copyProperties(competitionDetailInfo, competitionMainInfo);
            competitionMainInfo.setUpdateTime(DateUtils.getNowDate());
            competitionMainInfoMapper.updateCompetitionMainInfo(competitionMainInfo);
            // 跟新赛事系列主配置
            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
            BeanUtils.copyProperties(competitionDetailInfo, seriesInfo);
            seriesInfo.setUpdateTime(DateUtils.getNowDate());
            seriesInfo.setCheckStatus(Constants.DRAFT);
            seriesInfo.setPublishPerson(sysUserInfo.getUserId());
            if(Objects.isNull(seriesInfo.getCompetitionSeriesId())){
                seriesInfoMapper.insertCompetitionSeriesInfo(seriesInfo);
            } else {
                seriesInfoMapper.updateCompetitionSeriesInfo(seriesInfo);
            }
            // 更新赛事阶段配置
            if(CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionStageList())){
                stageConfigMapper.deleteCompetitionStageConfigBySeriesId(competitionDetailInfo.getCompetitionSeriesId());
                competitionDetailInfo.getCompetitionStageList().forEach(stageConfig -> {
                    stageConfig.setStageId(UUIDUtils.getUUID());
                    stageConfig.setCreateTime(DateUtils.getNowDate());
                    stageConfig.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
                });
                stageConfigMapper.insertCompetitionStageConfig(competitionDetailInfo.getCompetitionStageList());
            }
            // 更新赛事课程配置
            if(CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionCourseConfigList())){
                courseConfigMapper.deleteCompetitionCourseConfigBySeriesId(competitionDetailInfo.getCompetitionSeriesId());
                competitionDetailInfo.getCompetitionCourseConfigList().forEach(courseConfig -> {
                    courseConfig.setCourseConfigId(UUIDUtils.getUUID());
                    courseConfig.setCreateTime(DateUtils.getNowDate());
                    courseConfig.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
                });
                courseConfigMapper.insertCompetitionCourseConfig(competitionDetailInfo.getCompetitionCourseConfigList());
            }
            // 更新赛事赞助企业配置
            if(CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionEnterpriseRelaList())){
                enterpriseRelaMapper.deleteCompetitionEnterpriseRelaBySeriesId(competitionDetailInfo.getCompetitionSeriesId());
                competitionDetailInfo.getCompetitionEnterpriseRelaList().forEach(enterpriseRela -> {
                    enterpriseRela.setRelaId(UUIDUtils.getUUID());
                    enterpriseRela.setCreateTime(DateUtils.getNowDate());
                    enterpriseRela.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
                });
                enterpriseRelaMapper.insertCompetitionEnterpriseRela(competitionDetailInfo.getCompetitionEnterpriseRelaList());
            }
        } else {
            throw new ServiceException("草稿、审核驳回、已撤销发布状态可修改赛事信息,其他状态不可修改");
        }
        return 1;
    }

    // 去除空格
    public class StringTrimmer {
        public static void trimStrings(Object entity) throws IllegalAccessException {
            // 获取对象的所有声明字段
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 设置字段可访问（包括私有字段）
                field.setAccessible(true);
                // 判断字段类型是否为String
                if (field.getType() == String.class) {
                    // 获取字段值
                    String value = (String) field.get(entity);
                    // 去除前后空格，如果值为null则保持null
                    String trimmedValue = value != null ? value.trim() : null;
                    // 设置新的字段值
                    field.set(entity, trimmedValue);
                }
            }
        }
    }

    /**
     * 批量删除赛事主数据
     *
     * @param req 需要删除的赛事主数据主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCompetitionMainInfoByCompetitionIds(CompetitionMainInfoReq req) {
        CompetitionTrackInfo competitionTrackInfo = new CompetitionTrackInfo();
        competitionTrackInfo.setCompetitionSeriesId(req.getCompetitionSeriesId());
        List<CompetitionTrackInfo> competitionTrackInfos = competitionTrackInfoMapper.selectCompetitionTrackInfoList(competitionTrackInfo);
        if(CollectionUtils.isNotEmpty(competitionTrackInfos)){
            throw new ServiceException("该赛事下存在赛事配置，请先删除赛事配置");
        }
        CompetitionSeriesInfo competitionSeriesInfo = seriesInfoMapper.selectCompetitionSeriesInfoByCompetitionSeriesId
                (req.getCompetitionId(),req.getCompetitionSeriesId());
        if(!"1".equals(competitionSeriesInfo.getCheckStatus())){
            throw new ServiceException("非草稿状态不能删除赛事信息");
        }
        if(req.getCompetitionId() != null){
            competitionMainInfoMapper.deleteCompetitionMainInfoByCompetitionId(req.getCompetitionId());
        }
        if(req.getCompetitionSeriesId() != null){
            Long competitionSeriesId = req.getCompetitionSeriesId();
            seriesInfoMapper.deleteCompetitionSeriesInfoByCompetitionSeriesId(competitionSeriesId);
            stageConfigMapper.deleteCompetitionStageConfigBySeriesId(competitionSeriesId);
            enterpriseRelaMapper.deleteCompetitionEnterpriseRelaBySeriesId(competitionSeriesId);
        }
        return 1;
    }

    /**
     * 删除赛事主数据信息
     *
     * @param competitionId 赛事主数据主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionMainInfoByCompetitionId(Long competitionId)
    {
        return competitionMainInfoMapper.deleteCompetitionMainInfoByCompetitionId(competitionId);
    }
}
