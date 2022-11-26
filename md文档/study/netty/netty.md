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

### 3.2 Channel

channel 的主要作用

- close() 可以用来关闭 channel
- closeFuture() 用来处理 channel 的关闭
  - sync 方法作用是同步等待 channel 关闭
  - 而 addListener 方法是异步等待 channel 关闭
- pipeline() 方法添加处理器
- write() 方法将数据写入
- writeAndFlush() 方法将数据写入并刷出

#### 3.2.1 ChannelFuture

主要处理连接

```
// EventLoopServer
@Slf4j
public class ChannelFutureClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 带 future ,promise都是异步方法配套使用,用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringEncoder());
                }
            })
            // 1.连接到服务器， 异步非阻塞,main 发起调用, 真正执行连接的是另一个nio线程NioEventLoopGroup
            .connect("localhost", 8080); // 1s后连接，但是主线程如果不阻塞，main瞬间获取channel（空）

        // 2.1 使用sync main主线程阻塞等待nio建立完连接
        //        channelFuture.sync();
        //        Channel channel = channelFuture.channel();
        //        channel.writeAndFlush("hello");

        // 2.2 使用addListener方法处理异步结果， 主线程也不阻塞,
        channelFuture.addListener(new ChannelFutureListener() {
            // 这个对象传递给nio线程,连接建立好调用operationComplete
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                 Channel channel = channelFuture.channel();
                log.debug("channel = {}", channel);
                channel.writeAndFlush("hello");
            }
        });
    }
}
```

#### 3.2.2 CloseFuture

```
@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringEncoder());
                }
            })
            .connect("localhost", 8080);
        Channel channel = channelFuture.sync().channel();
        log.debug("{}",channel);

        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close(); // close 异步操作 1s 之后
                    // log.debug("处理关闭之后的操作"); // 不能在这里善后
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 获取 CloseFuture 对象， 1) 同步处理关闭， 2) 异步处理关闭
        ChannelFuture closeFuture = channel.closeFuture();
        /*log.debug("waiting close...");
        closeFuture.sync();
        log.debug("处理关闭之后的操作");*/
        closeFuture.addListener((ChannelFutureListener) future -> {
            log.debug("处理关闭之后的操作");
            group.shutdownGracefully(); // 如果不关闭group,idea客户端不会停止
        });
    }
}
```

#### 3.2.3 异步提升的是什么

netty 异步提升的是吞吐量（单位时间内能处理的请求的个数）

要点

- 单线程没法异步提高效率，必须配合多线程、多核 cpu 才能发挥异步的优势
- 异步并没有缩短响应时间，反而有所增加
- 合理进行任务拆分，也是利用异步的关键

### 3.3 Future & Promise

在异步处理时，经常用到这两个接口

首先要说明 netty 中的 Future 与 jdk 中的 Future 同名，但是是两个接口，netty 的 Future 继承自 jdk 的 Future，而 Promise 又对 netty Future 进行了扩展

- jdk Future 只能同步等待任务结束（或成功、或失败）才能得到结果
- netty Future 可以同步等待任务结束得到结果，也可以异步方式得到结果，但都是要等任务结束
- netty Promise 不仅有 netty Future 的功能，而且脱离了任务独立存在，只作为两个线程间传递结果的容器

| 功能/名称    | jdk Future                     | netty Future                                                 | Promise      |
| ------------ | ------------------------------ | ------------------------------------------------------------ | ------------ |
| cancel       | 取消任务                       | -                                                            | -            |
| isCanceled   | 任务是否取消                   | -                                                            | -            |
| isDone       | 任务是否完成，不能区分成功失败 | -                                                            | -            |
| get          | 获取任务结果，阻塞等待         | -                                                            | -            |
| getNow       | -                              | 获取任务结果，非阻塞，还未产生结果时返回 null                | -            |
| await        | -                              | 等待任务结束，如果任务失败，不会抛异常，而是通过 isSuccess 判断 | -            |
| sync         | -                              | 等待任务结束，如果任务失败，抛出异常                         | -            |
| isSuccess    | -                              | 判断任务是否成功                                             | -            |
| cause        | -                              | 获取失败信息，非阻塞，如果没有失败，返回null                 | -            |
| addLinstener | -                              | 添加回调，异步接收结果                                       | -            |
| setSuccess   | -                              | -                                                            | 设置成功结果 |
| setFailure   | -                              | -                                                            | 设置失败结果 |

#### 3.3.1  jdk-future

```
@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1500);
                return 50;
                // 任务执行完了，线程会把信息填入future
            }
        });
        log.debug("等待结果");
        // 主线程通过future获取结果，阻塞在get
        log.debug("结果是{}", future.get());
    }
}
```

