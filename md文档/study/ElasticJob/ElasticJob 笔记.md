ElasticJob 笔记

官网： https://shardingsphere.apache.org/elasticjob/current/cn/features/elastic/

https://shardingsphere.apache.org/elasticjob/legacy/lite-2.x/02-guide/event-trace/    推荐

分片策略： https://blog.csdn.net/qq_37960603/article/details/122270721

shardingTotalCount 指定任务执行的分片，1片；否则任务会分到不同的服务器执行，这里就参考上面分片策略。

```
  jobs:
    UrlJob:
      cron: 0 * * * * ?
      shardingTotalCount: 1
      description: desc
      jobErrorHandlerType: LOG
      elasticJobClass: xxx.UrlJob
      disabled: false
	  overwrite: true  //false会更改不会覆盖之前已经注册的
```

### POM依赖

```
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-boot-starter</artifactId>
    <version>3.0.1</version>
</dependency>
```

### 积累点

#### 1. 本地启动不启动job

```
@SpringBootApplication(exclude = ElasticJobLiteAutoConfiguration.class)
```

### 源码解析3.0.1

启动springboot项目，Elastic 流程

ElasticJobBootstrapConfiguration.afterSingletonsInstantiated()

JobScheduler.JobScheduler  这是每一个注册的job都是一个Scehduler  JobScheduler.findTracingConfiguration会去找trace配置

JobScheduler里面的 LiteJobFacade->JobTracingEventBus->RDBTracingListener->RDBJobEventStorage

RDBJobEventStorage.RDBJobEventStorage 会去判断是否已经创建了两张事件记录表  job_execution_log/job_status_trace_log



监听数据插入事件表流程

RDBTracingListener.listen()

RDBJobEventStorage.preparedStatement.execute()   执行插入逻辑

ShardingPreparedStatement.executeUpdate().initPreparedStatementExecutor()

execute().prepare()    JobStatusTraceLog



插入事件表流程

RDBTracingListener.repository.addJobStatusTraceEvent    会插入状态先，如果分片不为空则执行下面的execution

RDBTracingListener.repository.addJobExecutionEvent



如果不设置 overwrite=true  后面再加上 tracing type =RDB 会注册不上去，原先的还是tracing type没有这个值

原因读取的jobConfig未包含 extraConfigurations



注意事项:

如果本地启动，而dev环境上已经有了elasticJob，则分片不一定分到本地机器上执行

### 参数解析

