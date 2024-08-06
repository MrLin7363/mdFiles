spring相关积累

spring相关学习网站（不错）  https://www.baeldung.com/

## 一、spring-boot

### 1. 容器相关

#### spring-boot-启动显示已加载的类

```
@Bean
public CommandLineRunner run(ApplicationContext applicationContext){
    return args -> {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).sorted().forEach(System.out::println);
    };
}
```

#### 通过名称反射调用加载的类

```
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext=applicationContext;
    }

    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

}
```

```
 // 普通接口不会注册为bean,通过类型找bean需开头小写，然后是具体的实现类
Object WorkflowRepository = springContextUtil.getBean("workflowServiceImpl");
// Feign接口会注册为bean，但是名称是全路径名
Object feign = springContextUtil.getBean("com.coolpad.basic.infrastructure.outconfig.gatewayimpl.AppConfigFeign");
Method method = ReflectionUtils.findMethod(feign.getClass(), "queryConfig", ConfigQryGo.class);
ConfigQryGo configQryGo = new ConfigQryGo();
// 方法,对象Bean,入参
Object singleResponse = ReflectionUtils.invokeMethod(method, feign, configQryGo);
```

### 2. 自动配置-Condition

### 3. 定时任务

@Scheduled @EnableScheduling

```
@Configuration
@EnableScheduling
public class ScheduleingTest {

    @Scheduled(cron = "0/1 * * * * *")
//    @Scheduled(fixedRate=1000)
    public void test(){
        System.out.println(" test");
    }
}
```

接下来配置定时任务：

```
    @Scheduled(fixedRate = 2000)
    public void fixedRate() {
        System.out.println("fixedRate>>>"+new Date());    
    }
    @Scheduled(fixedDelay = 2000)
    public void fixedDelay() {
        System.out.println("fixedDelay>>>"+new Date());
    }
    @Scheduled(initialDelay = 2000,fixedDelay = 2000)
    public void initialDelay() {
        System.out.println("initialDelay>>>"+new Date());
    }
```

​	首先使用 @Scheduled 注解开启一个定时任务。

1. fixedRate 表示任务执行之间的时间间隔，具体是指两次任务的开始时间间隔，即第二次任务开始时，第一次任务可能还没结束。
2. fixedDelay 表示任务执行之间的时间间隔，具体是指本次任务结束到下次任务开始之间的时间间隔。
3. initialDelay 表示首次任务启动的延迟时间。
4. 所有时间的单位都是毫秒。

上面这是一个基本用法，除了这几个基本属性之外，@Scheduled 注解也支持 cron 表达式，使用 cron 表达式，可以非常丰富的描述定时任务的时间。cron 表达式格式如下：

> [秒] [分] [小时] [日] [月] [周] [年]

具体取值如下：

| 序号 | 说明 | 是否必填 | 允许填写的值    | 允许的通配符 |
| ---- | ---- | -------- | --------------- | ------------ |
| 1    | 秒   | 是       | 0-59            | - * /        |
| 2    | 分   | 是       | 0-59            | - * /        |
| 3    | 时   | 是       | 0-23            | - * /        |
| 4    | 日   | 是       | 1-31            | - * ? / L W  |
| 5    | 月   | 是       | 1-12 or JAN-DEC | - * /        |
| 6    | 周   | 是       | 1-7 or SUN-SAT  | - * ? / L #  |
| 7    | 年   | 否       | 1970-2099       | - * /        |

**这一块需要大家注意的是，月份中的日期和星期可能会起冲突，因此在配置时这两个得有一个是 `?`**

**通配符含义：**

- `?` 表示不指定值，即不关心某个字段的取值时使用。需要注意的是，月份中的日期和星期可能会起冲突，因此在配置时这两个得有一个是 `?`
- `*` 表示所有值，例如:在秒的字段上设置 `*`,表示每一秒都会触发
- `,` 用来分开多个值，例如在周字段上设置 "MON,WED,FRI" 表示周一，周三和周五触发
- `-` 表示区间，例如在秒上设置 "10-12",表示 10,11,12秒都会触发
- `/` 用于递增触发，如在秒上面设置"5/15" 表示从5秒开始，每增15秒触发(5,20,35,50)
- `#` 序号(表示每月的第几个周几)，例如在周字段上设置"6#3"表示在每月的第三个周六，(用 在母亲节和父亲节再合适不过了)
- 周字段的设置，若使用英文字母是不区分大小写的 ，即 MON 与mon相同
- `L` 表示最后的意思。在日字段设置上，表示当月的最后一天(依据当前月份，如果是二月还会自动判断是否是润年), 在周字段上表示星期六，相当于"7"或"SAT"（注意周日算是第一天）。如果在"L"前加上数字，则表示该数据的最后一个。例如在周字段上设置"6L"这样的格式,则表示"本月最后一个星期五"
- `W` 表示离指定日期的最近工作日(周一至周五)，例如在日字段上设置"15W"，表示离每月15号最近的那个工作日触发。如果15号正好是周六，则找最近的周五(14号)触发, 如果15号是周未，则找最近的下周一(16号)触发，如果15号正好在工作日(周一至周五)，则就在该天触发。如果指定格式为 "1W",它则表示每月1号往后最近的工作日触发。如果1号正是周六，则将在3号下周一触发。(注，"W"前只能设置具体的数字,不允许区间"-")
- `L` 和 `W` 可以一组合使用。如果在日字段上设置"LW",则表示在本月的最后一个工作日触发(一般指发工资 )

例如，在 @Scheduled 注解中来一个简单的 cron 表达式，每隔5秒触发一次，如下：

```
@Scheduled(cron = "0/5 * * * * *")
public void cron() {
    System.out.println(new Date());
}
```

### 4. 日志

info/debug/trace 

```
logging:
  level:
    root: debug
    com:
      lin: debug  // 定义什么包下日志打印
```

### 5. 注解

```
@Profile({"production","uat","test","development"})
@ConditionalOnProperty(name = "lin.check", havingValue = "true")
@ConditionalOnMissingBean(UserInterceptor.class)

@Import({JwtValidator.class, RoleValidator.class})
```

#### 5.1 @Import

```
public class TestA {
    private void print() {
        System.out.println("TestAAAAAA");
    }
}
 
public class TestB {
    private void print() {
        System.out.println("TestBBBBBB");
    }
}
 
public class TestC {
    private void print() {
        System.out.println("TestCCCC");
    }
}
```

1. 定义实现 ImportSelector 的类，用来注入 TestB 的实例

```
public class MySelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{"com.demo.TestB"};
    }
}
```

2. 定义实现 ImportBeanDefinitionRegistrar 接口的类，用来注入 TestC 的实例

```
public class MyRegistrar implements ImportBeanDefinitionRegistrar {
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //构造 BeanDefinition
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(TestC.class);
        //注册 bean, 并给其取个名字
        registry.registerBeanDefinition("testC",rootBeanDefinition);
    }
}
```

3. 定义配置类，通过 @Import 注解注入 bean 实例

```
@Import({TestA.class, MySelector.class, MyRegistrar.class})
public class ImportDemo {
}
```

```
public class ImportMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ImportDemo.class);
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        //打印出 IOC 容器里所有的 bean
        for (String name : beanDefinitionNames) {
            System.out.println(name);
        }
    }
}
```

总结：以上共三种方式能够注入bean

### 6.**spring-boot-starter-actuator**

actuator 可用于监控、管理生产应用，为微服务提供审计、检查检查、指标收集，HTTP 跟踪，动态修改日志等等特性

官网地址：https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.enabling

```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

```
management:
  endpoints:
    web:
      exposure:
        include: "*" #["health","info"]  或者  "health,info"
        exclude: "dev" 