#### 3.3.2  netty-future

```
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1500);
                return 50;
                // 任务执行完了，线程会把信息填入future
            }
        });
        // 1.同步方式
//        log.debug("等待结果");
//        log.debug("结果是{}", future.get());

        // 2. 异步方式
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                // getNow无就返回null,不阻塞，因为进入监听器是已经结束
                log.debug("结果是{}",future.getNow());
            }
        });
        log.debug("exe...");
    }
}
```

#### 3.3.3 netty-promise

```
@Slf4j
public class TestNettyPromise {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        EventLoop eventLoop = new NioEventLoopGroup().next();

        // 结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            log.debug("开始计算...");
            try {
                int i = 1 / 0;
                Thread.sleep(1000);
                promise.setSuccess(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
                promise.setFailure(e);
            }
        }).start();

        // 接收结果的线程，异常也会接收到
        log.debug("等待结果...");
        log.debug("结果是:{}", promise.get());
    }

}
```

### 3.2 Handler & Pipeline

ChannelHandler 用来处理 Channel 上的各种事件，分为入站、出站两种。所有 ChannelHandler 被连成一串，就是 Pipeline

- 入站处理器通常是 ChannelInboundHandlerAdapter 的子类，主要用来读取客户端数据，写回结果
- 出站处理器通常是 ChannelOutboundHandlerAdapter 的子类，主要对写回结果进行加工

打个比喻，每个 Channel 是一个产品的加工车间，Pipeline 是车间中的流水线，ChannelHandler 就是流水线上的各道工序，而后面要讲的 ByteBuf 是原材料，经过很多工序的加工：先经过一道道入站工序，再经过一道道出站工序最终变成产品

先搞清楚顺序，服务端

```
/**
 * CloseFutureClient 运行客户端
 *
 * ChannelHandlerContext write  从当前节点往前找
 * Channel write   从tail往前找
 */
public class TestPipeline {

    public static void main(String[] args) {
        new ServerBootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                protected void initChannel(NioSocketChannel ch) {
                    // hear 1 > 2 > 3 > 4 > 5 > 6 > tail
                    // 4 5 6 是出站操作，代码里控制了从后往前
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println(1);
                            // 将数据传递给下一个hanlder,如果不传递，则链路断开
                            // = ctx.fireChannelRead(msg)
                            super.channelRead(ctx, msg); // 1
                        }
                    });
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            System.out.println(2);
                            ctx.fireChannelRead(msg); // 2
                        }
                    });
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            System.out.println(3);
                            // ch.write channel是从tail找出站处理器, 如果处理顺序不对，会找不到执行链路执行下去
                            ch.write(msg); // 3
                        }
                    });
                    ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg,
                            ChannelPromise promise) throws Exception {
                            System.out.println(4);
                            // = ctx.write  是向前找出站处理器
                            super.write(ctx, msg, promise); // 4
                        }
                    });
                    ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg,
                            ChannelPromise promise) {
                            System.out.println(5);
                            ctx.write(msg, promise); // 5
                        }
                    });
                    ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg,
                            ChannelPromise promise) {
                            System.out.println(6);
                            ctx.write(msg, promise); // 6
                        }
                    });
                }
            })
            .bind(8080);
    }
}
```

ChannelInboundHandlerAdapter 是按照 addLast 的顺序执行的，而 ChannelOutboundHandlerAdapter 是按照 addLast 的逆序执行的。ChannelPipeline 的实现是一个 ChannelHandlerContext（包装了 ChannelHandler） 组成的双向链表

- ctx.channel().write(msg) 从尾部开始查找出站处理器
- ctx.write(msg) 是从当前节点找上一个出站处理器

### 3.3 ByteBuf

#### 3.3.1 创建

动态扩容

```
import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestByteBuf {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        StringBuilder sb = new StringBuilder(256);
        log(buffer);
        for (int i = 0; i < 500; i++) {
            sb.append('a');
        }
        buffer.writeBytes(sb.toString().getBytes());
        log(buffer);
    }

    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
            .append("read index:").append(buffer.readerIndex())
            .append(" write index:").append(buffer.writerIndex())
            .append(" capacity:").append(buffer.capacity())
            .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf);
    }
}
```

#### 3.3.2 直接内存 vs 堆内存

可以使用下面的代码来创建池化基于堆的 ByteBuf

```java
ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer(10);Copy to clipboardErrorCopied
```

也可以使用下面的代码来创建池化基于直接内存的 ByteBuf

```java
ByteBuf buffer = ByteBufAllocator.DEFAULT.directBuffer(10);Copy to clipboardErrorCopied
```

