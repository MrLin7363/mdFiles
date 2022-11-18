netty

参考指南

笔记版：

https://bright-boy.gitee.io/technical-notes/#/%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B/netty?id=%e7%ba%bf%e7%a8%8b%e6%b1%a0%e7%89%88%e8%ae%be%e8%ae%a1

B站： 

https://www.bilibili.com/video/BV1py4y1E7oA/?p=3&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b

netty聊天室项目： https://toscode.gitee.com/ni-zewen/netty-chat-room/

# 一、基础

## 1.概述

### 1.1 netty的地位

Netty 在 Java 网络应用框架中的地位就好比：Spring 框架在 JavaEE 开发中的地位

以下的框架都使用了 Netty，因为它们有网络通信需求！

- Cassandra - nosql 数据库
- Spark - 大数据分布式计算框架
- Hadoop - 大数据分布式存储框架
- RocketMQ - ali 开源的消息队列
- ElasticSearch - 搜索引擎
- gRPC - rpc 框架
- Dubbo - rpc 框架
- Spring 5.x - flux api 完全抛弃了 tomcat ，使用 netty 作为服务器端
- Zookeeper - 分布式协调框架

### 1.2 netty的优势

- Netty vs NIO，工作量大，bug 多
  - 需要自己构建协议
  - 解决 TCP 传输问题，如粘包、半包
  - epoll 空轮询导致 CPU 100%
  - 对 API 进行增强，使之更易用，如 FastThreadLocal => ThreadLocal，ByteBuf => ByteBuffer
- Netty vs 其它网络应用框架
  - Mina 由 apache 维护，将来 3.x 版本可能会有较大重构，破坏 API 向下兼容性，Netty 的开发迭代更迅速，API 更简洁、文档更优秀
  - 久经考验，16年，Netty 版本
    - 2.x 2004
    - 3.x 2008
    - 4.x 2013
    - 5.x 已废弃（没有明显的性能提升，维护成本高）

## 2. hellowrold

开发一个简单的服务器端和客户端