```

#### 6.1 Endpoints 端点

针对以上所说的功能，actuator提供了很多内置的端点，并用允许用户添加自己的端点。所谓端点，可以理解为功能接口。例如：健康检查端点（GET /actuator/health），日志端点（GET /actuator/loggers）等。

端点需要满足启用（enabled）和公开（exposed），才可用。

端点规则：`/actuator` + 端点ID，例如：

```
/env
/health 健康检查
/mappings 显示所有的@RequestMapping路径
/loggers 日志
/info 定制信息
/metrics 查看内存、CPU核心等系统参数
/trace 用户请求信息
```

访问 http://127.0.0.1:8003/actuator 列举所有的endpoints

源码比如  MetricsEndpoint

#### 6.2 logger

动态修改日志级别

get  查看所有类日志级别 http://127.0.0.1:8003/actuator/loggers

post   动态修改该包的日志级别 http://127.0.0.1:8003/actuator/loggers/com.lin.ServiceProxy

```
{
    "configuredLevel": "INFO"
}
```

post 动态修改全部结点的日志级别    http://127.0.0.1:8003/actuator/loggers/ROOT

#### 6.3 应用健康检查

https://blog.csdn.net/weixin_44421461/article/details/131199025

入口 ： HealthEndpoint

Spring boot的健康信息都是从`ApplicationContext`中的各种`HealthIndicator Beans`中收集到的，Spring boot框架中包含了大量的`HealthIndicators`的实现类，当然你也可以实现自己认为的健康状态。

默认情况下，最终的 Spring Boot 应用的状态是由 `HealthAggregator` 汇总而成的，汇总的算法是：

1. 设置状态码顺序：`setStatusOrder(Status.DOWN, Status.OUT_OF_SERVICE, Status.UP, Status.UNKNOWN);`。
2. 过滤掉不能识别的状态码。
3. 如果无任何状态码，整个 Spring Boot 应用的状态是 `UNKNOWN`。
4. 将所有收集到的状态码按照 1 中的顺序排序。
5. 返回有序状态码序列中的第一个状态码，作为整个 Spring Boot 应用的状态。

health 通过合并几个健康指数检查应用的健康情况。Spring boot框架自带的 `HealthIndicators` 目前包括：

| CassandraHealthIndicator       | Checks that a Cassandra database is up.                   |
| :----------------------------- | :-------------------------------------------------------- |
| `DiskSpaceHealthIndicator`     | Checks for low disk space.                                |
| `DataSourceHealthIndicator`    | Checks that a connection to `DataSource` can be obtained. |
| `ElasticsearchHealthIndicator` | Checks that an Elasticsearch cluster is up.               |
| `InfluxDbHealthIndicator`      | Checks that an InfluxDB server is up.                     |
| `JmsHealthIndicator`           | Checks that a JMS broker is up.                           |
| `MailHealthIndicator`          | Checks that a mail server is up.                          |
| `MongoHealthIndicator`         | Checks that a Mongo database is up.                       |
| `Neo4jHealthIndicator`         | Checks that a Neo4j server is up.                         |
| `RabbitHealthIndicator`        | Checks that a Neo4j server is up.                         |
| `RedisHealthIndicator`         | Checks that a Redis server is up.                         |
| `SolrHealthIndicator`          | Checks that a Solr server is up.                          |

##### 6.3.21 **自定义健康组**

https://dev.to/sabyasachi/inside-spring-boot-health-endpoint-2mej

因为/health默认全部都检查，如果单独检查某些可以配置健康组

```yaml
management:
  endpoint:
    health:
      show-details: always #可以看到哪些indicator
      group:
        custom: #自定义
          include: "redis"
```

You can then check the result by hitting `localhost:8080/actuator/health/custom`.

**自定义 HealthIndicator 健康检查**

通过实现`HealthIndicator`的接口来实现，并将该实现类注册为spring bean

或者继承  AbstractHealthIndicator 或者直接继承相关的类indicator

##### 6.3.2 自定义redis检查

```
management:
  endpoint:
    health:
      show-details: always
      group:
        redis:
          include: ["redisCheck"]
```

```
@Component("redisCheck")
public class RedisCheckHealthIndicator extends RedisReactiveHealthIndicator {
    public RedisCheckHealthIndicator(ReactiveRedisConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    protected Mono<Health> doHealthCheck(Health.Builder builder) {
        Mono<Health> healthMono = super.doHealthCheck(builder);
        // 自己实现redis不健康的一些逻辑等
        return healthMono;
    }
}
```

http://127.0.0.1:8003/actuator/health/redis  只访问这个indicator

##### 6.3.2 源码

HealthEndpointSupport.getAggregateContribution 会执行各个healthIndicator ,每一个都会经过HealthIndicator 然后到各自的 HealthIndicator

但是部分数据库或中间件比如mongo,redis,elasticSearch等，redisHealthIndicator 默认不启用，而是使用异步的RedisReactiveHealthIndicator

每次启动都会进行一次健康检查，如果有问题启动失败，但是如果是redis有问题，因为是异步的所以项目还能启动成功

关闭redis检查

```
  health:
    redis:
      enabled: false #默认开启的
```

注意：如果另外调health接口，那么RedisReactiveHealthIndicator会阻塞直到检查完毕，依然能检查出是否有问题

#### 6.4 监控页面配置

Spring Boot Monitor是一个对Spring boot admin监控工具做修改并适配单机的监控工具，完美继承了Spring boot admin的风格

- Spring Boot Monitor官网：https://www.pomit.cn/SpringBootMonitor

http://127.0.0.1:8080/monitor

## 二、SpringWeb

### 1.实现https访问

核心为以下三个部分：

- `sslSocketFactory`
- `HostnameVerifier`
- `X509TrustManager`

第一个是[套接字](https://so.csdn.net/so/search?q=套接字&spm=1001.2101.3001.7020)工厂，第二个用来验证主机名，第三个是证书信任器管理类

### 2. restTemplate

目前通过RestTemplate 的源码可知，RestTemplate 可支持多种 Http Client的http的访问，如下所示：

- 基于 JDK HttpURLConnection 的 SimpleClientHttpRequestFactory，**默认**。
- 基于 [Apache](https://so.csdn.net/so/search?q=Apache&spm=1001.2101.3001.7020) HttpComponents Client 的 HttpComponentsClientHttpRequestFactory // 使用poll并发性能更高
- 基于 OkHttp3的OkHttpClientHttpRequestFactory。// 不方便设置poll
- 基于 Netty4 的 Netty4ClientHttpRequestFactory。// 废弃

其中HttpURLConnection 和 HttpClient 为原生的网络访问类，OkHttp3采用了 OkHttp3的框架，Netty4 采用了Netty框架。



#### 2.1 基于jdk HttpURLConnection 

```
import org.springframework.http.client.SimpleClientHttpRequestFactory;
 
import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;
 
public class HttpsClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
  @Override
    public void prepareConnection(HttpURLConnection connection, String httpMethod) {
        try {
            // 如果不是https请求则直接转发，如果是https请求需要配置证书TSL等继续走下面逻辑
            if (!(connection instanceof HttpsURLConnection)) {
                super.prepareConnection(connection, httpMethod);
                return;
            }

            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

            X509TrustManager x509m = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }
            };

            TrustManager[] trustAllCerts = new TrustManager[] {
                x509m
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            httpsConnection.setSSLSocketFactory(new MyCustomSSLSocketFactory(sslContext.getSocketFactory()));

            httpsConnection.setHostnameVerifier((s, sslSession) -> true);

            super.prepareConnection(httpsConnection, httpMethod);
        } catch (Exception e) {
        }
    }

    private static class MyCustomSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        MyCustomSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException {
            final Socket underlyingSocket = delegate.createSocket(socket, host, port, autoClose);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final String host, final int port, final InetAddress localAddress,
            final int localPort) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress,
            final int localPort) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
            return overrideProtocol(underlyingSocket);
        }

        private Socket overrideProtocol(final Socket socket) {
            if (!(socket instanceof SSLSocket)) {
            }
            ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1", "TLSv1.2"});
            return socket;
        }
    }
}
```



```
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
 