- 直接内存创建和销毁的代价昂贵，但读写性能高（少一次内存复制），适合配合池化功能一起用
- 直接内存对 GC 压力小，因为这部分内存不受 JVM 垃圾回收的管理，但也要注意及时主动释放

#### 3.3.3 池化 vs 非池化

池化的最大意义在于可以重用 ByteBuf，优点有

- 没有池化，则每次都得创建新的 ByteBuf 实例，这个操作对直接内存代价昂贵，就算是堆内存，也会增加 GC 压力
- 有了池化，则可以重用池中 ByteBuf 实例，并且采用了与 jemalloc 类似的内存分配算法提升分配效率
- 高并发时，池化功能更节约内存，减少内存溢出的可能

池化功能是否开启，可以通过下面的系统环境变量来设置,idea是vm options

```java
-Dio.netty.allocator.type={unpooled|pooled}Copy to clipboardErrorCopied
```

- 4.1 以后，非 Android 平台默认启用池化实现，Android 平台启用非池化实现
- 4.1 之前，池化功能还不成熟，默认是非池化实现

```
 ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(); // 默认是直接内存  heapBuffer()堆内存
        System.out.println(buffer.getClass()); // PooledUnsafeDirectByteBuf 池化直接内存
```

#### 3.3.4 组成

ByteBuf由四个部分组成，读写指针都在0位置，支持扩容

而nio的ByteBuffer读写都用同一个指针，需要切换模式

四部分：  废弃字节 ，可读字节，可写字节，可扩容字节

#### 3.3.5 写入

|                                                              |                        |                                             |
| ------------------------------------------------------------ | ---------------------- | ------------------------------------------- |
| 方法签名                                                     | 含义                   | 备注                                        |
| writeBoolean(boolean value)                                  | 写入 boolean 值        | 用一字节 01\|00 代表 true\|false            |
| writeByte(int value)                                         | 写入 byte 值           |                                             |
| writeShort(int value)                                        | 写入 short 值          |                                             |
| writeInt(int value)                                          | 写入 int 值            | Big Endian，即 0x250，写入后 00 00 02 50    |
| writeIntLE(int value)                                        | 写入 int 值            | Little Endian，即 0x250，写入后 50 02 00 00 |
| writeLong(long value)                                        | 写入 long 值           |                                             |
| writeChar(int value)                                         | 写入 char 值           |                                             |
| writeFloat(float value)                                      | 写入 float 值          |                                             |
| writeDouble(double value)                                    | 写入 double 值         |                                             |
| writeBytes(ByteBuf src)                                      | 写入 netty 的 ByteBuf  |                                             |
| writeBytes(byte[] src)                                       | 写入 byte[]            |                                             |
| writeBytes(ByteBuffer src)                                   | 写入 nio 的 ByteBuffer |                                             |
| int writeCharSequence(CharSequence sequence, Charset charset) | 写入字符串             |                                             |

> 注意
>
> - 这些方法的未指明返回值的，其返回值都是 ByteBuf，意味着可以链式调用
> - 网络传输，默认习惯是 Big Endian

先写入 4 个字节

```java
buffer.writeBytes(new byte[]{1, 2, 3, 4});
log(buffer);
```

结果是

```
read index:0 write index:4 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04                                     |....            |
+--------+-------------------------------------------------+----------------+
```

再写入一个 int 整数，也是 4 个字节

```java
buffer.writeInt(5);
log(buffer);
```

结果是

```
read index:0 write index:8 capacity:10
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 00 00 00 05                         |........        |
+--------+-------------------------------------------------+----------------+
```

还有一类方法是 set 开头的一系列方法，也可以写入数据，但不会改变写指针位置

#### 3.3.6 扩容

```
ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10); // 默认是256字节capacity
```

再写入一个 int 整数时，容量不够了（初始容量是 10），这时会引发扩容

```java
buffer.writeInt(6);
log(buffer);
```

扩容规则是

- 如何写入后数据大小未超过 512，则选择下一个 16 的整数倍，例如写入后大小为 12 ，则扩容后 capacity 是 16
- 如果写入后数据大小超过 512，则选择下一个 2^n，例如写入后大小为 513，则扩容后 capacity 是 2^10=1024（2^9=512 已经不够了）
- 扩容不能超过 max capacity 会报错

结果是

```
read index:0 write index:12 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 00 00 00 05 00 00 00 06             |............    |
+--------+-------------------------------------------------+----------------+
```

#### 3.3.7 读取

例如读了 4 次，每次一个字节

```java
System.out.println(buffer.readByte());
System.out.println(buffer.readByte());
System.out.println(buffer.readByte());
System.out.println(buffer.readByte());
log(buffer);
```

