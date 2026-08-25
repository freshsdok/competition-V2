package com.teaching.system.api;

import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.oss.OssUploadFeignConfig;
import com.teaching.system.api.domain.PackageFileReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(
        contextId = "remoteOssUploadService",
        value = ServiceNameConstants.FILE_SERVICE,
        configuration = OssUploadFeignConfig.class
)
public interface RemoteOssUploadService {
    /**
     * 上传文件到oss服务器
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/oss/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<String> ossUpload(@RequestPart(value = "file") MultipartFile file,
                        @RequestParam(value = "bizSign") String bizSign,
                        @RequestParam(value = "bizCode", required = false) String bizCode);


    @PostMapping(value = "/oss/packageFile")
    R<Map<String, Object>> packageFile(@RequestBody List<PackageFileReq> fileList);

    /** 为私有对象生成短时预签名地址。 */
    @GetMapping(value = "/oss/presignedUrl")
    R<String> getPresignedUrl(@RequestParam("fileKey") String fileKey);

    /** 删除已过期的导出对象。 */
    @DeleteMapping(value = "/oss/delete")
    R<Boolean> deleteFile(@RequestParam("objectKey") String objectKey);

    /**
     * 下载OSS文件到本地
     *
     * @param ossUrl OSS文件地址
     * @param localPath 本地存储路径
     * @return 下载结果
     */
    @PostMapping("/oss/downloadToLocal")
    R<Boolean> downloadToLocal(@RequestParam("ossUrl") String ossUrl,
                               @RequestParam("localPath") String localPath);

    /**
     * 上传本地文件到OSS
     *
     * @param localPath 本地文件路径
     * @param bizSign 业务标识
     * @param bizCode 业务编码
     * @return OSS文件访问地址
     */
    @PostMapping("/oss/uploadLocalFile")
    R<String> uploadLocalFile(@RequestParam("localPath") String localPath,
                              @RequestParam("bizSign") String bizSign,
                              @RequestParam(value = "bizCode", required = false) String bizCode);

    /**
     * 处理PDF文件（下载、裁剪页面、添加水印、上传）
     *
     * @param ossUrl OSS文件地址
     * @return 处理后的OSS文件地址
     */
    @PostMapping("/oss/processPdfFile")
    R<String> processPdfFile(@RequestParam("ossUrl") String ossUrl);

}
