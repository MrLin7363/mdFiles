redis

## 一、 集群

### 1 . 集群三种模式

主从模式 < 哨兵模式 < 集群模式（多个node节点里面包含一主多从）

https://blog.csdn.net/m0_59439550/article/details/121286998

 **单体与集群**

redis默认16个数据库，单机模式通过select  0等切换数据库；  集群模式默认没有16个数据库

### 2. 集群模式-原理

#### 2.1 存储原理：

redis集群数据存储原理：

  在redis cluster中，如果想要存入一个key-value，

  这个key首先会通过CRC16算法取余（和16384取余），

  结果会对应上0-16383之间的哈希槽（hash slot）

  最后，redis cluster会将key-value放置在对应的哈希槽中。

  

redis集群数据获取原理：

  当client向redis cluster中的任意一个节点发送与数据库key有关的命令时，

  接收命令的节点会计算出要处理的key属于哪个哈希槽（hash slot），

  并且先检查这个hash slot是否属于自己（管辖）：

​    如果key所在的槽正好属于自己（管辖），节点会直接执行这个key相关命令。

​    如果key所在的槽不属于自己（管辖），那么节点会给client返回一个MOVED错误，

​    指引client转向负责对应槽的节点，并客户端需要再次发送想要执行的和key相关的命令。

#### 2.2 操作问题

使用一个客户端cmd，连接集群某一个节点

使用keys *只能查找到当前节点的keys

只有get 或者单独查询一个节点时，如果当前节点没有该数据，则会自动跳转到另一个节点上

 Redirected to slot [10313] located at 123.342.234.23:6379

## 二、数据结构

### 1. 常用指令

连接集群  redis-cli -h 7.225.150.145 -p 6379 -a password -c

1. 查看redis是否是集群模式，info cluster 命令

2. 查看集群的所有节点信息，cluster nodes 命令

3. 查看集群中各个节点的slot区间，cluster slots 命令

4. 查看指定key所在slot的值，cluster keyslot key 命令

  ```
cluster keyslot "keyname"
  ```

  原文链接：https://blog.csdn.net/yzf279533105/article/details/118333545

5.显示当前库的key数量  dbsize

6.所有库key的数量 info keyspace

7.模糊查询key 

```
keys *      # 查询所有key （最好别使用，太影响性能了）
keys zhao*  # 查询以 zhao 开头的key 
keys *zhao  # 查询以 zhao 结尾的key
```

8.获取keys的数据

```
get "keyname"
```

9. exists key 

10. expire key seconds  设置 key 的生存时间，超过时间，key 自动删除。单位是秒。

11. ttl   key   以秒为单位，返回 key 的剩余生存时间（ttl: time to live）返回值

12. type key   查看类型

13. del 删除 key

14. INCR命令用于实现Redis**计数器**

    - `INCRBY key increment`：将key对应的数字**加**increment
    - `DECR key`：对key对应的数字做**减1**操作
    - `DECRBY key decrement`：将key对应的数字**减**decrement

    ```
    172.16.255.101:6379> set k2 100
    OK
    172.16.255.101:6379> INCR k2
    (integer) 101
    172.16.255.101:6379> get k2
    "101"
    ```

15.过期时间

　　**EXPIRE** 接口定义：EXPIRE key "seconds"
　　　　接口描述：设置一个key在当前时间"seconds"(秒)之后过期。返回1代表设置成功，返回0代表key不存在或者无法设置过期时间。

　　　　**PEXPIRE** 接口定义：PEXPIRE key "milliseconds"
　　　　接口描述：设置一个key在当前时间"milliseconds"(毫秒)之后过期。返回1代表设置成功，返回0代表key不存在或者无法设置过期时间。

```
127.0.0.1:6379> set aa bb
OK
127.0.0.1:6379> EXPIRE aa 60
(integer) 1
127.0.0.1:6379> EXPIRE aa 600
(integer) 1
```

### 2. spring-data-代码实现部分指令

分布式锁

```
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
```

```
org.springframework.data.redis.connection;
```

keys * ... 