import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
 
 
@Component
@Lazy(false)
public class SimpleRestClient {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRestClient.class);
 
    private static RestTemplate restTemplate;
 
    static {
        HttpsClientHttpRequestFactory requestFactory = new HttpsClientHttpRequestFactory();
        requestFactory.setReadTimeout(5000);
        requestFactory.setConnectTimeout(5000);
 
        // 添加转换器
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
 
        restTemplate = new RestTemplate(messageConverters);
        restTemplate.setRequestFactory(requestFactory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
 
        LOGGER.info("SimpleRestClient初始化完成");
    }
 
    private SimpleRestClient() {
 
    }
 
    @PostConstruct
    public static RestTemplate getClient() {
        return restTemplate;
    }
 
}
```

#### 2.2 使用Apheche Httpclient连接池

```
@Component
@Lazy(false)
public class RestClient {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
 
    private static RestTemplate restTemplate;
 
    static {
    	// 底层使用Httpclient连接池的方式创建Http连接请求 推荐加上这个manager性能更好
        // 长连接保持30秒
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
        // 总连接数
        pollingConnectionManager.setMaxTotal(1000);
        // 同路由的并发数
        pollingConnectionManager.setDefaultMaxPerRoute(1000);
 
 		// 配置https访问ssl
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext,new NoopHostnameVerifier());
        
        // NoConnectionReuseStrategy 不保持长连接，keep-alive关闭，每个请求一个连接，避免服务端reset错误Connection reset by peer
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);

        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        // 重试次数，默认是3次，没有开启
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
        // 保持长连接配置，需要在头添加Keep-Alive
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
 

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));
 
        httpClientBuilder.setDefaultHeaders(headers);
 
        HttpClient httpClient = httpClientBuilder.build();
 
        // httpClient连接配置，底层是配置RequestConfig
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // 连接超时
        clientHttpRequestFactory.setConnectTimeout(5000);
        // 数据读取超时时间，即SocketTimeout
        clientHttpRequestFactory.setReadTimeout(5000);
        // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
        clientHttpRequestFactory.setConnectionRequestTimeout(200);
        // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
        // clientHttpRequestFactory.setBufferRequestBody(false);
 
        // 添加内容转换器
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
 
        restTemplate = new RestTemplate(messageConverters);
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        // 异常透传，不然可能过滤，请求返回的异常不能正常显示
        restTemplate.setErrorHandler(new NoErrorResultErrorHandler());
 
        LOGGER.info("RestClient初始化完成");
    }
 
    private RestClient() {
 
    }
 
    @PostConstruct
    public static RestTemplate getClient() {
        return restTemplate;
    }
 
}
```

```
public class NoErrorResultErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) {
        // 不报错
    }
}

```

##### 2.2.1 httpclient poll源码分析

```
AbstractConnPool.getPoolEntryBlocking()
...
// 应该是每个路由一个pool
RouteSpecificPool pool = this.getPool(route);
...
if (pool.getAllocatedCount() < maxPerRoute) {
```

##### 2.2.2 Connection reset by peer

https://www.cnblogs.com/yunnick/p/11290429.html

org.apache.http.NoHttpResponseException: 21.153.143.183:8080 failed to respond

通过查询资料发现这个异常与Http Header的一个参数 Connection: Keep-Alive 有关，我们使用的是Apache的httpclient。

- 先给出一个解决方法，很简单，在初始化httpclient时，使用如下配置，主要是NoConnectionReuseStrategy.INSTANCE 参数：

```
httpclient = HttpClientBuilder.create() 
.setMaxConnPerRoute(20) 
.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE) //解决NoHttpResponseException问题 
.setMaxConnTotal(200) 
.build(); 
```

#### 2.3 基于 OkHttp3

官网 https://square.github.io/okhttp/works_with_okhttp/

```
    @Bean("okHttpClient")
    public RestTemplate okHttpClient() {
        try {
            X509TrustManager x509m = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }
            };

            TrustManager[] trustAllCerts = new TrustManager[] {
                x509m
            };
            // 设置SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            OkHttpClient httpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory(), x509m)
                .hostnameVerifier((s, sslSession) -> true)
                .connectionPool(pool())
                // .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                // .readTimeout(readTimeout, TimeUnit.SECONDS)
                // .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                // 设置代理
                // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
                // 拦截器
                // .addInterceptor()
                .build();
            OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(httpClient);
            return new RestTemplate(requestFactory);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println("error");
        }
        return null;
    }

    public ConnectionPool pool() {
        return new ConnectionPool(1000, 30, TimeUnit.SECONDS);
    }
```



```
@Configuration
public class RestConfig {
 
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
 
    @Bean("urlConnection")
    public RestTemplate urlConnectionRestTemplate(){
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        return restTemplate;
    }
 
    @Bean("httpClient")
    public RestTemplate httpClientRestTemplate(){
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }
 
    @Bean("oKHttp3")
    public RestTemplate OKHttp3RestTemplate(){
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        return restTemplate;
    }
}
```

#### 2.4 restTemplate源码分析

以apache的httpclient为例

```
ResponseEntity<Resource> exchange = restTemplates.get(1)
    .exchange("http://127.0.0.1:8003/proxy-support/api/v1/test/health", HttpMethod.GET, null,
        Resource.class);
```

```
RestTemplate.exechange
->
RestTemplate.doExecute
->
// 创建请求，
  ClientHttpRequest request = this.createRequest(url, method); 
            if (requestCallback != null) {
            // 规定请求的accept为什么类型:如Resource类型的放回类型，只能接收content-type appclication/json  
                requestCallback.doWithRequest(request);
            }
            // 真正执行请求
            response = request.execute();
            this.handleResponse(url, method, response);
            ...
```

```
RestTemplate.AcceptHeaderRequestCallback
public void doWithRequest(ClientHttpRequest request) throws IOException {
    if (this.responseType != null) {
    // 获取支持的media类型   先过滤调不符合的Message转换器
        List<MediaType> allSupportedMediaTypes = (List)RestTemplate.this.getMessageConverters().stream().filter((converter) -> {
        	// 如Resource可以让RescourceHttpMessageConverter转换
            return this.canReadResponse(this.responseType, converter);
        }).flatMap((converter) -> {
        // 获取支持的Message转换器 进行处理
            return this.getSupportedMediaTypes(this.responseType, converter);
        }).distinct().sorted(MediaType.SPECIFICITY_COMPARATOR).collect(Collectors.toList());
        if (RestTemplate.this.logger.isDebugEnabled()) {
            RestTemplate.this.logger.debug("Accept=" + allSupportedMediaTypes);
        }
        request.getHeaders().setAccept(allSupportedMediaTypes);
    }
}
```

#### 2.5 重试interceptor

```
restTemplate.setInterceptors(Collections.singletonList(new RequestRetryInterceptor(retryTime, retryWait,UnknownHostException.class)));  // 请求重试机制，限定出现 UnknownHostException触发
```

```
public class RequestRetryInterceptor implements ClientHttpRequestInterceptor {

    private final int retryTime;

    private final int retryWait;

    private final Class<? extends IOException> retryClass;

    public RequestRetryInterceptor(int retryTime, int retryWait, Class<? extends IOException> retryClass) {
        this.retryTime = retryTime;
        this.retryWait = retryWait;
        this.retryClass = retryClass;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        return executeWithRetry(request, body, execution, retryTime);
    }

