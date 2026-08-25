package com.teaching.content.service;

import com.teaching.content.domain.ContentFile;

import java.util.List;

/**
 * 内容文件Service接口
 *
 * @author teaching
 * @date 2025-12-10
 */
public interface IContentFileService {
    /**
     * 查询内容文件
     *
     * @param fileId 内容文件主键
     * @return 内容文件
     */
    public ContentFile selectContentFileByFileId(Long fileId);

    /**
     * 查询内容文件列表
     *
     * @param contentFile 内容文件
     * @return 内容文件集合
     */
    public List<ContentFile> selectContentFileList(ContentFile contentFile);

    /**
     * 根据栏目ID查询文件列表
     *
     * @param columnId 栏目ID
     * @return 内容文件集合
     */
    public List<ContentFile> selectContentFileListByColumnId(Long columnId);

    /**
     * 新增内容文件
     *
     * @param contentFile 内容文件
     * @return 结果
     */
    public int insertContentFile(ContentFile contentFile);

    /**
     * 修改内容文件
     *
     * @param contentFile 内容文件
     * @return 结果
     */
    public int updateContentFile(ContentFile contentFile);

    /**
     * 删除内容文件
     *
     * @param fileId 内容文件主键
     * @return 结果
     */
    public int deleteContentFileByFileId(Long fileId);

    /**
     * 批量删除内容文件
     *
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteContentFileByFileIds(Long[] fileIds);

    /**
     * 根据栏目ID删除文件
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    public int deleteContentFileByColumnId(Long columnId);
}
