### MongoDB

#### 一、介绍

**使用行业**：

- 游戏场景，使用 MongoDB 存储游戏用户信息，用户的装备、积分等直接以内嵌文档的形式存储，方便查询、更新
- 物流场景，使用 MongoDB 存储订单信息，订单状态在运送过程中会不断更新，以 MongoDB 内嵌数组的形式来存储，一次查询就能将订单所有的变更读取出来。
- 社交场景，使用 MongoDB 存储存储用户信息，以及用户发表的朋友圈信息，通过地理位置索引实现附近的人、地点等功能
- 物联网场景，使用 MongoDB 存储所有接入的智能设备信息，以及设备汇报的日志信息，并对这些信息进行多维度的分析
- 视频直播，使用 MongoDB 存储用户信息、礼物信息等，点赞信息
- 社交场景，微信朋友圈，地理位置啥的

特点：三高

对数据库并发读写的需求

对海量数据和访问的需求

对数据库扩展性强，高可用性

对事务性要求不高，写入频繁，数据量大的适合mongoDB

QPS 2000-3000以上可用

数据库TB甚至PB级别

需要大量地理位置查询、文本查询

**对比**

数据结构是一种类似JSON的BJSON

![数据库区别](数据库区别.png)

![diffrence](diffrence.png)

| 传统数据库MYSQL | 文档型数据库mongoDB |
| --------------- | ------------------- |
| database        | database            |
| 表              | 集合                |
| 行              | 文档                |
| 字段            | 域                  |
| index索引       | index索引           |
| join表连接      | 不支持              |
| primary key     | _id自动作为主键     |

#### 二、start

下面基于6.0版本 MongoDB Server + MongoDB Shell + MongoDB compass

下载地址：https://www.mongodb.com/try/download/community

新版的无mongo.exe命令 需要 MongoDB Shell Download

https://www.mongodb.com/try/download/shell

##### 1.启动方式

1.命令行

安装目录下  /data  新建一个/db目录

cmd   bin目录  

```
mongod --dbpath=../data/db
```

启动数据库后db文件夹下会生成一些文件

2.配置文件方式

新建 conf/mongod.conf

```
dbpath=D:\software\mongoDB\data\db   #数据库路径
```

命令行

```
mongod -f ../conf/mongod.conf
或
mongod --config ../conf/mongod.conf
```

3.启动问题

```
 Unable to create/open the lock file: D:/software/mongoDB/data/db\\mongod.lock
```

只能同时启动一个server，关闭已启动的

##### 2.shell连接&compass使用（无命令行）

```
mongo --host=127.0.0.1 --port=27017
新版无mongo命令，下载mongosh
mongosh --host=127.0.0.1 --port=27017
```

#### 三、数据库操作

https://www.w3cschool.cn/mongodb/mongodb-create-database.html

##### 默认数据库

admin库  root权限

local  存在这里的数据不会被复制

config:  保存分片信息

##### 1.选择创建数据库

```
show dbs          查看磁盘的数据库
use  [db_name]   创建或使用，一开始创建在内存，有了集合后才持久化到磁盘，show dbs才能查到
db  查看当前数据库名
db.dropDatabase()   删除当前数据库
```

##### 2.集合

显式创建：创建一个集合 （逐渐弱化）

隐式创建：插入文档的时候自动把文档对应的集合创建

```
db.createCollection("chenjunlin")   创建
db.集合.drop()    删除
show collections
```

##### 3.文档

###### **插入**文档

```
db.col.insert({title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: 'lin',
    url: 'http://www.w3cschool.cn',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 99
})
```

```
db.col.insertMany([{title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: 'w3cschool',
    url: 'http://www.w3cschool.cn',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 100
},{title: 'lin 教程', 
    description: 'lin 是一个 Nosql 数据库',
    by: 'lin',
    url: 'http://www.w3cschool.cn',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 99
}])
```

