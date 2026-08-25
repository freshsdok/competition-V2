package com.teaching.competition.review.controller;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.service.IReviewFilePreviewService;
import com.teaching.competition.review.vo.ReviewPreviewResource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 评审材料预览与下载控制器。
 */
@RestController
@RequestMapping("/review/material")
public class ReviewMaterialController extends BaseController {
    @Autowired
    private IReviewFilePreviewService reviewFilePreviewService;

    @RequiresPermissions("competition:review:my-review:query")
    @GetMapping("/preview/{fileId}")
    public AjaxResult preview(@PathVariable("fileId") Long fileId) {
        return success(reviewFilePreviewService.preview(fileId));
    }

    @RequiresPermissions("competition:review:my-review:query")
    @GetMapping("/preview-stream/{fileId}")
    public void previewStream(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
        try {
            writeResource(response, reviewFilePreviewService.previewStream(fileId), "inline");
        } catch (ServiceException ex) {
            writeError(response, ex.getCode() == null ? 500 : ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(response, 500, "材料文件读取失败，请下载查看");
        }
    }

    @RequiresPermissions("competition:review:my-review:query")
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
        try {
            writeResource(response, reviewFilePreviewService.download(fileId), "attachment");
        } catch (ServiceException ex) {
            writeError(response, ex.getCode() == null ? 500 : ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(response, 500, "材料文件下载失败，请稍后重试");
        }
    }

    private void writeResource(HttpServletResponse response, ReviewPreviewResource resource, String dispositionType) throws Exception {
        try (ReviewPreviewResource outputResource = resource;
             InputStream input = outputResource.openStream()) {
            response.reset();
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(outputResource.getContentType());
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    dispositionType + "; filename*=UTF-8''" + encodeFileName(outputResource.getFileName()));
            if (outputResource.getContentLength() >= 0) {
                response.setContentLengthLong(outputResource.getContentLength());
            }
            try (OutputStream output = response.getOutputStream()) {
                input.transferTo(output);
            }
        }
    }

    private void writeError(HttpServletResponse response, int code, String message) {
        try {
            response.reset();
            response.setStatus(code);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            String safeMessage = message == null ? "材料预览失败，请下载查看" : message.replace("\"", "\\\"");
            response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + safeMessage + "\"}");
        } catch (Exception ignored) {
        }
    }

    private String encodeFileName(String fileName) {
        String safeName = fileName == null ? "download" : fileName;
        return URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
