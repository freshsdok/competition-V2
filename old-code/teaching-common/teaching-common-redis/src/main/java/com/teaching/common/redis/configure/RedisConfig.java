package com.teaching.common.redis.configure;

import com.teaching.common.core.utils.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

/**
 * redis配置
 *
 * @author teaching
 */
@Configuration
@EnableCaching
//@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.data.redis.host:}")
    private String host;

    @Value("${spring.data.redis.port:}")
    private String port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.redis.sentinel.master:}")
    private String sentinelMaster;

    @Value("${spring.redis.sentinel.nodes:}")
    private String sentinelNodes;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * 创建 Redis 连接工厂（支持哨兵模式）
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 检查是否配置了哨兵模式
        if (StringUtils.hasText(sentinelMaster) && StringUtils.hasText(sentinelNodes)) {
            // 哨兵模式配置
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.master(sentinelMaster);

            // 解析哨兵节点列表
            String[] nodes = sentinelNodes.split(",");
            for (String node : nodes) {
                // 移除可能的 redis:// 前缀
                String cleanNode = node.replace("redis://", "").trim();
                String[] parts = cleanNode.split(":");
                if (parts.length == 2) {
                    sentinelConfig.sentinel(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
            System.out.println("使用哨兵模式连接Redis database:" + database);
            sentinelConfig.setDatabase(database);

            // 设置密码
            if (StringUtils.hasText(password)) {
                sentinelConfig.setPassword(password);
            }

            LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfig);
            factory.afterPropertiesSet();
            return factory;
        } else {
            // 单节点模式配置
            LettuceConnectionFactory factory = new LettuceConnectionFactory();
            factory.setHostName(host);
            factory.setPort(Integer.parseInt(port));
            factory.setDatabase(database);

            if (StringUtils.hasText(password)) {
                factory.setPassword(password);
            }

            factory.afterPropertiesSet();
            return factory;
        }
    }



    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 设置redis缓存管理器
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new FastJson2JsonRedisSerializer<>(Object.class)));
        return RedisCacheManager.builder(factory).cacheDefaults(cacheConfiguration).build();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient()
    {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());

        // 检查是否配置了哨兵模式
        if (StringUtils.hasText(sentinelMaster) && StringUtils.hasText(sentinelNodes)) {
            System.out.println("使用哨兵模式连接Redis database:" + database);
            // 哨兵模式配置
            SentinelServersConfig sentinelConfig = config.useSentinelServers()
                    .setMasterName(sentinelMaster)
                    .addSentinelAddress(sentinelNodes.split(","))
                    .setDatabase(database);

            // 设置密码
            if (StringUtils.hasText(password)) {
                sentinelConfig.setPassword(password);
            } else {
                sentinelConfig.setPassword(null);
            }
        } else {
            // 单节点配置
            SingleServerConfig serverConfig = config.useSingleServer().setAddress("redis://" + host + ":" + port);

            // 设置密码
            if (StringUtils.hasText(password)) {
                serverConfig.setPassword(password);
            } else {
                serverConfig.setPassword(null);
            }
        }
        return Redisson.create(config);
    }
}
