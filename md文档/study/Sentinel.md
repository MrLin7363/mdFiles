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

## Sentinel客户端

主流框架接入：[主流框架的适配 · alibaba/Sentinel Wiki (github.com)](https://github.com/alibaba/Sentinel/wiki/主流框架的适配#web-适配)

### 1. springBoot接入

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
            <version>2.2.9.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-transport-simple-http</artifactId>
            <version>1.8.5</version>
        </dependency>

```
spring:
  cloud:
    sentinel:
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

### 2. Zuul网关接入

[dashboard | Sentinel (sentinelguard.io)](https://sentinelguard.io/zh-cn/docs/dashboard.html)

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
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-transport-simple-http</artifactId>
            <version>1.8.5</version>
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

## Pull模式

可以写在java微服务里的json,每次打包上去读取这个规则文件。但是此时控制台修改，也是修改jar包里的文件，但是如果重跑流水线就不行了，所以需要长期持久化的就放到json文件中，平常的都是临时用的限流

https://blog.csdn.net/qq_33589510/article/details/108966188

官网 https://github.com/alibaba/Sentinel/wiki/%E5%9C%A8%E7%94%9F%E4%BA%A7%E7%8E%AF%E5%A2%83%E4%B8%AD%E4%BD%BF%E7%94%A8-Sentinel