    private ClientHttpResponse executeWithRetry(HttpRequest request, byte[] body, ClientHttpRequestExecution execution,
        int retryCount) throws IOException {
        try {
            return execution.execute(request, body);
        } catch (IOException e) {
            // 如果是设置好的的exception，则重试，否则抛出异常
            if (retryCount > 0 && retryClass.isAssignableFrom(e.getClass())) {
                try {
                    TimeUnit.MILLISECONDS.sleep(retryWait);
                } catch (InterruptedException exception) {
                    LOGGER.error(exception.getMessage());
                }
                return executeWithRetry(request, body, execution, retryCount - 1);
            } else {
                throw e;
            }
        }
    }
}
```

### 3. webClient

官网： https://docs.spring.io/spring-framework/docs/5.3.28/reference/html/

中文指南：https://docs.flydean.com/spring-framework-documentation5/webreactive/2.webclient

好博客：https://blog.csdn.net/zzhongcy/article/details/105412842

reactor-netty  文档  **https://projectreactor.io/docs/netty/release/reference/index.html#faq.connection-closed**

#### 3.0 对比

**阻塞式客户端 vs 非阻塞客户端**

在 Web 应用程序中，向其他服务发起 HTTP 调用是常见需求。因此，需要一个 Web 客户端工具。

**RestTemplate 阻塞式客户端**

长期以来，Spring 一直把 RestTemplate 作为默认 Web 客户端。RestTemplate 使用 Java Servlet API，这种模型在底层会为每个请求分配处理线程。

这意味着线程会一直保持阻塞，直到 Web 客户端收到响应。阻塞式模型的问题在于每个线程都会消耗大量内存和 CPU 资源

让我们考虑这样的情况：系统收到大批请求时，等待某些服务返回结果，而这些服务本身执行缓慢。

结果，等待的请求会发生堆积。应用程序将创建许多线程，耗尽线程池并占掉所有可用内存。还可能因为 CPU 上下文（线程）频繁切换导致性能下降。

**WebClient 非阻塞客户端**

另一种 WebClient 方案，利用 Spring Reactive 框架提供异步、非阻塞式解决方案。

RestTemplate 为每个事件（HTTP 调用）创建一个新线程，而 WebClient 为每个事件创建类似 task 的结构。Reactive 框架会在后台对这些 task 进行排队，且只在响应结果就绪后才开始执行。

Reactive 框架采用事件驱动，通过Reactive Streams API 实现异步逻辑。相比同步阻塞式调用，Reactive 方法用更少的线程和系统资源处理了更多业务逻辑。

Webclient 是 Spring WebFlux 开发库的一部分。因此，写客户端代码时，还可以应用函数式编程与流式 API，支持 Reactive 类型（Mono 和 Flux）。



其实WebClient处理单个HTTP请求的响应时长并不比RestTemplate更快，但是它处理并发的能力更强，非阻塞的方式可以使用较少的线程以及硬件资源来处理更多的并发。

  所以响应式非阻塞IO模型的核心意义在于，提高了单位时间内有限资源下的服务请求的并发处理能力，而不是缩短了单个服务请求的响应时长。

- 与RestTemplate相比，WebClient的优势

  - 非阻塞响应式IO，单位时间内有限资源下支持更高的并发量。

  - 支持使用Java8 Lambda表达式函数。

  - 支持同步、异步、Stream流式传输。




socket采用非阻塞模式，整个过程只在调用select、poll、epoll时才会阻塞，收到客户端消息不会阻塞，这个进程就会被充分利用起来，这种模式一般被称为事件驱动，也就是**reactor反应模式**

**实例比较**

为了展示两种方法差异，需要多客户端并行请求进行性能测试。可以看到，收到多个客户端请求后，阻塞方法的性能显著下降。

而 Reactive 非阻塞方法的表现应该与请求数量无关，性能稳定。

3 WebClient与RestTemplate比较
WebClient是一个功能完善的Http请求客户端，与RestTemplate相比，WebClient支持以下内容：

非阻塞 I/O。
反应流背压（消费者消费负载过高时主动反馈生产者放慢生产速度的一种机制）。
具有高并发性，硬件资源消耗更少。
流畅的API设计。
同步和异步交互。
流式传输支持

#### 3.1 Codecs(编/解码器)

https://zhuanlan.zhihu.com/p/157490007

Encoder和Decoder是底层协议，用于编/解码分离开的HTTP内容。

HttpMessageReader和HttpMessageWriter是用于编/解码HTTP 消息内容的编/解码器。

一个编码器可以被EncoderHttpMessageWriter封装使其可以合适的用于Web应用； 同样，解码器也可以被DecoderHttpMessageReader封装。

DataBuffer抽象并封装了不同服务器的byte buffer(诸如Netty的ByteBuf, java.nio.ByteBuffer)； 这也是所有的编/解码器工作的数据依赖(或数据来源)。

spring-core模块提供了**byte[]，ByteBuffer，DataBuffer，Resource，和String**的编码器以及解码器实现。

```
AbstractDataBufferDecoder/AbstractSingleValueEncoder下实现了这些解码器
```

流程：

```。、
解码
BodyInserters.writeWithMessageWriters()
->
// 遍历每一个encoder是否能写当前body类型
return (Mono)context.messageWriters().stream().filter((messageWriter) -> {
            return messageWriter.canWrite(bodyType, mediaType);
        }).findFirst().map(BodyInserters::cast).map((writer) -> {
            return write(publisher, bodyType, mediaType, outputMessage, context, writer);
        }).orElseGet(() -> {
            return Mono.error(unsupportedError(bodyType, context, mediaType));
        });
        
EncoderHttpMessageWriter.canWrite()

EncoderHttpMessageWriter.write()

->
ReactorClientHttpConnector.connect() 发送请求

->
BodyExtractors.readWithMessageReaders

 return (Publisher)context.messageReaders().stream().filter((reader) -> {
                return reader.canRead(elementType, contentType);
            }).findFirst().map(BodyExtractors::cast).map(readerFunction).orElseGet(() -> {
                List<MediaType> mediaTypes = (List)context.messageReaders().stream().flatMap((reader) -> {
                    return reader.getReadableMediaTypes(elementType).stream();
                }).collect(Collectors.toList());
                return (Publisher)errorFunction.apply(new UnsupportedMediaTypeException(contentType, mediaTypes, elementType));
            });
            
DecoderHttpMessageReader.canRead()

AbstractDataBufferDecoder.decodeDataBuffer()

DefaultWebClient.ToEntity  // dubug这里可以查看response/request的请求头，返回头等信息
```

```
这个会采用StringDecoder/CharSequenceEncoder
ResponseEntity<String> doResponse = webClient.method(HttpMethod.GET)
    .uri("http://127.0.0.1:8003/xxxxxx")
    .headers(headers -> headers.addAll(myheaders))
    .bodyValue("asdas")
    .accept(MediaType.ALL)
    .retrieve()
    .toEntity(String.class)
    .block();
```

#### 3.2 配置代码

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <version>2.7.12</version>
</dependency>
```