| 属性名                      | 类型    | 是否必填 | 缺省值 | 描述                                                         |
| --------------------------- | ------- | -------- | ------ | ------------------------------------------------------------ |
| id                          | String  | `是`     |        | 作业名称                                                     |
| class                       | String  | 否       |        | 作业实现类，需实现`ElasticJob`接口，脚本型作业不需要配置     |
| registry-center-ref         | String  | `是`     |        | 注册中心`Bean`的引用，需引用`reg:zookeeper`的声明            |
| cron                        | String  | `是`     |        | `cron`表达式，用于配置作业触发时间                           |
| sharding-total-count        | int     | `是`     |        | 作业分片总数                                                 |
| sharding-item-parameters    | String  | 否       |        | 分片序列号和参数用等号分隔，多个键值对用逗号分隔 分片序列号从`0`开始，不可大于或等于作业分片总数 如： `0=a,1=b,2=c` |
| job-parameter               | String  | 否       |        | 作业自定义参数 可以配置多个相同的作业，但是用不同的参数作为不同的调度实例 |
| monitor-execution           | boolean | 否       | true   | 监控作业运行时状态 每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。 每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。 |
| monitor-port                | int     | 否       | -1     | 作业监控端口 建议配置作业监控端口, 方便开发者dump作业信息。 使用方法: echo “dump” \| nc 127.0.0.1 9888 |
| max-time-diff-seconds       | int     | 否       | -1     | 最大允许的本机与注册中心的时间误差秒数 如果时间误差超过配置秒数则作业启动时将抛异常 配置为`-1`表示不校验时间误差 |
| failover                    | boolean | 否       | false  | 是否开启失效转移 仅`monitorExecution`开启，失效转移才有效    |
| misfire                     | boolean | 否       | true   | 是否开启错过任务重新执行                                     |
| job-sharding-strategy-class | String  | 否       | true   | 作业分片策略实现类全路径 默认使用平均分配策略 详情参见：[作业分片策略](http://dangdangdotcom.github.io/elastic-job/post/job_strategy) |
| description                 | String  | 否       |        | 作业描述信息                                                 |
| disabled                    | boolean | 否       | false  | 作业是否禁止启动 可用于部署作业时，先禁止启动，部署结束后统一启动 |
| overwrite                   | boolean | 否       | false  | 本地配置是否可覆盖注册中心配置 如果可覆盖，每次启动作业都以本地配置为准 |

## 二、控制台

源码  https://github.com/apache/shardingsphere-elasticjob-ui/tree/3.0.2

各个构建的源码:apache源码下载  https://archive.apache.org/dist/shardingsphere/elasticjob-3.0.2/



设计理念

1. 本控制台和Elastic Job并无直接关系，是通过读取Elastic Job的注册中心数据展现作业状态，或更新注册中心数据修改全局配置。

2. 控制台只能控制作业本身是否运行，但不能控制作业进程的启停，因为控制台和作业本身服务器是完全分布式的，控制台并不能控制作业服务器。

主要功能

1. 查看作业以及服务器状态

2. 快捷的修改以及删除作业设置

3. 启用和禁用作业

4. 跨注册中心查看作业

5. 查看作业运行轨迹和运行状态

不支持项添加作业

### 2.1 搭建控制台

1.参考git源码，拉下来后构建，不过要下载node.js很麻烦

2.直接下载相应jar包

start.sh启动

控制台不依赖什么，只需要设置账号密码和端口

控制台页面启动后再手动添加zk获取 elasticjob的信息

如果要追溯每个作业的执行情况，则先设置数据源，然后再查询job执行情况

### 2.2 修改控制台后端代码

加压完elasticJob-lite-ui-bin 后会有lib文件夹，在里面放入打包好的shardingsphere-elasticjob-lite-ui-backend-3.0.2.jar（名称要改为一致） 就行；

官网下载的backend jar包里包含前端public文件夹静态资源，需要将这个拷贝进后端代码里一起package打包，将public文件夹放入resources里面就可以直接启动后端项目，访问到控制台了 。

如果需要mysql 需要在ext-lib里面放入 mysql-connector-java-8.0.30.jar包 或者后端代码增加pom文件 

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.30</version>
</dependency>
```

控制台conf下的appplication.properties冲突优先查询这个，但是代码里面也需要这个文件 

```
## Uncomment the following property to allow adding DataSource dynamically.
dynamic.datasource.allowed-driver-classes={'com.mysql.cj.jdbc.Driver','org.h2.Driver','org.postgresql.Driver'}
```

## 三、与zookeeper

实例选举实现过程分析：

每个Elastic-Job的任务执行实例作为ZooKeeper的客户端来操作ZooKeeper的znode

1）任意一个实例启动时首先创建一个 /server 的PERSISTENT节点

2）多个实例同时创建 /server/leader EPHEMERAL子节点

3）/server/leader子节点只能创建一个，后创建的会失败。创建成功的实例被选为leader节点 ，用来执行任务。

4）所有任务实例监听 /server/leader 的变化，一旦节点被删除，就重新进行选举，抢占式地创建 /server/leader节点，谁创建成功谁就是leader。

**elasticjob的每个客户端去抢创建leader节点，成功作为leader节点的可以在本机执行任务**



## 四、问题积累

### 4.1 控制台lite3.0.2启动数据库失败

控制台lite3.0.2启动数据库默认会去连接mysql启动失败，正常应该是连接h2 选择H2Dictionary

```
H2Dictionary  / MySQLDictionary
```

```
org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl
选择的核心代码
public DBDictionary getDBDictionaryInstance() {
    DBDictionary dbdictionary = (DBDictionary)this.dbdictionaryPlugin.get();
    if (dbdictionary == null) {
        String clsName = this.dbdictionaryPlugin.getClassName();
        String props = this.dbdictionaryPlugin.getProperties();
        if (!StringUtil.isEmpty(clsName)) {
            dbdictionary = DBDictionaryFactory.newDBDictionary(this, clsName, props);
        } else {
            dbdictionary = DBDictionaryFactory.calculateDBDictionary(this, this.getConnectionURL(), this.getConnectionDriverName(), props);
            if (dbdictionary == null) {
                Log log = this.getLog("openjpa.jdbc.JDBC");
                if (log.isTraceEnabled()) {
                    Localizer loc = Localizer.forPackage(JDBCConfigurationImpl.class);
                    log.trace(loc.get("connecting-for-dictionary"));
                }
				// 下面两行核心代码会去选择是哪个DB Using dictionary class "org.apache.openjpa.jdbc.sql.H2Dictionary"
				// 会先找datasource= DynamicDataSourceConfig 
                DataSource ds = this.createConnectionFactory();
                dbdictionary = DBDictionaryFactory.newDBDictionary(this, this.getDataSource((StoreContext)null, ds), props);
            }
        }

        this.dbdictionaryPlugin.set(dbdictionary, true);
    }

    return dbdictionary;
}
```

DynamicDataSourceConfig 先初始化找对应的-> JDBCConfigurationImpl

```
DynamicDataSourceConfig 


    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dynamicDataSource(final Environment environment) {
        DataSource defaultDataSource = createDefaultDataSource(environment);
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.addDataSource(DEFAULT_DATASOURCE_NAME, defaultDataSource);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }
    

public static class DynamicDataSource extends AbstractRoutingDataSource {
    
    private final Map<Object, Object> dataSourceMap = new HashMap<>(10);
    
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceName();
    }
    
    /**
     * Add a data source.
     * 
     * @param dataSourceName data source name
     * @param dataSource data source
     */
    public void addDataSource(final String dataSourceName, final DataSource dataSource) {
        dataSourceMap.put(dataSourceName, dataSource);
        setTargetDataSources(dataSourceMap);
        afterPropertiesSet();
    }
}
```

**问题原因** ： 如果ext-lib里面添加了mysql-connector的驱动，但是代码里面pom文件没有添加，那么代码执行mysql连接的时候找不到驱动代码报错，添加即可

```
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.30</version>
        </dependency>
