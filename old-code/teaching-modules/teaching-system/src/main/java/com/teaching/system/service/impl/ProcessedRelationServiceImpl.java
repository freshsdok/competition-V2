package com.teaching.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.ProcessedRelation;
import com.teaching.system.domain.ReviewRecord;
import com.teaching.system.mapper.ProcessedRelationMapper;
import com.teaching.system.mapper.ReviewRecordMapper;
import com.teaching.system.mapper.ReviewTaskInfoMapper;
import com.teaching.system.service.IProcessedRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 评审文件处理前后对应关系Service业务层处理
 *
 * @author teaching
 * @date 2026-04-23
 */
@Slf4j
@Service
public class ProcessedRelationServiceImpl implements IProcessedRelationService {
    @Autowired
    private ProcessedRelationMapper processedRelationMapper;
    @Autowired
    private ReviewRecordMapper reviewRecordMapper;
    @Autowired
    private ReviewTaskInfoMapper reviewTaskInfoMapper;

    /**
     * 查询评审文件处理前后对应关系
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 评审文件处理前后对应关系
     */
    @Override
    public ProcessedRelation selectProcessedRelationById(Long id) {
        return processedRelationMapper.selectProcessedRelationById(id);
    }

    /**
     * 查询评审文件处理前后对应关系列表
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 评审文件处理前后对应关系
     */
    @Override
    public List<ProcessedRelation> selectProcessedRelationList(ProcessedRelation processedRelation) {
        return processedRelationMapper.selectProcessedRelationList(processedRelation);
    }

    /**
     * 新增评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    @Override
    public int insertProcessedRelation(ProcessedRelation processedRelation) {
        processedRelation.setCreateTime(DateUtils.getNowDate());
        return processedRelationMapper.insertProcessedRelation(processedRelation);
    }

    /**
     * 修改评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    @Override
    public int updateProcessedRelation(ProcessedRelation processedRelation) {
        processedRelation.setUpdateTime(DateUtils.getNowDate());
        return processedRelationMapper.updateProcessedRelation(processedRelation);
    }

    /**
     * 批量删除评审文件处理前后对应关系
     *
     * @param ids 需要删除的评审文件处理前后对应关系主键
     * @return 结果
     */
    @Override
    public int deleteProcessedRelationByIds(Long[] ids) {
        return processedRelationMapper.deleteProcessedRelationByIds(ids);
    }

    /**
     * 删除评审文件处理前后对应关系信息
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 结果
     */
    @Override
    public int deleteProcessedRelationById(Long id) {
        return processedRelationMapper.deleteProcessedRelationById(id);
    }

    /**
     * 修改审阅状态
     *
     * @param reviewRecord file_id
     * @return
     */
    @Override
    public int updateProcessedRelationReviewStatus(ReviewRecord reviewRecord) {
        if(!checkCanContinue(reviewRecord.getFileId())){
            throw new GlobalException("当前时间不在审阅时间范围内！");
        }

        ReviewRecord record = reviewRecordMapper.selectRecordByExpertAndFile(reviewRecord.getExpertId(), reviewRecord.getFileId());
        if (record == null) {
            //新增
            reviewRecord.setReviewStatus("1");
            reviewRecord.setReviewTime(DateUtils.getNowDate());
            reviewRecord.setCreateTime(DateUtils.getNowDate());
            reviewRecord.setCreateBy(SecurityUtils.getLoginUser().getUserid() + "");
            return reviewRecordMapper.insertReviewRecord(reviewRecord);
        } else {
            //更新
            record.setReviewStatus("1");
            record.setReviewTime(DateUtils.getNowDate());
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(SecurityUtils.getLoginUser().getUserid() + "");
            return reviewRecordMapper.updateReviewRecord(record);
        }
    }

    /**
     * 更新最后预览到的页面
     *
     * @param reviewRecord file_id、expert_id、last_page
     * @return
     */
    @Override
    public int updateLastPageFlagByRelaId(ReviewRecord reviewRecord) {
        if(!checkCanContinue(reviewRecord.getFileId())){
            throw new GlobalException("当前时间不在审阅时间范围内！");
        }
        ReviewRecord record = reviewRecordMapper.selectRecordByExpertAndFile(reviewRecord.getExpertId(), reviewRecord.getFileId());
        if (record == null) {
            //新增
            reviewRecord.setCreateTime(DateUtils.getNowDate());
            reviewRecord.setCreateBy(SecurityUtils.getLoginUser().getUserid() + "");
            return reviewRecordMapper.insertReviewRecord(reviewRecord);
        } else {
            //更新
            record.setLastPage(reviewRecord.getLastPage());
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(SecurityUtils.getLoginUser().getUserid() + "");
            return reviewRecordMapper.updateReviewRecord(record);
        }
    }

    /**
     * 检查是否可以继续审阅  根据开始和截至时间
     * @param fileId 文件id即新旧文件关联关系id，审阅最小单位
     * @return
     */
    @Override
    public boolean checkCanContinue(Long fileId){
        Map<String, Object> resultMap = reviewTaskInfoMapper.selectTaskInfoByRelationId(fileId, SecurityUtils.getLoginUser().getUserid());
        Date reviewStartTime = MapUtil.getDate(resultMap, "reviewStartTime", null);
        Date reviewEndTime = MapUtil.getDate(resultMap, "reviewEndTime", null);
//        String reviewStatus = MapUtil.getStr(resultMap, "reviewStatus");
        Date nowDate = DateUtils.getNowDate();
        if (reviewStartTime != null && reviewEndTime != null) {
            if (!DateUtil.isIn(nowDate, reviewStartTime, reviewEndTime)) {
                log.warn("当前时间不在审阅时间范围内");
                return false;
            }
        }
        if (reviewStartTime == null && reviewEndTime != null) {
            if (nowDate.after(reviewEndTime)) {
                log.warn("当前时间已超过审阅截止时间");
                return false;
            }
        }
        /*if("1".equals(reviewStatus)){
            log.warn("当前文件已审阅");
            return false;
        }*/
        return true;
    }
}
