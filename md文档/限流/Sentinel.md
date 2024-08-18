官网：https://sentinelguard.io/zh-cn/docs/dashboard.html

## Sentinel控制台

### 1. 安装与配置

下载好jar包后，需要jdk环境

```
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.7.jar
```

http://localhost:8080/ 访问

sentinel自带一些接口context 如 http://localhost:8080/version

启动的jar包本身就是sentinel控制台监控的一台机器，所以另一台机器不能用8719端口了

### 2. 逻辑

控制台启动后修改规则，会推送到客户端也就是微服务，然后作为内存缓存在各自的微服务，sentinel控制台挂了也可以继续流控。

控制台重启，回去拉客户端的规则，实时轮询客户端的变化

如果客户端一重启断连，缓存没了就没有相对应的规则了

### 3. 源码安装与配置

另外他也可以下载源码下来，idea直接run也可以启动

### 4. 源码-重要-原生

```
FlowControllerV1./rule  get 刷新粗点链路流控规则的接口，http到对应的机器内存查询 /getRules接口
FlowControllerV1../rule post /delete.json /save.json   http到对应机器删除新增更新规则
```



## 规则持久化-Pull模式

可以写在java微服务里的json,每次打包上去读取这个规则文件。但是此时控制台修改，也是修改jar包里的文件，但是如果重跑流水线就不行了，所以需要长期持久化的就放到json文件中，平常的都是临时用的限流

https://blog.csdn.net/qq_33589510/article/details/108966188

官网 https://github.com/alibaba/Sentinel/wiki/%E5%9C%A8%E7%94%9F%E4%BA%A7%E7%8E%AF%E5%A2%83%E4%B8%AD%E4%BD%BF%E7%94%A8-Sentinel

