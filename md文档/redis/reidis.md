redis

## 一、 集群

### 1 . 集群三种模式

主从模式 < 哨兵模式 < 集群模式（多个node节点里面包含一主多从）

https://blog.csdn.net/m0_59439550/article/details/121286998

 **单体与集群**

redis默认16个数据库，单机模式通过select  0等切换数据库；  集群模式默认没有16个数据库

### 2. 集群模式-原理

#### 2.1 存储原理

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

#### 2.3 集群选举原理

https://blog.51cto.com/u_12192/6880456

如果master挂了，需要其他master（二个以上）投票选举挂了的master的从节点为主节点

Redis Cluster 集群选举跟哨兵集群选举跟哨兵集群选举还是不太一样的

在主节点宕机了的时候，它的从节点会向其他的主从节点发出选举，其他的主从节点收到选举的消息之后，会立马向发起者响应(这里响应并不是所有的节点都会去响应，而是只有主节点才会响应)，当发起者收到的响应数过半的时候，发起者会将自己的改为主节点

具体步骤如下：

1.slave发现自己的master变为FAIL

2.将自己记录的集群currentEpoch加1，并广播FAILOVER_AUTH_REQUEST 信息

3.其他节点收到该信息，只有master响应，判断请求者的合法性，并发送FAILOVER_AUTH_ACK，对每一个epoch只发送一次ack

4.尝试failover的slave收集master返回的FAILOVER_AUTH_ACK

5.slave收到超过半数master的ack后变成新Master(这里解释了集群为什么至少需要三个主节点，如果只有两个，当其中一个挂了，只剩一个主节点是不能选举成功的)

#### 2.4 请求原理

只有master节点具有处理请求的能力，slave节点主要是用于节点的高可用

slave广播Pong消息通知其他集群节点。

#### 2.5 actuate检测redis健康降级问题积累

原先方案：直接继承ReactiveClusterCommands.clusterGetClusterInfo() 

ReactiveClusterCommands.clusterGetClusterInfo() 获取集群信息也是随机节点去获取，可能有某个节点不通的情况会获取失败并且抛异常

LettuceReactiveRedisClusterConnection.executeCommandOnArbitraryNode

想判断集群是否健康降级，只能通过判断是否全部master节点都健康的情况

https://blog.csdn.net/jx_ZhangZhaoxuan/article/details/132002563

## 二、数据结构

### 1. 常用指令

