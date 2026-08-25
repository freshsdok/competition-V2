package com.teaching.system.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONArray;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.domain.SysUserGroupCompetitionRelation;
import com.teaching.system.mapper.FileTaskMapper;
import com.teaching.system.mapper.SysUserGroupMapper;
import com.teaching.system.mapper.SysUserMapper;
import com.teaching.system.service.ISysUserGroupService;
import com.teaching.system.service.SysAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户组管理Service业务层处理
 *
 * @author teaching
 * @date 2026-01-07
 */
@Slf4j
@Service
public class SysUserGroupServiceImpl implements ISysUserGroupService {
    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysAsyncService sysAsyncService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FileTaskMapper fileTaskMapper;

    /**
     * 查询用户组管理
     *
     * @param id 用户组管理主键
     * @return 用户组管理
     */
    @Override
    public SysUserGroup selectSysUserGroupById(Long id) {
        SysUserGroup sysUserGroup = sysUserGroupMapper.selectSysUserGroupById(id);
        String userIds = sysUserGroup.getUserIds();
        if (StringUtils.isNotBlank(userIds)) {
            Map<String, String> map = new HashMap<>();
            map.put("userIds", userIds);
            List<Map<String, Object>> maps = userMapper.selectUserListForUserGroup(map);
            sysUserGroup.setUserList(maps);
        }
        String blackUserIds = sysUserGroup.getBlackUserIds();
        if (StringUtils.isNotBlank(blackUserIds)) {
            Map<String, String> map = new HashMap<>();
            map.put("userIds", blackUserIds);
            List<Map<String, Object>> maps = userMapper.selectUserListForUserGroup(map);
            sysUserGroup.setBlackUserList(maps);
        }
        return sysUserGroup;
    }

