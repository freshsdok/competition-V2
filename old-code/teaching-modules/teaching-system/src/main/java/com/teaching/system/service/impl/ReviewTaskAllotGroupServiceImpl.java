package com.teaching.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.domain.ReviewTaskAllotGroup;
import com.teaching.system.domain.ReviewTaskAllotGroupRelation;
import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.mapper.NationwideCollegeInfoMapper;
import com.teaching.system.mapper.ReviewTaskAllotGroupMapper;
import com.teaching.system.mapper.ReviewTaskAllotGroupRelationMapper;
import com.teaching.system.mapper.ReviewTaskInfoMapper;
import com.teaching.system.service.IReviewTaskAllotGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评审任务分配组信息Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Service
public class ReviewTaskAllotGroupServiceImpl implements IReviewTaskAllotGroupService {
    @Autowired
    private ReviewTaskAllotGroupMapper reviewTaskAllotGroupMapper;

    @Autowired
    private ReviewTaskAllotGroupRelationMapper reviewTaskAllotGroupRelationMapper;

    @Autowired
    private ReviewTaskInfoMapper reviewTaskInfoMapper;

    @Autowired
    private NationwideCollegeInfoMapper nationwideCollegeInfoMapper;

    /**
     * 查询评审任务分配组信息
     *
     * @param reviewGroupId 评审任务分配组信息主键
     * @return 评审任务分配组信息
     */
    @Override
    public ReviewTaskAllotGroup selectReviewTaskAllotGroupByReviewGroupId(Long reviewGroupId) {
        ReviewTaskAllotGroup reviewTaskAllotGroup = reviewTaskAllotGroupMapper.selectReviewTaskAllotGroupByReviewGroupId(reviewGroupId);
        if(Objects.nonNull(reviewTaskAllotGroup)){
            List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList = reviewTaskAllotGroup.getReviewTaskAllotGroupRelationList();
            if(CollectionUtils.isNotEmpty(reviewTaskAllotGroupRelationList)){
                reviewTaskAllotGroupRelationList.stream().forEach(reviewTaskAllotGroupRelation -> {
                    ReviewTaskInfo reviewTaskInfo = reviewTaskInfoMapper.selectReviewTaskInfoByReviewId(reviewTaskAllotGroupRelation.getReviewId());
                    // 获取学校及省份信息信息
                    if(Objects.nonNull(reviewTaskInfo)){
                        NationwideCollegeInfo nationwideCollegeInfoRes =
                                nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(reviewTaskInfo.getSchoolId());
                        if(Objects.nonNull(nationwideCollegeInfoRes)){
                            reviewTaskInfo.setSchoolName(nationwideCollegeInfoRes.getSchoolName());
                            reviewTaskInfo.setProvince(nationwideCollegeInfoRes.getProvince());
                            reviewTaskInfo.setProvinceCode(nationwideCollegeInfoRes.getProvinceCode());
                        }
                        // 处理任务名称
                        String fileInfo = reviewTaskInfo.getFileInfo();
                        if (StringUtils.isNotEmpty(fileInfo)) {
                            StringBuilder builder = new StringBuilder();
                            List<Map> list = JSONUtil.toList(fileInfo, Map.class);
                            if (CollUtil.isNotEmpty(list)) {
                                list.forEach(map->{
                                    String getName = map.get("fileName").toString();
                                    builder.append(getName).append(",");
                                });
                            }
                            if (StringUtils.isNotEmpty(builder.toString())) {
                                reviewTaskInfo.setReviewName(builder.substring(0,builder.toString().length()-1));
                            }
                        }
                        reviewTaskAllotGroupRelation.setReviewTaskInfo(reviewTaskInfo);
                    }
                });
            }
        }
        return reviewTaskAllotGroup;
    }

