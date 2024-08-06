tomcat

spring-boot内嵌了tomcat

## 一、spring-boot默认tomcat配置

下面是 Spring Boot 中内嵌 Tomcat 的一些默认配置：

1. Tomcat 默认监听 8080 端口。您可以通过在 application.properties 文件中设置 server.port 属性来更改默认端口。
2. Tomcat 默认使用 UTF-8 编码。如果您需要更改编码，可以在 application.properties 文件中设置 server.tomcat.uri-encoding 属性。
3. 默认情况下，Tomcat 的最大线程数为 200。如果您需要更改此设置，可以在 application.properties 文件中设置 server.tomcat.max-threads 属性。
4. Tomcat 默认使用 HTTP/1.1 协议。如果您需要更改协议版本，可以在 application.properties 文件中设置 server.tomcat.protocol 属性。
5. 默认情况下，Tomcat 不会显示目录列表。如果您需要显示目录列表，可以在 application.properties 文件中设置 server.tomcat.directory-listing.enabled 属性为 true。

```
ServerProperties.Tomcat
```

```
默认值
public Tomcat() {
    this.uriEncoding = StandardCharsets.UTF_8;
    this.maxConnections = 8192;
    this.acceptCount = 100;  
    this.processorCache = 200;
    this.maxKeepAliveRequests = 100; // 线程存活时间 秒
    ......
}

public static class Threads {
            private int max = 200; // 最大线程数
            private int minSpare = 10;  // 最小线程数
```

```text
server:
  tomcat:
    accept-count: 500 //accept队列长度
    max-connections: 1000//最大连接数
    threads:
      max: 200 //最大工作线程数量
      min-spare: 10 //最小工作线程数量
    keep-alive-timeout: 5000
    connection-timeout: 3000  #server端的socket超时间，默认60s
```

HTTP Connector

其工作流程如下：

1. 每个非异步请求都需要一个线程来处理，如果并发请求大于当前可处理的线程数量，则会创建额外的线程来处理，至多创建到maxThreads 的数量。
2. 此时仍然接收到更多的并发请求，Tomcat会接受新的connection，直到connection数到达最大数maxConnections。此时这些connection会在Connector中创建的 server socket中排队，直到有线程可以来处理这些connection。
3. 一旦上面的排队数量达到maxConnections，然后还有新的请求进来，那么新进来的connection会在OS中排队，操作系统提供的排队数量为acceptCount。如果这个队列满了的话，后面进来的请求有可能被拒绝或者超时timeout

```
min-spare: 10
会初始化10个tamcat线程，这个nio就是
http-nio-8004-exec-1
http-nio-8004-exec-2
...
http-nio-8004-exec-10
```

### 源码

https://github.com/guobinhit/cg-blog/blob/master/articles/others/spring-boot-nested-tomcat-principle.md



### visualVm查看tomcat线程

可以看并发的情况下，tomcat默认最大创建200个线程，当线程无任务超过100s后，自动销毁，保留最小线程数