指令大全地址 [redis 命令手册](https://redis.com.cn/commands.html)

连接集群  redis-cli -h 7.225.150.145 -p 6379 -a password -c           

-a可能不安全   redis-cli -h 7.225.150.145 -p 6379 -c   然后-> auth <password>   

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

#### 1.1 注意事项

redis-cli -h 7.225.150.145 -p 6379 -c   如果是集群模式，只连接一台机器，可能某些keys是不在这台机器访问不到的



### 2. spring-data-代码实现部分指令

#### (1) 分布式锁

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

#### (2) 游标迭代器 scan 

有这么一个案例，Redis 服务器存储了海量的数据，其中登录用户信息是以 user_token_id 的形式存储的。运营人员想要当前所有的用户登录信息，然后悲剧就发生了：因为用了 `keys user_token_*` 来查询对应的用户，结果导致 Redis 假死不可用，以至于影响到线上的其他业务接连发生问题。并且这个假死的时间是和存储的数据成正比的，数据量越大假死的时间就越长，导致的故障时间也越长。

如何解决这种查询的情况？

在 Redis 2.8 之前，我们只能使用 keys 命令来查询我们想要的数据，但这个命令存在两个缺点：

1. 此命令没有分页功能，我们只能一次性查询出所有符合条件的 key 值，如果查询结果非常巨大，那么得到的输出信息也会非常多；
2. keys 命令是遍历查询，因此它的查询时间复杂度是 o(n)，所以数据量越大查询时间就越长。

在 Redis 2.8 时推出了 Scan

```css
scan cursor [MATCH pattern] [COUNT count]
```

- cursor：光标位置，整数值，从 0 开始，到 0 结束，查询结果是空，但游标值不为 0，表示遍历还没结束；
- match pattern：正则匹配字段；
- count：限定服务器单次遍历的字典槽位数量（约等于），只是对增量式迭代命令的一种提示（hint），并不是查询结果返回的最大数量，它的默认值是 10。

但有两个注意问题

1. 查询的结果为空，但游标值不为 0，表示遍历还没结束；
2. 如果设置了 count 10000，但返回是不固定的，这是因为 count 只是限定服务器单次遍历的字典槽位数量（约等于），而不是规定返回结果的 count 值。

scan其他命令

1. HScan 遍历字典游标迭代器
2. SScan 遍历集合的游标迭代器
3. ZScan 遍历有序集合的游标迭代器

查询规则

- 它可以完整返回开始到结束检索集合中出现的所有元素，也就是在整个查询过程中如果这些元素没有被删除，且符合检索条件，则一定会被查询出来；
- 它可以保证不会查询出，在开始检索之前删除的那些元素。

缺点：

- 一个元素可能被返回多次，需要客户端来实现去重；
- 在迭代过程中如果有元素被修改，那么修改的元素能不能被遍历到不确定。

 

总结：

查询命令：

1. Scan：用于检索当前数据库中所有数据；
2. HScan：用于检索哈希类型的数据；
3. SScan：用于检索集合类型中的数据；
4. ZScan：由于检索有序集合中的数据。

查询注意：

1. Scan 可以实现 keys 的匹配功能；
2. Scan 是通过游标进行查询的不会导致 Redis 假死；
3. Scan 提供了 count 参数，可以规定遍历的数量；
4. Scan 会把游标返回给客户端，用户客户端继续遍历查询；
5. Scan 返回的结果可能会有重复数据，需要客户端去重；
6. 单次返回空值且游标不为 0，说明遍历还没结束；
7. Scan 可以保证在开始检索之前，被删除的元素一定不会被查询出来；
8. 在迭代过程中如果有元素被修改， Scan 不保证能查询出相关的元素。

##### （1）Jdies客户端

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.9.0</version>
        </dependency>
```

单机redis版本

```
   // count每次扫描的个数
   public Set<String> getPatternKeys(String pattern, int count) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            // match different from java
            ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(count).build();
            Set<String> result = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    byte[] next = cursor.next();
                    result.add(new String(next, StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                LOGGER.error("RedisUtils getPatternKeys e={}", e);
            }
            return result;
        });
    }
