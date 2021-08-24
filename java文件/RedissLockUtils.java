package com.coolpad.ecark.order.fulfillment.domain.shared.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @Author dja
 * @Date 2021/3/30 12:01
 */
@Slf4j
public class RedissLockUtils {

    private static RedissonClient redissonClient;

    public void setRedissonClient(RedissonClient locker) {
        redissonClient = locker;
    }

    /**
     * 根据key获取锁
     * @param lockKey
     * @return
     */
    public RLock getLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock;
    }


    /**
     * 释放锁
     * @param lock
     */
    public void unlock(RLock lock) {
        lock.unlock();
    }

    /**
     * 释放锁
     * @param lockKey
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param waitTime 等待时间   单位：秒
     * lock.tryLock()第二个参数 leaseTime 续期参数,不设置默认等于-1，就是由redission的定时任务timetask去续期 arg=10  单位：秒
     * @return
     */
    public boolean tryLock(String lockKey, int waitTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param waitTime 等待时间   单位：秒
     * @param leaseTime 上锁后自动释放锁时间,不会自动续期   单位：秒
     * @return
     */
    public boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