###### **查询**文档

db.集合名.find()

cls 清空屏幕

```
db.col.find()
db.col.findOne()

db.col.find({title: 'MongoDB 教程'})

db.col.find({likes:99,by:"lin"})
```

###### 更新文档

```
db.collection.update(
   <query>, 条件
   <update>, 更新的字段
   {
     upsert: <boolean>,
     multi: <boolean>,
     writeConcern: <document>
   }
)
```

```
全局修改，这一条数据只剩下title字段
db.col.update({'title':'MongoDB 教程'},{'title':'MongoDB'})

局部修改，只修改这个字段，但是只修改查到的第一条数据
db.col.update({'title':'MongoDB 教程'},{$set:{'title':'MongoDB'}})

批量修改  修改所有查到title为mongoDB的条目
db.col.update({'title':'MongoDB 教程'},{$set:{'title':'MongoDB'}},{multi:true})

某列数据在原有值基础上增加或减少
db.col.update({by:"lin"},{$inc:{likes:1}})
```

###### 删除文档

```
db.collection.remove(
   <query>,
   <justOne>
)
```

```
db.col.remove({title:'MongoDB 教程'})
全删
db.col.remove({})
```

###### 分页查询

```
db.colletion.count(query,可选)  countDocuments()  estimatedDocumentCount()

db.col.count()
db.col.count({title:'lin 教程'})
```

```
db.col.find().limit(2).skip(2)
db.col.find({},{likes:1}).sort({likes:1})    1升序，2降序   默认是以_id排序

只展示likes字段，_id是必查出来展示的
db.col.find({},{likes:1})
```

###### 正则复杂查询

查找包含 w3cschool.cn 字符串

```
db.posts.find({post_text:{$regex:"w3cschool.cn"}})
db.posts.find({post_text:{$regex:"w3cschool.cn",$options:"$i"}})  不区分大小写

也可以下面
db.posts.find({post_text:/w3cschool.cn/})

查询以lin开头
db.col.find({title:/^lin/})
```

###### 比较查询

```
小于  db.col.find({likes:{$lt:50}})
小于等于  db.col.find({likes:{$lte:50}})	
大于  db.col.find({"likes":{$gt:50}})
大于等于 db.col.find({"likes":{$gte:50}})
不等于   db.col.find({"likes":{$ne:50}})
```

###### 包含查询 in

```
db.col.find({likes:{$in:[99,100]}})
db.col.find({likes:{$nin:[100]}})
```

###### 条件连接查询

```java
and 默认就是$and所以不用写$and也行
db.col.find({$and:[{"by":"w3cschool"},{"title": "MongoDB 教程"}]})

and + or SQL 语句为： 'where likes>50 AND (by = 'w3cschool' OR title = 'MongoDB 教程')'
db.col.find({"likes": {$gt:50}, $or: [{"by": "w3cschool"},{"title": "MongoDB 教程"}]})
```

#### 四、索引

mongoDB  B-TREE             mysql B+ Tree

##### 1.索引类型

单字段索引       

复合索引

其他索引：地理空间索引，文本索引（了解这个效率不高，推荐es）,哈希索引

##### 2.索引操作

###### **查看索引**

```
db.col.getIndexes()
```

###### **创建索引**

```
db.col.createIndex({likes:1})    // 1代表升序

db.col.createIndex({likes:1,.....})  // 复合索引
```

###### **删除索引**

```
db.col.dropIndex({likes:1}) // 删除likes升序的索引
db.col.dropIndex({"likes_1"})  // 根据名称去删除
db.col.dropIndexes() // 删除所有索引
```

##### 3.索引的使用

###### 执行计划

```
db.col.find({likes:99}).explain()
```

可以用compass客户端分析推荐

索引像mysql一样有**覆盖索引**

```
db.col.find({likes:99},{likes:1,_id:0}) // 只查询索引字段 likes
```