```
@GetMapping(value = "/patternDelete")
    public Set<String> patternDeleteCache(String pattern) {
        Set<String> deleteKeys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + pattern + "*").count(1000).build();
            Cursor<byte[]> scan = connection.scan(scanOptions);
            Set<String> keys = new HashSet<>();
            while (scan.hasNext()) {
                byte[] next = scan.next();
                keys.add(new String(next));
            }
            return keys;
        });
        if (CollectionUtils.isNotEmpty(deleteKeys)) {
            redisTemplate.delete(deleteKeys);
        }
        return deleteKeys;
    }
```

#### (1) bound...Ops和opsFor系列区别

```
    /**获取a，然后获取b，然后删除c，对同一个key有多次操作，按照opsForHash()的写法
     * 每次都是redisTemplate.opsForHash().xxx("key","value")写法很是啰嗦
     */
    int result = (Integer) redisTemplate.opsForHash().get("hash-key","a");
    result = (Integer)redisTemplate.opsForHash().get("hash-key","b");
    redisTemplate.opsForHash().delete("hash-key","c");
 
    /**
     * boundHashOps()则是直接将key和boundHashOperations对象进行了绑定，
     * 后续直接通过boundHashOperations对象进行相关操作即可，写法简洁，不需要
     * 每次都显式的将key写出来
     */
    BoundHashOperations<String, String, Object> boundHashOperations = redisTemplate.boundHashOps("hash-key");
    result = (Integer) boundHashOperations.get("a");
    result = (Integer) boundHashOperations.get("b");
    boundHashOperations.delete("c");
    
    //以上是个人拙见，如有不对，欢迎指正
```

## 

### 3. HyperLogLog

#### 3.1 业务场景

亿级用户访问量统计解决方案

1：业务场景引入
HyperLogLog常用于大数据量的统计，比如页面访问量统计或者用户访问量统计。

①需求：要统计一个页面的访问量(PV)

①方案：直接用**redis计数器**或者直接存数据库都可以

②需求：要统计一个页面的用户访问量(UV)，即：一个用户一天内如果访问多次的话，也只能算一次

②方案：可能会想到用SET集合来做，因为SET集合是有去重功能的，key存储页面对应的关键字，value存储对应userId

③需求：假如有几千万访问量，为了统计一个访问量，要频繁创建SET集合对象。

③方案：针对大访问量需要进行统计的问题，redis实现了一种HyperLogLog算法。 bitmap也可以实现，就是内存占用更大，因为一个用户一个位置

原文链接：https://blog.csdn.net/A_art_xiang/article/details/126767562

#### 3.2 基数统计

基数统计就是指统计一个集合中不重复的元素个数。比如说统计网页的 UV（访问用户量）。

网页 UV 的统计有个独特的地方，就是需要去重，一个用户一天内的多次访问只能算作一次。在 Redis 的集合类型中，Set 类型默认支持去重，所以看到有去重需求时，我们可能第一时间就会想到用 Set 类型。

但是！如果一个网站用户数非常多，使用set类型来记录用户数，会造成大量的内存浪费。而HyperLogLog可以很好的满足这个要求。

Redis HyperLogLog 是用来做基数统计的算法，HyperLogLog 的优点是，在输入元素的数量或者体积非常非常大时，计算基数所需的空间总是固定 的、并且是很小的。

在 Redis 里面，每个 HyperLogLog 键只需要花费 12 KB 内存，就可以计算接近 2^64 个不同元素的基 数。这和计算基数时，元素越多耗费内存就越多的集合形成鲜明对比。

但是，因为 HyperLogLog 只会根据输入元素来计算基数，而不会储存输入元素本身，所以 HyperLogLog 不能像集合那样，返回输入的各个元素。

HyperLogLog 的统计规则是基于概率完成的，所以它给出的统计结果是有一定误差的，标准误算率是 0.81%。这也就意味着，你使用 HyperLogLog 统计的 UV 是 100 万，但实际的 UV 可能是 101 万。虽然误差率不算大，但是，如果你需要精确统计结果的话，最好还是继续用 Set 或 Hash 类型

#### 3.3 指令

pfadd

```
127.0.0.1:6379> pfadd pfkey1 a b c d e f g h
(integer) 1
```

pfcount

