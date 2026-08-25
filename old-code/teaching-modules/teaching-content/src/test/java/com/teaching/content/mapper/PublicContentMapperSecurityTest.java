package com.teaching.content.mapper;

import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.content.domain.NewsInfo;
import com.teaching.content.domain.NoticeInfo;
import com.teaching.content.domain.query.PublicNewsQuery;
import com.teaching.content.domain.query.PublicNoticeQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;
import com.teaching.content.domain.vo.PublicNoticeInfo;
import com.teaching.content.service.impl.NoticeInfoServiceImpl;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PublicContentMapperSecurityTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void setUp() throws Exception {
        String databaseName = "public_content_" + UUID.randomUUID().toString().replace("-", "");
        DataSource dataSource = new UnpooledDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                "");
        createSchemaAndFixtures(dataSource);
        sqlSessionFactory = buildSqlSessionFactory(dataSource);
    }

    @Test
    public void noticeQueriesShouldOnlyReturnCurrentlyPublishedNonDeletedRows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NoticeInfoMapper mapper = session.getMapper(NoticeInfoMapper.class);

            PublicNoticeInfo published = mapper.selectPublicNoticeInfoByNoticeId(1L);
            assertNotNull(published);
            assertEquals("公开公告", published.getNoticeTitle());
            assertNull(mapper.selectPublicNoticeInfoByNoticeId(2L));
            assertNull(mapper.selectPublicNoticeInfoByNoticeId(3L));
            assertNull(mapper.selectPublicNoticeInfoByNoticeId(4L));
            assertNull(mapper.selectPublicNoticeInfoByNoticeId(5L));

            assertNotNull(mapper.selectPublicNoticeInfoByNoticeId(6L));

            NoticeInfoServiceImpl service = noticeService(mapper);
            assertNull(service.selectPublicNoticeInfoByNoticeId(6L));
            List<PublicNoticeInfo> list = service.selectPublicNoticeInfoList(new PublicNoticeQuery());
            assertEquals(1, list.size());
            assertEquals(Long.valueOf(1L), list.get(0).getNoticeId());
        }
    }

    @Test
    public void newsQueriesShouldOnlyReturnCurrentlyPublishedNonDeletedRows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NewsInfoMapper mapper = session.getMapper(NewsInfoMapper.class);

            PublicNewsInfo published = mapper.selectPublicNewsInfoByNewsId(11L);
            assertNotNull(published);
            assertEquals("公开资讯", published.getNewsTitle());
            assertNull(mapper.selectPublicNewsInfoByNewsId(12L));
            assertNull(mapper.selectPublicNewsInfoByNewsId(13L));
            assertNull(mapper.selectPublicNewsInfoByNewsId(14L));
            assertNull(mapper.selectPublicNewsInfoByNewsId(15L));

            List<PublicNewsInfo> list = mapper.selectPublicNewsInfoList(new PublicNewsQuery());
            assertEquals(1, list.size());
            assertEquals(Long.valueOf(11L), list.get(0).getNewsId());
        }
    }

    @Test
    public void publicModelsAndQueriesShouldNotExposeBackendOrDynamicSqlFields()
            throws IntrospectionException {
        Set<String> forbidden = Set.of(
                "createBy", "createTime", "updateBy", "updateTime", "userId", "orgId",
                "checkStatus", "delFlag", "version", "remark", "publishPersonName", "params", "dataScope");

        assertTrue(propertiesOf(PublicNoticeInfo.class).containsAll(Set.of(
                "noticeId", "noticeTitle", "noticeContent", "noticeAbstract",
                "noticeType", "noticeImage", "noticeAuthor", "publishTime")));
        assertTrue(propertiesOf(PublicNewsInfo.class).containsAll(Set.of(
                "newsId", "newsTitle", "newsCont", "newsAbstract", "newsImage", "publishTime")));
        assertTrue(disjoint(propertiesOf(PublicNoticeInfo.class), forbidden));
        assertTrue(disjoint(propertiesOf(PublicNewsInfo.class), forbidden));
        assertTrue(disjoint(propertiesOf(PublicNoticeQuery.class), forbidden));
        assertTrue(disjoint(propertiesOf(PublicNewsQuery.class), forbidden));
        assertFalse(BaseEntity.class.isAssignableFrom(PublicNoticeQuery.class));
        assertFalse(BaseEntity.class.isAssignableFrom(PublicNewsQuery.class));
    }

    private boolean disjoint(Set<String> properties, Set<String> forbidden) {
        return properties.stream().noneMatch(forbidden::contains);
    }

    private Set<String> propertiesOf(Class<?> type) throws IntrospectionException {
        return Arrays.stream(Introspector.getBeanInfo(type).getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .collect(Collectors.toSet());
    }

    private NoticeInfoServiceImpl noticeService(NoticeInfoMapper mapper) {
        try {
            NoticeInfoServiceImpl service = new NoticeInfoServiceImpl();
            Field field = NoticeInfoServiceImpl.class.getDeclaredField("noticeInfoMapper");
            field.setAccessible(true);
            field.set(service, mapper);
            return service;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws IOException {
        Environment environment = new Environment(
                "test",
                new JdbcTransactionFactory(),
                dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias("NoticeInfo", NoticeInfo.class);
        configuration.getTypeAliasRegistry().registerAlias("NewsInfo", NewsInfo.class);
        parseMapper(configuration, "mapper/content/NoticeInfoMapper.xml");
        parseMapper(configuration, "mapper/content/NewsInfoMapper.xml");
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private void parseMapper(Configuration configuration, String resource) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull("Mapper resource not found: " + resource, input);
            XMLMapperBuilder builder = new XMLMapperBuilder(
                    input,
                    configuration,
                    resource,
                    configuration.getSqlFragments());
            builder.parse();
        }
    }

    private void createSchemaAndFixtures(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    create table notice_info (
                        notice_id bigint primary key,
                        notice_title varchar(200),
                        notice_content clob,
                        notice_abstract varchar(500),
                        notice_type varchar(20),
                        notice_status varchar(10),
                        notice_image varchar(1000),
                        notice_author varchar(100),
                        publish_time timestamp,
                        check_status varchar(10),
                        create_by varchar(100),
                        create_time timestamp,
                        update_by varchar(100),
                        update_time timestamp,
                        version integer,
                        del_flag varchar(10),
                        user_id bigint,
                        org_id bigint
                    )
                    """);
            statement.execute("""
                    insert into notice_info (
                        notice_id, notice_title, notice_content, notice_abstract, notice_type,
                        notice_status, notice_image, notice_author, publish_time, check_status,
                        create_by, create_time, update_by, update_time, version, del_flag, user_id, org_id
                    ) values
                        (1, '公开公告', '<p>正文</p>', '摘要', '1', '6', '/profile/a.png', '作者',
                         dateadd('hour', -1, current_timestamp), '4', 'internal-user',
                         current_timestamp, 'internal-user', current_timestamp, 9, '0', 101, 201),
                        (2, '草稿公告', '<p>正文</p>', '摘要', '1', '1', '/profile/a.png', '作者',
                         dateadd('hour', -1, current_timestamp), '1', 'internal-user',
                         current_timestamp, null, null, 1, '0', 102, 202),
                        (3, '删除公告', '<p>正文</p>', '摘要', '1', '6', '/profile/a.png', '作者',
                         dateadd('hour', -1, current_timestamp), '4', 'internal-user',
                         current_timestamp, null, null, 1, '2', 103, 203),
                        (4, '未来公告', '<p>正文</p>', '摘要', '1', '6', '/profile/a.png', '作者',
                         dateadd('hour', 1, current_timestamp), '4', 'internal-user',
                         current_timestamp, null, null, 1, '0', 104, 204),
                        (5, '无发布时间公告', '<p>正文</p>', '摘要', '1', '6', '/profile/a.png', '作者',
                         null, '4', 'internal-user', current_timestamp, null, null, 1, '0', 105, 205),
                        (6, '历史脏数据公告',
                         '<p><img src="file:///C:/Users/alice/Desktop/internal.png"></p>',
                         '摘要', '1', '6', '/profile/a.png', '作者',
                         dateadd('hour', -1, current_timestamp), '4', 'internal-user',
                         current_timestamp, null, null, 1, '0', 106, 206)
                    """);

            statement.execute("""
                    create table news_info (
                        news_id bigint primary key,
                        news_title varchar(200),
                        news_vice_title varchar(200),
                        news_cont clob,
                        news_abstract varchar(500),
                        news_image varchar(1000),
                        news_tag varchar(100),
                        news_author varchar(100),
                        news_source varchar(100),
                        news_type varchar(20),
                        publish_time timestamp,
                        news_status varchar(10),
                        classify_id bigint,
                        reading_quantity integer,
                        likes_num integer,
                        is_top varchar(10),
                        check_status varchar(10),
                        create_by varchar(100),
                        create_time timestamp,
                        update_by varchar(100),
                        update_time timestamp,
                        version integer,
                        del_flag varchar(10),
                        user_id bigint,
                        org_id bigint
                    )
                    """);
            statement.execute("""
                    insert into news_info (
                        news_id, news_title, news_vice_title, news_cont, news_abstract, news_image,
                        news_tag, news_author, news_source, news_type, publish_time, news_status,
                        classify_id, reading_quantity, likes_num, is_top, check_status, create_by,
                        create_time, update_by, update_time, version, del_flag, user_id, org_id
                    ) values
                        (11, '公开资讯', '副标题', '<p>正文</p>', '摘要', '/profile/n.png',
                         '标签', '作者', '来源', '1', dateadd('hour', -1, current_timestamp), '6',
                         301, 8, 2, '1', '4', 'internal-user', current_timestamp,
                         'internal-user', current_timestamp, 9, '0', 111, 211),
                        (12, '草稿资讯', null, '<p>正文</p>', '摘要', '/profile/n.png',
                         null, '作者', '来源', '1', dateadd('hour', -1, current_timestamp), '1',
                         302, 0, 0, '0', '1', 'internal-user', current_timestamp,
                         null, null, 1, '0', 112, 212),
                        (13, '删除资讯', null, '<p>正文</p>', '摘要', '/profile/n.png',
                         null, '作者', '来源', '1', dateadd('hour', -1, current_timestamp), '6',
                         303, 0, 0, '0', '4', 'internal-user', current_timestamp,
                         null, null, 1, '2', 113, 213),
                        (14, '未来资讯', null, '<p>正文</p>', '摘要', '/profile/n.png',
                         null, '作者', '来源', '1', dateadd('hour', 1, current_timestamp), '6',
                         304, 0, 0, '0', '4', 'internal-user', current_timestamp,
                         null, null, 1, '0', 114, 214),
                        (15, '无发布时间资讯', null, '<p>正文</p>', '摘要', '/profile/n.png',
                         null, '作者', '来源', '1', null, '6',
                         305, 0, 0, '0', '4', 'internal-user', current_timestamp,
                         null, null, 1, '0', 115, 215)
                    """);
        }
    }
}
