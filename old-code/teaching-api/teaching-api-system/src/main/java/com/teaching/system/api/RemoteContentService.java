package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.factory.RemoteContentServiceFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 内容服务接口
 *
 * @author Administrator
 */
@FeignClient(contextId = "contentService", value = ServiceNameConstants.CONTENT_SERVICE, fallbackFactory = RemoteContentServiceFactory.class)
public interface RemoteContentService {

    /**
     * 通过id查询详情
     */
    @GetMapping("/page/{pageId}")
    public R<PageManagerInfo> getContentDetailInfoById(@PathVariable("pageId") Long pageId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     * 修改页面审核状态
     */
    @PostMapping("/page/updateStatus")
    public R<Integer> updateContentInfoStatus(@RequestBody PageInfo pageinfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过id查询资讯详情
     */
    @GetMapping("/newsInfo/inner/detail/{newsId}")
    public R<NewsInfo> getNewsDetailInfoById(@PathVariable("newsId") Long newsId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改资讯审核状态
     */
    @PostMapping("/newsInfo/inner/updateAuditStatus")
    public R<Integer> updateNewsInfoStatus(@RequestBody NewsInfo newsInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过id查询通知公告详情
     */
    @GetMapping("/noticeInfo/inner/detail/{noticeId}")
    public R<NoticeInfo> getNoticeDetailInfoById(@PathVariable("noticeId") Long noticeId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改通知公告审核状态
     */
    @PostMapping("/noticeInfo/inner/updateAuditStatus")
    public R<Integer> updateNoticeInfoStatus(@RequestBody NoticeInfo noticeInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
