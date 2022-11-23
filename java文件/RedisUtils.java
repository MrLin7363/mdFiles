package com.lin.netty.nettybasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class RedisUtils {
    private static final int REDIS_TIMEOUT = 30;

    private static final int TEMP_REDIS_TIMEOUT = 10;

    private static final String PREFIX = "com.lin.redis";

    private static final String DEFAULT_KEY = "com.lin.redis: ";

    private static final String DEFAULT_HASH_KEY = "com.lin.redis.hash: ";

    private static final String DEFAULT_LOCK_KEY = "com.lin.redis:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String getKey(String key) {
        return DEFAULT_KEY + key;
    }

    private String getHashKey(String key) {
        return DEFAULT_HASH_KEY + key;
    }


    public Set<String> getKeySet() {
        return redisTemplate.keys(DEFAULT_KEY + "*");
    }


    public Set<String> getKeySet(String key) {
        return redisTemplate.keys(DEFAULT_KEY + key + "*");
    }

    /**
     * 返回 key 的剩余的过期时间（分钟）
     *
     * @param key 键
     * @return 分钟
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(getKey(key));
    }

    /**
     * 添加缓存，默认缓存过期时间为30分钟
     *
     * @param key 键
     * @param value 值
     */
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(getKey(key), value, REDIS_TIMEOUT, TimeUnit.DAYS);
    }

    /**
     * 添加缓存，默认缓存过期时间为30分钟
     *
     * @param key 键
     * @param value 值
     */
    public void setTempValue(String key, String value) {
        redisTemplate.opsForValue().set(getKey(key), value, TEMP_REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public String getValue(String key) {
        if (existKey(key)) {
            return redisTemplate.opsForValue().get(getKey(key));
        }
        return "";
    }

    /**
     * 同时设置一个或多个 key-value 对，默认缓存过期时间为30分钟
     *
     * @param maps 集合
     */
    public void multiSet(Map<String, String> maps) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            result.put(getKey(entry.getKey()), entry.getValue());
        }
        redisTemplate.opsForValue().multiSet(result);
    }

    /**
     * 批量获取
     *
     * @param keys 键集合
     * @return 值集合
     */
    public Map<String, String> multiGet(Collection<String> keys) {
        Map<String, String> result = new HashMap<>();
        keys.forEach(key -> {
            result.put(key, getValue(key));
        });
        return result;
    }

    /**
     * 设置哈希数据
     *
     * @param key 键
     * @param field 字段
     * @param value 值
     */
    public void setHaskData(String key, String field, String value) {
        redisTemplate.boundHashOps(getHashKey(key)).put(field, value);
        redisTemplate.boundHashOps(getHashKey(key)).expire(REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * 设置哈希数据
     *
     * @param key 键
     * @param maps 哈希数据
     */
    public void setHaskDatas(String key, Map<String, String> maps) {
        redisTemplate.boundHashOps(getHashKey(key)).putAll(maps);
        redisTemplate.boundHashOps(getHashKey(key)).expire(REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * 设置哈希数据
     *
     * @param key 键
     */
    public void setHaskDatasExpire(String key) {
        redisTemplate.boundHashOps(getHashKey(key)).expire(REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key 键
     * @param field 字段
     * @return 值
     */
    public String getHaskDataForField(String key, String field) {
        if (existsHaskData(key, field)) {
            BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(getHashKey(key));
            return hashOperations.get(field);
        }
        return "";
    }

    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key 键
     * @param field 字段
     * @return 值
     */
    public List<String> getHaskDataForFields(String key, List<String> field) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(getHashKey(key));
        return hashOperations.multiGet(field);
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     *
     * @param key 键
     * @param field 字段
     * @return 是否存在
     */
    public boolean existsHaskData(String key, String field) {
        if (existHashKey(key)) {
            BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(getHashKey(key));
            return hashOperations.hasKey(field);
        }
        return false;
    }

    /**
     * 获取所有哈希数据
     *
     * @param key 键
     * @return 哈希数据
     */
    public Map<String, String> getHaskDatas(String key) {
        if (existHashKey(key)) {
            BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(getHashKey(key));
            return hashOperations.entries();
        }
        return new HashMap<>();
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key 键
     * @param field 字段
     * @return 哈希数据
     */
    public Map<String, String> getHaskDatasByField(String key, String field) {
        Map<String, String> result = new HashMap<>();
        if (existHashKey(key)) {
            BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(getHashKey(key));
            Map<String, String> datas = hashOperations.entries();
            for (Map.Entry<String, String> entry : datas.entrySet()) {
                if (entry.getKey().contains(field)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        return result;
    }

    /**
     * 返回 key 的剩余的过期时间（分钟）
     *
     * @param key 键
     * @return 分钟
     */
    public Long getHashDatasExpire(String key) {
        return redisTemplate.boundHashOps(getHashKey(key)).getExpire() / 60;
    }

    /**
     * 是否存在key
     *
     * @param key 键
     * @return 未知
     */
    public boolean existKey(String key) {
        return redisTemplate.hasKey(getKey(key));
    }

    /**
     * 是否存在Hashkey
     *
     * @param key 键
     * @return 未知
     */
    public boolean existHashKey(String key) {
        return redisTemplate.hasKey(getHashKey(key));
    }

    /**
     * 删除key
     *
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(getKey(key));
    }

    /**
     * 删除key
     *
     * @param key 键
     */
    public void deleteHash(String key) {
        redisTemplate.delete(getHashKey(key));
    }

    /**
     * 批量删除key
     *
     * @param keys 键集合
     */
    public void deleteAll(Collection<String> keys) {
        List<String> deleteKeys = keys.stream().map(this::getKey).collect(Collectors.toList());
        redisTemplate.delete(deleteKeys);
    }

    /**
     * 获取一半的超时时间
     *
     * @return 时间
     */
    public int getHalfTimeOut() {
        return REDIS_TIMEOUT >> 1;
    }

    /**
     * 设置临时哈希数据
     *
     * @param key 键
     * @param maps 哈希数据
     */
    public void setTempHaskDatas(String key, Map<String, String> maps) {
        redisTemplate.boundHashOps(getHashKey(key)).putAll(maps);
        redisTemplate.boundHashOps(getHashKey(key)).expire(TEMP_REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * 获取一半的临时超时时间
     *
     * @return 时间
     */
    public int getTempHalfTimeOut() {
        return TEMP_REDIS_TIMEOUT >> 1;
    }

    /**
     * lock
     *
     * @param key key
     * @param expire expire  ms
     * @return boolean
     */
    public Boolean lock(String key, long expire) {
        String lockKey = DEFAULT_LOCK_KEY + key;
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            long expaireAt = System.currentTimeMillis() + expire + 1;
            Boolean acquire = connection.setNX(lockKey.getBytes(StandardCharsets.UTF_8),
                String.valueOf(expaireAt).getBytes(StandardCharsets.UTF_8));
            if (acquire) {
                return Boolean.TRUE;
            } else {
                byte[] value = connection.get(lockKey.getBytes(StandardCharsets.UTF_8));
                if (Objects.nonNull(value) && value.length > 0) {
                    long expaireTime = Long.parseLong(new String(value));
                    if (expaireTime < System.currentTimeMillis()) {
                        byte[] oldValue = connection.getSet(lockKey.getBytes(StandardCharsets.UTF_8),
                            String.valueOf(System.currentTimeMillis() + expire + 1).getBytes(StandardCharsets.UTF_8));
                        return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                    }
                }
            }
            return Boolean.FALSE;
        });
    }

    /**
     * 设置30分钟缓存
     *
     * @param key key
     * @param value value
     */
    public void set30MinValue(String key, String value) {
        redisTemplate.opsForValue().set(getKey(key), value, REDIS_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * expire
     *
     * @param key key
     * @param timeout timeout
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(PREFIX + key, timeout, TimeUnit.MINUTES);
    }

    /**
     * increment
     *
     * @param key key
     * @param count count
     */
    public void increment(String key, int count) {
        redisTemplate.opsForValue().increment(PREFIX + key, count);
    }

    /**
     * existsPrefixKey
     *
     * @param key key
     * @return boolean
     */
    public boolean existsPrefixKey(String key) {
        return redisTemplate.hasKey(PREFIX + key);
    }

    /**
     * addHyperLog
     *
     * @param key key
     * @param value value
     */
    public void addHyperLog(String key, String value) {
        redisTemplate.opsForHyperLogLog().add(PREFIX + key, value);
    }

    /**
     * unionHyperLog
     *
     * @param key key
     * @param keys keys
     */
    public void unionHyperLog(String key, String... keys) {
        redisTemplate.opsForHyperLogLog().union(PREFIX + key, keys);
    }

    /**
     * countHyperLog
     *
     * @param key key
     * @return long
     */
    public long countHyperLog(String key) {
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    /**
     * getPatternKeys
     *
     * @param pattern pattern
     * @param count count
     * @return Set<String>
     */
    public Set<String> getPatternKeys(String pattern, int count) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            // match different from java
            ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(count).build();
            final Cursor<byte[]> cursor = connection.scan(scanOptions);
            Set<String> result = new HashSet<>();
            while (cursor.hasNext()) {
                final byte[] next = cursor.next();
                result.add(new String(next, StandardCharsets.UTF_8));
            }
            return result;
        });
    }

    /**
     * expireFullName
     *
     * @param key key
     * @param timeout timeout
     */
    public void expireFullName(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
    }

    /**
     * getFullValue
     *
     * @param key key
     * @return String
     */
    public String getFullValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * getPrefixValue
     *
     * @param key key
     * @return String
     */
    public String getPrefixValue(String key) {
        return redisTemplate.opsForValue().get(PREFIX + key);
    }

    public void addSet(String key, String... values) {
        redisTemplate.opsForSet().add(PREFIX + key, values);
    }

    public Set<String> getSet(String key) {
        return redisTemplate.opsForSet().members(PREFIX + key);
    }

    public void addZSet(String key, Set set) {
        redisTemplate.opsForZSet().add(PREFIX + key, set);
    }

    public Set rangeZSet(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(PREFIX + key, min, max);
    }

    public void setFullValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
}
