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

### 源码解析

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
