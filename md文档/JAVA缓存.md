# ConcurretnHashMap实现单线程缓存类



在规定时间内，使用 hashMap 实现一个缓存工具类，需要考虑一下几点

1. 不可变对象
2. 单例
3. 线程安全
4. 回收失效数据
5. 垃圾回收
6. 缓存大小
7. LRU

注备：

- LRU： Least Recently Used ，即最近最少使用，是一种常用的页面置换算法，选择最近最久未使用的页面淘汰。
- OPT : 最佳置换算法，是一种理想情况下的置换算法，但实际上不可实现。思想是标记每个页面多久后被使用，最大的将被淘汰
- FIFO：先进先出，建立一个FIFO 队列，收容所有在内存中的页，被置换的页总在队列头上进行。
- LFU ： 最少使用置换算法，使用最少使用置换算法在内存中的每个页面设置一个移位寄存器，记录页面被使用的频率。

```
package com.coolpad.pay.infrastructure.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: chenjunlin
 * @since: 2021/04/21
 * @descripe: 常量信息缓存
 */
public class ConstantsCatheUtil {
    /**
     * 预缓存信息 -- 类加载器启动JVM时就加载这个，所以是线程公用的
     */
    private static final Map<String, Object> CACHE_MAP = new ConcurrentHashMap<String, Object>();

    /**
     * 每个缓存生效时间1小时
     */
    public static final long CACHE_HOLD_TIME_1H = 60*1000L;// 24 * 60 * 60 * 1000L 24小时


    /**
     * 存放一个缓存对象，默认保存时间1小时
     * @param cacheName
     * @param obj
     */
    public static void put(String cacheName, Object obj) {
        put(cacheName, obj, CACHE_HOLD_TIME_1H);
    }

    /**
     * 存放一个缓存对象，保存时间为holdTime
     * @param cacheName
     * @param obj
     * @param holdTime
     */
    public static void put(String cacheName, Object obj, long holdTime) {
        CACHE_MAP.put(cacheName, obj);
        CACHE_MAP.put(cacheName + "_HoldTime", System.currentTimeMillis() + holdTime);//缓存失效时间
    }

    /**
     * 取出一个缓存对象
     * @param cacheName
     * @return
     */
    public static Object get(String cacheName) {
        if (checkCacheName(cacheName)) {
            return CACHE_MAP.get(cacheName);
        }
        return null;
    }

    /**
     * 删除所有缓存
     */
    public static void removeAll() {
        CACHE_MAP.clear();
    }

    /**
     * 删除某个缓存
     * @param cacheName
     */
    public static void remove(String cacheName) {
        CACHE_MAP.remove(cacheName);
        CACHE_MAP.remove(cacheName + "_HoldTime");
    }

    /**LocalCacheUtil
     * 检查缓存对象是否存在，
     * 若不存在，则返回false
     * 若存在，检查其是否已过有效期，如果已经过了则删除该缓存并返回false
     * @param cacheName
     * @return
     */
    public static boolean checkCacheName(String cacheName) {
        Long cacheHoldTime = (Long) CACHE_MAP.get(cacheName + "_HoldTime");
        if (cacheHoldTime == null || cacheHoldTime == 0L) {
            return false;
        }
        System.out.println("当前时间"+cacheHoldTime+"是否小于过期时间"+System.currentTimeMillis()+"==="+(cacheHoldTime < System.currentTimeMillis()));
        if (cacheHoldTime < System.currentTimeMillis()) {
            remove(cacheName);
            return false;
        }
        return true;
    }

}

// 示例实现代码
public PaymentConfig getPaymentConfig(String bizCode,String channel){
        String catheName=bizCode+channel;
        if (!ConstantsCatheUtil.checkCacheName(catheName)){
            PaymentConfig paymentConfig=paymentConfigRepository.getPaymentConfig(bizCode,channel);
            ConstantsCatheUtil.put(catheName,paymentConfig);
            return paymentConfig;
        }
        return (PaymentConfig)ConstantsCatheUtil.get(catheName);
    }
    // 测试
    @Test
    public void testCathe(){
        while (true){
            Thread thread = new Thread(
                    (Runnable) () -> {
                        wxLitePayGateway.getPaymentConfig("zg", "wx_lite");
                        System.out.println("线程名" + Thread.currentThread().getName());
                    }
            );
            thread.start();
            try {
                thread.sleep(5*1000L);
            }catch (Exception e){

            }
        }
    }
```

# Guava缓存

```
   <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
        </dependency>
```