```

集群reidis版本

```
@Component
public class RedisClusterUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    public List<String> getPatternKeysCluster(String matchKey) {
        List<String> result = new ArrayList<>();
        // redisTemplate.getConnectionFactory().getClusterConnection()是随机获取其中一个节点包括slave去连接，有可能ping不通失败
        Map<String, JedisPool> clusterNodes = ((JedisCluster) redisTemplate.getConnectionFactory()
            .getClusterConnection().getNativeConnection()).getClusterNodes();
        for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
            //获取单个的jedis对象
            Jedis jedis = entry.getValue().getResource();
            // 判断非从节点(因为若主从复制，从节点会跟随主节点的变化而变化)，此处要使用主节点从主节点获取数据
            if (!jedis.info("replication").contains("role:slave")) {
                List<String> keys = getScan(jedis, matchKey);
                if (keys.size() > 0) {
                    Map<Integer, List<String>> map = new HashMap<>(8);
                    //接下来的循环不是多余的，需要注意
                    for (String key : keys) {
                        // cluster模式执行多key操作的时候，这些key必须在同一个slot上，不然会报:JedisDataException:
                        // 所以这里把他们汇聚起来，一般如果只查询key可以，不要下面这段代码，直接set集合add keys
                        int slot = JedisClusterCRC16.getSlot(key);
                        // 按slot将key分组，相同slot的key一起提交
                        if (map.containsKey(slot)) {
                            map.get(slot).add(key);
                        } else {
                            List<String> list1 = new ArrayList();
                            list1.add(key);
                            map.put(slot, list1);
                        }
                    }
                    for (Map.Entry<Integer, List<String>> integerListEntry : map.entrySet()) {
                        result.addAll(integerListEntry.getValue());
                    }
                }
            }
        }
        return result;
    }

    public List<String> getScan(Jedis jedis, String key) {
        List<String> result = new ArrayList<>();
        //扫描的参数对象创建与封装,每次游标扫描1000行,这里可以根据业务需求进行修改
        ScanParams params = new ScanParams().match(key).count(1000);
        String cursor = "0";
        ScanResult scanResult = jedis.scan(cursor, params);

        // scan.getStringCursor() 存在 且不是 0 的时候，一直移动游标获取
        while (scanResult.getStringCursor() != null) {
            System.out.println("scan " + cursor);
            result.addAll(scanResult.getResult());
            String nextCursor = scanResult.getStringCursor();
            if (!"0".equals(nextCursor)) {
                scanResult = jedis.scan(nextCursor, params);
            } else {
                break;
            }
        }
        return result;
    }
}
```

单机和集群选择

```
    public Set<String> getPatternKeys(String pattern, int count) {
        // 生产环境单机
        if (redisConfig.isProd()) {
            return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(count).build();
                Set<String> result = new HashSet<>();
                try {
                    Cursor<byte[]> cursor = connection.scan(scanOptions);
                    while (cursor.hasNext()) {
                        byte[] next = cursor.next();
                        result.add(new String(next, StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    LOGGER.error("RedisUtils getPatternKeys e={}", e);
                }
                return result;
            });
        } else {
        	// 测试环境集群
            return getPatternKeysCluster(pattern, count);
        }
    }
 
    private Set<String> getPatternKeysCluster(String matchKey, int count) {
        Set<String> result = new HashSet<>();
        Map<String, JedisPool> clusterNodes = ((JedisCluster) redisTemplate.getConnectionFactory()
            .getClusterConnection().getNativeConnection()).getClusterNodes();
        for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
            Jedis jedis = entry.getValue().getResource();
            if (!jedis.info("replication").contains("role:slave")) {
                List<String> keys = getScan(jedis, matchKey, count);
                if (keys.size() > 0) {
                    result.addAll(keys);
                }
            }
        }
        return result;
    }
 
    private List<String> getScan(Jedis jedis, String key, int count) {
        List<String> result = new ArrayList<>();
        ScanParams params = new ScanParams().match(key).count(count);
        String cursor = "0";
        ScanResult scanResult = jedis.scan(cursor, params);
 
        while (scanResult.getStringCursor() != null) {
            result.addAll(scanResult.getResult());
            String nextCursor = scanResult.getStringCursor();
            if (!"0".equals(nextCursor)) {
                scanResult = jedis.scan(nextCursor, params);
            } else {
                break;
            }
        }
        return result;
    }
```

##### （2）luttce客户端

```  max-http-header-size: 16KB
spring:
  data:
    redis:
      repositories:
        enabled: false
  redis:
    cluster:
      nodes: xxx
      max-redirects: 2
    password: ${REDIS_PASSWORD:xxx}
    connect-timeout: 5000
    lettuce:
      pool:
        min-idle: 10 *#**最小空闲连接
        max-idle: 20 *#**最大空闲数
        max-wait: 2s *#* *连接池最大阻塞等待时间**(**使用负值标识没有限制**)
      cluster:    #配置拓扑刷新，redis节点挂了，更新.
        refresh:
          period: 30s
          adaptive: true
