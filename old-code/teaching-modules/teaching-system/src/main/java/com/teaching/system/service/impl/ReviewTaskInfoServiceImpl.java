package com.teaching.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.domain.*;
import com.teaching.system.domain.vo.ExpertReviewInfo;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IReviewTaskInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 评审任务分配信息Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Service
public class ReviewTaskInfoServiceImpl implements IReviewTaskInfoService {
    @Autowired
    private ReviewTaskInfoMapper reviewTaskInfoMapper;

    @Autowired
    private ReviewTaskAllotGroupRelationMapper reviewTaskAllotGroupRelationMapper;

    @Autowired
    private ReviewTaskSpecialistRelationMapper reviewTaskSpecialistRelationMapper;

    @Autowired
    private FileUploadManagerMapper fileUploadManagerMapper;

    @Autowired
    private NationwideCollegeInfoMapper nationwideCollegeInfoMapper;

    @Autowired
    private ReviewGroupSpecialistRelationMapper specialistRelationMapper;

    @Autowired
    private RemoteUserService userService;
    @Autowired
    private AuthInfoMapper authInfoMapper;

    /**
     * 查询评审任务分配信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 评审任务分配信息
     */
    @Override
    public ReviewTaskInfo selectReviewTaskInfoByReviewId(Long reviewId) {
        return reviewTaskInfoMapper.selectReviewTaskInfoByReviewId(reviewId);
    }

