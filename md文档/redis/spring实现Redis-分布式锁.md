# 一.RestTemplate实现方案

## 引入依赖

```
<!--springboot redis 启动包，包含下面单独的redis单独的依赖包-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
```



```
<!--redis单独的依赖-->
<dependency>
      <groupId>org.springframework.data</groupId>
      <artifactId>spring-data-redis</artifactId>
</denpendency>
```

## yaml文件添加redis配置

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/d_marketing?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  redis:
    host: 127.0.0.1
    port: 6379
    password:
    lettuce:
      pool:
        min-idle: 5
        max-idle: 10
        max-active: 8
        max-wait: 1000ms
```

## properties方式

```
#Redis数据库索引（默认为0）  
spring.redis.database=0  
# Redis服务器地址  
spring.redis.host=192.168.0.24  
# Redis服务器连接端口  
spring.redis.port=6379  
# Redis服务器连接密码（默认为空）  
spring.redis.password=  
# 连接池最大连接数（使用负值表示没有限制）  
spring.redis.pool.max-active=200  
# 连接池最大阻塞等待时间（使用负值表示没有限制）  
spring.redis.pool.max-wait=-1  
# 连接池中的最大空闲连接  
spring.redis.pool.max-idle=10 
# 连接池中的最小空闲连接  
spring.redis.pool.min-idle=0  
# 连接超时时间（毫秒）  
spring.redis.timeout=1000 
```

## spring容器源码生成了redisTemplete 和StringRedisTemplate

```
@Configuration
@ConditionalOnClass({RedisOperations.class})
@EnableConfigurationProperties({RedisProperties.class})
@Import({LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class})
public class RedisAutoConfiguration {
    public RedisAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(
        name = {"redisTemplate"}
    )
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
```

## 添加配置重新配置redisTemplate

```
@Configuration
public class RedisConfig {
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
```

## redisUtil

```
package com.coolpad.ecark.marketing.repositoryimpl.database.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: chenjunlin
 * @since: 2021/04/13
 * @descripe:
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // =============================common============================
    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 根据key 获取过期时间
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }
    // ============================String=============================
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }
    /**
     * 递减
     * @param key 键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }
    // ================================Map=================================
    /**
     * HashGet
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }
    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key 键 不能为null
     * @param item 项 可以使多个 不能为null
     */

    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }
    /**
     * hash递减
     * @param key 键
     * @param item 项
     * @param by 要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }
    // ============================set=============================
    /**
     * 根据key获取Set中的所有值
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     * @param key 键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将数据放入set缓存
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     * @param key 键
     * @param time 时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    // ===============================list=================================
    /**
     * 获取list缓存的内容
     * @param key 键
     * @param start 开始
     * @param end 结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 通过索引 获取list中的值
     * @param key 键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {

            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 移除N个值为value
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
```

## test测试

```
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {

    @Autowired
    MarketMapper marketMapper;
    @Test
    public void updateId(){
        System.out.println(marketMapper.selectByPrimaryKey(1).getName());
    }
}
```

```
public class RedisTest extends BaseTest{

    @Autowired
    RedisUtil redisUtil;

    @Test
    public void StringTest(){
        redisUtil.set("jun","love u");
        if (redisUtil.hasKey("jun")){
            System.out.println("hash this key");
        }
        System.out.println(redisUtil.get("jun"));
    }

}
```

## redis的启动注意事项

redis的启动指令如果直接点击 redis-server.exe 默认无密码登录  上述配置文件的密码要去掉

所以用配置文件的redis启动   目录下  redis-server.exe redis.windows.conf

客户端的启动    redis-cli -h {ip} -p {端口}         

客户端连接方法：

redis-cli -h localhost -p 6380

提供host为localhost，端口为6380

 

带密码的客户端连接方法一：

redis-cli -h localhost -p 6380 monitor -a 123456

监控host为localhost，端口为6380

-a 为连接密码

 

密码验证方法二：启动后用这个就行

在命令行输入：auth 123456  #123456是密码

 

windows下的客户端软件：

Redis Desktop Manager



# 二.分布式锁实现方案

- 数据库级别   乐观锁 基于version实现  读取频繁使用乐观锁，写入频繁使用悲观锁
- 数据库级别  悲观锁  基于数据库级别的 for update

- 基于Redis原子操作     基于setnx,expire实现
- 基于zookeeper实战锁  基于interProcessMutex实现
- 基于Redisson实战分布式锁

## 1.数据库乐观锁

> 乐观锁通常实现基于数据版本(version)的记录机制实现的，比如有一张红包表（t_bonus），有一个字段(left_count)记录礼物的剩余个数，用户每领取一个奖品，对应的left_count减1，在并发的情况下如何要保证left_count不为负数，乐观锁的实现方式为在红包表上添加一个版本号字段（version），默认为0。

异常实现流程

```
-- 可能会发生的异常情况
-- 线程1查询，当前left_count为1，则有记录
select * from t_bonus where id = 10001 and left_count > 0

-- 线程2查询，当前left_count为1，也有记录
select * from t_bonus where id = 10001 and left_count > 0

-- 线程1完成领取记录，修改left_count为0,
update t_bonus set left_count = left_count - 1 where id = 10001

-- 线程2完成领取记录，修改left_count为-1，产生脏数据
update t_bonus set left_count = left_count - 1 where id = 10001
```

过乐观锁实现

```
-- 添加版本号控制字段
ALTER TABLE table ADD COLUMN version INT DEFAULT '0' NOT NULL AFTER t_bonus;

-- 线程1查询，当前left_count为1，则有记录，当前版本号为1234
select left_count, version from t_bonus where id = 10001 and left_count > 0

-- 线程2查询，当前left_count为1，有记录，当前版本号为1234
select left_count, version from t_bonus where id = 10001 and left_count > 0

-- 线程1,更新完成后当前的version为1235，update状态为1，更新成功
update t_bonus set version = 1235, left_count = left_count-1 where id = 10001 and version = 1234

-- 线程2,更新由于当前的version为1235，udpate状态为0，更新失败，再针对相关业务做异常处理
update t_bonus set version = 1235, left_count = left_count-1 where id = 10001 and version = 1234
```

## 2.redis 原生实现分布式锁

set if not exist 

setnx num 10  >1

setnx num 12 >0

第二次setnx 12 因为锁已被占用未被释放，所以返回0操作操作失败

expire num 5    > 1

setnx num 13    >0

setnx num 13    >0

setnx num 13    >0

setnx num 13    >0

setnx num 13    >1

expire num 5    五秒后过时 ，五秒后获取成功

### getset指令

```
redis> GETSET db mongodb    # 没有旧值，返回 nil
(nil)

redis> GET db
"mongodb"

redis> GETSET db redis      # 返回旧值 mongodb
"mongodb"

redis> GET db
"redis"
```

### 代码

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
```

```
@Component
public class RedisTemplateUtil {

    //锁名称
    public static final String LOCK_PREFIX = "redis_lock:";
    //加锁失效时间，毫秒
    public static final int LOCK_EXPIRE = 500; // ms

    @Autowired
    RedisTemplate redisTemplate;


    /**
     *  最终加强分布式锁
     *
     * @param key key值
     * @return 是否获取到
     */
    public boolean lock(String key){
        String lock = LOCK_PREFIX + key;
        System.out.println(Thread.currentThread().getName()+"锁名="+lock);
        // 利用lambda表达式
        RedisCallback redisCallback=new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 过期时间=当前时间+锁失效时间+1
                long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
                // key自定义 value=过期时间
                Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());
                if (acquire) {
                    System.out.println(Thread.currentThread().getName()+"获取到锁");
                    return true;
                } else {
                    // 获取过期时间
                    byte[] value = connection.get(lock.getBytes());

                    if (Objects.nonNull(value) && value.length > 0) {

                        long expireTime = Long.parseLong(new String(value));
                        // 如果锁已经过期
                        if (expireTime < System.currentTimeMillis()) {
                            // 重新加锁，防止死锁
                            byte[] oldValue = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                              System.out.println(Thread.currentThread().getName()+"锁过期，重新加锁");
                            // 加锁后锁运行到下面这行还未过期就是成功加锁，下面是校验原先的过期时间是否已经到期
                            return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                        }
                    }
                }
                return false;
            }
        };
        return  (Boolean)redisTemplate.execute(redisCallback);
    }

    /**
     * 删除锁 释放锁，finally里面用
     *
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(LOCK_PREFIX+key);
        System.out.println(Thread.currentThread().getName()+"删除锁key="+LOCK_PREFIX+key);
    }
    
}
```

```
@Test
public void redisLockTest(){
    System.out.println("begin task");
    for (int i = 0; i < 20; i++) {
        Thread thread=new Thread(
                (Runnable) () ->{
            bussiness();
        });
        thread.start();
    }
   while (true){

   }
}

public void bussiness() {
    String key="num";
    boolean lock = redisTemplateUtil.lock(key);
    if (lock){
        // 执行业务逻辑操作
        System.out.println("获取锁成功："+Thread.currentThread().getName()+"执行业务操作");
        redisTemplateUtil.delete(key);
    }else{
        System.out.println(Thread.currentThread().getName()+"获取锁失败，自旋");
        // 设置失败次数计数器, 当到达5次时,自旋5次, 返回失败
        int failCount = 1;
        while(failCount <= 5){
            // 等待500ms重试taskScheduler
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (redisTemplateUtil.lock(key)){
                // 执行业务逻辑操作
                System.out.println(Thread.currentThread().getName()+"自旋后获取锁,执行业务操作");
                redisTemplateUtil.delete(key);
                // 退出自旋，否则一直尝试获取锁，自旋
                break;
            }else{
                failCount ++;
            }
            if (failCount>5){
                throw new RuntimeException(Thread.currentThread().getName()+"获取锁失败，现在创建的人太多了, 请稍等再试");
            }
        }
    }
    System.out.println(Thread.currentThread().getName()+"执行完业务");
}
```

## 3.redisson实现分布式锁-基础篇

官方文档：  https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95

### Redis几种架构

Redis发展到现在，几种常见的部署架构有：

1. 单机模式；
2. 主从模式；
3. 哨兵模式；
4. 集群模式；

我们首先基于这些架构讲解Redisson普通分布式锁实现，需要注意的是，只有充分了解普通分布式锁是如何实现的，才能更好的了解Redlock分布式锁的实现，因为**Redlock分布式锁的实现完全基于普通分布式锁**。

### 引入依赖

```
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version> 3.10.4 </version>
</dependency>
```

```
<!-- redisson 整合 springboot 启动版本 自带上面那个  3.10.4  -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>${redisson.version}</version>   
</dependency>
```

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/d_marketing?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  redis:
    host: 127.0.0.1
    port: 6379
    password: 123456
    lettuce:
      pool:
        min-idle: 5
        max-idle: 10
        max-active: 8
        max-wait: 1000ms
```

启动需配置密码等文件，否则出错

redis的启动指令如果直接点击 redis-server.exe 默认无密码登录  上述配置文件的密码要去掉

所以用配置文件的redis启动   目录下  redis-server.exe redis.windows.conf

### 代码

```
@Configuration // springboot启动时自动加载该配置文件
public class CommonConfig {

    /**
     * 自定义线程池   processors-服务器核数
     * @return
     */
    @Bean
    public ThreadPoolExecutor initThreadPoolExecutor(){

        int processors = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(processors, processors * 2, 1L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100));
        return pool;
    }

    /**
     * 将实例注入到RedissLockUtils中  因为RedissonClient是interface接口所以不能注入，通过加载这个类的时候把接口连接这个类
     * @param redissonClient
     * @return
     * @Bean 当作springBean去加载
     */
    @Bean
    RedissLockUtils redissLockUtil(RedissonClient redissonClient) {
        RedissLockUtils redissLockUtil = new RedissLockUtils();
        redissLockUtil.setRedissonClient(redissonClient);
        return redissLockUtil;
    }
}
```

```
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
    public static RLock getLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock;
    }


    /**
     * 尝试获取锁 -推荐
     * @param waitTime 等待时间   单位：秒   一般设置为-1默认获取不到立即失败
     */
    public static boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock();
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    
    /**
     * 尝试获取锁 
     * @param waitTime 等待时间   单位：秒   一般设置为-1默认获取不到立即失败
     * lock.tryLock()第二个参数 leaseTime 续期参数,不设置默认等于-1，就是由redission的定时任务timetask去续期  10s定时任务延期30s
     */
    public static boolean tryLock(String lockKey, int waitTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }


    /**
     * 尝试获取锁
     * @param waitTime 等待时间   单位：秒
     * @param leaseTime 如果设置 >=0 上锁后自动释放锁时间,不会自动续期   单位：秒
     */
    public static boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
