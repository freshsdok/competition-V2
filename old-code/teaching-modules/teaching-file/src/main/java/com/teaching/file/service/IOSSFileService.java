package com.teaching.file.service;

import com.teaching.system.api.domain.PackageFileReq;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件上传接口
 *
 * @author teaching
 */
public interface IOSSFileService {
    /**
     * 文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    public String uploadFile(MultipartFile file, String bizSign, String bizCode) throws Exception;

    /**
     * 生成预签名URL
     *
     * @param objectKey
     * @param expireTimeInMillis
     * @return
     */
    public String generatePresignedUrl(String objectKey, long expireTimeInMillis) throws Exception;
    public String generatePresignedUrl2(String objectKey, long expireTimeInMillis) throws Exception;

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
    public List<String> uploadFiles(MultipartFile[] files, String bizSign, String bizCode) throws Exception;

    /**
     * 文件删除接口
     *
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    public void deleteFile(String fileUrl) throws Exception;

    String getFilePath();

    public Map<String, String> generateSTSToken(String bizSign, String bizCode);

    Map<String, Object> packageFile(List<PackageFileReq> fileList);

    /**
     * 通过OSS地址下载文件到本地
     *
     * @param ossUrl OSS文件地址
     * @param localPath 本地存储路径
     * @return 下载结果
     * @throws Exception
     */
    boolean downloadFileToLocal(String ossUrl, String localPath) throws Exception;

    /**
     * 上传本地文件到OSS
     *
     * @param localPath 本地文件路径
     * @param bizSign 业务标识
     * @param bizCode 业务编码
     * @return OSS文件访问地址
     * @throws Exception
     */
    String uploadLocalFile(String localPath, String bizSign, String bizCode) throws Exception;

    /**
     * 上传本地目录下所有文件到OSS
     *
     * @param localDir 本地目录路径
     * @param bizSign 业务标识
     * @param bizCode 业务编码
     * @param recursive 是否递归上传子目录
     * @return OSS文件访问地址列表
     * @throws Exception
     */
    List<String> uploadLocalDirectory(String localDir, String bizSign, String bizCode, boolean recursive) throws Exception;

    /**
     * 处理PDF文件（下载、裁剪页面、添加水印、上传）
     * <p>
     * 功能说明：
     * 1. 从OSS下载文件到本地临时目录
     * 2. 裁剪PDF页面（删除前几页和后几页）
     * 3. 添加水印
     * 4. 上传处理后的文件到OSS
     * 5. 清理本地临时文件
     * </p>
     *
     * @param ossUrl OSS文件地址
     * @return 处理后的OSS文件地址
     * @throws Exception
     */
    String processPdfFile(String ossUrl) throws Exception;
}
