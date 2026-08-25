package com.teaching.wxApp.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class MiniQrCodeService {

    private final WxMaService wxMaService;

    /**
     * 生成无限制的小程序码（永久有效，数量无限制）
     * 使用 createWxaCodeUnlimit 接口，适合大多数业务场景
     * @param scene 场景值（必填，最大32个字符，如"userId=123&type=share"）
     * @param page 小程序页面路径（可选，默认首页，如"pages/index/index"）
     * @param width 二维码宽度（280-1280，默认430）
     * @return 二维码Base64编码（前端可直接展示）
     */
    public String generateUnlimitedQrCode(String scene, String page, Integer width) {
        try {
            // 1. 校验必填参数scene
            if (scene == null || scene.trim().isEmpty() || scene.length() > 32) {
                throw new IllegalArgumentException("场景值scene不能为空，且长度不能超过32个字符");
            }

            // 2. 校验page参数（可选，为空则默认首页）
            if (page != null) {
                if (page.contains(".html") || page.startsWith("/")) {
                    throw new IllegalArgumentException("页面路径格式错误，应为相对路径（如pages/index/index）");
                }
            }

            // 3. 校验width参数
            int validWidth = width != null ? width : 430;
            if (validWidth < 280 || validWidth > 1280) {
                throw new IllegalArgumentException("二维码宽度必须在280-1280之间");
            }

            // 4. 生成无限制二维码（核心方法：createWxaCodeUnlimit）
            WxMaCodeLineColor lineColor = new WxMaCodeLineColor("0", "0", "0"); // 黑色线条
            File qrCodeFile = wxMaService.getQrcodeService().createWxaCodeUnlimit(
                    scene,       // 场景值（必填）
                    page        // 页面路径（可选，null则跳首页）  ///pages/scan/result?id=x 
            );

            return fileToBase64(qrCodeFile);
        } catch (IllegalArgumentException e) {
            log.error("参数校验失败：{}", e.getMessage());
            throw e;
        } catch (WxErrorException e) {
            log.error("微信接口调用失败：错误码={}，错误信息={}", e.getError(), e.getMessage());
            throw new RuntimeException("生成无限制二维码失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("文件处理异常", e);
            throw new RuntimeException("二维码生成异常");
        }
    }

    /**
     * 将文件转为Base64编码（复用方法）
     */
    private String fileToBase64(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } finally {
            // 清理临时文件
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    log.warn("临时文件删除失败，路径：{}", file.getAbsolutePath());
                    file.deleteOnExit();
                }
            }
        }
    }
}