    /**
     * 查询评审任务分配信息列表
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 评审任务分配信息集合
     */
    @Override
    public List<ReviewTaskInfo> selectReviewTaskInfoList(ReviewTaskInfo reviewTaskInfo) {
        List<ReviewTaskInfo> reviewTaskInfoList = reviewTaskInfoMapper.selectFileUploadReviewTaskList(reviewTaskInfo);
        // 获取学校及省份信息信息
        if (CollectionUtils.isNotEmpty(reviewTaskInfoList)) {
            reviewTaskInfoList.stream().forEach(reviewTaskInfoRes -> {
                NationwideCollegeInfo nationwideCollegeInfo =
                        nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(reviewTaskInfoRes.getSchoolId());
                if (Objects.nonNull(nationwideCollegeInfo)) {
                    reviewTaskInfoRes.setSchoolName(nationwideCollegeInfo.getSchoolName());
                    reviewTaskInfoRes.setProvince(nationwideCollegeInfo.getProvince());
                    reviewTaskInfoRes.setProvinceCode(nationwideCollegeInfo.getProvinceCode());
                }
                // 处理任务名称
                String fileInfo = reviewTaskInfoRes.getFileInfo();
                if (StringUtils.isNotEmpty(fileInfo)) {
                    StringBuilder builder = new StringBuilder();
                    List<Map> list = JSONUtil.toList(fileInfo, Map.class);
                    if (CollUtil.isNotEmpty(list)) {
                        list.forEach(map -> {
                            String getName = map.get("fileName").toString();
                            builder.append(getName).append(",");
                        });
                    }
                    if (StringUtils.isNotEmpty(builder.toString())) {
                        reviewTaskInfoRes.setReviewName(builder.substring(0, builder.toString().length() - 1));
                    }
                }
            });
        }
        // 获取文件上传信息时候先进行入库操作
        List<ReviewTaskInfo> addReviewTaskInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(reviewTaskInfoList)) {
            addReviewTaskInfoList = reviewTaskInfoList.stream().
                    filter(reviewTaskInfoRes -> Objects.isNull(reviewTaskInfoRes.getReviewId())).toList();
        }
        if (CollectionUtils.isNotEmpty(addReviewTaskInfoList)) {
            addReviewTaskInfoList.stream().forEach(reviewTaskInfoRes -> {
                reviewTaskInfoRes.setCreateTime(DateUtils.getNowDate());
                reviewTaskInfoRes.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
            });
            reviewTaskInfoMapper.batchInsertReviewTaskInfo(addReviewTaskInfoList);
        }
        return reviewTaskInfoList;
    }

    /**
     * 新增评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertReviewTaskInfo(ReviewTaskInfo reviewTaskInfo) {
        reviewTaskInfo.setCreateTime(DateUtils.getNowDate());
        reviewTaskInfo.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
        return reviewTaskInfoMapper.insertReviewTaskInfo(reviewTaskInfo);
    }

    @Override
    public int batchInsertReviewTaskInfo(ReviewTaskInfoReq reviewTaskInfoReq) {
        List<Long> reviewIdList = reviewTaskInfoReq.getReviewIdList();
        if (CollectionUtils.isNotEmpty(reviewIdList)) {
            List<ReviewTaskInfo> reviewTaskInfoList = new ArrayList<>();
            reviewIdList.stream().forEach(reviewId -> {
                ReviewTaskInfo reviewTaskInfo = new ReviewTaskInfo();
                reviewTaskInfo.setUpdateTime(DateUtils.getNowDate());
                reviewTaskInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
                reviewTaskInfo.setReviewId(reviewId);
                reviewTaskInfo.setReviewDesc(reviewTaskInfoReq.getReviewDesc());
                reviewTaskInfo.setReviewStartTime(reviewTaskInfoReq.getReviewStartTime());
                reviewTaskInfo.setReviewEndTime(reviewTaskInfoReq.getReviewEndTime());
                reviewTaskInfo.setReferenceDocument(reviewTaskInfoReq.getReferenceDocument());
                reviewTaskInfoList.add(reviewTaskInfo);
            });
            if (CollectionUtils.isNotEmpty(reviewTaskInfoList)) {
                reviewTaskInfoMapper.batchUpdateReviewTaskInfo(reviewTaskInfoList);
            }
        }
        return 1;
    }

    @Override
    public int batchInsertSpecialistReviewTaskInfo(List<Long> reviewIdList, List<Long> userIdList) {
        // 分配专家
        if (CollectionUtils.isNotEmpty(reviewIdList) && CollectionUtils.isNotEmpty(userIdList)) {
            List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList = new ArrayList<>();
            List<ReviewTaskInfo> reviewTaskInfoList = new ArrayList<>();
            reviewIdList.stream().forEach(reviewId -> {
                ReviewTaskInfo reviewTaskInfo = new ReviewTaskInfo();
                reviewTaskInfo.setReviewId(reviewId);
                reviewTaskInfo.setDistributeStatus("1");
                reviewTaskInfoList.add(reviewTaskInfo);
                userIdList.stream().forEach(userId -> {
                    ReviewTaskSpecialistRelation reviewTaskSpecialistRelationReq = new ReviewTaskSpecialistRelation();
                    reviewTaskSpecialistRelationReq.setReviewId(reviewId);
                    reviewTaskSpecialistRelationReq.setUserId(userId);
                    List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelations =
                            reviewTaskSpecialistRelationMapper.selectReviewTaskSpecialistRelationList(reviewTaskSpecialistRelationReq);
                    if (CollectionUtils.isEmpty(reviewTaskSpecialistRelations)) {
                        ReviewTaskSpecialistRelation reviewTaskSpecialistRelation = new ReviewTaskSpecialistRelation();
                        reviewTaskSpecialistRelation.setReviewId(reviewId);
                        reviewTaskSpecialistRelation.setUserId(userId);
                        reviewTaskSpecialistRelationList.add(reviewTaskSpecialistRelation);
                    }
                });
            });
            if (CollectionUtils.isNotEmpty(reviewTaskSpecialistRelationList)) {
                reviewTaskSpecialistRelationMapper.batchInsertReviewTaskSpecialistRelation(reviewTaskSpecialistRelationList);
            }
            if (CollectionUtils.isNotEmpty(reviewTaskInfoList)) {
                reviewTaskInfoMapper.batchUpdateReviewTaskInfo(reviewTaskInfoList);
            }
        }
        return 1;
    }

    @Override
    public int saveSpecialistGroupReviewTaskInfo(List<Long> reviewIdList, List<Long> groupIdList) {
        if (CollectionUtils.isNotEmpty(reviewIdList) && CollectionUtils.isNotEmpty(groupIdList)) {
            List<ReviewGroupSpecialistRelation> reviewGroupSpecialistRelations = specialistRelationMapper.selectReviewGroupSpecialistRelationByGroupIds(groupIdList);
            if (CollectionUtils.isNotEmpty(reviewGroupSpecialistRelations)) {
                List<Long> userIdList = reviewGroupSpecialistRelations.stream()
                        .map(ReviewGroupSpecialistRelation::getUserId).distinct().toList();
                batchInsertSpecialistReviewTaskInfo(reviewIdList, userIdList);
            }
        }
        return 1;
    }

    /**
     * 修改评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateReviewTaskInfo(ReviewTaskInfo reviewTaskInfo) {
        reviewTaskInfo.setUpdateTime(DateUtils.getNowDate());
        reviewTaskInfoMapper.deleteReviewTaskAllotGroupRelationByReviewIds(new Long[]{reviewTaskInfo.getReviewId()});
        reviewTaskInfoMapper.deleteReviewTaskSpecialistRelationByReviewIds(new Long[]{reviewTaskInfo.getReviewId()});
        insertReviewTaskAllotGroupRelation(reviewTaskInfo);
        insertReviewTaskSpecialistRelation(reviewTaskInfo);
        return reviewTaskInfoMapper.updateReviewTaskInfo(reviewTaskInfo);
    }

    /**
     * 批量删除评审任务分配信息
     *
     * @param reviewIds 需要删除的评审任务分配信息主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskInfoByReviewIds(Long[] reviewIds) {
        reviewTaskInfoMapper.deleteReviewTaskAllotGroupRelationByReviewIds(reviewIds);
        reviewTaskInfoMapper.deleteReviewTaskSpecialistRelationByReviewIds(reviewIds);
        return reviewTaskInfoMapper.deleteReviewTaskInfoByReviewIds(reviewIds);
    }

    /**
     * 删除评审任务分配信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskInfoByReviewId(Long reviewId) {
        reviewTaskInfoMapper.deleteReviewTaskAllotGroupRelationByReviewIds(new Long[]{reviewId});
        reviewTaskInfoMapper.deleteReviewTaskSpecialistRelationByReviewIds(new Long[]{reviewId});
        return reviewTaskInfoMapper.deleteReviewTaskInfoByReviewId(reviewId);
    }

    /**
     * 新增评审任务分配组关联关系信息
     *
     * @param reviewTaskInfo 评审任务分配信息对象
     */
    public void insertReviewTaskAllotGroupRelation(ReviewTaskInfo reviewTaskInfo) {
        List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList = reviewTaskInfo.getReviewTaskAllotGroupRelationList();
        Long reviewId = reviewTaskInfo.getReviewId();
        if (reviewTaskAllotGroupRelationList != null && !reviewTaskAllotGroupRelationList.isEmpty()) {
            for (ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation : reviewTaskAllotGroupRelationList) {
                reviewTaskAllotGroupRelation.setReviewId(reviewId);
                reviewTaskAllotGroupRelation.setCreateTime(DateUtils.getNowDate());
            }
            reviewTaskInfoMapper.batchReviewTaskAllotGroupRelation(reviewTaskAllotGroupRelationList);
        }
    }

    /**
     * 新增任务分配专家关联关系信息
     *
     * @param reviewTaskInfo 评审任务分配信息对象
     */
    public void insertReviewTaskSpecialistRelation(ReviewTaskInfo reviewTaskInfo) {
        List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList = reviewTaskInfo.getReviewTaskSpecialistRelationList();
        Long reviewId = reviewTaskInfo.getReviewId();
        if (reviewTaskSpecialistRelationList != null && !reviewTaskSpecialistRelationList.isEmpty()) {
            for (ReviewTaskSpecialistRelation reviewTaskSpecialistRelation : reviewTaskSpecialistRelationList) {
                reviewTaskSpecialistRelation.setReviewId(reviewId);
                reviewTaskSpecialistRelation.setCreateTime(DateUtils.getNowDate());
            }
            reviewTaskInfoMapper.batchReviewTaskSpecialistRelation(reviewTaskSpecialistRelationList);
        }
    }

    @Override
    public List<ExpertReviewInfo> getExpertList(ExpertReviewInfo param) {
        param.setUserId(SecurityUtils.getLoginUser().getUserid());
        List<ExpertReviewInfo> expertReviewInfos = reviewTaskInfoMapper.selectExpertList(param);
        Date now = DateUtils.getNowDate();
        for (ExpertReviewInfo expertReviewInfo : expertReviewInfos) {
            if (StringUtils.isNotBlank(expertReviewInfo.getNewFileName())) {
                expertReviewInfo.setNewFileName(expertReviewInfo.getNewFileName().substring(0, expertReviewInfo.getNewFileName().length() - 4));
            }
            Date reviewStartTime = expertReviewInfo.getReviewStartTime();
            Date reviewEndTime = expertReviewInfo.getReviewEndTime();
            if (reviewStartTime != null && reviewEndTime != null) {
                expertReviewInfo.setContinueFlag(DateUtil.isIn(now, reviewStartTime, reviewEndTime));
            }
            if (reviewStartTime == null && reviewEndTime != null) {
                expertReviewInfo.setContinueFlag(!now.after(reviewEndTime));
            }
        }
        return expertReviewInfos;
    }

    @Override
    public Map<String, Object> getTaskInfoByProcessedId(Long processedId) {
        Long userid = SecurityUtils.getLoginUser().getUserid();
        List<Long> expertIds = reviewTaskInfoMapper.selectExpertIdsByProcessedId(processedId,userid);
        if (CollectionUtils.isEmpty(expertIds) || !expertIds.contains(userid)) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("continueFlag", false);
            return resultMap;
        }
        Map<String, Object> resultMap = reviewTaskInfoMapper.selectTaskInfoByProcessedId(processedId, userid);
        //判断当前时间是否在结束时间前  目前只按截至时间判断，未到截至时间已审批过仍是true可进入
        if (resultMap != null) {
            String newFileName = MapUtil.getStr(resultMap, "newFileName", null);
            if (StringUtils.isNotBlank(newFileName)) {
                resultMap.put("newFileName", newFileName.substring(0, newFileName.length() - 4));
            }
            Date now = DateUtils.getNowDate();
            Date reviewStartTime = MapUtil.getDate(resultMap, "reviewStartTime", null);
            Date reviewEndTime = MapUtil.getDate(resultMap, "reviewEndTime", null);
            if (reviewEndTime != null) {
                resultMap.put("reviewEndTime", DateUtils.dateTimeFormatr(reviewEndTime, "yyyy-MM-dd HH:mm:ss"));
            }
            //起止时间都有时
            if (reviewStartTime != null && reviewEndTime != null) {
                if (DateUtil.isIn(now, reviewStartTime, reviewEndTime)) {
                    resultMap.put("continueFlag", true);
                    resultMap.put("nowDate", now);
                } else {
                    resultMap.put("continueFlag", false);
                    return resultMap;
                }
            }
            //只有结束时间
            if (reviewEndTime != null && now.after(reviewEndTime)) {
                resultMap.put("continueFlag", false);
                return resultMap;
            } else {
                resultMap.put("continueFlag", true);
                resultMap.put("nowDate", now);
            }
            if (MapUtil.getBool(resultMap, "continueFlag")) {
                String phoneNumber = SecurityUtils.getLoginUser().getSysUser().getPhonenumber();
                String username = SecurityUtils.getLoginUser().getSysUser().getNickName();
                resultMap.put("userName", username);
                resultMap.put("phone",phoneNumber);
                /*AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(SecurityUtils.getLoginUser().getUserid());
                if (authInfo != null) {
                    String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
                    String userName = SecurityUtils.getLoginUser().getSysUser().getUserName();
                    resultMap.put("userName", StringUtils.isBlank(authInfo.getRealName()) ? (StringUtils.isBlank(nickName) ? userName : nickName) : authInfo.getRealName());
                    String phoneNumber = SecurityUtils.getLoginUser().getSysUser().getPhonenumber();
                    String email = SecurityUtils.getLoginUser().getSysUser().getEmail();
                    resultMap.put("phone", StringUtils.isNotBlank(phoneNumber) ? phoneNumber : email);
                }*/
            }
        }
        return resultMap;
    }
}
