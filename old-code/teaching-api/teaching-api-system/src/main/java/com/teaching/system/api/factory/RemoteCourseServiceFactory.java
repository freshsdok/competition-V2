package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteCourseService;
import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import com.teaching.system.api.domain.course.CourseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 内容服务降级处理
 *
 * @author teaching
 */
@Component
public class RemoteCourseServiceFactory implements FallbackFactory<RemoteCourseService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteCourseServiceFactory.class);

    @Override
    public RemoteCourseService create(Throwable cause) {
        return new RemoteCourseService()

        {
            @Override
            public R<CourseInfo> getCourseDetailInfoById(Long pageId, String source) {
                return R.fail("获取课程详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateCourseInfoStatus(PageInfo pageInfo, String source) {
                return R.fail("修改审核状态失败:" + cause.getMessage());
            }

            @Override
            public R<CourseChapterInfo> getChapterVideoDetailInfoById(Long videoId, String source) {
                return R.fail("获取章节视频详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateChapterVideoStatus(ChapterAuditResult auditResult, String source) {
                return R.fail("修改章节视频审核状态失败:" + cause.getMessage());
            }

            @Override
            public R<List<CourseChapterVideo>> getChapterVideoDetailInfoByIds(Long[] videoIds, String source) {
                return R.fail("获取章节视频详情信息失败:" + cause.getMessage());
            }
        };
    }
}
