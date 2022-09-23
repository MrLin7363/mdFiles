

学习视频  https://www.bilibili.com/video/BV1CL4y157ie?p=9&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b



### 分库分表

sharding-jdbc

### 读写分离

![image-20220905143839834](asserts/image-20220818111424820.png)

数据库(不同IP)->不同database(同一个IP下/不同IP下也行)->不同表





官方文档下载地址： https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-jdbc-quick-start/

实践案例：https://github.com/yudiandemingzi/spring-boot-sharding-sphere

2、基于客户端/服务器端实现分表分库区别

数据库中间件：mycat或者ShardingJDBC

基于服务器端-mycat：

优点：归并数据结果是完全解耦合，保证数据的安全性

缺点：效率偏低

基于客户端-ShardingJDBC

优点：效率高

缺点：归并数据结果没有实现解耦合，有可能会影响业务逻辑

原理：基于aop代理方式拦截改写sql

例如：select * from user -> select * from user_0, select * from user_1

3、单表达到多少数据量开始分表分库

【推荐】单表行数超过 500 万行或者单表容量超过 2GB，才推荐进行分库分表。 说明：如果预计三年后的数据量根本达不到这个级别，请不要在创建表时就分库分表。

4、数据库分表分库策略有哪些

取余/取模

按照范围分片

按照日期进行分片

按照月份进行分片

按照枚举进行分片

二进制取模范围分片

一致性hash分片

按照目标字段前缀指定的进行分区

按照前缀ASCII码和值进行取模范围分片



实战案例

https://www.cnblogs.com/lvxueyang/p/15006825.html

### 广播表

用广播表
在使用中，有些表没必要做分片，例如字典表、省份信息等，因为他们数据量不大，而且这种表可能需要与海量数据的表进行关联查询。

插入时，向所有数据源广播发送sql语句查询时，只查询其中的一个数据源



比如只有订单啥的数据分库，大部分数据在一个主库，这时候主库需要这个表，读库也需要这个表，就要设置为广播表