```

```
RLock lock = RedissLockUtils.getLock(lockKey);
if (RedissLockUtils.tryLock(lockKey, waitTime, leaseTime)) {
    try {
       // 执行业务逻辑
    }catch (Exception e){
    
    }finally {
        lock.unlock();
    }
}
```

### watchdog机制

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201119155758770.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1ODQzMDk1,size_16,color_FFFFFF,t_70#pic_center)

 watchdog机制。找了很多资料，最后基本弄明白了 watchdog的使用和 原理。

首先watchdog的具体思路是 加锁时，默认加锁 30秒，每10秒钟检查一次，如果存在就重新设置 过期时间为30秒。

然后设置默认加锁时间的参数是 lockWatchdogTimeout（监控锁的看门狗超时，单位：毫秒）


## 4.redisson实现分布式锁-高级篇-RedLock

RedissonRedLock 是针对多个redis服务器之间用的全局锁，如果redis为单体，那么用 RLock应该也够了

```java
Config config1 = new Config();
config1.useSingleServer().setAddress("redis://192.168.0.1:5378")
        .setPassword("a123456").setDatabase(0);
RedissonClient redissonClient1 = Redisson.create(config1);

Config config2 = new Config();
config2.useSingleServer().setAddress("redis://192.168.0.1:5379")
        .setPassword("a123456").setDatabase(0);
