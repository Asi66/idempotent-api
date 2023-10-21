package com.asi.idempotent.infra.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author asi
 * @date 2023/10/7 16:03
 */
public interface DistributedLock {

    /**
     * 加锁，不等待，锁持有时间为leaseTime
     *
     * @param leaseTime leaseTime
     * @param unit      unit
     */
    void lock(long leaseTime, TimeUnit unit);

    /**
     * 尝试加锁，等待时间为waitTime，锁持有时间为leaseTime
     *
     * @param waitTime  waitTime
     * @param leaseTime leaseTime
     * @param unit      unit
     * @return get lock or not
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 解锁
     */
    void unlock();

    /**
     * 是否持有锁
     *
     * @return hold lock or not
     */
    boolean isLock();

    /**
     * 锁是否被当前线程持有
     *
     * @return hold lock or not
     */
    boolean isHeldByCurrentThread();
}

