package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionSceneResourceBookableQuery;
import com.teaching.competition.domain.CompetitionSceneResourceReservationCancelReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationReq;
import com.teaching.competition.exception.CompetitionSceneReservationException;
import com.teaching.competition.service.IUserCompetitionSceneResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端大赛现场设备资源预约Controller。
 */
@RestController
@RequestMapping({"/userCompetition", "/competition/userCompetition"})
public class UserCompetitionSceneResourceController extends BaseController {

    @Autowired
    private IUserCompetitionSceneResourceService userCompetitionSceneResourceService;

    @GetMapping("/sceneResource/bookableList")
    public AjaxResult bookableList(CompetitionSceneResourceBookableQuery query) {
        return handle(() -> success(userCompetitionSceneResourceService
                .selectBookableResourceList(currentUserId(), query)));
    }

    @GetMapping("/sceneResource/{scheduleResourceId}")
    public AjaxResult resourceDetail(@PathVariable("scheduleResourceId") Long scheduleResourceId) {
        return handle(() -> success(userCompetitionSceneResourceService
                .selectBookableResourceById(currentUserId(), scheduleResourceId)));
    }

    @GetMapping("/sceneResourceSlot/list")
    public AjaxResult slotList(Long scheduleResourceId) {
        return handle(() -> success(userCompetitionSceneResourceService
                .selectBookableSlotList(currentUserId(), scheduleResourceId)));
    }

    @PostMapping("/sceneResourceReservation")
    public AjaxResult reserve(@RequestBody CompetitionSceneResourceReservationReq req) {
        return handle(() -> success(userCompetitionSceneResourceService
                .submitReservation(currentUserId(), req)));
    }

    @GetMapping("/sceneResourceReservation/myList")
    public AjaxResult myReservationList() {
        return handle(() -> success(userCompetitionSceneResourceService
                .selectMyReservationList(currentUserId())));
    }

    @PostMapping("/sceneResourceReservation/cancel")
    public AjaxResult cancel(@RequestBody CompetitionSceneResourceReservationCancelReq req) {
        return handle(() -> success(userCompetitionSceneResourceService
                .cancelReservation(currentUserId(), req)));
    }

    private Long currentUserId() {
        return SecurityUtils.getLoginUser().getSysUser().getUserId();
    }

    private AjaxResult handle(AjaxSupplier supplier) {
        try {
            return supplier.get();
        } catch (CompetitionSceneReservationException e) {
            AjaxResult result = AjaxResult.error(5008, e.getMessage());
            result.put("errorCode", e.getErrorCode());
            if (e.getExistingReservation() != null) {
                result.put("existingReservation", e.getExistingReservation());
            }
            return result;
        }
    }

    @FunctionalInterface
    private interface AjaxSupplier {
        AjaxResult get();
    }
}
