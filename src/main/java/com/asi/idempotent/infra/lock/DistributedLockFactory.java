package com.asi.idempotent.infra.lock;

/**
 * 分布式锁工厂
 *
 * @author asi
 * @date 2023/10/7 16:03
 */
public interface DistributedLockFactory {

    /**
     * 获取分布式锁
     *
     * @param lockKey lockKey
     * @return DistributedLock
     */
    DistributedLock provideDistributedLock(String lockKey);
}