```

```
Maven: io.lettuce:lettuce-core:6.1.10.RELEASE
```

```
    /*
    每个url去建立连接
    1.全部建立不了，全挂或不通->降级
    2.其中一个建立了，记录master节点是那些
    3.其中某个建立不了连接->记录下来，如果被后面建立连接节点的master命中->降级
     */ // 2分钟 2次master有问题就降，恢复也是
    public void connect() {
        // 创建RedisURI
        RedisURI redisURI = RedisURI.builder().withHost("7.xxx.xxx.145").withPort(6379)
            .withPassword(password.toCharArray()).withTimeout(Duration.ofSeconds(3)).build();
        StatefulRedisClusterConnection<String, String> connection = null;
        RedisClusterClient redisClusterClient = null;
        try {
            redisClusterClient = RedisClusterClient.create(redisURI);
            connection = redisClusterClient.connect();
            RedisAdvancedClusterCommands<String, String> commands = connection.sync();
            NodeSelection<String, String> clusterNodes = commands.all();
            // 获取所有cluster节点
            for (int i = 0; i < clusterNodes.size(); i++) {
                RedisClusterNode node = clusterNodes.node(i);
                if (StringUtils.isBlank(node.getSlaveOf())) {
                    // is master
                    String host = node.getUri().getHost();
                    if (masterList.get() == null) {
                        masterList.set(new StringBuffer(host));
                    } else if (masterList.get().indexOf(host) == -1) {
                        masterList.set(masterList.get().append(',').append(host));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("redis health check connect error", e);
        } finally {
            if (redisClusterClient != null) {
                redisClusterClient.shutdown();
            }
            // 关闭client  connection也会自动关闭
            //if (connection != null) {
           //     connection.close();
           // }

        }
    }
```

#### (3) bound...Ops和opsFor系列区别

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

ttl key 过期时间 getExpire()

- The command returns -2 if the key does not exist.
- The command returns -1 if the key exists but has no associated expire.
- 返回单位是 s 

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

### 6. 底层数据结构

redis String 的底层数据结构 SDS https://zhuanlan.zhihu.com/p/619828101

list的底层数据结构quicklist=linkedlist+ziplist https://baijiahao.baidu.com/s?id=1763496410158464261&wfr=spider&for=pc



hash =ziplist+hashtable

set = intset+hashtable

zset=ziplist+zskiplist

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

## 七、spring-boot配置redis cluster代码

```
spring:
  redis:
    cluster:
      # 集群节点
      nodes: 192.168.1.1:7000,192.168.1.1:7001,192.168.1.2:7002,192.168.1.2:7003
      # 最大重定向次数
      max-redirects: 5
    # 密码
    password: myredis
    lettuce:
      pool:
        min-idle: 10 #最小空闲连接
        max-idle: 20 #最大空闲数
        max-wait: 2s # 连接池最大阻塞等待时间(使用负值标识没有限制)
      cluster:
        refresh:
          period: 30s
          adaptive: true  #开启集群拓扑刷新功能，某个节点挂了，能通知到  必须的 springboot2.x默认用lettuce默认不开启,如果是jredis默认开启了
    timeout: 5s # 连接超时时间

```

```
SpringBoot2.x开始默认使用的Redis客户端由Jedis变成了Lettuce，但是当Redis集群中某个节点挂掉之后，Lettuce将无法继续操作Redis，原因在于此时Lettuce使用的仍然是有问题的连接信息。

实际上，Lettuce支持redis 集群拓扑动态刷新，但是默认并没有开启，SpringBoot在集成Lettuce时默认也没有开启。并且在SpringBoot2.3.0之前，是没有配置项设置Lettuce自动刷新拓扑的。

相关issue：Add configuration to enable Redis Cluster topology refresh

解决方案1：
升级到SpringBoot2.3.0或以上版本。并添加如下配置项

spring.redis.timeout=60s
spring.redis.lettuce.cluster.refresh.period=60s
spring.redis.lettuce.cluster.refresh.adaptive=true


lettuce是不会进行“心跳”操作的，也就是说，它不会保持连接，导致了连接超时
```

```
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *  redis配置
 * 集群版 Redis缓存配置类
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport
{
    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

## 九、方案

### 9.1 过期活跃版本统计预热数据

|                    |               |                                                              |
| ------------------ | ------------- | ------------------------------------------------------------ |
| 首页访问，预热数据 |               | aop 统计版本PV，uv                                           |
| set                | 业务优先的set | 指定版本优先级高                                             |
| uvZset             | uv>1          | 前5天内的uvZSet（设置过期时间，扫描时找不到），通过模式匹配scan模糊查找；根据版本，union hyperlog ，然后每个版本 addzSet，最后rangeZset |
| pvZset             | pv>3          | 前5天内的pvZSet（设置过期时间，扫描时找不到），通过模式匹配scan模糊查找，addzSet，rangeZset |
| setAll             | 活跃版本      | 优先取set，再从uvZset和pvZset中取top多少版本，值由redis实时控制；过期监听如果是活跃版本，则同步 |
| 过期监听           |               | 活跃版本缓存过期了，更新缓存                                 |

过期监听： 1. 使用redis的listener, 由于删除策略惰性删除，key过期不会马上监听到，使用定时任务25分钟扫描特定模式key*，这样会监听到过期；         

2. 扫描的时候发现生产和测试一个是集群一个是单机，改为集群cluster扫描模式或单机模式，由于keys比较少，所以扫描5s内就完成；

3. 过期监听到，检查:用pattern模式匹配是否是版本同步缓存过期的key

### 9.2 限流

第一推荐阅读，：读这个就行https://zhuanlan.zhihu.com/p/479956069

计数器，固定窗口，滑动窗口（无法处理短时间大流量），漏桶（控制请求的频率，无法处理突发流量），令牌桶

六种限流算法对比 -推荐阅读   [常见的限流方式_aiguangyuan的博客-CSDN博客](https://blog.csdn.net/weixin_40629244/article/details/125970505)

#### 9.1.1 计数器算法

计数器算法是Redis实现限流的常见手段，其核心思想为统计单位时间内的请求数量并与阈值进行比较，当达到阈值时就拒绝后续访问，从而起到限制流量的目的。具体实现方法如下：

1.1 使用Redis的原子操作incr操作，实现计数器的自增。

1.2 通过Redis对key设置过期时间，例如设置一分钟后过期。

1.3 当计算器的值超过限制阈值时，拒绝访问，否则可以继续访问并重置计数器值。

需要注意的是，由于计数器算法只记录请求数量，无法区分不同类型的请求，可能会存在被恶意用户绕过的可能性。因此，这种方法适用于单一请求的场景，如接口限流。

#### 9.1.2 漏桶算法

漏桶算法也是一种流量控制算法，和计数器算法相比，漏桶算法会对请求进行一个统一的速率限制，而非单纯地限制访问量。其主要思想为模拟水桶中的水流量，加入一个固定的速率加入水，如果水桶满了，就拒绝后续的请求，否则按照固定的速率处理请求。具体实现方法如下：

2.1 将漏桶看作一个固定大小的容器，以固定的速率漏出水。

2.2 使用Redis的List数据类型，将每个请求按照时间顺序加入List中，即水流进入水桶的过程。

2.3 使用Redis的过期机制，将List中已经达到一定时间的请求移出，即水从桶中漏出的过程。

2.4 当请求加入List时，判断List的长度是否达到桶的最大限制，如果超过限制，就拒绝请求，否则可以正常处理。

漏桶算法可用于应对各种请求，由于限制速率而非请求数量，不容易被恶意用户绕过，常用于对整个应用的限流控制。

#### 9.1.3 令牌桶算法

令牌桶算法也属于流量控制算法，其主要思想为固定速率向令牌桶中添加令牌，一个请求需要获取令牌才能执行，当令牌桶中没有令牌时，请求将被拒绝。具体实现方法如下：

3.1 使用Redis的List数据类型，将一定数量的令牌添加到List中，表示令牌桶的容量。

3.2 使用Redis过期机制，每当有请求到来时，如果List中还有令牌，则可以正常处理请求，并从List中移除一个令牌，否则拒绝请求。

3.3 当令牌生成速度过快或者请求到来速度过慢时，可能会出现令牌桶溢出的情况。因此，可使用Redis的有序集合数据类型，记录每次执行的时间和执行次数，用于在下一次添加令牌时，调整添加令牌的数量，以适应实际情况。

令牌桶算法不仅能够限制并发数，而且可以控制请求速率，比较适合对底层资源进行保护，比如数据库连接池、磁盘IO等。

由于除了两个数字出现了一次，其他数字都出现了两次。根据异或运算的性质：**两个相同的数异或结果为000，一个数与000异或还是它自己，异或运算满足交换律**。把numsnums*n**u**m**s*中的元素全部异或起来的结果eoreor*eor*就是那两个只出现一次的数字的异或结果。而这两个数不相同，意味着eoreor*eor*至少有一位是111，我们可以用lowbitlowbit*l**o**w**bi**t*运算拿到最低位的111，然后遍历numsnums*n**u**m**s*数组，将所有数nums[i]nums[i]*n**u**m**s*[*i*]按照这一位是不是111分成两类，初始化num1=num2=0num_1=num_2=0*n**u**m*1=*n**u**m*2=0。

1. 如果当前位是111，就将nums[i]nums[i]*n**u**m**s*[*i*]异或到num1num_1*n**u**m*1上。
2. 如果当前位是000，就将nums[i]nums[i]*n**u**m**s*[*i*]异或到num2num_2*n**u**m*2上。

这样一来，两个只出现一次的数就会被分别异或到num1num_1*n**u**m*1和num2num_2*n**u**m*2上，而其他数也会被分别异或到这两个数上。而由于其他数都出现了两次，所以最终它们就会被异或成000，num1num_1*n**u**m*1和num2num_2*n**u**m*2就是那两个只出现一次的数。