RedissonClient redissonClient2 = Redisson.create(config2);

Config config3 = new Config();
config3.useSingleServer().setAddress("redis://192.168.0.1:5380")
        .setPassword("a123456").setDatabase(0);
RedissonClient redissonClient3 = Redisson.create(config3);

String resourceName = "REDLOCK_KEY";

RLock lock1 = redissonClient1.getLock(resourceName);
RLock lock2 = redissonClient2.getLock(resourceName);
RLock lock3 = redissonClient3.getLock(resourceName);
// 向3个redis实例尝试加锁
RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
boolean isLock;
try {
    // isLock = redLock.tryLock();
    // 500ms拿不到锁, 就认为获取锁失败。10000ms即10s是锁失效时间。
    // long waitTime, long leaseTime, TimeUnit unit
    isLock = redLock.tryLock(500, 10000, TimeUnit.MILLISECONDS);
    System.out.println("isLock = "+isLock);
    if (isLock) {
        //TODO if get lock success, do something;
    }
} catch (Exception e) {
} finally {
    // 无论如何, 最后都要解锁
    redLock.unlock();
}
```

```
	可以不设置获取时间和过期时间， 这样直接获取全部结点直到成功或失败，并且自动延期  
  isLock = redLock.tryLock();
```



唯一**ID**

实现分布式锁的一个非常重要的点就是set的value要具有唯一性，redisson的value是怎样保证value的唯一性呢？答案是**UUID+threadId**。入口在redissonClient.getLock("REDLOCK_KEY")，源码在Redisson.java和RedissonLock.java中：



```java
protected final UUID id = UUID.randomUUID();
String getLockName(long threadId) {
    return id + ":" + threadId;
}
```

**获取锁**

获取锁的代码为redLock.tryLock()或者redLock.tryLock(500, 10000, TimeUnit.MILLISECONDS)，两者的最终核心源码都是下面这段代码，只不过前者获取锁的默认租约时间（leaseTime）是LOCK_EXPIRATION_INTERVAL_SECONDS，即30s：

```java
<T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
    internalLockLeaseTime = unit.toMillis(leaseTime);
    // 获取锁时需要在redis实例上执行的lua命令
    return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
              // 首先分布式锁的KEY不能存在，如果确实不存在，那么执行hset命令（hset REDLOCK_KEY uuid+threadId 1），并通过pexpire设置失效时间（也是锁的租约时间）
              "if (redis.call('exists', KEYS[1]) == 0) then " +
                  "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                  "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                  "return nil; " +
              "end; " +
              // 如果分布式锁的KEY已经存在，并且value也匹配，表示是当前线程持有的锁，那么重入次数加1，并且设置失效时间
              "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                  "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                  "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                  "return nil; " +
              "end; " +
              // 获取分布式锁的KEY的失效时间毫秒数
              "return redis.call('pttl', KEYS[1]);",
              // 这三个参数分别对应KEYS[1]，ARGV[1]和ARGV[2]
                Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
}
```

获取锁的命令中，

- **KEYS[1]**就是Collections.singletonList(getName())，表示分布式锁的key，即REDLOCK_KEY；
- **ARGV[1]**就是internalLockLeaseTime，即锁的租约时间，默认30s；
- **ARGV[2]**就是getLockName(threadId)，是获取锁时set的唯一值，即UUID+threadId：

------

**释放锁**

释放锁的代码为redLock.unlock()，核心源码如下：

```java
protected RFuture<Boolean> unlockInnerAsync(long threadId) {
    // 释放锁时需要在redis实例上执行的lua命令
    return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
            // 如果分布式锁KEY不存在，那么向channel发布一条消息
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                "redis.call('publish', KEYS[2], ARGV[1]); " +
                "return 1; " +
            "end;" +
            // 如果分布式锁存在，但是value不匹配，表示锁已经被占用，那么直接返回
            "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                "return nil;" +
            "end; " +
            // 如果就是当前线程占有分布式锁，那么将重入次数减1
            "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
            // 重入次数减1后的值如果大于0，表示分布式锁有重入过，那么只设置失效时间，还不能删除
            "if (counter > 0) then " +
                "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                "return 0; " +
            "else " +
                // 重入次数减1后的值如果为0，表示分布式锁只获取过1次，那么删除这个KEY，并发布解锁消息
                "redis.call('del', KEYS[1]); " +
                "redis.call('publish', KEYS[2], ARGV[1]); " +
                "return 1; "+
            "end; " +
            "return nil;",
            // 这5个参数分别对应KEYS[1]，KEYS[2]，ARGV[1]，ARGV[2]和ARGV[3]
            Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.unlockMessage, internalLockLeaseTime, getLockName(threadId));

}
```

# 三.分布式锁七种方案！探讨Redis分布式锁的正确使用姿势

- 什么是分布式锁
- 方案一：SETNX + EXPIRE
- 方案二：SETNX + value值是（系统时间+过期时间）
- 方案三：使用Lua脚本(包含SETNX + EXPIRE两条指令)
- 方案四：SET的扩展命令（SET EX PX NX）
- 方案五：SET EX PX NX  + 校验唯一随机值,再释放锁
- 方案六: 开源框架~Redisson
- 方案七：多机实现的分布式锁Redlock

## 什么是分布式锁

- **「互斥性」**: 任意时刻，只有一个客户端能持有锁。
- **「锁超时释放」**：持有锁超时，可以释放，防止不必要的资源浪费，也可以防止死锁。
- **「可重入性」**:一个线程如果获取了锁之后,可以再次对其请求加锁。
- **「高性能和高可用」**：加锁和解锁需要开销尽可能低，同时也要保证高可用，避免分布式锁失效。
- **「安全性」**：锁只能被持有的客户端删除，不能被其他客户端删除

## 方案一：SETNX + EXPIRE

提到Redis的分布式锁，很多小伙伴马上就会想到`setnx`+ `expire`命令。即先用`setnx`来抢锁，如果抢到之后，再用`expire`给锁设置一个过期时间，防止锁忘记了释放。

> ❝
>
> SETNX 是SET IF NOT EXISTS的简写.日常命令格式是SETNX key value，如果 key不存在，则SETNX成功返回1，如果这个key已经存在了，则返回0。
>
> ❞

假设某电商网站的某商品做秒杀活动，key可以设置为key_resource_id,value设置任意值，伪代码如下：

```
if（jedis.setnx(key_resource_id,lock_value) == 1）{ //加锁
    expire（key_resource_id，100）; //设置过期时间
    try {
        do something  //业务请求
    }catch(){
  }
  finally {
       jedis.del(key_resource_id); //释放锁
    }
}
```

但是这个方案中，`setnx`和`expire`两个命令分开了，**「不是原子操作」**。如果执行完`setnx`加锁，正要执行`expire`设置过期时间时，进程crash或者要重启维护了，那么这个锁就“长生不老”了，**「别的线程永远获取不到锁啦」**。

## 方案二：SETNX + value值是(系统时间+过期时间)

为了解决方案一，**「发生异常锁得不到释放的场景」**，有小伙伴认为，可以把过期时间放到`setnx`的value值里面。如果加锁失败，再拿出value值校验一下即可。加锁代码如下：

```
long expires = System.currentTimeMillis() + expireTime; //系统时间+设置的过期时间
String expiresStr = String.valueOf(expires);

