package com.teaching.file.controller;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.file.FileUtils;
import com.teaching.common.core.utils.poi.CustomMultipartFile;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.file.service.ISysFileService;
import com.teaching.file.utils.FileSizeUtil;
import com.teaching.system.api.domain.SysFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件请求处理
 *
 * @author teaching
 */
@RestController
public class SysFileController {
    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    @Autowired
    private ISysFileService sysFileService;

    /**
     * 上传文件存储在本地的根路径
     */
    @Value("${file.path}")
    private String localFilePath;

    /**
     * 资源映射路径 前缀
     */
    @Value("${file.prefix}")
    public String localFilePrefix;

    @Value("${file.downloadPath}")
    public String downloadPath;

    /**
     * 文件上传请求
     */
    @PostMapping("upload")
    public R<SysFile> upload(MultipartFile file) {
        try {
            // 上传并返回访问地址
            String url = sysFileService.uploadFile(file);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 视频上传请求
     */
    @PostMapping("uploadVideo")
    public R<SysFile> uploadVideo(MultipartFile file) {
        try {
            // 上传并返回访问地址
            Map<String,Object> map = sysFileService.uploadVideo(file);
            SysFile sysFile = new SysFile();
            String url = MapUtils.getString(map, "objectKey");
            String duration = MapUtils.getString(map, "duration");
            sysFile.setDuration(duration);
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 文件删除请求
     */
    @DeleteMapping("delete")
    public R<Boolean> delete(String fileUrl) {
        try {
            if (!FileUtils.validateFilePath(fileUrl)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许删除。 ", fileUrl));
            }
            sysFileService.deleteFile(fileUrl);
            return R.ok();
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/getFilePath")
    public R<String> getFilePath() {
        return R.ok(sysFileService.getFilePath());
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/download")
    public void resourceDownload(@RequestParam String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            // 本地资源路径
            String localPath = localFilePath;
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, localFilePrefix);
            log.info("下载地址：{}", downloadPath);
            System.out.println("下载地址"+downloadPath);
            File file = new File(downloadPath);
            // 清空缓冲区，状态码和响应头(headers)
            response.reset();
            // 设置ContentType，响应内容为二进制数据流，编码为utf-8，此处设定的编码是文件内容的编码
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 以（Content-Disposition: attachment; filename="filename.jpg"）格式设定默认文件名，设定utf编码，此处的编码是文件名的编码，使能正确显示中文文件名
            //文件名
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");
            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                // 获取字节流
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                log.info("Download successfully!");
            }
            catch (Exception e) {
                log.info("Download failed!");
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * excel文件下载
     */
    @GetMapping("/excel/download/{fileName}")
    public void excelDownload(HttpServletRequest request, HttpServletResponse response,@PathVariable String fileName)
            throws Exception{
        try{
            // 数据库资源地址
            log.info("下载地址：{}", downloadPath);
            System.out.println("下载地址"+downloadPath);
            File file = new File(downloadPath+fileName);
            // 清空缓冲区，状态码和响应头(headers)
            response.reset();
            // 设置ContentType，响应内容为二进制数据流，编码为utf-8，此处设定的编码是文件内容的编码
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 以（Content-Disposition: attachment; filename="filename.jpg"）格式设定默认文件名，设定utf编码，此处的编码是文件名的编码，使能正确显示中文文件名
            //文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");
            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                // 获取字节流
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                log.info("Download successfully!");
            }
            catch (Exception e) {
                log.info("Download failed!");
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }


}
