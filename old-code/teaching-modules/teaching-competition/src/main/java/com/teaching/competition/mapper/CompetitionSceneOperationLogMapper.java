package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneOperationLog;

import java.util.List;

/**
 * 赛事现场扫码操作流水Mapper接口。
 */
public interface CompetitionSceneOperationLogMapper {

    List<CompetitionSceneOperationLog> selectCompetitionSceneOperationLogList(CompetitionSceneOperationLog log);

    int insertCompetitionSceneOperationLog(CompetitionSceneOperationLog log);
}