读过的内容，就属于废弃部分了，再读只能读那些尚未读取的部分

```
1
2
3
4
read index:4 write index:12 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 00 05 00 00 00 06                         |........        |
+--------+-------------------------------------------------+----------------+
```

如果需要重复读取 int 整数 5，怎么办？

可以在 read 前先做个标记 mark

```java
buffer.markReaderIndex();
System.out.println(buffer.readInt());
log(buffer);Copy to clipboardErrorCopied
```

结果

```
5
read index:8 write index:12 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 00 06                                     |....            |
+--------+-------------------------------------------------+----------------+
```

这时要重复读取的话，重置到标记位置 reset

```java
buffer.resetReaderIndex();
log(buffer);Copy to clipboardErrorCopied
```

这时

```
read index:4 write index:12 capacity:16
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 00 05 00 00 00 06                         |........        |
+--------+-------------------------------------------------+----------------+
```

还有种办法是采用 get 开头的一系列方法，这些方法不会改变 read index

#### 3.3.8 retain & release 内存回收

由于 Netty 中有堆外内存的 ByteBuf 实现，堆外内存最好是手动来释放，而不是等 GC 垃圾回收。

- UnpooledHeapByteBuf 使用的是 JVM 内存，只需等 GC 回收内存即可
- UnpooledDirectByteBuf 使用的就是直接内存了，需要特殊的方法来回收内存，JVM回收的时候也会间接回收
- PooledByteBuf 和它的子类使用了池化机制，需要更复杂的规则来回收内存

> 回收内存的源码实现，请关注下面方法的不同实现
>
> ```
> protected abstract void deallocate()
> ```

Netty 这里采用了**引用计数法**来控制回收内存，每个 ByteBuf 都实现了 **ReferenceCounted** 接口

- 每个 ByteBuf 对象的初始计数为 1
- 调用 release 方法计数减 1，如果计数为 0，ByteBuf 内存被回收
- 调用 retain 方法计数加 1，表示调用者没用完之前，其它 handler 即使调用了 release 也不会造成回收
- 当计数为 0 时，底层内存会被回收，这时即使 ByteBuf 对象还在，其各个方法均无法正常使用

谁来负责 release 呢？

不是我们想象的（一般情况下）

```java
ByteBuf buf = ...
try {
    ...
} finally {
    buf.release();
}
```

请思考，因为 pipeline 的存在，一般需要将 ByteBuf 传递给下一个 ChannelHandler，如果在 finally 中 release 了，就失去了传递性（当然，如果在这个 ChannelHandler 内这个 ByteBuf 已完成了它的使命，那么便无须再传递）

基本规则是，**谁是最后使用者，谁负责 release**，详细分析如下

- 起点，对于 NIO 实现来讲，在 io.netty.channel.nio.AbstractNioByteChannel.NioByteUnsafe#read 方法中首次创建 ByteBuf 放入 pipeline（line 163 pipeline.fireChannelRead(byteBuf)）
- 入站 ByteBuf 处理原则
  - 对原始 ByteBuf 不做处理，调用 ctx.fireChannelRead(msg) 向后传递，这时无须 release
  - 将原始 ByteBuf 转换为其它类型的 Java 对象，这时 ByteBuf 就没用了，必须 release
  - 如果不调用 ctx.fireChannelRead(msg) 向后传递，那么也必须 release
  - 注意各种异常，如果 ByteBuf 没有成功传递到下一个 ChannelHandler，必须 release
  - 假设消息一直向后传，那么 TailContext 会负责释放未处理消息（原始的 ByteBuf）
- 出站 ByteBuf 处理原则
  - 出站消息最终都会转为 ByteBuf 输出，一直向前传，由 **HeadContext** flush 后 release
- 异常处理原则
  - 有时候不清楚 ByteBuf 被引用了多少次，但又必须彻底释放，可以循环调用 release 直到返回 true

**TailContext** 释放未处理消息逻辑

```java
// io.netty.channel.DefaultChannelPipeline#onUnhandledInboundMessage(java.lang.Object)
protected void onUnhandledInboundMessage(Object msg) {
    try {
        logger.debug(
            "Discarded inbound message {} that reached at the tail of the pipeline. " +
            "Please check your pipeline configuration.", msg);
    } finally {
        ReferenceCountUtil.release(msg);
    }
}Copy to clipboardErrorCopied
```

具体代码

```java
// io.netty.util.ReferenceCountUtil#release(java.lang.Object)
public static boolean release(Object msg) {
    if (msg instanceof ReferenceCounted) {
        return ((ReferenceCounted) msg).release();
    }
    return false;
}
```

#### 3.3.9 slice 零拷贝

