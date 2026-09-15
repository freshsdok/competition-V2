package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.integration.nativecollection.NativeCollectionIntent;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 新入口只在本地短事务内核对办理权并加密意图，没有V2 HTTP调用或跨用户交互的事务。 */
@Service
public class TeamCollectionNativeService {
    private final TeamCollectionService teams;
    private final boolean enabled;
    private final NativeCollectionIntent intents;
    public TeamCollectionNativeService(TeamCollectionService teams,
            @Value("${v1.team-collection.native-enabled:false}") boolean enabled) {
        this.teams=teams;this.enabled=enabled;
        this.intents=new NativeCollectionIntent(System.getenv("V1_V2_INTERNAL_BRIDGE_HMAC_SECRET"),Clock.systemUTC());
    }
    public List<TeamCollectionService.View> mine(long userId) { return enabled ? teams.mine(userId) : List.of(); }
    @Transactional
    public NativeCollectionIntent.Entry entry(long teamId,long userId,long version) {
        if (!enabled) throw new ServiceException("新版登录填报入口尚未开放",503);
        teams.assertNativeHolder(teamId,userId,version);
        try { return intents.issue(userId); }
        catch (IllegalStateException unavailable) { throw new ServiceException("新版登录填报入口配置不完整，办理名额已保留",503); }
    }
}
