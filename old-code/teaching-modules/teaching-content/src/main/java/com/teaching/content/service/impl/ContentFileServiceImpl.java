package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ContentFile;
import com.teaching.content.mapper.ContentFileMapper;
import com.teaching.content.service.IContentFileService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容文件Service业务层处理
 *
 * @author teaching
 * @date 2025-12-10
 */
@Service
public class ContentFileServiceImpl implements IContentFileService {
    @Autowired
    private ContentFileMapper contentFileMapper;

    /**
     * 查询内容文件
     *
     * @param fileId 内容文件主键
     * @return 内容文件
     */
    @Override
    public ContentFile selectContentFileByFileId(Long fileId) {
        return contentFileMapper.selectContentFileByFileId(fileId);
    }

    /**
     * 查询内容文件列表
     *
     * @param contentFile 内容文件
     * @return 内容文件集合
     */
    @Override
    public List<ContentFile> selectContentFileList(ContentFile contentFile) {
        return contentFileMapper.selectContentFileList(contentFile);
    }

    /**
     * 根据栏目ID查询文件列表
     *
     * @param columnId 栏目ID
     * @return 内容文件集合
     */
    @Override
    public List<ContentFile> selectContentFileListByColumnId(Long columnId) {
        ContentFile contentFile = new ContentFile();
        contentFile.setColumnId(columnId);
        contentFile.setDelFlag("0");  // 只查询未删除的文件
        return contentFileMapper.selectContentFileList(contentFile);
    }

    /**
     * 新增内容文件
     *
     * @param contentFile 内容文件
     * @return 结果
     */
    @Override
    public int insertContentFile(ContentFile contentFile) {
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentFile.setCreateBy(sysUser.getNickName());
        }
        contentFile.setCreateTime(DateUtils.getNowDate());
        // 默认删除标志为0（未删除）
        if (contentFile.getDelFlag() == null || contentFile.getDelFlag().isEmpty()) {
            contentFile.setDelFlag("0");
        }
        // 默认状态为正常（0）
        if (contentFile.getStatus() == null || contentFile.getStatus().isEmpty()) {
            contentFile.setStatus("0");
        }
        // 默认排序为0
        if (contentFile.getOrderNum() == null) {
            contentFile.setOrderNum(0);
        }
        return contentFileMapper.insertContentFile(contentFile);
    }

    /**
     * 修改内容文件
     *
     * @param contentFile 内容文件
     * @return 结果
     */
    @Override
    public int updateContentFile(ContentFile contentFile) {
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentFile.setUpdateBy(sysUser.getNickName());
        }
        contentFile.setUpdateTime(DateUtils.getNowDate());
        return contentFileMapper.updateContentFile(contentFile);
    }

    /**
     * 删除内容文件
     *
     * @param fileId 内容文件主键
     * @return 结果
     */
    @Override
    public int deleteContentFileByFileId(Long fileId) {
        return contentFileMapper.deleteContentFileByFileId(fileId);
    }

    /**
     * 批量删除内容文件
     *
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteContentFileByFileIds(Long[] fileIds) {
        return contentFileMapper.deleteContentFileByFileIds(fileIds);
    }

    /**
     * 根据栏目ID删除文件
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    @Override
    public int deleteContentFileByColumnId(Long columnId) {
        // 先查询该栏目下的所有文件
        ContentFile contentFile = new ContentFile();
        contentFile.setColumnId(columnId);
        List<ContentFile> files = contentFileMapper.selectContentFileList(contentFile);
        
        // 如果有文件，则批量删除
        if (files != null && !files.isEmpty()) {
            Long[] fileIds = new Long[files.size()];
            for (int i = 0; i < files.size(); i++) {
                fileIds[i] = files.get(i).getFileId();
            }
            return contentFileMapper.deleteContentFileByFileIds(fileIds);
        }
        return 0;
    }
}