【零拷贝】的体现之一，对原始 ByteBuf 进行切片成多个 ByteBuf，切片后的 ByteBuf 并没有发生内存复制，还是使用原始 ByteBuf 的内存，切片后的 ByteBuf 维护独立的 read，write 指针

无参 slice 是从原始 ByteBuf 的 read index 到 write index 之间的内容进行切片，切片后的 max capacity 被固定为这个区间的大小，因此不能追加 write

```
public class TestSlice {

    public static void main(String[] args) {
        ByteBuf origin = ByteBufAllocator.DEFAULT.buffer(10);
        origin.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        TestByteBuf.log(origin);

        // 切片过程中没有发生数据复制,最大长度固定
        ByteBuf slice1 = origin.slice(0, 5);
        ByteBuf slice2 = origin.slice(5, 5);
        TestByteBuf.log(slice1);
        TestByteBuf.log(slice2);

        // 切片和原来是同一块内存
        slice1.setByte(0,'z');
        TestByteBuf.log(origin);

        // 切片增加引用+1=2
        slice1.retain();
        slice2.retain();

        System.out.println("释放原有ByteBuf内存");
        origin.release(); // 释放原始引用 会使切片引用-1

        TestByteBuf.log(slice1); // 由于原有内存释放了，如果没有retain则这里报错
        TestByteBuf.log(slice2);

        // 正确写法：先释放原始，再释放切片
        slice1.release();
        slice2.release();
    }
}
```

#### 3.3.10 duplicate

【零拷贝】的体现之一，就好比截取了原始 ByteBuf 所有内容，并且没有 max capacity 的限制，也是与原始 ByteBuf 使用同一块底层内存，只是读写指针是独立的

#### 3.3.11 copy

会将底层内存数据进行深拷贝，因此无论读写，都与原始 ByteBuf 无关

#### 3.3.12 CompositeByteBuf

零拷贝的体现之一，可以将多个 ByteBuf 合并为一个逻辑上的 ByteBuf，避免拷贝

CompositeByteBuf 是一个组合的 ByteBuf，它内部维护了一个 Component 数组，每个 Component 管理一个 ByteBuf，记录了这个 ByteBuf 相对于整体偏移量等信息，代表着整体中某一段的数据。

- 优点，对外是一个虚拟视图，组合这些 ByteBuf 不会产生内存复制
- 缺点，复杂了很多，多次操作会带来性能的损耗

```
public class TestCompositBuf {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(5);
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(5);
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 进行了数据的内存复制操作
//        ByteBuf buf3 = ByteBufAllocator.DEFAULT.buffer(buf1.readableBytes() + buf2.readableBytes());
//        buf3.writeBytes(buf1);
//        buf3.writeBytes(buf2);

        CompositeByteBuf buf3 = ByteBufAllocator.DEFAULT.compositeBuffer();
        // true 表示增加新的 ByteBuf 自动递增 write index, 否则 write index 会始终为 0
        buf3.addComponents(true, buf1, buf2);

        System.out.println(ByteBufUtil.prettyHexDump(buf1));
        System.out.println(ByteBufUtil.prettyHexDump(buf2));
        System.out.println(ByteBufUtil.prettyHexDump(buf3));
    }
}
```

#### 3.3.13 Unpooled

Unpooled 是一个工具类，类如其名，提供了非池化的 ByteBuf 创建、组合、复制等操作

这里仅介绍其跟【零拷贝】相关的 wrappedBuffer 方法，可以用来包装 ByteBuf

```java
ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(5);
buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(5);
buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

// 当包装 ByteBuf 个数超过一个时, 底层使用了 CompositeByteBuf
ByteBuf buf3 = Unpooled.wrappedBuffer(buf1, buf2);
System.out.println(ByteBufUtil.prettyHexDump(buf3));
```

输出

```
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05 06 07 08 09 0a                   |..........      |
+--------+-------------------------------------------------+----------------+
```

也可以用来包装普通字节数组，底层也不会有拷贝操作

```java
ByteBuf buf4 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
System.out.println(buf4.getClass());
System.out.println(ByteBufUtil.prettyHexDump(buf4));
```

输出

```
class io.netty.buffer.CompositeByteBuf
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 01 02 03 04 05 06                               |......          |
+--------+-------------------------------------------------+----------------+
```

#### 3.3.14 ByteBuf 优势

- 池化 - 可以重用池中 ByteBuf 实例，更节约内存，减少内存溢出的可能
- 读写指针分离，不需要像 ByteBuffer 一样切换读写模式
- 可以自动扩容
- 支持链式调用，使用更流畅
- 很多地方体现零拷贝，例如 slice、duplicate、CompositeByteBuf

