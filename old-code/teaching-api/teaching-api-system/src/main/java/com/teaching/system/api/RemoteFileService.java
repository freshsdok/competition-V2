package com.teaching.system.api;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.PackageFileReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.SysFile;
import com.teaching.system.api.factory.RemoteFileFallbackFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件服务
 * 
 * @author teaching
 */
@FeignClient(contextId = "remoteFileService", value = ServiceNameConstants.FILE_SERVICE, fallbackFactory = RemoteFileFallbackFactory.class)
public interface RemoteFileService
{
    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFile> upload(@RequestPart(value = "file") MultipartFile file);

    /**
     * 上传文件到oss服务器
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/oss/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> ossUpload(@RequestPart(value = "file") MultipartFile file,
                                @RequestParam(value = "bizSign") String bizSign,
                                @RequestParam(value = "bizCode",required = false) String bizCode);

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     * @return 结果
     */
    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<Boolean> delete(@RequestParam("fileUrl") String fileUrl);


    /**
     * 获取配置的默认文件路径
     */
    @GetMapping(value = "/getFilePath",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<String> getFilePath();

    @GetMapping(value = "/common/download")
    public void resourceDownload(@RequestParam String resource);


    @PostMapping(value= "/exportZipFile")
    public R<Map<String, Object>> zipFile(@RequestBody List<String> urls) throws IOException;

    @PostMapping(value = "/oss/packageFile")
    public R<Map<String, Object>> packageFile(@RequestBody List<PackageFileReq> fileList);

}