- 客户端向服务器端发送 hello, world
- 服务器仅接收，不返回

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.39.Final</version>
</dependency>
```

**服务器端**

```java
public class HelloServer {
    public static void main(String[] args) {
        // 1. 启动器，负责组装nettry组件，启动服务器
        new ServerBootstrap()
            // 2. BossEventLoop,workerEventLoop(selector,thread), group 组
            .group(new NioEventLoopGroup()) // 16. 由某个Eventloop处理read事件，接收到ByteBuffer
            // 3. 选择服务器的ServerSocketChannel实现
            .channel(NioServerSocketChannel.class)
            // 4. boss 负责处理连接 worker(child) 负责处理读写，决定了worker(child)能执行什么
            .childHandler(
                // 5. channel 代表和客户端进行数据读写的通道初始化
                new ChannelInitializer<NioSocketChannel>() {

                    // 12. 连接建立后，调用初始化方法
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 17. SocketChannel 的处理器，解码 ByteBuf => String
                        ch.pipeline().addLast(new StringDecoder());
                        // SocketChannel 的处理器，解码 ByteBuf => String
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                            // 18. 执行 read方法，打印Hello
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                }
            )
            // 6. ServerSocketChannel 绑定的监听端口
            .bind(8080);
    }
```

**客户端**

```
public class HelloClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        // 7.
        new Bootstrap()
            // 8.创建 NioEventLoopGroup，同 Server
            .group(new NioEventLoopGroup())
            // 9.选择客户 Socket 实现类，NioSocketChannel 表示基于 NIO 的客户端实现
            .channel(NioSocketChannel.class)
            // 10.添加 SocketChannel 的处理器，ChannelInitializer 处理器（仅执行一次）
            //，它的作用是待客户端 SocketChannel 建立连接后，执行 initChannel 以便添加更多的处理器
            .handler(new ChannelInitializer<Channel>() {

                // 12. 连接建立后调用
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    // 15. 消息会经过通道 handler 处理，这里是将 String => ByteBuf 发出
                    ch.pipeline().addLast(new StringEncoder());
                }
            })
            // 11. 连接到服务器
            .connect("localhost", 8080)
            // 13. 阻塞方法，直到连接建立
            .sync() // Netty 中很多方法都是异步的，如 connect，这时需要使用 sync 方法等待 connect 建立连接完毕
            .channel() // 获取 channel 对象，它即为通道抽象，可以进行数据读写操作
            .writeAndFlush(new Date() + ": hello world!"); // 14.发送消息并清空缓冲区
        System.in.read();
    }
}
```

一开始需要树立正确的观念

- 把 channel 理解为数据的通道
- 把 msg 理解为流动的数据，最开始输入是 ByteBuf，但经过 pipeline 的加工，会变成其它类型对象，最后输出又变成 ByteBuf
- 把 handler 理解为数据的处理工序
  - 工序有多道，合在一起就是 pipeline，pipeline 负责发布事件（读、读取完成...）传播给每个 handler， handler 对自己感兴趣的事件进行处理（重写了相应事件处理方法）
  - handler 分 Inbound 和 Outbound 两类
- 把 eventLoop 理解为处理数据的工人
  - 工人可以管理多个 channel 的 io 操作，并且一旦工人负责了某个 channel，就要负责到底（绑定）
  - 工人既可以执行 io 操作，也可以进行任务处理，每位工人有任务队列，队列里可以堆放多个 channel 的待处理任务，任务分为普通任务、定时任务
  - 工人按照 pipeline 顺序，依次按照 handler 的规划（代码）处理数据，可以为每道工序指定不同的工人

## 3. 组件

### 3.1 EventLoop

事件循环对象

EventLoop 本质是一个单线程执行器（同时维护了一个 Selector），里面有 run 方法处理 Channel 上源源不断的 io 事件。

它的继承关系比较复杂

- 一条线是继承自 j.u.c.ScheduledExecutorService 因此包含了线程池中所有的方法
- 另一条线是继承自 netty 自己的 OrderedEventExecutor，
  - 提供了 boolean inEventLoop(Thread thread) 方法判断一个线程是否属于此 EventLoop
  - 提供了 parent 方法来看看自己属于哪个 EventLoopGroup

事件循环组

EventLoopGroup 是一组 EventLoop，Channel 一般会调用 EventLoopGroup 的 register 方法来绑定其中一个 EventLoop，后续这个 Channel 上的 io 事件都由此 EventLoop 来处理（保证了 io 事件处理时的线程安全）

- 继承自 netty 自己的 EventExecutorGroup
  - 实现了 Iterable 接口提供遍历 EventLoop 的能力
  - 另有 next 方法获取集合中下一个 EventLoop

##### 3.1.1 普通任务:

```
public class TestEventLoop {
    public static void main(String[] args) {
        // 内部创建了两个 EventLoop, 每个 EventLoop 维护一个线程
        EventLoopGroup group = new NioEventLoopGroup(2); // io任务,普通任务,定时任务
        //        EventLoopGroup group = new DefaultEventLoopGroup(2); // 普通任务,定时任务
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next()); // 循环会回到第一个,也可以使用增强for循环一次读完
        // 1.执行普通任务
        group.next().execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("普通任务");
        });

        // 2.定时任务
        group.next().scheduleAtFixedRate(()->{
            System.out.println("scheduleAtFixedRate");
        },1,1, TimeUnit.SECONDS); // 延迟1s,间隔1s
        System.out.println("main");
    }
}
```

##### 3.1.2  IO任务 1

**每个eventloop处理一个channel不会变,建立绑定关系， 但是绑定后channel只会由这个loop管理**

```
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        new ServerBootstrap()
            // boss只负责accept 事件, worker只负责socketChannel上读写,1个loop可以管理多个channel，但是绑定后channel只会由这个loop管理
            .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
            // NioServerSocketChannel只有1个，只会占用 1个NioEventLoopGroup
            .channel(NioServerSocketChannel.class)
            .childHandler(
                new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                // 每个eventloop处理一个channel不会变,建立绑定关系
                                log.debug(byteBuf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                }
            )
            .bind(8080);
    }
}
```

```
public class EventClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        Channel channel = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {

                // 12. 连接建立后调用
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringEncoder());
                }
            })
            .connect("localhost", 8080)
            .sync()
            .channel();
        System.out.println(channel);
        // 多个channel发 channel.writeAndFlush("hello")
        // 左侧dubug 线程改为只阻塞当前main线程而不是All
        System.in.read();
    }
}
```

```
14:47:35.648 [nioEventLoopGroup-2-6] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
14:47:54.118 [nioEventLoopGroup-2-6] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
14:47:54.270 [nioEventLoopGroup-2-6] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
14:47:54.443 [nioEventLoopGroup-2-6] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
14:48:33.784 [nioEventLoopGroup-2-7] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
14:48:33.913 [nioEventLoopGroup-2-7] DEBUG com.lin.netty.netty.module.EventLoopServer - hello
```

##### 3.1.2  IO任务 2

添加其他eventloop处理大事件

```
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();
        new ServerBootstrap()
            // boss只负责accept 事件, worker只负责socketChannel上读写,1个loop可以管理多个channel，但是绑定后channel只会由这个loop管理
            .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
            // NioServerSocketChannel只有1个，只会占用 1个NioEventLoopGroup
            .channel(NioServerSocketChannel.class)
            .childHandler(
                new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                // 每个eventloop处理一个channel不会变,建立绑定关系
                                log.debug(byteBuf.toString(Charset.defaultCharset()));
                                // 传递给下一个处理
                                ctx.fireChannelRead(msg);
                            }
                        });

                        // 假如这个IO处理很久，可以新建个eventloop处理，不影响主要的eventloop
                        ch.pipeline().addLast(defaultEventLoop, new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.debug(byteBuf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                }
            )
            .bind(8080);
    }
}
```
