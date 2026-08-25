package com.teaching.file.service;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.file.FileUtils;
import com.teaching.file.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地文件存储
 *
 * @author teaching
 */
@Primary
@Service
public class LocalSysFileServiceImpl implements ISysFileService {
    /**
     * 资源映射路径 前缀
     */
    @Value("${file.prefix}")
    public String localFilePrefix;

    /**
     * 域名或本机访问地址
     */
    @Value("${file.domain}")
    public String domain;

    /**
     * 上传文件存储在本地的根路径
     */
    @Value("${file.path}")
    private String localFilePath;

    /**
     * 本地文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String name = FileUploadUtils.upload(localFilePath, file);
        String url = domain + localFilePrefix + name;
        return url;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) throws Exception {
        List<String> objectKeys = new ArrayList<>();
        for (MultipartFile file : files) {
            objectKeys.add(uploadFile(file));
        }
        return objectKeys;
    }

    /**
     * 本地文件删除接口
     *
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    @Override
    public void deleteFile(String fileUrl) throws Exception {
        String localFile = StringUtils.substringAfter(fileUrl, localFilePrefix);
        FileUtils.deleteFile(localFilePath + localFile);
    }

    @Override
    public String getFilePath() {
        return localFilePath;
    }

    @Override
    public Map<String, Object> uploadVideo(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String name = FileUploadUtils.upload(localFilePath, file);
        String url = domain + localFilePrefix + name;
        // 获取视频时长
        int time = FileUploadUtils.getVideoDurationWithFFmpeg(file);
        result.put("duration", time);
        result.put("objectKey", url);
        return result;
    }

}