// 如果当前锁不存在，返回加锁成功
if (jedis.setnx(key_resource_id, expiresStr) == 1) {
        return true;
} 
// 如果锁已经存在，获取锁的过期时间
String currentValueStr = jedis.get(key_resource_id);

// 如果获取到的过期时间，小于系统当前时间，表示已经过期
if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {

     // 锁已过期，获取上一个锁的过期时间，并设置现在锁的过期时间（不了解redis的getSet命令的小伙伴，可以去官网看下哈）
    String oldValueStr = jedis.getSet(key_resource_id, expiresStr);
    
    if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
         // 考虑多线程并发的情况，只有一个线程的设置值和当前值相同，它才可以加锁
         return true;
    }
}
        
//其他情况，均返回加锁失败
return false;
}
```

这个方案的优点是，巧妙移除`expire`单独设置过期时间的操作，把**「过期时间放到setnx的value值」**里面来。解决了方案一发生异常，锁得不到释放的问题。但是这个方案还有别的缺点：

- 过期时间是客户端自己生成的（System.currentTimeMillis()是当前系统的时间），必须要求分布式环境下，每个客户端的时间必须同步。
- 如果锁过期的时候，并发多个客户端同时请求过来，都执行jedis.getSet()，最终只能有一个客户端加锁成功，但是该客户端锁的过期时间，可能被别的客户端覆盖
- 该锁没有保存持有者的唯一标识，可能被别的客户端释放/解锁。

## 方案三：使用Lua脚本(包含SETNX + EXPIRE两条指令)

实际上，我们还可以使用Lua脚本来保证原子性（包含setnx和expire两条指令），lua脚本如下：

```
if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then
   redis.call('expire',KEYS[1],ARGV[2])
