package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteContentService;
import com.teaching.system.api.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 内容服务降级处理
 *
 * @author teaching
 */
@Component
public class RemoteContentServiceFactory implements FallbackFactory<RemoteContentService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteContentServiceFactory.class);

    @Override
    public RemoteContentService create(Throwable cause) {
        return new RemoteContentService() {
            @Override
            public R<PageManagerInfo> getContentDetailInfoById(Long pageId, String source) {
                return R.fail("获取页面详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateContentInfoStatus(PageInfo pageInfo, String source) {
                return R.fail("修改审核状态失败:" + cause.getMessage());
            }

            @Override
            public R<NewsInfo> getNewsDetailInfoById(Long newsId, String source) {
                log.error("获取资讯详情信息失败:{}", cause.getMessage());
                return R.fail("获取资讯详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateNewsInfoStatus(NewsInfo newsInfo, String source) {
                log.error("修改资讯审核状态失败:{}", cause.getMessage());
                return R.fail("修改资讯审核状态失败:" + cause.getMessage());
            }

            @Override
            public R<NoticeInfo> getNoticeDetailInfoById(Long noticeId, String source) {
                log.error("获取通知公告详情信息失败:{}", cause.getMessage());
                return R.fail("获取通知公告详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateNoticeInfoStatus(NoticeInfo noticeInfo, String source) {
                log.error("修改通知公告审核状态失败:{}", cause.getMessage());
                return R.fail("修改通知公告审核状态失败:" + cause.getMessage());
            }
        };
    }
}
