package com.teaching.flowable.service;


import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.bo.WfCopyBo;
import com.teaching.flowable.domain.bo.WfTaskBo;
import com.teaching.flowable.domain.vo.WfCopyVo;

import java.util.List;

/**
 * 流程抄送Service接口
 *
 * @author KonBAI
 * @date 2022-05-19
 */
public interface IWfCopyService {

    /**
     * 查询流程抄送
     *
     * @param copyId 流程抄送主键
     * @return 流程抄送
     */
    WfCopyVo queryById(Long copyId);

    /**
     * 查询流程抄送列表
     *
     * @param wfCopy 流程抄送
     * @return 流程抄送集合
     */
    TableDataInfo<WfCopyVo> selectPageList(WfCopyBo wfCopy, PageQuery pageQuery);

    /**
     * 查询流程抄送列表
     *
     * @param wfCopy 流程抄送
     * @return 流程抄送集合
     */
    List<WfCopyVo> selectList(WfCopyBo wfCopy);

    /**
     * 抄送
     *
     * @param taskBo
     * @return
     */
    Boolean makeCopy(WfTaskBo taskBo);
}
