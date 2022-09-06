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

