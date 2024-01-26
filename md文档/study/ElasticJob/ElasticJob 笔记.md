ElasticJob 笔记

官网： https://shardingsphere.apache.org/elasticjob/current/cn/features/elastic/

https://shardingsphere.apache.org/elasticjob/legacy/lite-2.x/02-guide/event-trace/    推荐

分片策略： https://blog.csdn.net/qq_37960603/article/details/122270721

shardingTotalCount 指定任务执行的分片，推荐1片；否则任务会分到不同的服务器执行，这里就参考上面分片策略。

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
