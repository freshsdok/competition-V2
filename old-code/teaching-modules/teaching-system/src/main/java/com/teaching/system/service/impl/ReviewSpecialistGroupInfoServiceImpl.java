package com.teaching.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.ReviewGroupSpecialistRelation;
import com.teaching.system.domain.ReviewSpecialistGroupInfo;
import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.domain.SpecialistSysUser;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IReviewSpecialistGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 专家组Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Slf4j
@Service
public class ReviewSpecialistGroupInfoServiceImpl implements IReviewSpecialistGroupInfoService {
    @Autowired
    private ReviewSpecialistGroupInfoMapper reviewSpecialistGroupInfoMapper;

    @Autowired
    private ReviewGroupSpecialistRelationMapper reviewGroupSpecialistRelationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IdentityInfoMapper identityInfoMapper;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private ReviewTaskInfoMapper reviewTaskInfoMapper;

    @Autowired
    private NationwideCollegeInfoMapper nationwideCollegeInfoMapper;

    /**
     * 查询专家组
     *
     * @param groupId 专家组主键
     * @return 专家组
     */
    @Override
    public ReviewSpecialistGroupInfo selectReviewSpecialistGroupInfoByGroupId(Long groupId) {
        return reviewSpecialistGroupInfoMapper.selectReviewSpecialistGroupInfoByGroupId(groupId);
    }

    /**
     * 查询专家组列表
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 专家组集合
     */
    @Override
    public List<ReviewSpecialistGroupInfo> selectReviewSpecialistGroupInfoList(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        return reviewSpecialistGroupInfoMapper.selectReviewSpecialistGroupInfoList(reviewSpecialistGroupInfo);
    }

    /**
     * 新增专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    @Transactional
    @Override
    public int insertReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        reviewSpecialistGroupInfo.setCreateTime(DateUtils.getNowDate());
        reviewSpecialistGroupInfo.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
        int rows = reviewSpecialistGroupInfoMapper.insertReviewSpecialistGroupInfo(reviewSpecialistGroupInfo);
        insertReviewGroupSpecialistRelation(reviewSpecialistGroupInfo);
        return rows;
    }

    /**
     * 修改专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    @Transactional
    @Override
    public int updateReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        reviewSpecialistGroupInfo.setUpdateTime(DateUtils.getNowDate());
        reviewSpecialistGroupInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
        if(CollectionUtils.isNotEmpty(reviewSpecialistGroupInfo.getSpecialistUserIdList())){
            reviewSpecialistGroupInfo.setAllotStatus(Constants.GROUP_ALLOT_STATUS_YES);
        }
        reviewSpecialistGroupInfoMapper.deleteReviewGroupSpecialistRelationByGroupIds(new Long[]{reviewSpecialistGroupInfo.getGroupId()});
        insertReviewGroupSpecialistRelation(reviewSpecialistGroupInfo);
        return reviewSpecialistGroupInfoMapper.updateReviewSpecialistGroupInfo(reviewSpecialistGroupInfo);
    }

    /**
     * 批量删除专家组
     *
     * @param groupIds 需要删除的专家组主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewSpecialistGroupInfoByGroupIds(Long[] groupIds) {
        reviewSpecialistGroupInfoMapper.deleteReviewGroupSpecialistRelationByGroupIds(groupIds);
        return reviewSpecialistGroupInfoMapper.deleteReviewSpecialistGroupInfoByGroupIds(groupIds);
    }

    /**
     * 删除专家组信息
     *
     * @param groupId 专家组主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewSpecialistGroupInfoByGroupId(Long groupId) {
        reviewSpecialistGroupInfoMapper.deleteReviewGroupSpecialistRelationByGroupIds(new Long[]{groupId});
        return reviewSpecialistGroupInfoMapper.deleteReviewSpecialistGroupInfoByGroupId(groupId);
    }

    @Override
    public List<SpecialistSysUser> getSpecialistInfo(SpecialistSysUser specialistSysUserReq) {
        List<SpecialistSysUser> sysUserList = sysUserMapper.selectSpecialUserList(specialistSysUserReq);
        if (CollectionUtils.isNotEmpty(sysUserList)) {
            sysUserList.stream().forEach(sysUserDes -> {
                List<ReviewTaskInfo> reviewTaskInfoList = reviewTaskInfoMapper.selectReviewTaskInfoByUserId(sysUserDes.getUserId());
                // 获取学校及省份信息信息
                if(CollectionUtils.isNotEmpty(reviewTaskInfoList)){
                    reviewTaskInfoList.stream().forEach(reviewTaskInfoRes -> {
//                        NationwideCollegeInfo nationwideCollegeInfoRes =
//                                nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(reviewTaskInfoRes.getSchoolId());
//                        if(Objects.nonNull(nationwideCollegeInfoRes)){
//                            reviewTaskInfoRes.setSchoolName(nationwideCollegeInfoRes.getSchoolName());
//                            reviewTaskInfoRes.setProvince(nationwideCollegeInfoRes.getProvince());
//                            reviewTaskInfoRes.setProvinceCode(nationwideCollegeInfoRes.getProvinceCode());
//                        }
                        // 处理任务名称
                        String fileInfo = reviewTaskInfoRes.getFileInfo();
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
                                reviewTaskInfoRes.setReviewName(builder.substring(0,builder.toString().length()-1));
                            }
                        }
                    });
                }
                sysUserDes.setReviewTaskInfoList(reviewTaskInfoList);
            });
        }
        return sysUserList;
    }

    /**
     * 新增专家组与专家关联关系信息
     *
     * @param reviewSpecialistGroupInfo 专家组对象
     */
    public void insertReviewGroupSpecialistRelation(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        List<Long> specialistUserIdList = reviewSpecialistGroupInfo.getSpecialistUserIdList();
        Long groupId = reviewSpecialistGroupInfo.getGroupId();
        if (CollectionUtils.isNotEmpty(specialistUserIdList)) {
            List<ReviewGroupSpecialistRelation> reviewGroupSpecialistRelationList = new ArrayList<>();
            specialistUserIdList.stream().forEach(specialistUserId -> {
                ReviewGroupSpecialistRelation reviewGroupSpecialistRelation = new ReviewGroupSpecialistRelation();
                reviewGroupSpecialistRelation.setUserId(specialistUserId);
                reviewGroupSpecialistRelation.setGroupId(groupId);
                reviewGroupSpecialistRelation.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getUserName());
                reviewGroupSpecialistRelationList.add(reviewGroupSpecialistRelation);
            });
            reviewSpecialistGroupInfoMapper.batchReviewGroupSpecialistRelation(reviewGroupSpecialistRelationList);
        }
    }
}
