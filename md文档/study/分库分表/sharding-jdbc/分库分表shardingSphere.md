

学习视频  https://www.bilibili.com/video/BV1CL4y157ie?p=9&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b

中文手册-极力推荐：https://shardingsphere.apache.org/document/legacy/3.x/document/cn/manual/

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

### 分片策略

五、sharding-jdbc 分片策略分片策略
包含分片键和分片算法，由于分片算法的独立性，将其独立抽离。真正可用于分片操作的是分片键 + 分片算法，也就是分片策略。目前提供5种分片策略。

标准分片策略
对应StandardShardingStrategy。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。StandardShardingStrategy只支持单分片键，提供PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法。PreciseShardingAlgorithm是必选的，用于处理=和IN的分片。RangeShardingAlgorithm是可选的，用于处理BETWEEN AND, >, <, >=, <=分片，如果不配置RangeShardingAlgorithm，SQL中的BETWEEN AND将按照全库路由处理。

PreciseShardingAlgorithm 一般情况用于插入时

复合分片策略
对应ComplexShardingStrategy。复合分片策略。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。ComplexShardingStrategy支持多分片键，由于多分片键之间的关系复杂，因此并未进行过多的封装，而是直接将分片键值组合以及分片操作符透传至分片算法，完全由应用开发者实现，提供最大的灵活度。

行表达式分片策略
对应InlineShardingStrategy。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，只支持单分片键。对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，如: t_user_$->{u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

Hint分片策略
对应HintShardingStrategy。通过Hint指定分片值而非从SQL中提取分片值的方式进行分片的策略。

不分片策略
对应NoneShardingStrategy。不分片的策略。



### 问题

1、自定义子查询无法分表，还是照原SQL表名执行，尽量不进行子查询。

（1）使用join关联查询，或者单表查询，或者利用jdbc的algorithm获取到哪些表，自己传入表名执行原生SQL

（2）重写AbstractSQLBuilder这个方法，加入额外的业务逻辑

2、数据库字段名和sharding jdbc关键字重名，就会报“no viable alternative at input”

使用mybatis的xml重新编写sql语句，重名的字段名加上`` 这个符号。

3、只有SQL包含指定的表，才会启动sharding-jdbc，但是数据源作为shardingjdbc的，还是会启动mysql 8 默认校验器

#### 4. 行表达式

行表达式的使用非常直观，只需要在配置中使用`${ expression }`或`$->{ expression }`标识行表达式即可。 目前支持数据节点和分片算法这两个部分的配置。行表达式的内容使用的是Groovy的语法，Groovy能够支持的所有操作，行表达式均能够支持。例如：

`${begin..end}`表示范围区间

`${[unit1, unit2, unit_x]}`表示枚举值

行表达式中如果出现连续多个`${ expression }`或`$->{ expression }`表达式，整个表达式最终的结果将会根据每个子表达式的结果进行笛卡尔组合。

例如，以下行表达式：

```
${['online', 'offline']}_table${1..3}
->
online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3
```

```
db$->{0..1}.t_order$->{0..1}
->
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

#### 6.遇见难题

springboot启动后加载yaml文件后如何获取已经初始化的所有表，通过看源码的方式知道了ShardingDataSource

#### 7.报错积累

```
java.lang.NullPointerException: null
	at org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSet.getString(ShardingResultSet.java:160) ~[sharding-jdbc-core-4.1.1.jar:4.1.1]
```

一般由于字段 _下划线没有自动转驼峰问题

```
数据表中有一个 system 的字段，是mysql的关键字，如果走shardingjdbc的数据操作，就会报空指针异常，解决方法是在涉及到system字段的数据操作接口加上``进行转义
```

### 源码

#### 1. 初始化后如何代码获取配置

SpringBootConfiguration 实现了bean并将yaml的shardingjdbc的配置读入，初始化了ShardingDataSource

```
@Component
public class StatisticConfig {
    private static final Map<String, List<String>> TABLES_MAP = new HashMap<>();

    @Resource
    private ShardingDataSource shardingDataSource;

    @PostConstruct
    public void init(){
        Collection<TableRule> tableRules = shardingDataSource.getRuntimeContext().getRule().getTableRules();
        TableRule rule = tableRules.stream().findFirst().orElse(null);
        rule.getActualDataNodes().forEach(table->{
            
        });
    }
}
```

#### 2. 解析SQL运行过程

ShardingRouteDecorator.decorate() -> RouteContext ->BasePrepareEngine.prepare -> SQLRewriteEntry.createSQLRewriteContext()

->SQLRouteRewriteEngine.rewrite() -> AbstractSQLBuilder.toSQL()

#### 3. 插入操作

BasePrepareEngine.executeRewrite

