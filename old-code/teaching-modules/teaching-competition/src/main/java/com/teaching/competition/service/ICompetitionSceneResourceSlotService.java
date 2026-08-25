package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotBatchReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;

import java.util.List;

/**
 * 大赛现场设备资源预约时段Service接口。
 */
public interface ICompetitionSceneResourceSlotService {

    CompetitionSceneResourceSlotVO selectCompetitionSceneResourceSlotById(Long slotId);

    List<CompetitionSceneResourceSlotVO> selectCompetitionSceneResourceSlotList(CompetitionSceneResourceSlotQuery query);

    int insertCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot);

    int batchGenerateCompetitionSceneResourceSlot(CompetitionSceneResourceSlotBatchReq req);

    int updateCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot);

    int deleteCompetitionSceneResourceSlotByIds(Long[] slotIds);

    int changeCompetitionSceneResourceSlotStatus(CompetitionSceneResourceSlotStatusReq req);
}