![image-20230319132407747](https://i-blog.csdnimg.cn/blog_migrate/98bd808b2b6920afc95a6defd453cc2e.png)

Sentinel的Sentinel Dashboard 控制台,后面是一个微服务。它是Sentinel的客户端。

你要知道我们在实际生产当中，微服务一定是集群同一个微服务是不是也会部署多份啊？

那当你向一个Dashboard 里编写规则时，那会把这个规则推送给这个微服务的某一个Sentinel 的客户端。而它就会将这个规则持久化到一个本地的文件或者是数据库里去，那这样我们就实现了规则的持久化。

但是呢，如果说我还有一个服务，也需要这个规则呢？我怎么知道这个规则有没有变化呢？所以呢，我们的微服务呢，就会去定时轮询啊，这个文件或者是数据库。

当监听到数据库或者文件的内容发生变化时，我就知道规则更新了，那我是不是就可以去更新我自己的这个规则缓存了？

这样呢，就可以实现多个Sentinel 客户端的规则同步了，但是呢，这种定时轮询的方式。它存在一些缺点啊，第一呢，是时效性比较差，你想你这边刚写进去。那边那个服务它还不一定去读取呢，对不对？

它是定时的呀，那如果它现在还没轮到去读取，那现在你的服务与服务之间。是不是数据就不一致了呀？规则就不一致了。

所以这种模式存在一个时效性的问题，从而就导致了一个数据的不一致问题啊。那因此，这种方案也不是非常的推荐。


## 规则持久化-Push模式-推荐

https://blog.csdn.net/weixin_53041251/article/details/129651977

需要修改控制台源码然后打包成jar包，发布之后就能连特定的数据中心比如zk等

![image-20230319134318519](https://i-blog.csdnimg.cn/blog_migrate/4a123b8010317a2e357af96dd60296c8.png)

这个图是我们已经讲过的啊，这个部署模式的流程图，那我们知道啊，在这种模式当中Sentinel Dashboard 需要把规则推送到nacos，而不再是推送到Sentinel 的客户端。

但是呢，在Sentinel 的默认实现当中啊，它都是推到客户端去的。

而推到nacos 的这些功能并没有在Sentinel Dashboard的源码中实现。所以如果我们要实现push模式，我们不得不自己去改动Sentinel Dashboard的源代码。


## Sentinel客户端

主流框架接入：[主流框架的适配 · alibaba/Sentinel Wiki (github.com)](https://github.com/alibaba/Sentinel/wiki/主流框架的适配#web-适配)

### 1. Zuul网关接入

https://sentinelguard.io/zh-cn/docs/dashboard.html

[Zuul1.x\2.x继承Sentinel实现网关限流并Nacos持久化配置_sentinel zuul 配置接口限流-CSDN博客](https://blog.csdn.net/u011663693/article/details/128033199)

```
       <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
            <version>2.2.10.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-zuul-adapter</artifactId>
            <version>1.8.5</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
            <version>2.2.9.RELEASE</version>
        </dependency>
```

配置这个才能监控到经过网关的URL

```
@Configuration
public class ZuulConfig {

    @Bean
    public ZuulFilter sentinelZuulPreFilter() {
        return new SentinelZuulPreFilter();
    }

    @Bean
    public ZuulFilter sentinelZuulPostFilter() {
        return new SentinelZuulPostFilter();
    }

    @Bean
    public ZuulFilter sentinelZuulErrorFilter() {
        return new SentinelZuulErrorFilter();
    }
    
    //初始化自定义限流异常返回信息
    @PostConstruct
    public void doInit() {
        // 注册 FallbackProvider
        ZuulBlockFallbackManager.registerProvider(new MyBlockFallbackProvider());
    }
}
```

```
@Component
public class MyBlockFallbackProvider implements ZuulBlockFallbackProvider {
    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public BlockResponse fallbackResponse(String route, Throwable cause) {
        RecordLog.info(String.format("[Sentinel DefaultBlockFallbackProvider] Run fallback route: %s", route));
        if (cause instanceof BlockException) {
            return new BlockResponse(429, "当前人员访问过多，请稍等一下", route);
        } else {
            return new BlockResponse(500, "System Error", route);
        }
    }
}
```

zuul的配置

```
zuul:
  sensitive-headers: Access-Control-Allow-Origin
  ignored-headers: Access-Control-Allow-Origin
  host:
    connect-timeout-millis: 2000
    socket-timeout-millis: 300000
  max:
    host:
      connections: 500
  routes:
    user-service:
      path: ${PATH:}/user/**      
      url: http://user-service/user
```

网关会按user-service API监控而不是URL粒度

```
spring:
  cloud:
    sentinel:
      filter:
        enabled: true
      eager: true  #是否提前触发 Sentinel 初始化
      transport:
        port: 8729
        dashboard: localhost:8080
```

```
jvm启动参数-标识这是一个网关 ： -Dcsp.sentinel.app.type=1
或者springBootApplication启动时加上  System.setProperty("csp.sentinel.app.type", "1");
```

### 1. springBoot接入

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
            <version>2.2.9.RELEASE</version>
        </dependency>

```
spring:
  cloud:
    sentinel:
      filter:
        enabled: true
      eager: true  #是否提前触发 Sentinel 初始化 ，需要true，每次项目启动自动注册到控制台
      transport:
        port: 8729 #该客户端注册的端口,多实例同一个服务注册到同一个端口
        dashboard: localhost:8080 #控制台的url
```

```
如果不是spring-boot则启动jvm里带参数
-Dcsp.sentinel.dashboard.server=localhost:8080
-Dcsp.sentinel.api.port=8729
-Dproject.name=csi-proxy-support-service
```

方法上添加SentinelResource注册一个entry

```
    @SentinelResource(value = name")
    public xxx getUriStatistic(@RequestBody @Valid Vo Vo) {
        return xxx;
    }
```

```
@Configuration
public class SentinelAspectConfiguration {
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
```

#### @SentinelResource

https://blog.csdn.net/apple_52109766/article/details/126695131

不仅可以加在接口上，也可以直接加在某个方法上

```
public @interface SentinelResource {
	// 默认在控制台显示uri是方法的全限定类名； 所以要命名好名称
    String value() default "";

    EntryType entryType() default EntryType.OUT;

    int resourceType() default 0;

- blockHandler函数访问范围需要是 public
- 返回类型需要与原方法相匹配
- 参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 BlockException
	// 服务限流后会抛出BlockException 通过这里指定的方法函数取处理限流异常
    String blockHandler() default "";

	// 如果方法不在同一个类中，这里配合blockHandler()方法一起使用
    Class<?>[] blockHandlerClass() default {};

	// 用于在抛出异常（包括 BlockException）时，提供 fallback 处理逻辑
    String fallback() default "";

	// 默认的 fallback 函数名称，通常用于通用的 fallback 逻辑（即可以用于很多服务或方法）
    String defaultFallback() default "";

	// 若 fallback 函数与原方法不在同一个类中，则需要使用该属性指定 fallback 函数所在的类。
    Class<?>[] fallbackClass() default {};

	// 用于指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，而是会原样抛出
    Class<? extends Throwable>[] exceptionsToTrace() default {Throwable.class};

    Class<? extends Throwable>[] exceptionsToIgnore() default {};
}
```

注意：若 blockHandler 和 fallback 都进行了配置，则被限流降级而抛出 `BlockException` 时只会进入 `blockHandler` 处理逻辑



### 3. 代码中单独配置规则

https://blog.csdn.net/qq_31319235/article/details/121398768

每次项目启动都会加载这个默认规则


    @PostConstruct // 此注解会在应用启动并且加载该类bean的时候自动执行，是一个init-method的方法
    private static void initFlowRules(){
        // 流控规则列表
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("/index"); //流控可限制的资源 : 和 @SentinelResource 的value匹配
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS); //设置QPS
        rule.setCount(1);
        rules.add(rule);
     
        FlowRuleManager.loadRules(rules); //加载配置好的规则
    }

```
	@GetMapping("/index")
    @SentinelResource(value = "/index",blockHandler = "blockHandlerForGetUser")
    public String index(){
        return "sentinel";
    }
```

