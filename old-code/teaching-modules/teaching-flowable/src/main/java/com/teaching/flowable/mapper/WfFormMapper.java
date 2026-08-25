package com.teaching.flowable.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.teaching.flowable.core.mapper.BaseMapperPlus;
import com.teaching.flowable.domain.WfForm;
import com.teaching.flowable.domain.vo.WfFormVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程表单Mapper接口
 *
 * @author KonBAI
 * @createTime 2022/3/7 22:07
 */
public interface WfFormMapper extends BaseMapperPlus<WfFormMapper, WfForm, WfFormVo> {

    List<WfFormVo> selectFormVoList(@Param(Constants.WRAPPER) Wrapper<WfForm> queryWrapper);
}
