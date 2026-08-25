package com.teaching.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件上传接口
 *
 * @author teaching
 */
public interface ISysFileService {
    /**
     * 文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    public String uploadFile(MultipartFile file) throws Exception;

    /**
     * 视频上传接口
     *
     * @param file
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadVideo(MultipartFile file) throws Exception;

    /**
     * 文件上传接口 多文件
     *
     * @param files
     * @return
     * @throws Exception
     */
    public List<String> uploadFiles(MultipartFile[] files) throws Exception;

    /**
     * 文件删除接口
     *
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    public void deleteFile(String fileUrl) throws Exception;

    String getFilePath();

}