    /**
     * 查询评审任务分配组信息列表
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 评审任务分配组信息集合
     */
    @Override
    public List<ReviewTaskAllotGroup> selectReviewTaskAllotGroupList(ReviewTaskAllotGroup reviewTaskAllotGroup) {
        List<ReviewTaskAllotGroup> reviewTaskAllotGroups = reviewTaskAllotGroupMapper.selectReviewTaskAllotGroupList(reviewTaskAllotGroup);
//        if(CollectionUtils.isNotEmpty(reviewTaskAllotGroups)){
//            reviewTaskAllotGroups.stream().forEach(reviewTaskAllotGroupRes -> {
//                List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList = reviewTaskAllotGroupRes.getReviewTaskAllotGroupRelationList();
//                if(CollectionUtils.isNotEmpty(reviewTaskAllotGroupRelationList)){
//                    reviewTaskAllotGroupRelationList.stream().forEach(reviewTaskAllotGroupRelation -> {
//                        ReviewTaskInfo reviewTaskInfo = reviewTaskInfoMapper.selectReviewTaskInfoByReviewId(reviewTaskAllotGroupRelation.getReviewId());
//                        // 获取学校及省份信息信息
//                        if(Objects.nonNull(reviewTaskInfo)){
//                            NationwideCollegeInfo nationwideCollegeInfoRes =
//                                    nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(reviewTaskInfo.getSchoolId());
//                            if(Objects.nonNull(nationwideCollegeInfoRes)){
//                                reviewTaskInfo.setSchoolName(nationwideCollegeInfoRes.getSchoolName());
//                                reviewTaskInfo.setProvince(nationwideCollegeInfoRes.getProvince());
//                                reviewTaskInfo.setProvinceCode(nationwideCollegeInfoRes.getProvinceCode());
//                            }
//                            reviewTaskAllotGroupRelation.setReviewTaskInfo(reviewTaskInfo);
//                        }
//                    });
//                }
//            });
//        }
        return reviewTaskAllotGroups;
    }

    /**
     * 新增评审任务分配组信息
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertReviewTaskAllotGroup(ReviewTaskAllotGroup reviewTaskAllotGroup) {
        reviewTaskAllotGroup.setCreateTime(DateUtils.getNowDate());
        int rows = reviewTaskAllotGroupMapper.insertReviewTaskAllotGroup(reviewTaskAllotGroup);
        insertReviewTaskAllotGroupRelation(reviewTaskAllotGroup);
        return rows;
    }

    /**
     * 修改评审任务分配组信息
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateReviewTaskAllotGroup(ReviewTaskAllotGroup reviewTaskAllotGroup) {
        reviewTaskAllotGroup.setUpdateTime(DateUtils.getNowDate());
        reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupRelationByReviewGroupIds(new Long[]{reviewTaskAllotGroup.getReviewGroupId()});
        insertReviewTaskAllotGroupRelation(reviewTaskAllotGroup);
        return reviewTaskAllotGroupMapper.updateReviewTaskAllotGroup(reviewTaskAllotGroup);
    }

    /**
     * 批量删除评审任务分配组信息
     *
     * @param reviewGroupIds 需要删除的评审任务分配组信息主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskAllotGroupByReviewGroupIds(Long[] reviewGroupIds) {
        reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupRelationByReviewGroupIds(reviewGroupIds);
        return reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupByReviewGroupIds(reviewGroupIds);
    }

    /**
     * 删除评审任务分配组信息
     *
     * @param reviewGroupId 评审任务分配组信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskAllotGroupByReviewGroupId(Long reviewGroupId) {
        reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupRelationByReviewGroupIds(new Long[]{reviewGroupId});
        return reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupByReviewGroupId(reviewGroupId);
    }

    /**
     * 新增评审任务分配组关联关系信息
     *
     * @param reviewTaskAllotGroup 评审任务分配组对象
     */
    public void insertReviewTaskAllotGroupRelation(ReviewTaskAllotGroup reviewTaskAllotGroup) {
        List<Long> reviewIdList = reviewTaskAllotGroup.getReviewIdList();
        Long reviewGroupId = reviewTaskAllotGroup.getReviewGroupId();
        if(CollectionUtils.isNotEmpty(reviewIdList)){
            List<ReviewTaskAllotGroupRelation> relationList = new ArrayList<>();
            reviewIdList.stream().forEach(reviewId -> {
                ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation = new ReviewTaskAllotGroupRelation();
                reviewTaskAllotGroupRelation.setReviewGroupId(reviewGroupId);
                reviewTaskAllotGroupRelation.setReviewId(reviewId);
                reviewTaskAllotGroupRelation.setCreateTime(DateUtils.getNowDate());
                relationList.add(reviewTaskAllotGroupRelation);
            });
            reviewTaskAllotGroupMapper.batchReviewTaskAllotGroupRelation(relationList);
        }
    }
}