    /**
     * 查询用户组管理列表
     *
     * @param sysUserGroup 用户组管理
     * @return 用户组管理
     */
    @Override
    public List<SysUserGroup> selectSysUserGroupList(SysUserGroup sysUserGroup) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        boolean admin = sysUser.isAdmin();
        List<SysUserGroup> sysUserGroups = sysUserGroupMapper.selectSysUserGroupList(sysUserGroup);
        if (CollectionUtils.isNotEmpty(sysUserGroups)) {
            sysUserGroups.forEach(userGroup -> {
                Set<Long> cacheList = redisService.getCacheObject("groupUserIds:info:" + userGroup.getId());
                userGroup.setUserIdCount(CollectionUtils.isNotEmpty(cacheList) ? cacheList.size() : 0L);
                if (admin) {
                    userGroup.setAdmin(true);
                    return;
                }
                Long createBy = Long.parseLong(userGroup.getCreateBy());
                if (sysUser.getUserId().equals(createBy)) {
                    userGroup.setAdmin(true);
                    return;
                }
                List<Map<String, Object>> groupManagerList = userGroup.getGroupManagerList();
                if (CollectionUtils.isNotEmpty(groupManagerList)) {
                    boolean isManager = groupManagerList.stream()
                            .anyMatch(groupManager ->
                                    sysUser.getUserId().equals(MapUtil.getLong(groupManager, "userId"))
                            );
                    if (isManager) {
                        userGroup.setAdmin(true);
                    }
                }
            });
        }
        return sysUserGroups;
    }

    /**
     * 新增用户组管理
     *
     * @param sysUserGroup 用户组管理
     * @return 结果
     */
    @Transactional
    @Override
    public int insertSysUserGroup(SysUserGroup sysUserGroup) {
        List<Map<String, Object>> groupManagerList = sysUserGroup.getGroupManagerList();
        sysUserGroup.setGroupManager(JSONArray.toJSONString(groupManagerList));
        int rows = sysUserGroupMapper.insertSysUserGroup(sysUserGroup);
        insertSysUserGroupCompetitionRelation(sysUserGroup);
        sysAsyncService.getUserInfosByUserGroups(List.of(sysUserGroup), sysUserGroup.getId());
        return rows;
    }

    /**
     * 修改用户组管理
     *
     * @param sysUserGroup 用户组管理
     * @return 结果
     */
    @Transactional
    @Override
    public int updateSysUserGroup(SysUserGroup sysUserGroup) {
        sysUserGroupMapper.deleteSysUserGroupCompetitionRelationByUserGroupId(sysUserGroup.getId());
        insertSysUserGroupCompetitionRelation(sysUserGroup);
        sysAsyncService.updateCachedUserGroupInfo(sysUserGroup.getId());
        sysAsyncService.getUserInfosByUserGroups(List.of(sysUserGroup), null);
        List<Map<String, Object>> groupManagerList = sysUserGroup.getGroupManagerList();
        if (CollectionUtils.isNotEmpty(groupManagerList)) {
            sysUserGroup.setGroupManager(JSONArray.toJSONString(groupManagerList));
        }
        return sysUserGroupMapper.updateSysUserGroup(sysUserGroup);
    }

    /**
     * 批量删除用户组管理
     *
     * @param ids 需要删除的用户组管理主键
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteSysUserGroupByIds(Long[] ids) {
        checkUserGroupIsUsed(ids);
        sysUserGroupMapper.deleteSysUserGroupCompetitionRelationByUserGroupIds(ids);
        sysAsyncService.delUserGroupIds(ids);
        return sysUserGroupMapper.deleteSysUserGroupByIds(ids, SecurityUtils.getUsername());
    }

    /**
     * 删除用户组管理信息
     *
     * @param id 用户组管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteSysUserGroupById(Long id) {
        sysUserGroupMapper.deleteSysUserGroupCompetitionRelationByUserGroupId(id);
        sysAsyncService.delUserGroupId(id);
        return sysUserGroupMapper.deleteSysUserGroupById(id, SecurityUtils.getUsername());
    }

    /**
     * 新增用户组关联赛事关系信息
     *
     * @param sysUserGroup 用户组管理对象
     */
    public void insertSysUserGroupCompetitionRelation(SysUserGroup sysUserGroup) {
        List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelationList = sysUserGroup.getSysUserGroupCompetitionRelationList();
        Long id = sysUserGroup.getId();
        if (StringUtils.isNotNull(sysUserGroupCompetitionRelationList)) {
            List<SysUserGroupCompetitionRelation> list = new ArrayList<SysUserGroupCompetitionRelation>();
            for (SysUserGroupCompetitionRelation sysUserGroupCompetitionRelation : sysUserGroupCompetitionRelationList) {
                sysUserGroupCompetitionRelation.setUserGroupId(id);
                list.add(sysUserGroupCompetitionRelation);
            }
            if (!list.isEmpty()) {
                sysUserGroupMapper.batchSysUserGroupCompetitionRelation(list);
            }
        }
    }

    /**
     * 校验用户组是否被使用
     *
     * @param ids
     */
    public void checkUserGroupIsUsed(Long[] ids) {
        Long l = fileTaskMapper.selectTaskByUserGroupIds(ids);
        if (l != null && l > 0L) {
            throw new GlobalException("该用户组已被使用，无法删除");
        }
    }

    @Override
    public List<Map<String, Object>> getUserByUserGroup(Long groupId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> cacheList = redisService.getCacheObject("groupUserIds:info:" + groupId);
        if (CollectionUtils.isNotEmpty(cacheList)) {
            //分批次查询
            List<Long> idList = new ArrayList<>(cacheList);
            int batchSize = 200;
            for (int i = 0; i < cacheList.size(); i += batchSize) {
                List<Long> batchIds = idList.subList(i, Math.min(i + batchSize, idList.size()));
                List<Map<String, Object>> maps = userMapper.selectUserListByUserGroupIds(batchIds);
                result.addAll(maps);
            }
        }
        return result;
    }

    /**
     * 根据用户组ids查询，并保持ids顺序
     * @param userGroupIds
     * @return
     */
    @Override
    public List<SysUserGroup> getSysUserGroupByIds(List<Long> userGroupIds) {
        List<SysUserGroup> sysUserGroups = sysUserGroupMapper.selectSysUserGroupByIds(userGroupIds);
        //保持顺序
        if(CollectionUtils.isNotEmpty(sysUserGroups)){
            Map<Long, SysUserGroup> groupMap = sysUserGroups.stream()
                    .collect(Collectors.toMap(SysUserGroup::getId, Function.identity()));
            List<SysUserGroup> orderedList = userGroupIds.stream()
                    .map(groupMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            return orderedList;
        }
        return sysUserGroups;
    }
}