```

### 4.2 3.0.2版本将数据库连接源的密码不展示在页面

```
# 3.0.2版本需要手动将下面的注释打开，在conf/application.properties  但是后端工程代码里面的注释不要打开，不然每次启动backend.jar就会去连接mysql会启动失败
# Uncomment the following property to allow adding DataSource dynamically.
# 第一种方案：这里要注释掉，否则启动控制台会连接mysql启动失败，直接默认使用conf/application，那里面打开注释就行
# 第二种方案：如果这里打开，则pom文件需要添加mysql的依赖
dynamic.datasource.allowed-driver-classes={'com.mysql.cj.jdbc.Driver','org.h2.Driver','org.postgresql.Driver'}


# 不同于3.0.1 这里添加了驱动校验，所以上面要写明驱动
    @PostMapping(value = "/connectTest")
    public ResponseResult<Boolean> connectTest(@RequestBody final EventTraceDataSourceConfiguration config, final HttpServletRequest request) {
        failedIfDriverClassNotAllowed(config.getDriver()); // 扫描上面的allowed-driver-classes值判断是否已经添加了驱动
        setDataSourceNameToSession(config, request.getSession());
        return ResponseResultUtil.build(true);
    }
```

密码过滤不展示

```
    @GetMapping("/load")
    public ResponseResult<Collection<EventTraceDataSourceConfiguration>> load(final HttpServletRequest request) {
        eventTraceDataSourceConfigurationService.loadActivated().ifPresent(
            eventTraceDataSourceConfig -> setDataSourceNameToSession(eventTraceDataSourceConfig, request.getSession()));
        Set<EventTraceDataSourceConfiguration> res = eventTraceDataSourceConfigurationService.loadAll()
            .getEventTraceDataSourceConfiguration();
        res.forEach(r -> r.setPassword("")); // 直接将返回的xml密码设置为空，因为每次都是重新查询所有不影响
        return ResponseResultUtil.build(res);
    }
```

### 4.3 启动load接口获取数据源和注册中心超时10s

因为zk连接不会那么快，所以刚启动项目需要等待10分钟让连接上注册中心