#### 五、mongoDB-springdata

**mongodb-driver**    相当于jdbc驱动

**springdataMongoDB**, 封装了mongodb-driver  

##### 1. 环境搭建

pom

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
    </dependencies>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
    </parent>
```

springdatamongodb 

两种方式对数据库操作 ： 

第一种方式，直接继承MongoRepository接口，其最终将会继承Repository标记接口，我们可以不必自己写实现类，轻松实现增删改查、分页、排序操作，但是对于比较复杂的查询，使用起来就比较费力。

第二种方式，直接使用mongoTemplate，这需要自己写实现类，但是这样增删改查可以自己控制，对于复杂查询，用起来得心应手。

##### 2. 继承MongoRepository接口

SimpleMongoRepository 继承MongoRepository实现了一些基本操作

yaml

```
spring:
  data:
    mongodb:
      host: 127.0.0.1
      database: articledb
      port: 27017 # 默认就是
```

```
@Document(collation = "comment") // 执行文档对应的集合
//@CompoundIndex(def = "{'userid':1,'nickname':-1}") // 复合索引,推荐命令行建
public class Comment implements Serializable {
    // 代表_id
    @Id
    private String id;

    private String content;

    @Indexed
    private String userId;

    private String nickname;

    private Integer likes;
}
```

```
db.comment.insertMany([{
    content: '太赞了',
    userId: '222',
    nickname: 'leo',
    likes: 100
},{
    content: '太赞了',
    userId: '111',
    nickname: 'lin',
    likes: 99
}])
```

```
/**
 * 第二个 ID 的类型
 */
public interface CommentRepo extends MongoRepository<Comment, String> {

    /*
    类似jpa的形式
     */
    Page<Comment> findCommentByUserId(String userId, Pageable pageable);
}
```

```
    @Test
    public void insert() {
        Comment comment=new Comment();
        comment.setContent("spring data insert");
        comment.setLikes(10);
        comment.setUserId("12345");
        comment.setNickname("nick name");
        comment.setId("1234");
        commentRepo.insert(comment);
        Comment comment2=new Comment();
        comment2.setContent("spring data insert");
        comment2.setLikes(199);
        comment2.setUserId("123456789");
        comment2.setNickname("nick name22");
        // 不指定ID，mongoDB会自动生成ObjectID
        commentRepo.insert(comment2);
    }

    @Test
    public void queryList() {
        List<Comment> comments = commentRepo.findAll();
        comments.forEach(comment-> System.out.println(comment.toString()));
    }
    
        @Test
    public void querySingle() {
        Comment comment = commentRepo.findById("6332a266b2aaef383206d6f6").orElseGet(null);
        System.out.println(comment);
    }

    @Test
    public void queryPage() {
        Page<Comment> page = commentRepo.findCommentByUserId("12345", PageRequest.of(0, 10));
        System.out.println(page.getTotalElements());
        System.out.println(page.getContent());
    }
```

##### 3.mongoTemplate实现

template意思就是直接执行命令行，效率更高，在java代码里写

如查询出来更新的场景，repo需要两次， template调用updata即可，减少IO操作

```
@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoTemplateTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void queryPage() {
        // 查询条件，更新条件，集合:字符串名称或类名
        Query query = Query.query(Criteria.where("_id").is("1234"));
        Update update = new Update();
        update.inc("likes");
        update.set("comment", "更新后的");
        long modifiedCount = mongoTemplate.updateFirst(query, update, Comment.class).getModifiedCount();
        System.out.println(modifiedCount);
    }
}
```

##### 4. 遇到题

1. MongoDB 查询异常：

Query failed with error code 2 and error message ‘Field ‘locale’ is invalid

```
检查实体类
//正确的
@Document(collection  = "user")
//导致错误的
@Document(collation  = "user")
```

#### 六、副本集

在副本集中，当主库遇到问题，无法继续提供服务的时候，副本集将选举一个新的主库继续提供服务。

副本节点/仲裁节点（不读写数据）

集群搭建可参考

https://blog.csdn.net/Mr_YanMingXin/article/details/125551491

**故障测试**

副本节点挂，再启动会再次去同步主节点。

主节点挂，选举其他节点

java连接方式

```
spring:
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27017,123.76.127.123/27018/articledb?connect=replicaSet&slaveOk=true&replicaSet=myrs
      