else
   return 0
end;
```

加锁代码如下：

```
 String lua_scripts = "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then" +
            " redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";   
Object result = jedis.eval(lua_scripts, Collections.singletonList(key_resource_id), Collections.singletonList(values));
//判断是否成功
return result.equals(1L);
```

## 方案四：SET的扩展命令（SET EX PX NX）

除了使用，使用Lua脚本，保证`SETNX + EXPIRE`两条指令的原子性，我们还可以巧用Redis的SET指令扩展参数！（`SET key value[EX seconds][PX milliseconds][NX|XX]`），它也是原子性的！

SET key value[EX seconds][PX milliseconds][NX|XX]

- NX :表示key不存在的时候，才能set成功，也即保证只有第一个客户端请求才能获得锁，而其他客户端请求只能等其释放锁，才能获取。
- EX seconds :设定key的过期时间，时间单位是秒。
- PX milliseconds: 设定key的过期时间，单位为毫秒
- XX: 仅当key存在时设置值

伪代码demo如下：

```
if（jedis.set(key_resource_id, lock_value, "NX", "EX", 100s) == 1）{ //加锁
    try {
        do something  //业务处理
    }catch(){
  }
  finally {
       jedis.del(key_resource_id); //释放锁
    }
}
```

但是呢，这个方案还是可能存在问题：

- 问题一：**「锁过期释放了，业务还没执行完」**。假设线程a获取锁成功，一直在执行临界区的代码。但是100s过去后，它还没执行完。但是，这时候锁已经过期了，此时线程b又请求过来。显然线程b就可以获得锁成功，也开始执行临界区的代码。那么问题就来了，临界区的业务代码都不是严格串行执行的啦。
- 问题二：**「锁被别的线程误删」**。假设线程a执行完后，去释放锁。但是它不知道当前的锁可能是线程b持有的（线程a去释放锁时，有可能过期时间已经到了，此时线程b进来占有了锁）。那线程a就把线程b的锁释放掉了，但是线程b临界区业务代码可能都还没执行完呢。

## 方案五：SET EX PX NX  + 校验唯一随机值,再删除

既然锁可能被别的线程误删，那我们给value值设置一个标记当前线程唯一的随机数，在删除的时候，校验一下，不就OK了嘛。伪代码如下：

```
if（jedis.set(key_resource_id, uni_request_id, "NX", "EX", 100s) == 1）{ //加锁
    try {
        do something  //业务处理
    }catch(){
  }
  finally {
       //判断是不是当前线程加的锁,是才释放
       if (uni_request_id.equals(jedis.get(key_resource_id))) {
        jedis.del(lockKey); //释放锁
        }
    }
}
```

在这里，**「判断是不是当前线程加的锁」**和**「释放锁」**不是一个原子操作。如果调用jedis.del()释放锁的时候，可能这把锁已经不属于当前客户端，会解除他人加的锁。

为了更严谨，一般也是用lua脚本代替。lua脚本如下：

```
if redis.call('get',KEYS[1]) == ARGV[1] then 
   return redis.call('del',KEYS[1]) 
