package com.asi.idempotent.config;


import com.asi.idempotent.infra.lock.DistributedLockFactory;
import com.asi.idempotent.infra.lock.RedissonDistributedLockFactory;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author asi
 * @date 2023/10/7 15:55
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.redisson.api.RedissonClient")
public class RedissonAutoConfiguration {

    private final RedisProperties redisProperties;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort()));
        if (redisProperties.getDatabase() > 0) {
            singleServerConfig.setDatabase(redisProperties.getDatabase());
        }
        if (null != redisProperties.getPassword()) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    public DistributedLockFactory distributedLockFactory(RedissonClient redissonClient) {
        return new RedissonDistributedLockFactory(redissonClient);
    }
}