slaveOk=true  自动读写分离   集群连接的时候只需要连接路由节点集群就可以
```

#### 七、分片集群

可参考搭建：https://blog.csdn.net/bxg_kyjgs/article/details/125784629

请求  》  路由  》 config servers(调度的配置，配置服务器集群的元数据和配置)   》副本集（每个分片也算一个副本（存储））

副本集里包括了  一个主分片，多个分片副本，一个仲裁节点

 分片副本集：可以设置多个分片副本，一个主分片副本，一个仲裁副本

基础组件
其利用到了四个组件：mongos，config server，shard，replica set

**mongos**
 数据库集群请求的入口，所有请求需要经过mongos进行协调，无需在应用层面利用程序来进行路由选择，mongos其自身是一个请求分发中心，负责将外部的请求分发到对应的shard服务器上，mongos作为统一的请求入口，为防止mongos单节点故障，一般需要对其做HA（高可用，Highly Available缩写）。

**config server**
 配置服务器，存储所有数据库元数据（分片，路由）的配置。mongos本身没有物理存储分片服务器和数据路由信息，只是缓存在内存中来读取数据，mongos在第一次启动或后期重启时候，就会从config server中加载配置信息，如果配置服务器信息发生更新会通知所有的mongos来更新自己的状态，从而保证准确的请求路由，生产环境中通常也需要多个config server，防止配置文件存在单节点丢失问题。

**shard**
 在传统意义上来讲，如果存在海量数据，单台服务器存储1T压力非常大，考虑到数据库的硬盘，网络IO，还有CPU，内存的瓶颈，如果多台进行分摊1T的数据，到每台上就是可估量的较小数据，在mongodb集群只要设置好分片规则，通过mongos操作数据库，就可以自动把对应的操作请求转发到对应的后端分片服务器上。

**replica set**
 在总体mongodb集群架构中，对应的分片节点，如果单台机器下线，对应整个集群的数据就会出现部分缺失，这是不能发生的，因此对于shard节点需要replica set来保证数据的可靠性，生产环境通常为2个副本+1个仲裁。

##### 分片策略

1.哈希

2.值范围策略

sh.shardCollection("articledb.comment",{"nickname":"hashed"})

有点类似mysql分库分表

#### 八、角色权限

admin库里面

db.createUser({user:"myroot",pwd:"123456",roles:[{role:"root"]})

db.createUser({user:"myroot",pwd:"123456",roles:[{role:"readWrite",db:"articledb"}]})

db.system.users.find()

测试是否可用，也是登录操作

db.auth("root","123456")

#### 九、安全认证

路由集群

```
有认证的话 ，用户密码 mongodb://root:123456@127.0.0.1:27017,123.76.127.123:27018/articledb?connect=replicaSet&slaveOk=true&replicaSet=myrs
```

副本集群安全认证

keyfile副本间通信

#### 十、参考链接

笔记

https://zhenye-na.github.io/blog/2020/01/27/intro-to-mongodb.html#51-mongoose-%E6%8F%90%E4%BE%9B%E7%9A%84%E6%96%B0%E5%AF%B9%E8%B1%A1%E7%B1%BB%E5%9E%8B



https://zhenye-na.github.io/blog/2020/02/07/advanced-mongodb.html#2-%E5%88%86%E7%89%87%E9%9B%86%E7%BE%A4---sharded-cluster



w3school:

https://www.w3cschool.cn/mongodb/mongodb-objectid.html