#### 3.3.15 例子-双向通信

```
public class EchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf buf = (ByteBuf) msg;
                            System.out.println(buf.toString());

                            // 建议使用 ctx.alloc() 创建 ByteBuf
                            final ByteBuf response = ctx.alloc().buffer();
                            response.writeBytes(buf);
                            ctx.writeAndFlush(response);
                            // 思考：需要释放 buffer 吗
                            // 思考：需要释放 response 吗
                        }
                    });
                }
            }).bind(8080);
    }
}
```

```
public class EchoClient {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            ByteBuf buffer = (ByteBuf) msg;
                            System.out.println(buffer.toString(Charset.defaultCharset()));

                            // 思考：需要释放 buffer 吗
                        }
                    });
                }
            }).connect("127.0.0.1", 8080).sync().channel();

        channel.closeFuture().addListener(future -> {
            group.shutdownGracefully();
        });

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line);
            }
        }).start();

    }
}
```

# 二、进阶

## 2. 粘包与半包

### 2.1 粘包现象

```
public class HelloWorldClient {
    static final Logger log = LoggerFactory.getLogger(HelloWorldClient.class);

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("connetted...");
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("sending...");
                            for (int i = 0; i < 10; i++) {
                                ByteBuf buffer = ctx.alloc().buffer();
                                buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                ctx.writeAndFlush(buffer);
                            }
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            log.debug("client start");
            channelFuture.channel().closeFuture().sync();
            log.debug("client close");
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
```

客户端代码希望发送 10 个消息，每个消息是 16 字节

```
@Slf4j
public class HelloWorldServer {

    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("connected {}", ctx.channel());
                            super.channelActive(ctx);
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("disconnect {}", ctx.channel());
                            super.channelInactive(ctx);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.debug("stoped");
        }
    }
}
```

服务器端的某次输出，可以看到一次就接收了 160 个字节，而非分 10 次接收

```
08:24:55 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x94132411, L:/127.0.0.1:8080 - R:/127.0.0.1:58177] READ: 160B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000010| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000020| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000030| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000040| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000050| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000060| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000070| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000080| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
|00000090| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
+--------+-------------------------------------------------+----------------+
```

### 2.2 半包现象

客户端代码希望发送 1 个消息，这个消息是 160 字节

```
ByteBuf buffer = ctx.alloc().buffer();
for (int i = 0; i < 10; i++) {
    buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
}
ctx.writeAndFlush(buffer);
```

服务端修改一下接收缓冲区，其它代码不变

```
serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
```

服务器端的某次输出，可以看到接收的消息被分为几节，每次接收20字节或者140字节等

**注意**

serverBootstrap.option(ChannelOption.SO_RCVBUF, 10) 影响的底层接收缓冲区（即滑动窗口）大小，仅决定了 netty 读取的最小单位，netty 实际每次读取的一般是它的整数倍

### 2.3 现象分析

粘包

- 现象，发送 abc def，接收 abcdef
- 原因
  - 应用层：接收方 ByteBuf 设置太大（Netty 默认 1024）
  - 滑动窗口：假设发送方 256 bytes 表示一个完整报文，但由于接收方处理不及时且窗口大小足够大，这 256 bytes 字节就会缓冲在接收方的滑动窗口中，当滑动窗口中缓冲了多个报文就会粘包
  - Nagle 算法：会造成粘包

半包

- 现象，发送 abcdef，接收 abc def
- 原因
  - 应用层：接收方 ByteBuf 小于实际发送数据量
  - 滑动窗口：假设接收方的窗口只剩了 128 bytes，发送方的报文大小是 256 bytes，这时放不下了，只能先发送前 128 bytes，等待 ack 后才能发送剩余部分，这就造成了半包
  - MSS 限制：当发送的数据超过 MSS 限制后，会将数据切分发送，就会造成半包

本质是因为 TCP 是流式协议，消息无边界；UDP不会产生这些包现象

**滑动窗口**

滑动窗口

