

学习视频  https://www.bilibili.com/video/BV1CL4y157ie?p=9&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b

中文手册-这版本是3.x过时了，培训机构的：https://shardingsphere.apache.org/document/legacy/3.x/document/cn/manual/

可以选版本的手册current，每个版本差别挺大：https://shardingsphere.apache.org/document/

最新官方手册5.1.2：https://shardingsphere.apache.org/document/5.1.2/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/mode/

源码地址：https://github.com/apache/shardingsphere

源码开发进展：https://github.com/apache/shardingsphere/releases

### 分库分表

sharding-jdbc

![在这里插入图片描述](https://img-blog.csdnimg.cn/6cea23748854491a8307a8ef353fce77.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5rKu5Lin55qE5Y2X55Oc,size_14,color_FFFFFF,t_70,g_se,x_16)

很显然，ShardingJDBC只是客户端的一个工具包，可以理解为一个特殊的JDBC驱动包，**所有分库分表逻辑均由业务方自己控制**，所以他的功能相对灵活，支持的数据库也非常多，**但是对业务侵入大，需要业务方自己定制所有的分库分表逻辑**。

而**ShardingProxy是一个独立部署的服务，对业务方无侵入**，业务方可以像用一个普通的MySQL服务一样进行数据交互，基本上感觉不到后端分库分表逻辑的存在，但是这也意味着功能会比较固定，能够支持的数据库也比较少。这两者各有优劣。类似mycat

### 读写分离

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



4.x 实战案例

https://www.cnblogs.com/lvxueyang/p/15006825.html

### 广播表

用广播表
在使用中，有些表没必要做分片，例如字典表、省份信息等，因为他们数据量不大，而且这种表可能需要与海量数据的表进行关联查询。

插入时，向所有数据源广播发送sql语句查询时，只查询其中的一个数据源



比如只有订单啥的数据分库，大部分数据在一个主库，这时候主库需要这个表，读库也需要这个表，就要设置为广播表

### 绑定表

指分片规则一致的一组分片表。 使用绑定表进行多表关联查询时，必须使用分片键进行关联，否则会出现笛卡尔积关联或跨库关联，从而影响查询效率。 例如：`t_order` 表和 `t_order_item` 表，均按照 `order_id` 分片，并且使用 `order_id` 进行关联，则此两张表互为绑定表关系。 绑定表之间的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升

```
rules:
  sharding:
    binding-tables:
      - "tablename"
```

### 分片策略

五、sharding-jdbc 分片策略分片策略
包含分片键和分片算法，由于分片算法的独立性，将其独立抽离。真正可用于分片操作的是分片键 + 分片算法，也就是分片策略。目前提供5种分片策略。

#### 标准分片策略

对应**StandardShardingStrategy**。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。StandardShardingStrategy只支持单分片键，提供**PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法**。PreciseShardingAlgorithm是必选的，用于处理=和IN的分片。RangeShardingAlgorithm是可选的，用于处理BETWEEN AND, >, <, >=, <=分片，如果不配置RangeShardingAlgorithm，SQL中的BETWEEN AND将按照全库路由处理。

PreciseShardingAlgorithm 一般情况用于插入时

#### 复合分片策略

对应**ComplexShardingStrategy**。复合分片策略。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。ComplexShardingStrategy支持多分片键，由于多分片键之间的关系复杂，因此并未进行过多的封装，而是直接将分片键值组合以及分片操作符透传至分片算法，完全由应用开发者实现，提供最大的灵活度。

#### 行表达式分片策略

对应InlineShardingStrategy。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，只支持单分片键。对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，如: t_user_$->{u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

#### Hint分片策略

用与SQL无关的方式进行分片

对应HintShardingStrategy，通过Hint指定分片值而非从SQL中提取分片值的方式进行分片的策略

不分片策略
对应NoneShardingStrategy，不分片的策略



### 行表达式

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

一个月分两张表

```
ds1.table_$->{2020..2030}$->{(1..12).collect{t ->t.toString().padLeft(2,'0')}}${['01', '16']}
```

### 4.x 源码

#### 1. 初始化后如何代码获取配置

SpringBootConfiguration 实现了bean并将yaml的shardingjdbc的配置读入，初始化了ShardingDataSource

```
@Component
public class StatisticConfig {
    private static final Map<String, List<String>> TABLES_MAP = new HashMap<>();

    @Resource
    private ShardingDataSource shardingDataSource; // 5.x ShadingsphereDataSource

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

ShardingRouteDecorator.decorate() -> RouteContext ->BasePrepareEngine.prepare -> **SQLRewriteEntry.createSQLRewriteContext()**

->SQLRouteRewriteEngine.rewrite() -> AbstractSQLBuilder.toSQL()

#### 3. 插入操作

BasePrepareEngine.executeRewrite

#### 4.报错积累

```
java.lang.NullPointerException: null
	at org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSet.getString(ShardingResultSet.java:160) ~[sharding-jdbc-core-4.1.1.jar:4.1.1]
```

一般由于字段 _下划线没有自动转驼峰问题

```
数据表中有一个 system 的字段，是mysql的关键字，如果走shardingjdbc的数据操作，就会报空指针异常，解决方法是在涉及到system字段的数据操作接口加上``进行转义
```

#### 5. 问题

1、自定义子查询无法分表，还是照原SQL表名执行，尽量不进行子查询。

（1）使用join关联查询，或者单表查询，或者利用jdbc的algorithm获取到哪些表，自己传入表名执行原生SQL

（2）重写AbstractSQLBuilder这个方法，加入额外的业务逻辑

2、数据库字段名和sharding jdbc关键字重名，就会报“no viable alternative at input”

使用mybatis的xml重新编写sql语句，重名的字段名加上`` 这个符号。

3、数据源作为shardingjdbc的，会启动mysql 8 默认校验器

### 解决方案

1. 时间范围分表， 如果要加上分库，可以考虑ID啥的分库，即每个库都有相同时间范围的表，减少每个表的压力，同时把压力分散到多个机器

### 最新版本5.x

手册：https://shardingsphere.apache.org/document/5.1.2/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/mode/

YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-tables.yaml"));

#### springboot配置相关

```
spring.shardingsphere.mode.type= # 运行模式类型。可选配置：Memory、Standalone、Cluster
spring.shardingsphere.mode.repository= # 持久化仓库配置。Memory 类型无需持久化
spring.shardingsphere.mode.overwrite= # 是否使用本地配置覆盖持久化配置
```

```
spring:
  application:
    name: lin-spring-boot
  jpa:
    open-in-view: false
    show-sql: false
    database: mysql
  shardingsphere:
    mode:
      type: Memory
    datasource:
      names: ds1
      ds1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: org.mariadb.jdbc.Driver
        jdbc-url: jdbc:mariadb:......
        username: ${MYSQL_USER}
        password: ${MYSQL_PWD}
    rules:
      sharding:
        tables:
          proxy_statistic_new:
            actual-data-nodes: ds1.table_$->{2023..2033}$->{(1..12).collect{t ->t.toString().padLeft(2,'0')}}${['01', '16']}
            table-strategy:
              standard:
                sharding-column: proxy_start
                sharding-algorithm-name: tableAlgorithm
        sharding-algorithms:
          tableAlgorithm:
            type: CLASS_BASED
            props:
              strategy: standard
              # 自定义标准分配算法
              algorithmClassName: com.lin.algorithm.tableAlgorithm
    props:
      sql-show: true
```

#### pom

```
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>5.1.2</version>
</dependency>
```

```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.11</version>
    <relativePath />
</parent>

<dependencies>
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
</dependencies>
```

#### spring bean

以下是框架加载后可以从容器获取的，可以通过InlineExpressionParser获取所有表名

shardingSphereAutoConfiguration-》

shardingSphereDataSource-》 

yamlShardingRuleSpringBootConfiguration-》作为bean加载配置文件信息到这个类

shardingRuleSpringBootConfiguration-》 配置类的信息转换为rule等

ShardingRuleAlgorithmProviderConfigurationYamlSwapper-》  转换

algorithmProvidedShardingRuleConfiguration  转换成rule

```
@Resource
private ShardingSphereDataSource shardingSphereDataSource;

@Resource
private ShardingSphereAutoConfiguration shardingSphereAutoConfiguration;

@Resource
private AlgorithmProvidedShardingRuleConfiguration algorithmProvidedShardingRuleConfiguration; // 获取$ 表 表达式

// 加载配置相关的初始化器，再加载完shardingSphereAutoConfiguration后
@Resource
private ShardingRuleSpringBootConfiguration shardingRuleSpringBootConfiguration;

@Resource
private YamlShardingRuleSpringBootConfiguration yamlShardingRuleSpringBootConfiguration;

```

#### 源码

源码到github上下zip包，下来后 pom.xml  add as maven project即可

调试技巧之一，类的构造方法打个断点，可以看到类什么时候初始化

##### 1. 启动流程

###### ShardingSphereAutoConfiguration

这里采用实现EnvironmentAware接口来将Envieronment对象回调到本配置类中，从yaml中读取配置创建连接，然后利用shardingjdbc的工具类创建一个datasource bean对象给orm框架（这里是mybatis plus）使用.

ShardingSphereAutoConfiguration.setEnvironment()  设置环境

```
-> ShardingSphereAutoConfiguration.DataSourceMapSetter.getDataSourceMap(environment)  获取数据源
-> ShardingSphereAutoConfiguration.shardingSphereDataSource
.... 初始化各种规则

-> ShardingSphereAutoConfiguration.shardingSphereDataSource   初始化规则后，初始化shardingDataSource
-> ShardingSphereDataSourceFactory.createDataSource
-> new ShardingSphereDataSource  初始化
-> ShardingSphereDataSource.this.createContextManager  创建上下文，创建表元数据
-> MemoryContextManagerBuilder.build(ContextManagerBuilderParameter parameter)    mod是Memory策略
-> MetaDataContextsBuilder.build  this.getDatabases  getGenericDatabases  创建数据库信息ShardingSphereDatabase里面包含rule+表名
-> ShardingSphereDatabase.create
-> DatabaseRulesBuilder.build(name, databaseConfig, props)  创建表rule  Collection<ShardingSphereRule>

->  ShardingRuleBuilder.new ShardingRule()   创建分片表
->  ShardingRule.this.createTableRules  创建所有表
->  TableRule.this.createTableRules 
->  TableRule. (new InlineExpressionParser(tableRuleConfig.getActualDataNodes())).splitAndEvaluate()  根据表达式解析出所有表名
->  InlineExpressionParser  解析表达式的类

-> SingleTableRuleBuilder.new SingleTableRule()  创建不分片的单表
```

RuleConfiguration 的默认初始化是 AlgorithmProvidedShardingRuleConfiguration

```
public DataSource shardingSphereDataSource(ObjectProvider<List<RuleConfiguration>> rules, ObjectProvider<ModeConfiguration> modeConfig) throws SQLException {
	// 下面这行的执行会初始化RuleConfiguration,会取初始化AlgorithmProvidedShardingRuleConfiguration,打个构造方法的端点就可以看到
	// 第2点会分析这一行，这里分析最后一行return
    Collection<RuleConfiguration> ruleConfigs = (Collection)Optional.ofNullable((List)rules.getIfAvailable()).orElseGet(Collections::emptyList);
    return ShardingSphereDataSourceFactory.createDataSource(this.databaseName, (ModeConfiguration)modeConfig.getIfAvailable(), this.dataSourceMap, ruleConfigs, this.props.getProps());
}
```

###### ShardingRule

```
public ShardingRule(AlgorithmProvidedShardingRuleConfiguration config, Collection<String> dataSourceNames) {
	// 获取数据库集合 ds1
    this.dataSourceNames = this.getDataSourceNames(config.getTables(), config.getAutoTables(), dataSourceNames);
    // 获取算法
    this.shardingAlgorithms.putAll(config.getShardingAlgorithms());
    // 主键生成策略
    this.keyGenerators.putAll(config.getKeyGenerators());
    // 生成所有表
    this.tableRules.putAll(this.createTableRules(config.getTables(), config.getDefaultKeyGenerateStrategy()));
    this.tableRules.putAll(this.createAutoTableRules(config.getAutoTables(), config.getDefaultKeyGenerateStrategy()));
    // 绑定表
    this.bindingTableRules.putAll(this.createBindingTableRules(config.getBindingTableGroups(), config.getTables(), config.getAutoTables(), this.tableRules));
    // 广播表
    this.broadcastTables = this.createBroadcastTables(config.getBroadcastTables());
    // 分库策略
    this.defaultDatabaseShardingStrategyConfig = (ShardingStrategyConfiguration)(null == config.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultDatabaseShardingStrategy());
    // 分表策略
    this.defaultTableShardingStrategyConfig = (ShardingStrategyConfiguration)(null == config.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultTableShardingStrategy());
    // 主键生成算法
    this.defaultKeyGenerateAlgorithm = null == config.getDefaultKeyGenerateStrategy() ? KeyGenerateAlgorithmFactory.newInstance() : (KeyGenerateAlgorithm)this.keyGenerators.get(config.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
    // 分表字段
    this.defaultShardingColumn = config.getDefaultShardingColumn();
    // 每个DS下有哪些节点表 Map<String, Collection<DataNode>> 
    this.shardingTableDataNodes = this.createShardingTableDataNodes(this.tableRules);
    ......
}
```

###### TableRule

获取所有的表 DataNode

```
public TableRule(ShardingTableRuleConfiguration tableRuleConfig, Collection<String> dataSourceNames, String defaultGenerateKeyColumn) {
    this.replaceTablePrefix = tableRuleConfig.getReplaceTablePrefix();
    // 分表的表  table
    this.logicTable = Strings.isNullOrEmpty(this.replaceTablePrefix) ? tableRuleConfig.getLogicTable() : this.replaceTablePrefix + tableRuleConfig.getLogicTable();
    // 所有的nodes  如 table_20230601 ....
    List<String> dataNodes = (new InlineExpressionParser(tableRuleConfig.getActualDataNodes())).splitAndEvaluate();
    this.dataNodeIndexMap = new HashMap(dataNodes.size(), 1.0F);
    // 所有的nodes解析设置到属性里   解析node表达式
    this.actualDataNodes = this.isEmptyDataNodes(dataNodes) ? this.generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames, this.replaceTablePrefix) : this.generateDataNodes(dataNodes, dataSourceNames, this.replaceTablePrefix);
    // Set<表名>
    this.actualTables = this.getActualTables();
    ......
}
```

###### InlineExpressionParser

解析如  ds1.table_$->{2023..2033}$->{(1..12).collect{t ->t.toString().padLeft(2,'0')}}${['01', '16']}

**使用源码解析获取表名，自己实现自动建表等操作**

```
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration ruleConfiguration;

  List<String> tableList = new InlineExpressionParser(
            ruleConfiguration.getTables().stream().findFirst().get().getActualDataNodes()).splitAndEvaluate();

        List<String> tableNameList = tableList.stream()
            .map(t -> t.substring(t.indexOf('.') + 1))
            .collect(Collectors.toList());
```

##### 2. 获取配置文件解析RuleConfigration

(1)

```
public DataSource shardingSphereDataSource(ObjectProvider<List<RuleConfiguration>> rules, ObjectProvider<ModeConfiguration> modeConfig) throws SQLException {
	// 下面这行的执行会初始化RuleConfiguration,会取初始化AlgorithmProvidedShardingRuleConfiguration,打个构造方法的端点就可以看到
    Collection<RuleConfiguration> ruleConfigs = (Collection)Optional.ofNullable((List)rules.getIfAvailable()).orElseGet(Collections::emptyList);
```

(2) 加载bean  YamlShardingRuleConfiguration

(3) **shardingRuleSpringBootConfiguration**-》 配置类的信息转换为rule等，里面包含 YamlShardingRuleConfiguration属性

ShardingRuleAlgorithmProviderConfigurationYamlSwapper-》  转换

AlgorithmProvidedShardingRuleConfiguration转换成rule

AlgorithmProvidedShardingRuleConfiguration

##### SQL运行过程

###### **query单表查询**

```
ShardingSpherePreparedStatement.executeQuery()
->
ShardingSpherePreparedStatement.createExecutionContext(LogicSQL logicSQL).kernelProcessor.generateExecutionContext
->
KernelProcessor.generateExecutionContext     重要：重写SQL+打印日志
->
SQLRewriteResult rewriteResult = KernelProcessor.rewrite   返回SQLRewriteResult的时候表名啥的已经拼接完SQL
->
SQLRewriteEntry.rewrite
->
RouteSQLRewriteEngine.rewrite
->
RouteSQLRewriteEngine.rewrite this.aggregateRouteUnitGroups  获取路由组，以数据库如ds1为key Map<String, Collection<RouteUnit>>
```

ShardingSpherePreparedStatement

```
    public ResultSet executeQuery() throws SQLException {
        ShardingSphereResultSet result;
        label93: {
            ResultSet var2;
            try {
                if (!this.statementsCacheable || this.statements.isEmpty()) {
                    this.clearPrevious();
                    LogicSQL logicSQL = this.createLogicSQL();
                    this.trafficContext = this.getTrafficContext(logicSQL);
                    // 创建执行SQL上下文，这里面解析SQL
                    this.executionContext = this.createExecutionContext(logicSQL);
                    if (this.executionContext.getRouteContext().isFederated()) {
                        ResultSet var11 = this.executeFederationQuery(logicSQL);
                        return var11;
                    }
					// 真正执行SQL的地方
                    List<QueryResult> queryResults = this.executeQuery0();
                    // 流式归并 
                    MergedResult mergedResult = this.mergeQuery(queryResults);
                    result = new ShardingSphereResultSet(this.getShardingSphereResultSet(), mergedResult, this, this.executionContext);
                    break label93;
                }

                this.resetParameters();
                var2 = ((PreparedStatement)this.statements.iterator().next()).executeQuery();
            } catch (SQLException var8) {
                this.handleExceptionInTransaction(this.connection, this.metaDataContexts);
                throw var8;
            } finally {
                this.clearBatch();
            }

            return var2;
        }

        this.currentResultSet = result;
        return result;
    }
```



KernelProcessor

```
public ExecutionContext generateExecutionContext(LogicSQL logicSQL, ShardingSphereDatabase database, ConfigurationProperties props) {
	// 获取路由上下文
    RouteContext routeContext = this.route(logicSQL, database, props);
    // 改写SQL
    SQLRewriteResult rewriteResult = this.rewrite(logicSQL, database, props, routeContext);
    // 创建执行SQL上下文
    ExecutionContext result = this.createExecutionContext(logicSQL, database, routeContext, rewriteResult);
    // 打印shardingsphere日志
    this.logSQL(logicSQL, props, result);
    return result;
}
```

RouteSQLRewriteEngine

```
  public RouteSQLRewriteResult rewrite(SQLRewriteContext sqlRewriteContext, RouteContext routeContext) {
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new LinkedHashMap(routeContext.getRouteUnits().size(), 1.0F);
        // 获取路由组，以数据库如ds1为key Map<String, Collection<RouteUnit>>
        Iterator var4 = this.aggregateRouteUnitGroups(routeContext.getRouteUnits()).entrySet().iterator();
		// 迭代路由，改写SQL
        while(var4.hasNext()) {
            Entry<String, Collection<RouteUnit>> entry = (Entry)var4.next();
            Collection<RouteUnit> routeUnits = (Collection)entry.getValue();
            // 判断是否需要改写SQL，如果包含子查询等就不改写SQL，新版本的优势
            if (this.isNeedAggregateRewrite(sqlRewriteContext.getSqlStatementContext(), routeUnits)) {
                sqlRewriteUnits.put((RouteUnit)routeUnits.iterator().next(), this.createSQLRewriteUnit(sqlRewriteContext, routeContext, routeUnits));
            } else {
                this.addSQLRewriteUnits(sqlRewriteUnits, sqlRewriteContext, routeContext, routeUnits);
            }
        }
		// 返回改写后的SQL
        return new RouteSQLRewriteResult(this.translate(sqlRewriteContext.getSqlStatementContext().getSqlStatement(), sqlRewriteUnits));
    }
```

###### query多表流式归并查询

```
ShardingSpherePreparedStatement.executeQuery()
->
ShardingSpherePreparedStatement.createExecutionContext(LogicSQL logicSQL).kernelProcessor.generateExecutionContext
......
这里创建和单表查询一样
->
ShardingSpherePreparedStatement   List<QueryResult> queryResults = this.executeQuery0();    执行SQL
->
ShardingSpherePreparedStatement   MergedResult mergedResult = this.mergeQuery(queryResults);    流式归并SQL
->
MergeEngine.merge().executeMerge()
->
ShardingResultMergerEngine.newInstance()
->
ShardingDQLResultMerger.merge()
->
ShardingDQLResultMerger MergedResult mergedResult = this.build();
->
ShardingDQLResultMerger.getGroupByMergedResult()
->
MemoryMergedResult.MemoryMergedResult
->
GroupByMemoryMergedResult.init
->
GroupByMemoryMergedResult.getValueCaseSensitive  getValueCaseSensitiveFromTables
```

###### 更新插入操作

ShardingSpherePreparedStatement.executeUpdate()

###### 元数据初始化过程

ShardingSphereSchema初始化过程

```
createContextManager, ShardingSphereDataSource
getDatabases:69, MetaDataContextsBuilder
create:75, ShardingSphereDatabase
build:80, GenericSchemaBuilder   loadSchemas  会去查数据库校验第一张表是否存在  如 order_20230101
decorate:114, GenericSchemaBuilder   	ShardingSchemaMetaDataDecorator.decorate  会把order_20230101按TableRule转换为 order逻辑表名
convertToSchemaMap:124, GenericSchemaBuilder
<init>:43, ShardingSphereSchema    初始化表信息  逻辑表名order放入map
```

### 踩坑记录

#### 1. 涉及到多张表的流式归并获取不到表

ShardingSphereSchema，这张是记录表名等信息

订单表分了10年，每个月两张表

```
ds1.order_$->{2023..2033}$->{(1..12).collect{t ->t.toString().padLeft(2,'0')}}
```

当涉及到流式归并时，会去schema取分表的规则ShardingSphereTable

GroupByMemoryMergedResult.getValueCaseSensitiveFromTables-> schema.get("order")

而此时的ShardingSphereSchema并不包含order 表，所以报空指针异常，我觉得这个地方应该报错提示一些信息：比如未建立分表规则的第一张表

ShardingSphereSchema的初始化会去检查分表规则的第一张表是否建立，如果建立了才加入 分表名称

如 order_202301 如果存在，schema才加入 order 表

官网这个属性check-table-metadata-enabled 默认是flase，如果是true会检查所有的表，但是会检查第一张表

| check-table-metadata-enabled (?) | boolean | 在程序启动和更新时，是否检查分片元数据的结构一致性 | false |
| -------------------------------- | ------- | -------------------------------------------------- | ----- |
| sql-show (?)                     | boolean |                                                    |       |

##### **解决方案：**

由于从3月份开始分表，所以一开始未建立**order_202301，建表**即可

在shardingjdbc 4 没有这个问题

GenericSchemaBuilder

```
GenericSchemaBuilder

private static Map<String, SchemaMetaData> loadSchemas(Collection<String> tableNames, GenericSchemaBuilderMaterials materials) throws SQLException {
	// 这个常量就是check-table-metadata-enabled
    boolean isCheckingMetaData = (Boolean)materials.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
    Collection<SchemaMetaDataLoaderMaterials> schemaMetaDataLoaderMaterials = SchemaMetaDataUtil.getSchemaMetaDataLoaderMaterials(tableNames, materials, isCheckingMetaData);
    return schemaMetaDataLoaderMaterials.isEmpty() ? Collections.emptyMap() : SchemaMetaDataLoaderEngine.load(schemaMetaDataLoaderMaterials, materials.getStorageType());
}


if (checkMetaDataEnable) {
    addAllActualTableDataNode(materials, dataSourceTableGroups, dataNodes, each);
} else {
	// 会添加第一张表
    addOneActualTableDataNode(materials, dataSourceTableGroups, dataNodes, each);
}
```

```
SchemaMetaDataLoaderEngine  此方法

public static Map<String, SchemaMetaData> load(Collection<SchemaMetaDataLoaderMaterials> materials, DatabaseType databaseType) throws SQLException {
   		.....
        return loadByDefault(materials, databaseType);
}

// 关于数据库
            while(var6.hasNext()) {
                String tableName = (String)var6.next();
                Optional var10000 = TableMetaDataLoader.load(each.getDataSource(), tableName, databaseType);
                Objects.requireNonNull(result);
                var10000.ifPresent(result::add);
            }
```

#### 2. 解析获取表名

```
@Component
public class ShardingData {
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration ruleConfiguration;

    /**
     * init tables
     */
    @PostConstruct
    public void initTables() {
        Map<String, List<String>> tablesMap = new HAshMap();
        List<String> tableList = new InlineExpressionParser(
            ruleConfiguration.getTables().stream().findFirst().get().getActualDataNodes()).splitAndEvaluate();

        List<String> tableNameList = tableList.stream()
            .map(t -> t.substring(t.indexOf('.') + 1))
            .collect(Collectors.toList());
		
		// 每年24张表
        tableNameList.forEach(tableName -> {
            String year = tableName.substring(20, 24);
            if (!tablesMap.containsKey(year)) {
                tablesMap.put(year, new LinkedList<>());
            }
            tablesMap.get(year).add(tableName);
        });
    }
}
```

