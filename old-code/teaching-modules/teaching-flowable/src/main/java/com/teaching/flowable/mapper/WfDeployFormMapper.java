package com.teaching.flowable.mapper;

import com.teaching.flowable.core.mapper.BaseMapperPlus;
import com.teaching.flowable.domain.WfDeployForm;
import com.teaching.flowable.domain.vo.WfDeployFormVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程实例关联表单Mapper接口
 *
 * @author KonBAI
 * @createTime 2022/3/7 22:07
 */
public interface WfDeployFormMapper extends BaseMapperPlus<WfDeployFormMapper, WfDeployForm, WfDeployFormVo> {

    /**
     * 根据流程定义ID更新在线状态
     *
     * @param ids
     * @param status
     */
    void updateReProcdefOnLineStatusById(@Param("ids") List<String> ids, @Param("status") String status);
}
