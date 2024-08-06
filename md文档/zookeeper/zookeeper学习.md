### zookeeper学习

学习视频    https://www.bilibili.com/video/BV1Ph411n7Ep?p=27&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b

连接zk可视化客户端工具 prettyZoo 

监听器文章   https://blog.csdn.net/linxingliang/article/details/120275120

##### 调用代码示例

```java
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperTest {
    private ZooKeeper zooKeeper;

    private String url = "127.0.0.1:2181";

    @Test
    public void test() throws IOException, InterruptedException, KeeperException {
        // zookeeper原生调用
        zooKeeper = new ZooKeeper(url, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("触发了" + watchedEvent.getType() + "的事件");
            }
        });
        //getChildren方法可以得到所有的子节点的名称，本例可以获得/itheima中所有子节点的名字
        List<String> children = zooKeeper.getChildren("/", true);
        for (String child : children) {
            System.out.println("getChildren " + child);
        }
        //        byte[] data = zooKeeper.getData("/", false, null);
        //        System.out.println("data="+new String(data));

        //        String s = zooKeeper.create("/ch", "ch".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
        //            CreateMode.PERSISTENT);
        //        System.out.println("成功创建了"+s+"节点");

        //        byte[] data2 = zooKeeper.getData("/ch", false, null);
        //        System.out.println("data2="+new String(data2));

        //        zooKeeper.delete("/ch",-1);

        // Curator调用zk
        //创建客户端实例
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(url)
            .sessionTimeoutMs(10000) // 会话超时时间
            .connectionTimeoutMs(50000) // 连接超时时间
            .retryPolicy(new ExponentialBackoffRetry(100, 3))
            .namespace("dev") // 指定隔离名称(就是前缀)，表示所有节点的操作都会在该工作空间下进行。不指定时，使用自定义的节点path
            .build();
        client.start();
        InterProcessReadWriteLock interProcessReadWriteLock =
            new InterProcessReadWriteLock(client, "/lock");
        InterProcessMutex writeLock = interProcessReadWriteLock.writeLock();
        try {
            writeLock.acquire();
            Thread.sleep(1000);
            System.out.println("doing some things");
            writeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //获取监听对象 注意client就有了前缀
        NodeCache nodeCache = new NodeCache(client, "/chen2");
        //添加监听
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            //监听回调函数，单个监听到节点发生增删改操作时会执行此方法
            @Override
            public void nodeChanged() throws Exception {
                String path = nodeCache.getCurrentData().getPath();
                System.out.println(path + "节点");

                //获取当前节点更新后的数据
                byte[] data = nodeCache.getCurrentData().getData();
                System.out.println("更新后的数据为：" + new String(data));
            }
        });
        //开启监听，如果为true则开启监听器
        nodeCache.start(true);
        System.out.println("监听器已开启！");
        //让线程休眠10s(为了方便测试)
        Thread.sleep(1000 * 10);
        nodeCache.close();

        try {
            // 创建节点
            String path1 = client.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/chen2", "data".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        client.close();
    }
}
```
### 运用

zookeeper运用于 kafka , ElasticJob

**1）Broker 信息**

　　在 ZooKeeper 上会有一个专门用来进行 Broker 服务器列表记录的节点，节点路径为 /brokers/ids。Kafka 的每个 Broker 启动时，都会在 ZooKeeper 中注册，创建 /brokers/ids/[0-N] 节点，写入 IP，端口等信息，每个 Broker 都有一个 BrokerId。Broker 创建的是临时节点，在连接断开时节点就会自动删除，所以在 ZooKeeper 上就可以通过 Broker 中节点的变化来得到 Broker 的可用性。

**2）Topic 信息**

　　在 Kafka 中可以定义很多个 Topic，每个 Topic 又被分为很多个 Partition。一般情况下，每个 Partition 独立在存在一个 Broker 上，所有的这些 Topic 和 Broker 的对应关系都由 ZooKeeper 进行维护。

**3）负载均衡**

　　生产者需要将消息发送给 Broker，消费者需要从 Broker 上获取消息，通过使用 ZooKeeper，就都能监听 Broker 上节点的状态信息，从而实现动态负载均衡。

**4）offset 信息**

　　在上一篇博客中提到过，offset 用于记录消费者消费到的位置，在老版本（0.9以前）里 offset 是保存在 ZooKeeper 中的。

**5）Controller 选举**

　　在 Kafka 中会有多个 Broker，其中一个 Broker 会被选举成为 Controller（控制器），在任意时刻，Kafka 集群中有且仅有一个控制器。Controller 负责管理集群中所有分区和副本的状态，当某个分区的 leader 副本出现故障时，由 Controller 为该分区选举出一个新的 leader。Kafka 的 Controller 选举就依靠 ZooKeeper 来完成，成功竞选为 Controller 的 Broker 会在 ZooKeeper 中创建 /controller 这个临时节点，在 ZooKeeper 中使用 get 命令查看节点内容：

　　![img](https://img2020.cnblogs.com/blog/1450803/202007/1450803-20200727203043181-34987159.png)

　　其中“version”在目前版本中固定为1，“brokerid”表示 Broker 的编号，“timestamp”表示竞选称为 Controller 时的时间戳。 

　　当 Broker 启动时，会尝试读取 /controller 中的“brokerid ”，如果读取到的值不是-1，则表示已经有节点竞选成为 Controller 了，当前节点就会放弃竞选；而如果读取到的值为-1，ZooKeeper 就会尝试创建 /controller 节点，当该 Broker 去创建的时候，可能还有其他 Broker 一起同时创建节点，但只有一个 Broker 能够创建成功，即成为唯一的 Controller。
