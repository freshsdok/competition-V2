package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPublicity;
import com.teaching.competition.mapper.AwardPublicityMapper;
import com.teaching.competition.service.IAwardDetailsService;
import com.teaching.competition.service.IAwardPublicityService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 获奖公示管理Service业务层处理
 *
 * @author teaching
 * @date 2026-05-12
 */
@Service
public class AwardPublicityServiceImpl implements IAwardPublicityService {
    private static final Logger log = LoggerFactory.getLogger(AwardPublicityServiceImpl.class);
    @Autowired
    private AwardPublicityMapper awardPublicityMapper;

    @Autowired
    private IAwardDetailsService awardDetailsService;

    /**
     * 查询获奖公示管理
     *
     * @param id 获奖公示管理主键
     * @return 获奖公示管理
     */
    @Override
    public AwardPublicity selectAwardPublicityById(Long id) {
        return awardPublicityMapper.selectAwardPublicityById(id);
    }

    /**
     * * 导入/重导获奖公示数据
     *
     * @param detailList          要导入的数据 都必传
     * @param importType          导入类型 都必传 addition:追加导入  replace:重导
     * @param competitionSeriesId 赛事系列id 新建导入时必传，重导时可不传
     * @param competitionName     赛事名称  新建导入时必传，重导时可不传
     * @param awardPublicityId    公示管理记录id  新建导入时不传,重导时必传
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importData(List<AwardDetails> detailList, String importType, Long competitionSeriesId, String competitionName, Long awardPublicityId) {
        Map<String, Object> result = new HashMap<>();
        detailList = detailList.stream().filter(item -> !Objects.isNull(item) && !Objects.isNull(item.getTeamCode()) && !"".equals(item.getAwardsName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)) {
            log.info("导入数据失败,请检查模板或导入的文件内容");
            result.put("msg", "导入数据失败,请检查模板或导入的文件内容");
            result.put("importSuccess", false);
            return result;
        }
        //detailList中过滤掉teamCode是空的
        String username = SecurityUtils.getLoginUser().getSysUser().getNickName();
        Date nowDate = DateUtils.getNowDate();
        AwardPublicity publicity = new AwardPublicity();
        Long publicityId;
        if (Objects.isNull(awardPublicityId)) {
            //新建导入
            log.info("新建导入");
            publicity.setCompetitionName(competitionName);
            publicity.setCompetitionSeriesId(competitionSeriesId);
            publicity.setCreateTime(nowDate);
            publicity.setCreateBy(username);
            awardPublicityMapper.insertAwardPublicity(publicity);
            publicityId = publicity.getId();
        } else {
            log.info("重导");
            AwardPublicity awardPublicity = awardPublicityMapper.selectAwardPublicityById(awardPublicityId);
            if (Objects.isNull(awardPublicity)) {
                throw new GlobalException("要重导的记录不存在，请刷新列表后重试");
            }
            AwardPublicity updateAwardPublicity = new AwardPublicity();
            updateAwardPublicity.setUpdateBy(username);
            updateAwardPublicity.setUpdateTime(nowDate);
            updateAwardPublicity.setId(awardPublicityId);
            awardPublicityMapper.updateAwardPublicityUpdateInfo(updateAwardPublicity);
            publicityId = awardPublicityId;
        }
        Long finalPublicityId = publicityId;
        detailList.forEach(detail -> {
            detail.setTeamCode(detail.getTeamCode().trim());
            detail.setCreateTime(nowDate);
            detail.setCreateBy(username);
            detail.setAwardPublicityId(finalPublicityId);
            detail.setImportType(importType);
        });
        if ("addition".equals(importType)) {
            log.info("追加导入数据");
            //直接新增明细
            return awardDetailsService.insertAwardDetailsBatch(detailList);
        } else if ("replace".equals(importType)) {
            log.info("替换导入数据");
            //替换  先根据teamCode删除明细再新增
            Set<String> collect = detailList.stream().map(AwardDetails::getTeamCode).collect(Collectors.toSet());
            awardDetailsService.batchLogicDeleteByTeamCodes(collect, finalPublicityId);
            return awardDetailsService.insertAwardDetailsBatch(detailList);
        }
        result.put("importSuccess", false);
        result.put("msg", "导入数据失败，请检查导入类型是否有误");
        return result;
    }

    /**
     * 查询获奖公示管理列表
     *
     * @param awardPublicity 获奖公示管理
     * @return 获奖公示管理
     */
    @Override
    public List<AwardPublicity> selectAwardPublicityList(AwardPublicity awardPublicity) {
        List<AwardPublicity> awardPublicises = awardPublicityMapper.selectAwardPublicityList(awardPublicity);
        if (CollectionUtils.isEmpty(awardPublicises)) {
            return awardPublicises;
        }
        Date now = DateUtils.getNowDate();
        return awardPublicises.stream()
                .peek(awardPublicity1 -> {
                    Date expirationTime = awardPublicity1.getExpirationTime();
                    if (expirationTime == null) {
                        awardPublicity1.setStatus("未开始");
                    } else if (expirationTime.after(now)) {
                        awardPublicity1.setStatus("公示中");
                    } else {
                        awardPublicity1.setStatus("已结束");
                    }
                }).collect(Collectors.toList());
    }

    /**
     * 新增获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    @Override
    public int insertAwardPublicity(AwardPublicity awardPublicity) {
        awardPublicity.setCreateTime(DateUtils.getNowDate());
        awardPublicity.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        return awardPublicityMapper.insertAwardPublicity(awardPublicity);
    }

    /**
     * 修改获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    @Override
    public int updateAwardPublicity(AwardPublicity awardPublicity) {
        awardPublicity.setUpdateTime(DateUtils.getNowDate());
        awardPublicity.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return awardPublicityMapper.updateAwardPublicity(awardPublicity);
    }

    /**
     * 修改获奖公示管理 提示信息
     *
     * @param awardPublicity
     * @return
     */
    @Override
    public int updateAwardPublicityTipInfo(AwardPublicity awardPublicity) {
        AwardPublicity awardPublicityTip = new AwardPublicity();
        awardPublicityTip.setId(awardPublicity.getId());
        awardPublicityTip.setUpdateTime(DateUtils.getNowDate());
        awardPublicityTip.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        awardPublicityTip.setTipInfo(awardPublicity.getTipInfo());
        return awardPublicityMapper.updateAwardPublicityTipInfo(awardPublicityTip);
    }

    /**
     * 批量删除获奖公示管理
     *
     * @param ids 需要删除的获奖公示管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAwardPublicityByIds(Long[] ids) {
        String username = SecurityUtils.getLoginUser().getSysUser().getNickName();
        awardDetailsService.batchLogicDeleteByAwardPublicityId(ids, username);
        return awardPublicityMapper.batchLogicDeleteByTeamCodes(ids, username);
    }

    /**
     * 删除获奖公示管理信息
     *
     * @param id 获奖公示管理主键
     * @return 结果
     */
    @Override
    public int deleteAwardPublicityById(Long id) {
        return awardPublicityMapper.deleteAwardPublicityById(id);
    }
}
