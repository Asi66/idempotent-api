package com.asi.idempotent.infra.lock;


import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

/**
 * 分布式锁工厂 - Redisson实现
 *
 * @author asi
 * @date 2023/10/7 16:03
 */
@RequiredArgsConstructor
public class RedissonDistributedLockFactory implements DistributedLockFactory {

    private static final String DISTRIBUTED_LOCK_PATH_PREFIX = "dl:";

    private final RedissonClient redissonClient;

    @Override
    public DistributedLock provideDistributedLock(String lockKey) {
        String lockPath = DISTRIBUTED_LOCK_PATH_PREFIX + lockKey;
        return new RedissonDistributedLock(redissonClient, lockPath);
    }
}
