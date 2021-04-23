/*
package com.coolpad.pay.infrastructure.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

*/
/**
 * @author: chenjunlin
 * @since: 2021/04/21
 * @descripe:
 *//*

@Slf4j
public class LocalCacheUtil {
    */
/**
     * 缓存容器
     *//*

    public static Map<String,Cache<String, Object>> cacheContainer = new ConcurrentHashMap<String,Cache<String, Object>>();

    public static Cache<String,Object> cache = CacheBuilder.newBuilder().build();

    public static final String PAYMENT_CONFIG="paymentConfig";
    */
/**
     * 获取默认的配置的localcache，不用每次新建Cathe
     * @param cacheKey
     * @return
     *//*

    public static Cache<String,Object> getLocalCache(String cacheKey) {
        Cache<String, Object> localCache = cacheContainer.get(cacheKey);
        if (null != localCache) {
            log.info("取已有Cathe缓存");
            return localCache;
        }
        synchronized (LocalCacheUtil.cacheContainer) {
            log.info("新建Cathe缓存");
            if (null == localCache) {
                localCache = CacheBuilder.newBuilder()
                        .expireAfterWrite(3L, TimeUnit.SECONDS)
                        .initialCapacity(50)
                        .maximumSize(500)
                        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                        .recordStats()
                        .build();
                cacheContainer.put(cacheKey,localCache);
            }
            return localCache;
        }
    }

    */
/**
     * 自定义配置
     * @param cacheKey
     * @param duration 时间系数
     * @param unit 时间单位
     * @param initialCapacity 初始化容量
     * @param maximumSize 最大容量
     * @param concurrencyLevel 并发级别
     * @return
     *//*

    public static Cache<String,Object> getLocalCache(String cacheKey, long duration, TimeUnit unit, int initialCapacity, int maximumSize, int concurrencyLevel) {
        Cache<String, Object> localCache = cacheContainer.get(cacheKey);
        if (null != localCache) {
            return localCache;
        }
        synchronized (LocalCacheUtil.cacheContainer) {
            if (null == localCache) {
                //recordStats开启缓存状况统计,expireAfterAccess过期时间,initialCapacity初始化大小,maximumSize最大值
                localCache = CacheBuilder.newBuilder()
                        .expireAfterWrite(duration, unit)
                        .initialCapacity(initialCapacity)
                        .maximumSize(maximumSize)
                        .concurrencyLevel(concurrencyLevel)
                        .recordStats()
                        .build();
                cacheContainer.put(cacheKey,localCache);
            }
            return localCache;
        }
    }


    public static void main(String[] args) {
        String key = "instance_id";

        Cache<String,Double> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

        int i = 1;
        while (i < 5){
            i++;
            try {
                Double value = cache.get(key, new Callable<Double>() {
                    @Override
                    public Double call() throws Exception {
                        System.out.println("hello");  //第一次没有的时候会运算然后添加到缓存里面。后面就直接从缓存里面读取数据.
                        return 120d;
                    }
                });
                System.out.println(value);
            } catch (ExecutionException ex) {
                System.out.println(ex.getCause());
            }
        }
    }

}
*/