```
@Configuration
public class WebClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConfig.class);

    private static final X509TrustManager X509M = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }
    };

    @Autowired
    private SettingUtil settingUtil;

    /**
     * 创建webclient
     *
     * @return Map<Integer, WebClient>
     */
    @Bean
    public Map<Integer, WebClient> getWebClient() {
        Map<Integer, WebClient> clientMap = new HashMap<>();
        settingUtil.getAllRestSetting()
            .forEach(setting -> clientMap.putIfAbsent(setting.getReadTimeout(),
                createWebClient(setting.getConnectTimeout(), setting.getReadTimeout())));
        return clientMap;
    }

    private WebClient createWebClient(int connectTimeout, int readTimeout) {
        try {
            SslContext sslContext = getSslContext();
            
            // 自定义线程池-有必要-默认不建线程池，可能会connection reset  
  			ConnectionProvider provider = ConnectionProvider.builder("webClient").maxConnections(500)
  				//最大空闲时间
                .maxIdleTime(Duration.ofSeconds(20)) // 能够防止connection reset
                .maxLifeTime(Duration.ofSeconds(60))
                // 等待队列超时时间
              .pendingAcquireTimeout(Duration.ofSeconds(60))
              // 等待队列大小 pendingAcquireMaxCount
              .evictInBackground(Duration.ofSeconds(120)).build();
                
            SslContext sslContext = getSslContext();
            HttpClient httpClient = HttpClient.create(provider).secure(t -> t.sslContext(sslContext))
            	// 连接超时一般3s-5s
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
                // 读取超时一般5s-10s
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout)));
                // .addHandlerLast(new WriteTimeoutHandler(10));也可以设置写超时一般不用
            WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
            	// 默认响应buffer只有256K左右很容易超
            	.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                // 异常的透传有更好的方案3.8.3  .filter(errorHandler())
                .build();
            return webClient;
        } catch (SSLException e) {
            LOGGER.error("createWebClient error", e);
        }
        throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot create webclient");
    }
	
	// 3.8.1 异常透传场景下怎么处理  异常需要抛出的情况   推荐3.8.3的配置即可
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> getClientResponseMono(clientResponse));
    }

    private Mono<ClientResponse> getClientResponseMono(ClientResponse clientResponse) {
        if (clientResponse.statusCode().is5xxServerError()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)));
        } else if (clientResponse.statusCode().is4xxClientError()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)));
        } else {
            return Mono.just(clientResponse);
        }
    }

    private SslContext getSslContext() throws SSLException {
        TrustManager[] trustAllCerts = new TrustManager[] {X509M};
        SslContext sslContext = SslContextBuilder.forClient().trustManager(trustAllCerts[0]).build();
        return sslContext;
    }
}
```

```

Netty 库配置 
// 配置动态连接池 ConnectionProvider provider = ConnectionProvider.elastic("elastic pool"); 配置固定大小连接池，如最大连接数、连接获取超时、空闲连接死亡时间等
ConnectionProvider provider = ConnectionProvider.fixed("fixed", 45, 4000, Duration.ofSeconds(6));
HttpClient httpClient = HttpClient.create(provider)
		.secure(sslContextSpec -> {
			SslContextBuilder sslContextBuilder = SslContextBuilder.forClient().trustManager(new File("E://server.truststore"));
			sslContextSpec.sslContext(sslContextBuilder);
		}).tcpConfiguration(tcpClient -> {
			// 指定Netty的 select 和 work 线程数量
			LoopResources loop = LoopResources.create("kl-event-loop", 1, 4, true);
			return tcpClient.doOnConnected(connection -> {
				// 读写超时设置
				connection
				.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
				.addHandlerLast(new WriteTimeoutHandler(10));
			})
				// 连接超时设置
			   .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
			   .option(ChannelOption.TCP_NODELAY, true)
			   .runOn(loop);
		});
WebClient.builder()
		.clientConnector(new ReactorClientHttpConnector(httpClient))
		.build()
                        
原文链接：https://blog.csdn.net/weixin_42679286/article/details/135607779
```



#### 3.3 实现代码

```
    @GetMapping("v1/test/webclient")
    public void webclient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpHeaders myheaders = new HttpHeaders();
        ResponseEntity<Resource> doResponse = webClient.method(HttpMethod.GET)
            .uri("http://127.0.0.1:8003/xxxx/api/xxx/xx")
            .headers(headers -> headers.addAll(myheaders))
            .bodyValue("asdas")
            .acceptCharset(StandardCharsets.UTF_8)
            // 将上面的资源整合为ResponseSpec
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.empty()) // 异常不处理，如果要异常处理，则需要errorHandler
            // 具体执行请求
            .toEntity(Resource.class)
            .block();
      
        byte[] bytes = StreamUtils.copyToByteArray(doResponse.getBody().getInputStream());
        if (doResponse.getHeaders() != null) {
            HttpHeaders headers = doResponse.getHeaders();
            response.setHeader(Constants.CONTENT_TYPE, headers.getContentType().toString());
        }
        // resp.subscribe(e -> System.out.println(e));  // 后执行
        response.getOutputStream().write(bytes);
    }
```

##### 3.3.1 传参详解

1. /{id} 路径参数           对应uriVariables 

2. ？xxx=xxx请求参数      对应queryParam   

3. bogy参数

**注意事项**：区分uriVariables 和 queryParam   

```
.uri(requestUrl + "/{id}", 1) // http://xxx/1
.uri(requestUrl + "/{id}", new HashMap())   // 这里的 map是 {id:1}的形式，会填充{id} ,不是请求的parama

                .uri(requestUrl, builder -> {
                    MultiValueMap<String, String> objectObjectMultiValueMap = new LinkedMultiValueMap<>();
                    objectObjectMultiValueMap.add("id", "1");
                    objectObjectMultiValueMap.add("name", "chen");
                    // add queryParam   
                    builder.queryParam("name22", "asdasd");
                    builder.queryParams(objectObjectMultiValueMap);
                    // add uriVariables 
                    URI uri = builder.build(objectObjectMultiValueMap);
                    return uri;
                })
```

第2中形式的参数webclient不支持，只能在url里自己拼接就好

##### 3.3.2 URI和URL

实际底层调用的时候是URI，需要转义一些空格等字符

```
public interface UriSpec<S extends WebClient.RequestHeadersSpec<?>> {
	// 这个方法 new URI(requestUrl)不会解析 | 空格等字符串，包含会报错
    S uri(URI uri);
	// 自动解析 成sURI的形式
    S uri(String uri, Object... uriVariables);
```

```
restTemplate 和 webclient都会到这里解析URI
传参是url的会解析，是URI的默认通过
URI expanded = this.getUriTemplateHandler().expand(url, uriVariables);

public interface UriTemplateHandler {
    URI expand(String uriTemplate, Map<String, ?> uriVariables);

    URI expand(String uriTemplate, Object... uriVariables);
}
```

##### 3.3.3 并发编程

不用再自己写countdownlaunch

```
Mono<Person> personMono = client.get().uri("/person/{id}", personId)
        .retrieve().bodyToMono(Person.class);

Mono<List<Hobby>> hobbiesMono = client.get().uri("/person/{id}/hobbies", personId)
        .retrieve().bodyToFlux(Hobby.class).collectList();

Map<String, Object> data = Mono.zip(personMono, hobbiesMono, (person, hobbies) -> {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("person", person);
            map.put("hobbies", hobbies);
            return map;
        })
        .block();
```

当接口返回差不多的时候，可以参考下面的并发，不过这种情况可能性很小

```
    public void doMultiThreadRequest() {
        List<String> urls = Arrays.asList("http://example.com", "http://example.org", "http://example.net");

        Flux.fromIterable(urls)
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(url -> webClient.get().uri(url).retrieve().bodyToMono(String.class))
                .sequential()
                .subscribe(response -> {
                    // 处理响应
                });
    }
```

##### 3.3.4 Mono&Flux

Mon接收单个

Flux接收集合

当响应的结果是JSON时，也可以直接指定为一个Object，WebClient将接收到响应后把JSON字符串转换为对应的对象。比如下面这样。

```
WebClient webClient = WebClient.create();
Mono<User> mono = webClient.get().uri("http://localhost:8081/user/1").retrieve().bodyToMono(User.class);
User user = mono.block();
```

如果响应的结果是一个集合，则不能继续使用bodyToMono()，应该改用bodyToFlux()，然后依次处理每一个元素，比如下面这样。

```
String baseUrl = "http://localhost:8081";
WebClient webClient = WebClient.create(baseUrl);
Flux<User> userFlux = webClient.get().uri("users").retrieve().bodyToFlux(User.class);
userFlux.subscribe(System.out::println);
```