else
   return 0
end;
```

## 方案六：Redisson框架

方案五还是可能存在**「锁过期释放，业务没执行完」**的问题。有些小伙伴认为，稍微把锁过期时间设置长一些就可以啦。其实我们设想一下，是否可以给获得锁的线程，开启一个定时守护线程，每隔一段时间检查锁是否还存在，存在则对锁的过期时间延长，防止锁过期提前释放。

当前开源框架Redisson解决了这个问题。我们一起来看下Redisson底层原理图吧：

前面六种方案都只是基于单机版的讨论，还不是很完美。其实Redis一般都是集群部署的：

![图片](https://mmbiz.qpic.cn/mmbiz_png/sMmr4XOCBzGxM0ZotibjMv7bw8KMNT5buE9kFbH2E2StG56oQeRv7AcuwxgCJlaeMS9IreeGKQJth7wicjpK1ALw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果线程一在Redis的master节点上拿到了锁，但是加锁的key还没同步到slave节点。恰好这时，master节点发生故障，一个slave节点就会升级为master节点。线程二就可以获取同个key的锁啦，但线程一也已经拿到锁了，锁的安全性就没了。

为了解决这个问题，Redis作者 antirez提出一种高级的分布式锁算法：Redlock。Redlock核心思想是这样的：

只要线程一加锁成功，就会启动一个`watch dog`看门狗，它是一个后台线程，会每隔10秒检查一下，如果线程1还持有锁，那么就会不断的延长锁key的生存时间。因此，Redisson就是使用watch dog解决了**「锁过期释放，业务没执行完」**问题。



### 原理

[Redission实现分布式锁_redission分布式锁实现-CSDN博客](https://blog.csdn.net/m0_64116616/article/details/138925028)

#### **trylock（）方法**

三个参数**waitTime**（就是重试等待时间     默认-1, 0这种情况就是立即抢锁，获取不到就失败），**leaseTime**（过期时间默认-1），**TimeUnit**（时间单位）



先获取锁，如果获取失败，判断当前耗时是否超过waitTime，如果超过获取失败；  如果小于，通过消息订阅的方式，如果锁释放了，唤醒并且再次判断是否超时，再去抢锁



#### 看门狗

不设置 leaseTime 或者设置=-1  开启看门狗机制

Redisson的出现，其中的看门狗机制很好解决续期的问题，它的主要步骤如下：

在获取锁的时候，不指定leaseTime或者只能将leaseTime设置为-1，这样才能开启看门狗机制。

在tryLockInnerAsync方法里尝试获取锁，如果获取锁成功调用scheduleExpirationRenewal执行看门狗机制

在scheduleExpirationRenewal中比较重要的方法就是renewExpiration，当线程第一次获取到锁（也就是不是重入的情况），那么就会调用renewExpiration方法开启看门狗机制。

在renewExpiration会为当前锁添加一个**延迟任务task**，这个延迟任务会在10s后执行，执行的任务就是将锁的有效期刷新为30s（这是看门狗机制的默认锁释放时间）
并且在任务最后还会继续递归调用renewExpiration。



**安全性考虑避免死锁**

而当程序出现异常，那么看门狗机制就不会继续递归调用`renewExpiration`，这样锁会在30s后自动释放。

‌**安全性**‌：如果将锁设置为永久有效期，一旦线程因为某种原因（如死锁、资源不足等）无法完成操作，锁将一直被占用，导致其他线程无法访问共享资源，这可能会引发系统瓶颈。而看门狗机制可以在一定程度上避免这种情况，因为它会定期检查锁的状态，如果线程仍然占用锁但未完成操作，看门狗会延长锁的期限，但**如果线程不再活动（如崩溃）或者redis客户端连接关闭，redis的看门狗机制可以配置为在一定时间内没有活动就释放锁，从而避免死锁现象。**



**假如CPU占满了，看门狗续期的时刻延迟了，此时其他锁已经持有锁怎么办？**

这种情况一般设置看门狗续期的时候判断一下时间，如果过期时间<当前时间，说明之前没有连续续期成功，则不继续续期。

## 方案七：多机实现的分布式锁Redlock+Redisson

前面六种方案都只是基于单机版的讨论，还不是很完美。其实Redis一般都是集群部署的：

![图片](https://mmbiz.qpic.cn/mmbiz_png/sMmr4XOCBzGxM0ZotibjMv7bw8KMNT5buE9kFbH2E2StG56oQeRv7AcuwxgCJlaeMS9IreeGKQJth7wicjpK1ALw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果线程一在**Redis的master节点上拿到了锁，但是加锁的key还没同步到slave节点**。恰好这时，master节点发生故障，一个slave节点就会升级为master节点。线程二就可以获取同个key的锁啦，但线程一也已经拿到锁了，锁的安全性就没了。

为了解决这个问题，Redis作者 antirez提出一种高级的分布式锁算法：Redlock。Redlock核心思想是这样的：

> 搞多个Redis master部署，以保证它们不会同时宕掉。并且这些master节点是完全相互独立的，相互之间不存在数据同步。同时，需要确保在这多个master实例上，是与在Redis单实例，使用相同方法来获取和释放锁。
>
> ❞

我们假设当前有5个Redis master节点，在5台服务器上面运行这些Redis实例。

![图片](https://mmbiz.qpic.cn/mmbiz_png/sMmr4XOCBzGxM0ZotibjMv7bw8KMNT5buqeibzdOFibXn3jIHD1laLVPdMW7X1xTE9hdaAuJAiaWOq8xkJ9xNbyVlg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

RedLock的实现步骤:如下

> ❝
>
> - 1.获取当前时间，以毫秒为单位。
> - 2.按顺序向5个master节点请求加锁。客户端设置网络连接和响应超时时间，并且超时时间要小于锁的失效时间。（假设锁自动失效时间为10秒，则超时时间一般在5-50毫秒之间,我们就假设超时时间是50ms吧）。如果超时，跳过该master节点，尽快去尝试下一个master节点。
> - 3.客户端使用当前时间减去开始获取锁时间（即步骤1记录的时间），得到获取锁使用的时间。当且仅当超过一半（N/2+1，这里是5/2+1=3个节点）的Redis master节点都获得锁，并且使用的时间小于锁失效时间时，锁才算获取成功。（如上图，10s> 30ms+40ms+50ms+4m0s+50ms）
> - 如果取到了锁，key的真正有效时间就变啦，需要减去获取锁所使用的时间。
> - 如果获取锁失败（没有在至少N/2+1个master实例取到锁，有或者获取锁时间已经超过了有效时间），客户端要在所有的master节点上解锁（即便有些master节点根本就没有加锁成功，也需要解锁，以防止有些漏网之鱼）。
>
> ❞

简化下步骤就是：

- 按顺序向5个master节点请求加锁
- 根据设置的超时时间来判断，是不是要跳过该master节点。
- 如果大于等于3个节点加锁成功，并且使用的时间小于锁的有效期，即可认定加锁成功啦。
- 如果获取锁失败，解锁！



为什么要向多个Redis申请锁？

> 向多台Redis申请锁，即使部分服务器异常宕机，剩余的Redis加锁成功，整个锁服务依旧可用。

为什么步骤 3 加锁成功后，还要计算加锁的累计耗时？

> 加锁操作的针对的是分布式中的多个节点，所以耗时肯定是比单个实例耗时更，还要考虑网络延迟、丢包、超时等情况发生，网络请求次数越多，异常的概率越大。
> 所以即使 N/2+1 个节点加锁成功，但如果加锁的累计耗时已经超过了锁的过期时间，那么此时的锁已经没有意义了

释放锁操作为什么要针对所有结点？

> 为了清除干净所有的锁。在之前申请锁的操作过程中，锁虽然已经加在Redis上，但是在获取结果的时候，出现网络等方面的问题，导致显示失败。所以在释放锁的时候，不管以前有没有加锁成功，都要释放所有节点相关锁。
>
> 

# 四，比较好的博客方案 

1. set  if not exist     key是随机字符串 
2. set if not exist  + expire     key是随机字符串 
3. set if not exist + expire + finally {finally删除的时候判断是不是上锁的key来删除的 }
4.   set if not exist + expire + finally {删除判断用 lua脚本原子性 }
5. redission RLock  自动续期
6. redission RedLock 半数加锁成功才成功





# 五. 如何让剩下999个线程不读数据库

```
  if( redis!=null ){ return redis数据;}
  else{
  	boolean isLock= xxx.tryLock(-1,Seconds); // 不等待，获取不到就是false
  	if(isLock){
  		// 去数据库查，并把数据放入redis
  	}else{
  		// 方案：自旋个两三次，每次睡眠500ms啥的 
  		int cnt=0;
  		While(cnt<3){
  			Thread.sleep(500);
  			if( redis!=null ){ return redis数据;}
  			// 如果害怕查数据库线程失效了，这里可以再加逻辑，保证一直有个线程去数据库查
  				boolean isLock= xxx.tryLock(-1,Seconds);
  				if(isLock){
  					// 去数据库查，并把数据放入redis
  				}
  		}
  		if(cnt>=3){
  			// 去数据库查，并把数据放入redis
  		}
  	}
  }
  
  // 剩下999个干什么取决于业务
  如果并发量不高，直接redis没有查数据库也没事；
  如果保数据库，
  1. 不推荐：可以加锁，剩下的999串行去redis获取 
  2. 推荐： 自己循环几次，等待那一个线程查询到缓存

RLock lock = RedissLockUtils.getLock(lockKey);
if (RedissLockUtils.tryLock(lockKey, waitTime, leaseTime)) {
    try {
       // 执行业务逻辑
    }catch (Exception e){
    
    }finally {
        lock.unlock();
    }
}


if( redis!=null ) return redis数据;
{
加锁   {
 if( redis!=null ) return redis数据;
 读MYSQL，将MySQL数据写入redis
}
}
```



## 与zk锁区别 

redis

**性能**： 更快，存储在内存。

**自动释放**：redis的客户端如果崩溃了，锁不会释放，只能等待超时

**CAP: **牺牲了一部分**一致性**保可用性  只需要写入一个节点，同步问题不需要马上同步slave,，即使网络延迟，各个节点数据不一致，也能写入数据

zk: 

**自动释放**：可靠性高，客户端断开连接，锁就断开， **对死锁更友好**

**性能**：数据存储在磁盘

**CAP: **牺牲了一部分可用性保一致性， 集群少于一半节点不可用时，会拒绝新的请求