- TCP 以一个段（segment）为单位，每发送一个段就需要进行一次确认应答（ack）处理，但如果这么做，缺点是包的往返时间越长性能就越差

  ![img](https://bright-boy.gitee.io/technical-notes/%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B/img/0049.png)

- 为了解决此问题，引入了窗口概念，窗口大小即决定了无需等待应答而可以继续发送的数据最大值

  ![img](https://bright-boy.gitee.io/technical-notes/%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B/img/0051.png)

- 窗口实际就起到一个缓冲区的作用，同时也能起到流量控制的作用

  - 图中深色的部分即要发送的数据，高亮的部分即窗口
  - 窗口内的数据才允许被发送，当应答未到达前，窗口必须停止滑动
  - 如果 1001~2000 这个段的数据 ack 回来了，窗口就可以向前滑动
  - 接收方也会维护一个窗口，只有落在窗口内的数据才能允许接收

> MSS 限制
>
> - 链路层对一次能够发送的最大数据有限制，这个限制称之为 MTU（maximum transmission unit），不同的链路设备的 MTU 值也有所不同，例如
> - 以太网的 MTU 是 1500
> - FDDI（光纤分布式数据接口）的 MTU 是 4352
> - 本地回环地址的 MTU 是 65535 - 本地测试不走网卡
> - MSS 是最大段长度（maximum segment size），它是 MTU 刨去 tcp 头和 ip 头后剩余能够作为数据传输的字节数
> - ipv4 tcp 头占用 20 bytes，ip 头占用 20 bytes，因此以太网 MSS 的值为 1500 - 40 = 1460
> - TCP 在传递大量数据时，会按照 MSS 大小将数据进行分割发送
> - MSS 的值在三次握手时通知对方自己 MSS 的值，然后在两者之间选择一个小值作为 MSS

> Nagle 算法
>
> - 即使发送一个字节，也需要加入 tcp 头和 ip 头，也就是总字节数会使用 41 bytes，非常不经济。因此为了提高网络利用率，tcp 希望尽可能发送足够大的数据，这就是 Nagle 算法产生的缘由
> - 该算法是指发送端即使还有应该发送的数据，但如果这部分数据很少的话，则进行延迟发送
>   - 如果 SO_SNDBUF 的数据达到 MSS，则需要发送
>   - 如果 SO_SNDBUF 中含有 FIN（表示需要连接关闭）这时将剩余数据发送，再关闭
>   - 如果 TCP_NODELAY = true，则需要发送
>   - 已发送的数据都收到 ack 时，则需要发送
>   - 上述条件不满足，但发生超时（一般为 200ms）则需要发送
>   - 除上述情况，延迟发送

### 2.4 解决方案

1. 短链接，发一个包建立一次连接，这样连接建立到连接断开之间就是消息的边界，缺点效率太低
2. 每一条消息采用固定长度，缺点浪费空间
3. 每一条消息采用分隔符，例如 \n，缺点需要转义
4. 每一条消息分为 head 和 body，head 中包含 body 的长度

#### 2.4.1 短链接

以解决粘包为例

发送方缓存区默认1024

```java
// 短链接，解决粘包现象
public class t1ShortLinkClient {
    static final Logger log = LoggerFactory.getLogger(HelloWorldClient.class);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            send();
        }
    }

    private static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("connetted...");
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("sending...");
                            ByteBuf buffer = ctx.alloc().buffer();
                            buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
                                18});
                            ctx.writeAndFlush(buffer);
                            // 发完即关
                            ctx.close();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            log.debug("client start");
            channelFuture.channel().closeFuture().sync();
            log.debug("client close");
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
```

半包用这种办法还是不好解决，因为接收方的缓冲区大小是有限的

server端

```
            // 系统的接收缓存区（滑动窗口）
            //           serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
            // 调整netty的接收缓存区(byteBuf)  childOption只针对 channel
            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
```

#### 2.4.2 固定长度-定长解码器

让所有数据包长度固定（假设长度为 8 字节），服务器端加入

```
ch.pipeline().addLast(new FixedLengthFrameDecoder(8));
```

客户端测试代码，注意, 采用这种方法后，客户端什么时候 flush 都可以

```
protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("connetted...");
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("sending...");
                            // 发送内容随机的数据包
                            Random r = new Random();
                            char c = 'a';
                            ByteBuf buffer = ctx.alloc().buffer();
                            for (int i = 0; i < 10; i++) {
                                byte[] bytes = new byte[8];
                                for (int j = 0; j < r.nextInt(8); j++) {
                                    bytes[j] = (byte) c;
                                }
                                c++;
                                buffer.writeBytes(bytes);
                            }
                            ctx.writeAndFlush(buffer);
                        }
                    });
                }
```

缺点是，数据包的大小不好把握

- 长度定的太大，浪费
- 长度定的太小，对某些数据包又显得不够

#### 2.4.3 固定分隔符-行解析器

服务端加入，默认以 \n 或 \r\n 作为分隔符，如果超出指定长度仍未出现分隔符，则抛出异常

```java
ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
```

客户端在每条消息之后，加入 \n 分隔符

```
for (int i = 0; i < 10; i++) {
                                byte[] bytes = new byte[8];
                                for (int j = 0; j < r.nextInt(7); j++) {
                                    bytes[j] = (byte) c;
                                }
                                bytes[7]='\n'; // 行分隔符
                                c++;
                                buffer.writeBytes(bytes);
                            }
```

缺点，处理字符数据比较合适，但如果内容本身包含了分隔符（字节数据常常会有此情况），那么就会解析错误

#### 2.4.4 预设长度-LTC解析器

在发送消息前，先约定用定长字节表示接下来数据的长度

Length | Actual Content

HEADER | Length | Actual Content

Length | HEADER | Actual Content

HEADER | Length | HEADER | Actual Content

```java
// 最大长度，长度字段开始的位置，长度占用字节，从长度占用末位开始多少个字节后是内容，从头剥离字节数(可以剥离头部)
ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 1, 0, 1));
```

剥离字节数可以把长度字段解析后剥离掉

## 3. 协议设计与解析

#### 3.1 为什么需要协议?

TCP/IP 中消息传输基于流的方式，没有边界。

协议的目的就是划定消息的边界，制定通信双方要共同遵守的通信规则

例如：在网络上传输

```
下雨天留客天留我不留
```

是中文一句著名的无标点符号句子，在没有标点符号情况下，这句话有数种拆解方式，而意思却是完全不同，所以常被用作讲述标点符号的重要性

一种解读

```
下雨天留客，天留，我不留
```

另一种解读

```
下雨天，留客天，留我不？留
```

如何设计协议呢？其实就是给网络传输的信息加上“标点符号”。但通过分隔符来断句不是很好，因为分隔符本身如果用于传输，那么必须加以区分。因此，下面一种协议较为常用

```
定长字节表示内容长度 + 实际内容
```

例如，假设一个中文字符长度为 3，按照上述协议的规则，发送信息方式如下，就不会被接收方弄错意思了

```
0f下雨天留客06天留09我不留
```

#### 3.2 redis协议举例

```
// 连接redis协议
public class TestRedisProtocol {

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        byte[] LINE = {13, 10}; // /r /n  换行回车
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LoggingHandler());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        // 会在连接 channel 建立成功后，会触发 active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            set(ctx);
                            get(ctx);
                        }

                        private void get(ChannelHandlerContext ctx) {
                            // redis command : get name   *2 后面2个参数， $3 参数3个字节
                            ByteBuf buf = ctx.alloc().buffer();
                            buf.writeBytes("*2".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("$3".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("get".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("$4".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("name".getBytes());
                            buf.writeBytes(LINE);
                            // 可以string方式运行
                            //                            String command = "*2\r\n$3\r\nget$4name";
                            //                            buf.writeBytes(command.getBytes());
                            ctx.writeAndFlush(buf);
                        }

                        private void set(ChannelHandlerContext ctx) {
                            // redis command : set nam lin
                            ByteBuf buf = ctx.alloc().buffer();
                            buf.writeBytes("*3".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("$3".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("set".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("$4".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("name".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("$3".getBytes());
                            buf.writeBytes(LINE);
                            buf.writeBytes("lin".getBytes());
                            buf.writeBytes(LINE);
                            ctx.writeAndFlush(buf);
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf buf = (ByteBuf) msg;
                            System.out.println("result = " + buf.toString(Charset.defaultCharset()));
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println(e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
```

#### 3.3 http协议举例

```
@Slf4j
public class TestHttpProtocol {

    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new HttpServerCodec());
                    // SimpleChannelInboundHandler可以指定是HttpRequest类型的才监听到
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 获取请求
                            log.debug(msg.uri());

                            // 返回响应
                            DefaultFullHttpResponse response =
                                new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);

                            byte[] bytes = "<h1>Hello, world!</h1>".getBytes();

                            response.headers().setInt(CONTENT_LENGTH, bytes.length);
                            response.content().writeBytes(bytes);

                            // 写回响应
                            ctx.writeAndFlush(response);
                        }
                    });

/*
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("class = {}", msg.getClass());

                            if (msg instanceof HttpRequest) { // 请求行，请求头 get请求  DefaultHttpRequest
                                System.out.println("get= "+ msg);
                            } else if (msg instanceof HttpContent) { //请求体  post请求 LastHttpContent
                                System.out.println("post = "+msg);
                            }
                        }
                    });
*/
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
```

#### 3.4 自定义协议要素

- 魔数，用来在第一时间判定是否是无效数据包
- 版本号，可以支持协议的升级
- 序列化算法，消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
- 指令类型，是登录、注册、单聊、群聊... 跟业务相关
- 请求序号，为了双工通信，提供异步能力
- 正文长度
- 消息正文

##### 3.4.1 编解码器

根据上面的要素，设计一个登录请求消息和登录响应消息，并使用 Netty 完成收发

