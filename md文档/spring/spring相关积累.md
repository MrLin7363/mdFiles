spring相关积累

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
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
 
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

##### 2.2.1 httpclient poll源码分析

```
AbstractConnPool.getPoolEntryBlocking()
...
// 应该是每个路由一个pool
RouteSpecificPool pool = this.getPool(route);
...
if (pool.getAllocatedCount() < maxPerRoute) {
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

### 3. webClient

官网： https://docs.spring.io/spring-framework/docs/5.3.28/reference/html/

中文指南：https://docs.flydean.com/spring-framework-documentation5/webreactive/2.webclient

好博客：https://blog.csdn.net/zzhongcy/article/details/105412842

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
    @Bean("webClient")
    public WebClient webClient() {
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

            SslContext sslContext = SslContextBuilder.forClient().trustManager(trustAllCerts[0]).build();

            // ConnectionProvider connectionProvider = ConnectionProvider.builder("fixed")
            //     .pendingAcquireTimeout(Duration.ofMillis(10000))
            //     .maxConnections(50)
            //     .pendingAcquireMaxCount(3000)
            //     .build();

            // HttpClient httpClient = HttpClient.create(connectionProvider)
            HttpClient httpClient = HttpClient.create(connectionProvider)
                .secure(t -> t.sslContext(sslContext))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30)));
            WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
            // .codecs(configurer -> {
            //     // configurer.customCodecs().register(...);
            //     configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024);
            // })
            return webClient;
        } catch (SSLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

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
            .accept(MediaType.APPLICATION_JSON)
            .acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
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

#### 3.9 注意事项

reactor-http-nio的12个线程在服务刚起来时不会创建，只有当第一次使用webclient的时候才会创建，如果还没创建，1s并发5000以上可能直接报错

**Pending acquire queue has reached its maximum size of 1000**

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

### 7.踩坑

#### ISO-8859-1不支持中文

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