如果需要通过Flux取到所有的元素构造为一个List，则可以通过如下的方式获取。

```
List<User> users = userFlux.collectList().block();
```

##### 3.3.5 Schedulers

![img](https://pic4.zhimg.com/v2-0af8ce0b8c6ee5023f7f1301bf209d8f_b.webp?consumer=ZHI_MENG)

- 如上当调用线程使用webclient发起请求后，内部会先创建一个Mono响应对象，然后切换到IO线程具体发起网络请求。
- 调用线程获取到Mono对象后，一般会订阅，也就是设置一个Consumer用来具体处理服务端响应结果。
- 服务端接受请求后，进行处理，最后把结果写回客户端，客户端接受响应后，使用IO线程把结果设置到Mono对象，从而触发设置的Consumer回调函数的执行。

注：WebClient默认内部使用Netty实现http客户端调用，这里IO线程其实是netty的IO线程，而**netty客户端的IO线程内是不建议做耗时操作的**，因为IO线程是用来轮训注册到select上的channel的数据的，如果阻塞了，那么其他channel的读写请求就会得不到及时处理。所以如果consumer内逻辑比较耗时，建议从IO线程切换到其他线程来做。

那么如何切换那？可以使用publishOn把IO线程切换到自定义线程池进行处理：

```
resp.publishOn(Schedulers.elastic())//切换到Schedulers.elastic()对应的线程池进行处理
                .onErrorMap(throwable -> {
                    System.out.println("onErrorMap:" + throwable.getLocalizedMessage());
                    return throwable;
                }).subscribe(s -> System.out.println("result:" + Thread.currentThread().getName() + " " + s));

......block()// 如果是block方式，则不会占用netty的io 默认的CPU个数的selector线程(只负责发请求)，而使用的是服务器tomcat的io线程处理后面逻辑
```

Reactor中Schedulers提供了几种内置实现：

- Schedulers.elastic():线程池中的线程是可以复用的,按需创建与空闲回收，该调度器适用于 I/O 密集型任务。
- Schedulers.parallel()：含有固定个数的线程池，该调度器适用于计算密集型任务。
- Schedulers.single():单一线程来执行任务
- Schedulers.immediate():立刻使用调用线程来执行。
- Schedulers.fromExecutor():使用已有的Executor转换为Scheduler来执行任务。

#### 3.5 底层依赖Netty库配置

配置参考 https://stackoverflow.com/questions/71347590/correct-way-of-using-spring-webclient-in-spring-amqp?r=SearchResults

通过定制Netty底层库，可以配置SSl安全连接，以及请求超时，读写超时等。这里需要注意一个问题，默认的连接池最大连接500。获取连接超时默认是45000ms，你可以配置成动态的连接池，就可以突破这些默认配置，也可以根据业务自己制定。包括Netty的select线程和工作线程也都可以自己设置

##### 3.5.1 ConnectionProvider

```
ConnectionProvider

int DEFAULT_POOL_MAX_CONNECTIONS = Integer.parseInt(System.getProperty("reactor.netty.pool.maxConnections", "" + Math.max(Runtime.getRuntime().availableProcessors(), 8) * 2));
long DEFAULT_POOL_ACQUIRE_TIMEOUT = Long.parseLong(System.getProperty("reactor.netty.pool.acquireTimeout", "45000"));
long DEFAULT_POOL_MAX_IDLE_TIME = Long.parseLong(System.getProperty("reactor.netty.pool.maxIdleTime", "-1"));
long DEFAULT_POOL_MAX_LIFE_TIME = Long.parseLong(System.getProperty("reactor.netty.pool.maxLifeTime", "-1"));

默认值
DEFAULT_POOL_MAX_CONNECTIONS=24
DEFAULT_POOL_ACQUIRE_TIMEOUT=45000
DEFAULT_POOL_MAX_IDLE_TIME=-1
DEFAULT_POOL_MAX_LIFE_TIME=-1
DEFAULT_POOL_LEASING_STRATEGY=fifo
```

```
ConnectionProvider connectionProvider = ConnectionProvider
.builder("fixed")
.lifo()
.pendingAcquireTimeout(Duration.ofMillis(200000))
.maxConnections(16)
.pendingAcquireMaxCount(3000)
.maxIdleTime(Duration.ofMillis(290000))
.build();
```

##### 3.5.2 参数设置

https://blog.csdn.net/weixin_44266223/article/details/122967933

maxConnections：最大连接数，默认最大连接数为处理器数量*2（但最小值为16），最大只能设置为200（其实就是取决于tomcat的nio线程数），超过这个数值设置无效。  

pendingAcquireMaxCount：等待队列大小，默认是最大连接数的2倍，等待队列pendingAcquireMaxCount调大，同时处理的任务数等于最大连接数，未被处理的任务在队列中等待处理。   **默认100**

pendingAcquireTimeout:任务等待超时时间，当队列中的任务等待超过pendingAcquireTimeout还获取不到连接，就会抛出异常。

```
等待队列超过会报错
Pending acquire queue has reached its maximum size of 100; nested exception is reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException: Pending acquire queue has reached its maximum size of 100
```

#### 3.6 流式API源码

采用interface的方式，能够按照 build模式增加属性，提升代码阅读性

```
public interface WebClient {
    WebClient.RequestHeadersUriSpec<?> get();

    WebClient.RequestHeadersUriSpec<?> head();

    WebClient.RequestBodyUriSpec post();
```

DefaultWebClient作为实现类

```
class DefaultWebClient implements WebClient {
```

#### 3.7 调用执行源码

##### 3.7.1 调用过程

```
Mono.block()    
this.subscribe((Subscriber)subscriber);  // 这行代码会初始化线程
subscriber.blockingGet() // 真正执行调用

[nio-8004-exec-1] io.netty.channel.nio.NioEventLoop        : instrumented a special java.util.Set into: sun.nio.ch.WindowsSelectorImpl
......
```

reactor-http-nio的12个线程在服务刚起来时不会创建，只有当第一次使用webclient的时候才会创建

##### 3.7.2 创建过程

reactor-http-nio的12个select线程 创建过程

```
NioEventLoopGroup
->
MultithreadEventLoopGroup
for 12 循环创建12个EventLoop
->
NioEventLoop
this.openSelector()  每个开启select  dubug可以在这里
```

请求第一次来

```
NameResolverProvider.newNameResolverGroup()
LoopResources  默认selectors数为CPU个数
int DEFAULT_IO_WORKER_COUNT = Integer.parseInt(System.getProperty("reactor.netty.ioWorkerCount", "" + Math.max(Runtime.getRuntime().availableProcessors(), 4)));
```

##### 3.7.3 ConnectionProvider

请求调用会调用线程池这个方法

```
ConnectionProvider.acquire()
->
HttpConnectionProvider.acquire()
->
NewConnectionProvider..acquire()
->
```





#### 3.8 异常

##### 3.8.1 WebClientResponseException

weblient 调用错误都是这个异常WebClientResponseException

默认的情况，会把请求返回的body给过滤掉，getMessage会不一样

比如401返回

```
{
	"result": "invalid username or password.",
	"status": "failed"
}
```

weblient默认messgae会过滤为   源码WebClientResponseException.initMessage()

```
401 Unauthorized from POST https://apigw.huawei.com/api/ssoproxysvr/v2/tokens
```

这样会获取不到返回的 body  如果是**网关**透传服务

##### 3.8.2 透传异常

https://stackoverflow.com/questions/71643036/getting-the-response-body-in-error-case-with-spring-webclient

To suppress the treatment of a status code as an error and process it as a normal response, return Mono.empty() from the function. The response will then propagate downstream to be processed.

1.如果返回 401+body 这段代码会照样返回全部response，但是如果try catch这段代码是接收不到异常的

```
var response = webClient
           .method()
           .retrieve()
           .onStatus(HttpStatus::isError, clientResponse -> Mono.empty())
           .toEntity(String.class)
```

2.这种情况是把respose组合成一个新的exception，这样外层的try catch就能捕获到

```
.onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(String.class)
    .flatMap(message -> Mono.error(new RuntimeException(message))))
    .toEntity(bodyClass).block()
```

3. 其他参考  https://stackoverflow.com/questions/44593066/spring-webflux-webclient-get-body-on-error

```java
    public Mono<Void> cancel(SomeDTO requestDto) {
        return webClient.post().uri(SOME_URL)
                .body(fromObject(requestDto))
                .header("API_KEY", properties.getApiKey())
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(log, response);
                    return Mono.error(new IllegalStateException(
                            String.format("Failed! %s", requestDto.getCartId())
                    ));
                })
                .bodyToMono(Void.class)
                .timeout(timeout);
    }
```

And:

```java
    public static void logTraceResponse(Logger log, ClientResponse response) {
        if (log.isTraceEnabled()) {
            log.trace("Response status: {}", response.statusCode());
            log.trace("Response headers: {}", response.headers().asHttpHeaders());
            response.bodyToMono(String.class)
                    .publishOn(Schedulers.elastic())
                    .subscribe(body -> log.trace("Response body: {}", body));
        }
    }
```

```
Mono<ClientResponse> responseMono = requestSpec.exchange()
            .doOnNext(response -> {
                HttpStatus httpStatus = response.statusCode();
                if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
                    throw new WebClientException(
                            "ClientResponse has erroneous status code: " + httpStatus.value() +
                                    " " + httpStatus.getReasonPhrase());
                }
            });
```

###### 3.8.2.1 全局配置

直接在weblient定义的时候就指定抛出异常， 上面的都是在请求的时候做操作，这里可以全局

```
WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
   .filter(errorHandler()).build();

public static ExchangeFilterFunction errorHandler() {
    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
        if (clientResponse.statusCode().is5xxServerError()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)));
        } else if (clientResponse.statusCode().is4xxClientError()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)));
        } else {
            return Mono.just(clientResponse);
        }
    });
}
```

##### 3.8.3 推荐做法-不抛出异常

```java
  webClient.post()
            .uri("http://localhost:9000/api")
            .body(BodyInserters.fromValue(notification))
            .retrieve()
      		// 不抛出异常
      		.onStatus(HttpStatus::isError, clientResponse -> Mono.empty()))
      		// 自定义异常，异常信息
            // .onStatus(HttpStatus::isError, clientResponse -> Mono.error(NotificationException::new))
            .toBodilessEntity()
            .block();
```

#### 3.9 httpClient

weblient封装了httpclient(使用reactor-netty，自己也能发请求)

weblient API更加丰富

#### 3.10 问题记录

##### 3.10.1 线程初始化

reactor-http-nio的12个线程在服务刚起来时不会创建，只有当第一次使用webclient的时候才会创建，如果还没创建，1s并发5000以上可能直接报错

**Pending acquire queue has reached its maximum size of 1000**

##### 3.10.2 failed: Connection reset by peer

```
recvAddress(..) failed: Connection reset by peer; nested exception is io.netty.channel.unix.Errors$NativeIoException: recvAddress(..) failed: Connection reset by peer
```

```
The connection observed an error, the request cannot be retried as the headers/body were sent
io.netty.channel.unix.Errors$NativeIoException: readAddress(..) failed: Connection reset by peer
```

**解决方案** ： https://github.com/reactor/reactor-netty/issues/1774

https://projectreactor.io/docs/netty/release/reference/index.html#_connection_pool_2

客户端由于连接池的复用，之前请求成功的连接可能还放在连接池，但是目标服务端过了某段时间就会断开连接；

此时客户端的连接复用，会导致  connection reset by peer

如果一端的Socket被关闭，另一端仍发送数据，发送的第一个数据包引发该异常

1. idleTimeout 表示数据库连接在数据库连接池中最大的闲置时间。描述是 600000 （十分钟）。
2. maxLifetime 表示连接池中连接最大的声明周期。默认是 1800000 （30分钟）。

**自定义定义线程池**

```
ConnectionProvider provider = ConnectionProvider.builder("webClient").maxConnections(100)
    .maxIdleTime(Duration.ofSeconds(20)) // 规定空闲超时时间
    .maxLifeTime(Duration.ofSeconds(60))
    .pendingAcquireTimeout(Duration.ofSeconds(60))
    .evictInBackground(Duration.ofSeconds(120))
    .build();
```

**Connection reset by peer的常见原因：**

1，如果一端的Socket被关闭（或主动关闭，或因为异常退出而 引起的关闭），另一端仍发送数据，发送的第一个数据包引发该异常(Connect reset by peer)。

Socket默认连接60秒，60秒之内没有进行心跳交互，即读写数据，就会自动关闭连接。

客户端15分钟超时，而服务端60秒超时，tcp通过3次握手建立连接，4次握手关闭连接，可以看第三个箭头指明的位置，50657端口连接了8090端口。建立连接过程是正常的，而关闭链接时只是服务端一厢情愿的发了个Fin包，客户端没有回应Fin包（此时连接已不可用），如果httpclient使用这个不可用的连接发送请求就会反生not response异常。等过了15分钟，客户端Fin，服务端说，这个连接不存在啊，Reset吧。

2，一端退出，但退出时并未关闭该连接，另一端如果在从连接中读数据则抛出该异常（Connection reset）。



1）服务器的并发连接数超过了其承载量，服务器会将其中一些连接关闭；(可参考提高服务器并发tcp连接数)

如果知道实际连接服务器的并发客户数没有超过服务器的承载量，则有可能是中了病毒或者木马，引起网络流量异常。可以使用netstat -an查看网络连接情况。

2）客户关掉了浏览器，而服务器还在给客户端发送数据；

3）浏览器端按了Stop；

这两种情况一般不会影响服务器。但是如果对异常信息没有特别处理，有可能在服务器的日志文件中，重复出现该异常，造成服务器日志文件过大，影响服务器的运行。可以对引起异常的部分，使用try…catch捕获该异常，然后不输出或者只输出一句提示信息，避免使用e.printStackTrace();输出全部异常信息。

4）防火墙的问题；

如果网络连接通过防火墙，而防火墙一般都会有超时的机制，在网络连接长时间不传输数据时，会关闭这个TCP的会话，关闭后在读写，就会导致异常。 如果关闭防火墙，解决了问题，需要重新配置防火墙，或者自己编写程序实现TCP的长连接。实现TCP的长连接，需要自己定义心跳协议，每隔一段时间，发送一次心跳协议，双方维持连接。

##### 3.10.3  Exceeded limit on max bytes to buffer

org.springframework.core.io.buffer.DataBufferLimitException: Exceeded limit on max bytes to buffer : 262144

响应体超大小 默认 256K左右

```
WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))  // 官网一般是2M就够了，自己定义
    .build();
```

##### 3.10.4 请求chatset

.acceptCharset(StandardCharsets.UTF_8)  没用，只是请求服务器返回utf-8的编码集

凡是如果服务器返回  application/json   默认还是 iso编码集，如果客户端不自己转码，那么返回给客户就会是乱码，如果是restTemplate那就是     HttpMessageConverter





### 4.消息编解码器-restTemplate

可参考2.4里面有编码器的运行原理，是在执行请求的过程前执行编码器

charset不一样  byte[]都不一样

#### 4.1  HttpMessageConverter

https://www.baeldung.com/spring-httpmessageconverter-rest

By default, the following *HttpMessageConverter*s instances are pre-enabled:

- *ByteArrayHttpMessageConverter* – converts byte arrays
- ***StringHttpMessageConverter*** – converts Strings
- *ResourceHttpMessageConverter* – converts *org.springframework.core.io.Resource* for any type of octet stream
- *SourceHttpMessageConverter* – converts *javax.xml.transform.Source*
- *FormHttpMessageConverter* – converts form data to/from a *MultiValueMap<String, String>*
- *Jaxb2RootElementHttpMessageConverter* – converts Java objects to/from XML (added only if JAXB2 is present on the classpath)
- *MappingJackson2HttpMessageConverter* – converts JSON (added only if Jackson 2 is present on the classpath)*
  *
- *MappingJacksonHttpMessageConverter* – converts JSON (added only if Jackson is present on the classpath)
- *AtomFeedHttpMessageConverter* – converts Atom feeds (added only if Rome is present on the classpath)
- *RssChannelHttpMessageConverter* – converts RSS feeds (added only if Rome is present on the classpath)

#### 4.2 WebMvcConfigurer

```
https://apigw.huawei.com/api/ssoproxysvr
/**
 *desc: 这个对的调用返回controller 生效
 * 如加上这个，restTemplate返回text/plain;charset=ISO-8859-1 会全部改为  text/plain;charset=UTF-8
 **/
@Configuration
@EnableWebMvc
public class MessageConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter httpMessageConverter : converters) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}
```

### 5. WebMvcConfigurationSupport

```
@Configuration
@Profile("!llt")
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(), HttpMethod.PATCH.name());
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(....implements  AsyncHandlerInterceptor);
        registry.addInterceptor(.....implements  AsyncHandlerInterceptor);
        super.addInterceptors(registry);
    }
```

### 6. Resource

使用这个返回的exchange content-type默认是application/json(utf-8)

```
ResponseEntity<Resource> exchange = restTemplates.exchange("http://127.0.0.1:8003/xxxx", HttpMethod.GET, null,
        Resource.class);
```

### 7. 问题记录

#### 7.1 ISO-8859-1不支持中文

由于没有配置这个，controller返回 String 字符串会默认是 text/plain;chaset=ios-8859-1

```
@Configuration
@EnableWebMvc
public class MessageConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter httpMessageConverter : converters) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}
```

而spring 的 restTemlate返回Resource.class 默认按 content-type是application/json;utf-8去转换

如果不用Rescoure也会造成中文字符串解析不了，所以controller可以加上面的配置类 或者 规定返回格式

```
@GetMapping(path = "xxx/body", produces = "application/json") // text/plain;chaset=utf-8
```

如果是weblient   加上 .acceptCharset(StandardCharsets.UTF_8) 即可

#### 7.2 gzip问题

```
HttpMessageNotReadableException: JSON parse error: Illegal character ((CTRL-CHAR, code 31)): only regular white space (\r, \n, \t) is allowed between tokens; 
nested exception is com.fasterxml.jackson.core.JsonParseException: Illegal character ((CTRL-CHAR, code 31)): only regular white space (\r, \n, \t) is allowed between tokens
```

请求头里包含  accept-encoding = gzip 

A->B->C

B服务加上这个gzip请求头，得到response给A的时候，A会解析不了报错

去掉请求头后，webclient就可以不用加accept=application/json了

#### 7.3 host 503问题

如果用postman调用，然后再调另一个接口时如果继承了postman的header(host) 会返回503  Service not available

去掉请求头即可

### 8. Controller相关

#### 8.1 返回格式增加默认参数

```
{
    "msg": "SUCCESS",
    "code": 200,
    "data": [
        {
            "id": 1,
            "roleDesktopSign": "DE",
            "roleDesktopDesc": "开发工程师",
            "cardLayout": "[{\"h\": \"2\"}]"
        }
    ]
}
```

默认增加 msg + code , body 就放在data里

```
@RestControllerAdvice(basePackages = {"com.lin.api"})
public class ResultResponseAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }
    @Override
    public Object beforeBodyWrite(Object returnValue, MethodParameter returnType, MediaType mediaType, Class aClass,
        ServerHttpRequest request, ServerHttpResponse response) {
        MyResponse response;
        // 获取方法的返回类型
        String returnClassType = returnType.getParameterType().getSimpleName();
        switch (returnClassType) {
            case "String":
            case "AjaxResult":
                return returnValue;
            case "void":
                return MyResponse.success();
            case "PageDomain":
                return MyResponse.page((PageDomain) returnValue);
            default:
                if (Objects.nonNull(returnValue)) {
                    result = MyResponse.success(returnValue);
                } else {
                    result = MyResponse.success();
                }
                return result;
        }
    }
}
```

#### 8.2 接收text/plain的请求

添加consumes = MediaType.TEXT_PLAIN_VALUE，添加RequestBody 一般只有String或byte[]

如果url一样，springmvc可能找不到报错：Ambiguous handler methods mapped for

所以最好URL也可以不同

```
@GetMapping("/**")
public void proxyGet(@RequestParam(required = false) Map<String, String> param,
    @RequestBody(required = false) Object body, HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    proxy(param, body, request, response, HttpMethod.GET);
}
```

```
@GetMapping(value = "/text/**", consumes = MediaType.TEXT_PLAIN_VALUE)
public void proxyGetText(@RequestParam(required = false) Map<String, String> param,
    @RequestBody(required = false) byte[] text, HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    proxy(param, text, request, response, HttpMethod.GET);
}
```

## 三、Spring

### 1. Swagger

#### 1.1 swagger2

```
<!--Swagger2  原生版本  localhost:8080/swagger-ui/index.html    -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.9.2</version>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.9.2</version>
        </dependency>
```

#### 1.2 knife4j

代码仓  https://gitee.com/xiaoym/knife4j

官网  https://doc.xiaominfo.com/

```
@Configuration
@Profile("!llt")
public class WebMvcConfiguration extends WebMvcConfigurationSupport {


    /**
     * 如果继承了WebMvcConfigurationSupport
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
```

```
import com.google.common.collect.Sets;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    /**
     *  路径${requestBaseUrl}/doc.html, 扫描@RestController的handler
     *
     * @return the docket
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).produces(Sets.newHashSet("application/json"))
            .select().apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).paths(PathSelectors.any())
            .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("xxxx").description(" xxxxxx接口文档")
            .version("1.0.0").build();
    }
}
```

#### 1.3 spring-fox源码学习

https://doc.xiaominfo.com/docs/action/springfox/springfox3

查看 spring源码那个文件

### 2. 事务

2.1 tr

## 四、spring-cloud

### 4.1 Flux

[Flux、Mono、Reactor 实战（史上最全）_reactor mono-CSDN博客](https://blog.csdn.net/crazymakercircle/article/details/124120506?spm=1001.2101.3001.6650.3&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~Rate-3-124120506-blog-124292452.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~Rate-3-124120506-blog-124292452.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=6)

基于Pblisher机制，只有订阅了才会真正执行

Flux
Flux 是一个发出(emit)0-N个元素组成的异步序列的Publisher,可以被onComplete信号或者onError信号所终止。

在响应流规范中存在三种给下游消费者调用的方法 onNext, onComplete, 和onError


### 4.2 Mono

Mono 是一个发出(emit)`0-1`个元素的Publisher,可以被`onComplete`信号或者`onError`信号所终止

```
        healthMono.doOnNext(health->{
            System.out.println(health.getStatus());
            if (!health.getStatus().equals(Status.UP)){

            }
        }).subscribe(); // 订阅才会执行
```

## 五. flyway自动建表

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ums_bd1?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  flyway:
    baselineOnMigrate: false   # 是否启动项目时创建表创建

```

![image-20210413174616245](C:\Users\cool\AppData\Roaming\Typora\typora-user-images\image-20210413174616245.png)

```
  <dependency>
  	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-core</artifactId>
  </dependency>
```

