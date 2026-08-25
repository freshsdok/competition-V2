package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场主体操作状态Mapper接口。
 */
public interface CompetitionSceneSubjectOperationStateMapper {

    CompetitionSceneSubjectOperationState selectDoneOperationState(CompetitionSceneSubjectOperationStateQuery query);

    List<CompetitionSceneSubjectOperationState> selectDoneOperationStateList(CompetitionSceneSubjectOperationStateQuery query);

    int insertDoneOperationStateIfAbsent(CompetitionSceneSubjectOperationState state);

    int cancelDoneOperationState(@Param("query") CompetitionSceneSubjectOperationStateQuery query,
                                 @Param("updateBy") String updateBy);

    int updateOperationStateLastLogId(@Param("stateId") Long stateId,
                                      @Param("lastLogId") Long lastLogId,
                                      @Param("updateBy") String updateBy);
}
