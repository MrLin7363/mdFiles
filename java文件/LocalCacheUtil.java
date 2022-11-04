@Slf4j
public class LocalCacheUtil {
    /**
     * cacheContainer
     */
    public static Map<String, Cache<String, Object>> cacheContainer = new ConcurrentHashMap<>();
    
    /**
     * 自定义配置
     * @param cacheKey
     * @param duration 时间系数
     * @param initialCapacity 初始化容量
     * @param maximumSize 最大容量
     * @param concurrencyLevel 并发级别
     * @return
     */
    public static Cache<String, Object> getLocalCache(String cacheKey, long duration,
        int initialCapacity, int maximumSize, int concurrencyLevel) {
        Cache<String, Object> localCache = cacheContainer.get(cacheKey);
        if (null != localCache) {
            return localCache;
        }
        synchronized (LocalCacheUtil.cacheContainer) {
            if (null == localCache) {
                //recordStats开启缓存状况统计,expireAfterAccess过期时间,initialCapacity初始化大小,maximumSize最大值
                localCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(duration, TimeUnit.MINUTES)
                    .initialCapacity(initialCapacity)
                    .maximumSize(maximumSize)
                    .concurrencyLevel(concurrencyLevel)
                    .recordStats()
                    .build();
                cacheContainer.put(cacheKey, localCache);
            }
            return localCache;
        }
    }

    public static Cache<String,Object> getLocalCache(String cacheKey) {
        Cache<String, Object> localCache = cacheContainer.get(cacheKey);
        if (null != localCache) {
            log.info("取已有Cathe缓存");
            return localCache;
        }
        synchronized (LocalCacheUtil.cacheContainer) {
            log.info("新建Cathe缓存");
            if (null == localCache) {
                localCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(3L, TimeUnit.SECONDS)
                    .initialCapacity(50)
                    .maximumSize(500)
                    .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                    .recordStats()
                    .build();
                cacheContainer.put(cacheKey,localCache);
            }
            return localCache;
        }
    }

    public static void main(String[] args) {
        String key = "instance_id";

        Cache<String, Double> cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2L,
            TimeUnit.SECONDS).build();

        int i = 1;
        while (i < 10) {
            i++;
            try {
                Thread.sleep(1000);
                Double value = cache.get(key, new Callable<Double>() {
                    @Override
                    public Double call() throws Exception {
                        System.out.println("hello");  //第一次没有的时候会运算然后添加到缓存里面。后面就直接从缓存里面读取数据.
                        return 120d;
                    }
                });
                System.out.println(value);
            } catch (ExecutionException | InterruptedException ex) {
                System.out.println(ex.getCause());
            }
        }
    }

}