```
127.0.0.1:6379> pfcount pfkey1
(integer) 8

# 我们多设置几个key
127.0.0.1:6379> pfadd pfkey2 i j k
(integer) 1
# pfcount也可以计算多个key的总和
127.0.0.1:6379> pfcount pfkey1 pfkey2
(integer) 11

# 计算总数时，会进行去重
127.0.0.1:6379> pfadd pfkey3 j k l
(integer) 1
127.0.0.1:6379> pfcount pfkey1 pfkey2 pfkey3
(integer) 12

# 对单个key也会进行去重
127.0.0.1:6379> pfadd pfkey4 a b a c
(integer) 1
127.0.0.1:6379> pfcount pfkey4
(integer) 3
```

pfmerge

```
# 合并并去重
127.0.0.1:6379> pfmerge mergekey pfkey1 pfkey2 pfkey3 pfkey4
OK
127.0.0.1:6379> pfcount mergekey
(integer) 12
```

### 4. 计数器

```
 // 对value进行加1操作
stringRedisTemplate.opsForValue().increment(key,1);

// 判断在redis中是否有key值
Boolean redisKey = stringRedisTemplate.hasKey(key);
```

### 5. zset

正无穷 +inf  负无穷 -inf

(2  3  :  2<value<=3

```
zrangebyscore com.lin.redis:pv_set 2 +inf withscores
```

## 三、 内存淘汰机制&删除策略

查看过期策略    config get maxmemory-policy

查看最大内存   config get maxmemory

返回字节数

Redis 的内存占用会越来越高。Redis 为了限制最大使用内存，提供了 redis.conf 中的 配置参数 maxmemory。当内存超出 maxmemory，Redis 提供了几种内存淘汰机制让用户选择，配置 maxmemory-policy：

noeviction：当内存超出 maxmemory，写入请求会报错，但是删除和读请求可以继续。
allkeys-lru：当内存超出 maxmemory，在所有的 key 中，移除最少使用的key。只把 Redis 既当缓存是使用这种策略。
allkeys-random：当内存超出 maxmemory，在所有的 key 中，随机移除某个 key。
volatile-lru：当内存超出 maxmemory，在设置了过期时间 key 的字典中，移除最少使用的 key。把 Redis 既当缓存，又做持久化的时候使用这种策略。
volatile-random：当内存超出 maxmemory，在设置了过期时间 key 的字典中，随机移除某个key。
volatile-ttl：当内存超出 maxmemory，在设置了过期时间 key 的字典中，优先移除 ttl 小的
逐出算法
redis使用内存存储数据，在执行每一个命令前，会调用freeMemoryIfNedded()检测内存是否充足，如果内存不满足新加入数据的最低存储要求，redis要临时删除一些数据为当前指令清理存储空间，清理数据的策略称为逐出算法
逐出数据的过程不是100%能够清理出足够的可使用的内存空间的，如果不成功则反复执行，当对所有数据尝试完毕后，如果不能达到内存中的存储要求，将报错

### 1. 删除策略

redis默认是 定期删除+惰性删除

## 四、过期监听

由于删除策略的存在，如果key很多，那么key 过期了，不会立即监听到，必须等redis删除才能监听到。  如果此时get查询key则触发惰性删除，监听到当前key

```
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }
```

```
@Component
public class KeyExpiredListener extends KeyExpirationEventMessageListener {
    public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // open or close
        System.out.println("key 过期了"+message.toString());
        byte[] channel = message.getChannel();
        String topic = new String(channel);
        System.out.println("key 过期了, topic = "+ topic);
    }
}
```

## 五、RESP协议

Redis Serialization Protocol

在netty学习中，使用到RESP协议连接redis，  用户在Redis客户端键入命令后，Redis-cli会把命令转化为RESP协议格式，然后发送给服务器。

## 六、客户端工具

### 6.1 **AnotherRedisDesktopManager**

下载地址  ： https://github.com/qishibo/AnotherRedisDesktopManager/releases

RedisDesktop这个款工具已经开始收费

能不用cmd不用cmd，客户端工具好用

这里的界面搜索，能直接搜索全部节点；而且能打开命令行

