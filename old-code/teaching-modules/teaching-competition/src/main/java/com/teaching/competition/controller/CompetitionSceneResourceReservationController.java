package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneResourceReservationQuery;
import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import com.teaching.competition.mapper.CompetitionSceneResourceReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大赛现场设备资源预约记录Controller。
 */
@RestController
@RequestMapping({"/sceneResourceReservation", "/competition/sceneResourceReservation"})
public class CompetitionSceneResourceReservationController extends BaseController {

    @Autowired
    private CompetitionSceneResourceReservationMapper reservationMapper;

    @RequiresPermissions("competition:sceneScheduleResource:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneResourceReservationQuery query) {
        startPage();
        List<CompetitionSceneResourceReservationVO> list =
                reservationMapper.selectCompetitionSceneResourceReservationList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneScheduleResource:list")
    @GetMapping("/{reservationId}")
    public AjaxResult getInfo(@PathVariable("reservationId") Long reservationId) {
        return success(reservationMapper.selectCompetitionSceneResourceReservationById(reservationId));
    }
}
