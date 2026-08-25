package com.teaching.system.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.vo.TreeSelect;
import com.teaching.system.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.SysOrgMapper;
import com.teaching.system.service.ISysOrgService;

/**
 * 系统机构信息Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-23
 */
@Service
public class SysOrgServiceImpl implements ISysOrgService 
{
    @Autowired
    private SysOrgMapper sysOrgMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 查询系统机构信息
     * 
     * @param orgId 系统机构信息主键
     * @return 系统机构信息
     */
    @Override
    public SysOrg selectSysOrgByOrgId(Long orgId)
    {
        return sysOrgMapper.selectSysOrgByOrgId(orgId);
    }

    @Override
    public List<Long> selectSysOrgListByRoleId(Long roleId) {

        SysRole role = roleMapper.selectRoleById(roleId);
        return sysOrgMapper.selectSysOrgListByRoleId(role.getRoleId(),role.isOrgCheckStrictly());
    }

    /**
     * 查询系统机构信息列表
     * 
     * @param sysOrg 系统机构信息
     * @return 系统机构信息
     */
    @Override
    @DataScope(orgAlias = "o")
    public List<SysOrg> selectSysOrgList(SysOrg sysOrg)
    {
        return sysOrgMapper.selectSysOrgList(sysOrg);
    }

    /**
     * 查询机构树结构信息
     *
     * @param sysOrg 机构信息
     * @return 机构树信息集合
     */
    @Override
    public List<TreeSelect> selectOrgTreeList(SysOrg sysOrg)
    {
        List<SysOrg> sysOrgList = SpringUtils.getAopProxy(this).selectSysOrgList(sysOrg);
        return buildOrgTreeSelect(sysOrgList);
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param sysOrgList 机构信息
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildOrgTreeSelect(List<SysOrg> sysOrgList)
    {
        List<SysOrg> orgTreeList = buildOrgTree(sysOrgList);
        return orgTreeList.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要树结构
     *
     * @param sysOrgList 机构信息
     * @return 树结构列表
     */
    @Override
    public List<SysOrg> buildOrgTree(List<SysOrg> sysOrgList)
    {
        List<SysOrg> returnList = new ArrayList<SysOrg>();
        List<Long> tempList = sysOrgList.stream().map(SysOrg::getOrgId).collect(Collectors.toList());
        for (SysOrg org : sysOrgList)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(org.getParentId()))
            {
                recursionFn(sysOrgList, org);
                returnList.add(org);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = sysOrgList;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysOrg> list, SysOrg t)
    {
        // 得到子节点列表
        List<SysOrg> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysOrg tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysOrg> list, SysOrg t)
    {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * 得到子节点列表
     */
    private List<SysOrg> getChildList(List<SysOrg> list, SysOrg t)
    {
        List<SysOrg> tlist = new ArrayList<SysOrg>();
        Iterator<SysOrg> it = list.iterator();
        while (it.hasNext())
        {
            SysOrg n = (SysOrg) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getOrgId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 新增系统机构信息
     * 
     * @param sysOrg 系统机构信息
     * @return 结果
     */
    @Override
    public int insertSysOrg(SysOrg sysOrg)
    {
        sysOrg.setCreateTime(DateUtils.getNowDate());
        SysOrg info = sysOrgMapper.selectSysOrgByOrgId(sysOrg.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (info !=null && !UserConstants.DEPT_NORMAL.equals(info.getStatus()))
        {
            throw new ServiceException("机构停用，不允许新增");
        }
        if(info !=null){
            sysOrg.setAncestors(info.getAncestors() + "," + sysOrg.getParentId());
        }
        return sysOrgMapper.insertSysOrg(sysOrg);
    }

    /**
     * 修改系统机构信息
     * 
     * @param sysOrg 系统机构信息
     * @return 结果
     */
    @Override
    public int updateSysOrg(SysOrg sysOrg)
    {
        sysOrg.setUpdateTime(DateUtils.getNowDate());
        SysOrg newParentOrg = sysOrgMapper.selectSysOrgByOrgId(sysOrg.getParentId());
        SysOrg oldOrg = sysOrgMapper.selectSysOrgByOrgId(sysOrg.getOrgId());
        if (StringUtils.isNotNull(newParentOrg) && StringUtils.isNotNull(oldOrg)) {
            String newAncestors = newParentOrg.getAncestors() + "," + newParentOrg.getOrgId();
            String oldAncestors = oldOrg.getAncestors();
            sysOrg.setAncestors(newAncestors);
            updateOrgChildren(sysOrg.getOrgId(), newAncestors, oldAncestors);
        }
        int result = sysOrgMapper.updateSysOrg(sysOrg);
        if (UserConstants.DEPT_NORMAL.equals(sysOrg.getStatus()) && StringUtils.isNotEmpty(sysOrg.getAncestors())
                && !StringUtils.equals("0", sysOrg.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentOrgStatusNormal(sysOrg);
        }
        return result;
    }

    private void updateParentOrgStatusNormal(SysOrg sysOrg) {
        String ancestors = sysOrg.getAncestors();
        Long[] orgIds = Convert.toLongArray(ancestors);
        sysOrgMapper.updateOrgStatusNormal(orgIds);
    }

    /**
     * 修改子元素关系
     *
     * @param orgId 被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    private void updateOrgChildren(Long orgId, String newAncestors, String oldAncestors) {
        List<SysOrg> children = sysOrgMapper.selectChildrenOrgById(orgId);
        for (SysOrg child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            sysOrgMapper.updateOrgChildren(children);
        }
    }

    /**
     * 批量删除系统机构信息
     * 
     * @param orgIds 需要删除的系统机构信息主键
     * @return 结果
     */
    @Override
    public int deleteSysOrgByOrgIds(Long[] orgIds)
    {
        return sysOrgMapper.deleteSysOrgByOrgIds(orgIds);
    }

    /**
     * 删除系统机构信息信息
     * 
     * @param orgId 系统机构信息主键
     * @return 结果
     */
    @Override
    public int deleteSysOrgByOrgId(Long orgId)
    {
        return sysOrgMapper.deleteSysOrgByOrgId(orgId);
    }

    @Override
    public void checkOrgDataScope(Long orgId) {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()) && StringUtils.isNotNull(orgId))
        {
            SysOrg org = new SysOrg();
            org.setOrgId(orgId);
            List<SysOrg> orgs = SpringUtils.getAopProxy(this).selectSysOrgList(org);
            if (StringUtils.isEmpty(orgs))
            {
                throw new ServiceException("没有权限访问机构数据！");
            }
        }
    }

    @Override
    public boolean checkOrgNameUnique(SysOrg org) {
        Long orgId = StringUtils.isNull(org.getOrgId()) ? -1L : org.getOrgId();
        SysOrg info = sysOrgMapper.checkOrgNameUnique(org.getOrgName(), org.getParentId());
        if (StringUtils.isNotNull(info) && info.getOrgId().longValue() != orgId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public int selectNormalChildrenOrgById(Long orgId) {
        return sysOrgMapper.selectNormalChildrenOrgById(orgId);
    }

    @Override
    public boolean hasChildByOrgId(Long orgId) {
        int result = sysOrgMapper.hasChildByOrgId(orgId);
        return result > 0;
    }

    @Override
    public boolean checkOrgExistUser(Long orgId) {
        int result = sysOrgMapper.checkOrgExistUser(orgId);
        return result > 0;
    }
}
