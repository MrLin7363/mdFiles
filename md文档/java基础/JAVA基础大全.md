廖雪峰  https://www.liaoxuefeng.com/wiki/1252599548343744/1282384941023266

## 一、基础类

### 1. DecimalFormat

1. 以“0”补位时

如果数字少了，就会补“0”，小数和整数都会补；

如果数字多了，就切掉，但只切小数的末尾，整数不能切；

同时被切掉的小数位会进行四舍五入处理。

2. 以“#”补位时

如果数字少了，则不处理，不会补“0”，也不会补“#”；

如果数字多了，就切掉，但只切小数的末尾，整数不能切；

同时被切掉的小数位会进行四舍五入处理。

```
public static String parseBigDecimalToTwoFraction(BigDecimal bd) {
    DecimalFormat decimalFormat = new DecimalFormat("0.##");
    decimalFormat.setRoundingMode(RoundingMode.FLOOR); // 99.999999999  -> 99.99
    return decimalFormat.format(bd);
}
```

### 2. 面向对象

封装：JAVA对象的属性

继承：

多态：对象变量是多态的。 一个 Employee 变量既可以引用一个
Employee 类对象， 也可以引用一个 Employee 类的任何一个子类的对象（例如， Manager、
Executive、 Secretary 等）  

类之间关系：

依赖： use a

聚合：has a

继承：is a

注意：方法入参，不能改变引用对象的引用

```
class Employee
{
private static int nextld;
private int id;

// object initialization block 初始化块在Employee对象被实例化的时候执行，和static块不一样
//为了避免循环定义，不要读取在块后面初始化的域
{
id = nextld;
nextld++;
}

```

### 3. 反射

Class<?> 任意一个类

Class<?>[] 任意一个接口

### 4. 泛型

```
// 第一个T是方法参数限定，表示（）内的T必须是T类型的
// 第二个T是方法的返回类型
// 第三个T和第一个T对应
public static <T extends Comparable & Serializable> T getMin(T...a){
```

### 5. ascll码

转换网站 https://www.asciim.cn/m/tools/convert_ascii_to_string.html

如  /x22 代表 “       \x5C 代表 \

/x是十六进制， 网站里选十六进制输入22就可以展示出来

### 6. 定时任务Timer

https://blog.csdn.net/weixin_45734473/article/details/133647534

`Timer`提供了三种定时模式：

- 一次性任务
- 按照固定的延迟执行(`fixed delay`) 不管上次执行时间
- 按照固定的周期执行（`fixed rate`） 等待上次执行完的间隔

```
@Service
public class HealthCheckTimer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckTimer.class);

    private Timer timer = null;

    @Autowired
    private HealthIndicator healthIndicator;

    @PostConstruct
    public void init() {
        timer = new Timer("thread-redisHealthCheck");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("HealthCheckTimer：" + new Date());
                healthIndicator.doHealthCheck(new Health.Builder());
            }
        };
        long delay = 3000L;
        timer.scheduleAtFixedRate(task, delay, 5000l);
    }
}
```

通过上面分析，Java的定时调度可以通过`Timer&TimerTask`来实现。由于其实现的方式为单线程，因此从JDK1.3发布之后就一直存在一些问题，大致如下：

1. 多个任务之间会相互影响
2. 多个任务的执行是串行的，性能较低

此时可以使用 **ScheduledExecutorService**

## 二、异常

JAVA异常

![img](https://img-blog.csdn.net/20180601142231719)

Throwable 

-> Error -> virtulMachineError -> StackoverFlowError ,OutofMemoryError

-> Exception  -> IOException -> EOException , FileNotFoundException 

-> Exception  -> RuntimeException -> ArrithmeticException , ClassNotFoundException , NullPointerException , IllegalArgumentException 



Error（错误）
程序在执行过程中所遇到的硬件或操作系统的错误。错误对程序而言是致命的，将导致程序无法运行。常见的错误有内存溢出，jvm虚拟机自身的非正常运行，calss文件没有主方法。程序本生是不能处理错误的，只能依靠外界干预。Error是系统内部的错误，由jvm抛出，交给系统来处理。

     Exception（异常）

是程序正常运行中，可以预料的意外情况。比如数据库连接中断，空指针，数组下标越界。异常出现可以导致程序非正常终止，也可以预先检测，被捕获处理掉，使程序继续运行。

 

EXCEPTION（异常）按照性质，又分为编译异常（可检测）和运行时异常（不可检测）。

     编译时异常：

又叫可检查异常，通常时由语法错和环境因素（外部资源）造成的异常。比如输入输出异常IOException，数据库操作SQLException。其特点是，Java语言强制要求捕获和处理所有非运行时异常。通过行为规范，强化程序的健壮性和安全性。

     运行时异常：

又叫不检查异常RuntimeException，这些异常一般是由程序逻辑错误引起的，即语义错。比如算术异常，空指针异常NullPointerException，下标越界IndexOutOfBoundsException。运行时异常应该在程序测试期间被暴露出来，由程序员去调试，而避免捕获。

#### Java常见异常

##### runtimeException子类

```
1、java.lang.ArrayIndexOutOfBoundsException
    数组索引越界异常。当对数组的索引值为负数或大于等于数组大小时抛出。
    2、java.lang.ArithmeticException
    算术条件异常。譬如：整数除零等。
    3、java.lang.NullPointerException
    空指针异常。当应用试图在要求使用对象的地方使用了null时，抛出该异常。譬如：调用null对象的实例方法、访问null对象的属性、计算null对象的长度、使用throw语句抛出null等等
    4、java.lang.ClassNotFoundException
    找不到类异常。当应用试图根据字符串形式的类名构造类，而在遍历CLASSPAH之后找不到对应名称的class文件时，抛出该异常。
    5、java.lang.NegativeArraySizeException  
    数组长度为负异常
    6、java.lang.ArrayStoreException 
    数组中包含不兼容的值抛出的异常
    7、java.lang.SecurityException 
    安全性异常
    8、java.lang.IllegalArgumentException 
    非法参数异常
```

##### IOException

```
IOException：操作输入流和输出流时可能出现的异常。
EOFException：文件已结束异常
FileNotFoundException：文件未找到异常
```

##### 其他

```
ClassCastException    类型转换异常类
ArrayStoreException  数组中包含不兼容的值抛出的异常
SQLException   操作数据库异常类
NoSuchFieldException   字段未找到异常
NoSuchMethodException   方法未找到抛出的异常
NumberFormatException    字符串转换为数字抛出的异常
StringIndexOutOfBoundsException 字符串索引超出范围抛出的异常
IllegalAccessException  不允许访问某类异常
InstantiationException  当应用程序试图使用Class类中的newInstance()方法创建一个类的实例，而指定的类对象无法被实例化时，抛出该异常
```

##### 自定义异常

使用Java内置的异常类可以描述在编程时出现的大部分异常情况。除此之外，用户还可以自定义异常。用户自定义异常类，只需继承Exception类即可。
　　在程序中使用自定义异常类，大体可分为以下几个步骤。
　　（1）创建自定义异常类。
　　（2）在方法中通过**throw关键字抛出异常对象**。
　　（3）如果在当前抛出异常的方法中处理异常，可以使用try-catch语句捕获并处理；否则在方法的声明处通过throws关键字指明要抛出给方法调用者的异常，继续进行下一步操作。
　　（4）在出现异常方法的**调用者**中捕获并处理异常。

## 三、注解

#### JDK自带注解

***\*@Override 表示当前方法覆盖了父类的方法
@Deprecation 表示方法已经过时,方法上有横线，使用时会有警告。
@SuppviseWarnings 表示关闭一些警告信息(通知java编译器忽略特定的编译警告)\****

#### 元注解

     何为元注解？就是注解的注解，就是给你自己定义的注解添加注解，你自己定义了一个注解，但你想要你的注解有什么样的功能，此时就需要用元注解对你的注解进行说明了。
    
    元注解有4个

##### @Retention

1. 用@Retention(RetentionPolicy.CLASS)修饰的注解，表示注解的信息被保留在class文件(字节码文件)中当程序编译时，但不会被虚拟机读取在运行的时候；
2. 用@Retention(RetentionPolicy.SOURCE)修饰的注解,表示注解的信息会被编译器抛弃，不会留在class文件中，注解的信息只会留在源文件中；
3. 用@Retention(RetentionPolicy.RUNTIME)修饰的注解，表示注解的信息被保留在class文件(字节码文件)中，当程序编译时，会被虚拟机保留在运行时。

##### **@Target**

即注解的作用域，用于说明注解的使用范围（即注解可以用在什么地方，比如类的注解，方法注解，成员变量注解等等）

**取值：**

     ElemenetType.CONSTRUCTOR----------------------------构造器声明 
     ElemenetType.FIELD --------------------------------------域声明（包括 enum 实例） 
     ElemenetType.LOCAL_VARIABLE------------------------- 局部变量声明 
     ElemenetType.METHOD ----------------------------------方法声明 
     ElemenetType.PACKAGE --------------------------------- 包声明 
     ElemenetType.PARAMETER ------------------------------参数声明 
     ElemenetType.TYPE--------------------------------------- 类，接口（包括注解类型）或enum声明

```
@SingleValue(desc = "只能手输入") //使用时需指明成员名和赋值号"="
public class TestZhuJie {

    @DefaultValue("asd")//使用时可以忽略成员名和赋值号“=”
    public void test(){}

    @MutiValue(age = 23,name = "chenjunlin")
    private void getUser(){}
}
```

```
@SingleValue(desc = "只能手输入") //使用时需指明成员名和赋值号"="
public class TestZhuJie {

    @DefaultValue("asd")//使用时可以忽略成员名和赋值号“=”
    public void test(){}

    @MutiValue(name = "chenjunlin")
    private void getUser(){
        System.out.println("chenjunlasdasdasd");
    }

    public static void main(String[] args) {
        TestZhuJie testZhuJie=new TestZhuJie();
        testZhuJie.getUser();
          /**
           * 用反射方式获得注解对应的实例对象后，在通过该对象调用属性对应的方法
           */
        // 这个类必须设置Runtime类型，还有注释在类上，这个类才能这样调用
        SingleValue singleValue=(SingleValue)TestZhuJie.class.getAnnotation(SingleValue.class);
        System.out.println(singleValue.desc());
    }
```

##### @Inherited

1. 是一个标记注解，没有成员，表示允许子类继承该注解，也就是说如果一个使用了@Inherited修饰的注解被用于一个class时，则这个注解将被该class的子类继承拥有

2. 使用了@Inherited修饰的注解只能被子类所继承，并不可以从它所实现的接口继承

3. 子类继承父类的注解时，并不能从它所重载的方法继承注解

##### @**Documented**

指定被标注的注解会包含在javadoc中。

##### @FunctionalInterface

函数式接口 (Functional Interface) 其实就是一个只具有一个方法的普通接口。

```
@FunctionalInterface
public interface Runnable {
    void run();
}
```

#### **自定义@Interface注解**

1.可以使用default为成员指定一个默认值

**2.**成员类型是受限的，合法的类型包括原始类型以及String、Class、Annotation、Enumeration （JAVA的基本数据类型有8种：byte(字节)、short(短整型)、int(整数型)、long(长整型)、float(单精度浮点数类型)、double(双精度浮点数类型)、char(字符类型)、boolean(布尔类型）

**3.**如果注解只有一个成员，并且把成员取名为value()，则在使用时可以忽略成员名和赋值号“=” ,例如JDK注解的@SuppviseWarnings ；如果成员名不为value，则使用时需指明成员名和赋值号"="

```
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface SingleValue {
    String desc();
}

@Target({ElementType.METHOD})
public @interface DefaultValue {
    String value(); // 只有一个方法，注解使用时不用写 =
}

@SingleValue(desc = "只能手输入") //使用时需指明成员名和赋值号"="
public class TestZhuJie {

    @DefaultValue("asd")//使用时可以忽略成员名和赋值号“=”
    public void test(){}
}
```

##### (1) 实现例子

自定义注解  https://blog.csdn.net/qq_27304827/article/details/126137980

##### (2) spel表达式

https://blog.csdn.net/weixin_42645678/article/details/125414902v

## 四、设计模式

### 1. 责任链模式

https://zhuanlan.zhihu.com/p/634642747

#### spring版本

假设我们有一个 Spring 框架开发的订单处理系统，订单需要依次经过订单检查、库存处理、支付处理。如果某个处理环节无法处理订单，将会终止处理并返回错误信息，只有每个处理器都完成了请求处理，这个订单才算法下单成功。

```
@Data
@AllArgsConstructor
public class orderNo {
    private String orderNumber;
    private String paymentMethod;
    private boolean stockAvailability;
    private String shippingAddress;
}
```

```
public abstract class OrderHandler {
    public abstract void handleOrder(Order order);
}
```

```
@Component
public class CheckOrderHandler extends OrderHandler {
    public void handleOrder(Order order) {
        if (StringUtils.isBlank(order.getOrderNo())) {
            throw new RuntimeException("订单编号不能为空");
        }
        if (order.getPrice().compareTo(BigDecimal.ONE) <= 0) {
            throw new RuntimeException("订单金额不能小于等于0");
        }
        if (StringUtils.isBlank(order.getShippingAddress())) {
            throw new RuntimeException("收货地址不能为空");
        }
        System.out.println("订单参数检验通过");
    }
}

@Component
public class StockHandler extends OrderHandler {
    public void handleOrder(Order order) {
        if (!order.isStockAvailability()) {
            throw new RuntimeException("订单库存不足");
        }
        System.out.println("库存扣减成功");
    }
}

@Component
public class AliPaymentHandler extends OrderHandler {
    public void handleOrder(Order order) {
        if (!order.getPaymentMethod().equals("支付宝")) {
            throw new RuntimeException("不支持支付宝以外的支付方式");
        }
        System.out.println("支付宝预下单成功");
    }
}
```

```
@Component
public class BuildOrderChain {

    @Autowired
    private AliPaymentHandler aliPaymentHandler;

    @Autowired
    private CheckOrderHandler checkOrderHandler;

    @Autowired
    private StockHandler stockHandler;

    List<OrderHandler> list = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 1. 检查订单参数
        list.add(checkOrderHandler);
        // 2. 扣减库存
        list.add(stockHandler);
        // 3. 支付宝预下单
        list.add(aliPaymentHandler);
    }

    public void doFilter(Order order) {
        for (OrderHandler orderHandler : this.list) {
            orderHandler.handleOrder(order);
        }
    }
}
```

假如我们的订单针对的是虚拟不限库存商品，我们不需要进行库存扣减，那我们可以直接新建 `VirtualGoodsOrderChain` 虚拟商品订单生产链条类，代码如下

```
@Component
public class VirtualGoodsOrderChain {
    @Autowired
    private AliPaymentHandler aliPaymentHandler;

    @Autowired
    private CheckOrderHandler checkOrderHandler;

    List<OrderHandler> list = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 1. 检查订单参数
        list.add(checkOrderHandler);
        // 2 支付宝预下单
        list.add(aliPaymentHandler);
    }

    public void doFilter(Order order) {
        for (OrderHandler orderHandler : this.list) {
            orderHandler.handleOrder(order);
        }
    }
}
```

也可以写一个抽象类把所有的抽象链条的公共代码逻辑抽出来

```
public abstract class AbstractChains {
    abstract List<ChainInterface> getChains();

    public void doFilter(Request request, Response response, ProcessControl processControl) {
        for (ChainInterface chainInterface : getChains()) {
        	// 
            if (processControl.isDone()) {
                return;
            }
            chainInterface.handle(request, response, processControl);
        }
    }
}
```

#### java版本

```
public interface Handler {
    void handleRequest(Request request);
}

public class Request {
    private String type;
    // 省略getter、setter
}
```

```
public class ConcreteHandlerA implements Handler {
    private Handler successor;

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    public void handleRequest(Request request) {
        if (request.getType().equals("A")) {
            // 处理请求的逻辑
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}

public class ConcreteHandlerB implements Handler {
    private Handler successor;

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    public void handleRequest(Request request) {
        if (request.getType().equals("B")) {
            // 处理请求的逻辑
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}

public class ConcreteHandlerC implements Handler {
    private Handler successor;

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    public void handleRequest(Request request) {
        if (request.getType().equals("C")) {
            // 处理请求的逻辑
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}
```

```
public class Client {
    public static void main(String[] args) {
        Handler handlerA = new ConcreteHandlerA();
        Handler handlerB = new ConcreteHandlerB();
        Handler handlerC = new ConcreteHandlerC();

        handlerA.setSuccessor(handlerB);
        handlerB.setSuccessor(handlerC);

        // 创建请求并发送给第一个处理者
        Request request = new Request("A");
        handlerA.handleRequest(request);
    }
}
```

### 2. 策略模式

spring 实现版      

```
@Component
public class RecycleContext {

    @Autowired
    private PlanService planService;

    @Autowired
    private FixedService fixedService;

    private static final Map<Integer, RecycleService> TASK_PROCCESS_MAP = new HashMap<>();

    @PostConstruct
    private void initMap() {
        TASK_PROCCESS_MAP.put(TaskEnum.FIXED.getCode(), fixedService);
        TASK_PROCCESS_MAP.put(TaskEnum.PLAN.getCode(), planService);
    }

    public SwRecycleService getService(TaskStatusEnum taskStatusEnum) {
        return TASK_PROCCESS_MAP.get(taskStatusEnum.getCode());
    }
}
```

```
public interface RecycleService<T> {
	// 如果有不同VO可以写个泛型，相同的写个另外参数
    boolean recycle(T t, String ..., String ...);
}

```

实现类

```
@Service
public class PlanService implements RecycleService<VO> {

    @Autowired
    private PlanMapper planMapper;

    @Override
    public boolean recycle(VO VO,......) {
		......
        return false;
    }
}
```



#### 优化

```
通过ApplicationContextAware 不用每个mapper单独加进去避免后期太多
@Component
public class RefundHandlerContext implements ApplicationContextAware {

    private Map<String, AbstractRefundHandler> refundHandlerMap;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        // AbstractRefundHandler实现各个子策略，然后抽象类拿到并加入到map中
        String[] names = context.getBeanNamesForType(AbstractRefundHandler.class);
        refundHandlerMap = new HashMap<>(names.length);
        for (String name : names) {
            AbstractRefundHandler handler = context.getBean(name, AbstractRefundHandler.class);
            String key = handler.getBizCode().getCode();
            refundHandlerMap.put(key, handler);
        }
    }

    public AbstractRefundHandler getHandler(String key){
        return refundHandlerMap.get(key);
    }
}
```



## 五、缓存



### ConcurretnHashMap实现单线程缓存类



在规定时间内，使用 hashMap 实现一个缓存工具类，需要考虑一下几点

1. 不可变对象
2. 单例
3. 线程安全
4. 回收失效数据
5. 垃圾回收
6. 缓存大小
7. LRU

注备：

- LRU： Least Recently Used ，即最近最少使用，是一种常用的页面置换算法，选择最近最久未使用的页面淘汰。
- OPT : 最佳置换算法，是一种理想情况下的置换算法，但实际上不可实现。思想是标记每个页面多久后被使用，最大的将被淘汰
- FIFO：先进先出，建立一个FIFO 队列，收容所有在内存中的页，被置换的页总在队列头上进行。
- LFU ： 最少使用置换算法，使用最少使用置换算法在内存中的每个页面设置一个移位寄存器，记录页面被使用的频率。

```
package com.coolpad.pay.infrastructure.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: chenjunlin
 * @since: 2021/04/21
 * @descripe: 常量信息缓存
 */
public class ConstantsCatheUtil {
    /**
     * 预缓存信息 -- 类加载器启动JVM时就加载这个，所以是线程公用的
     */
    private static final Map<String, Object> CACHE_MAP = new ConcurrentHashMap<String, Object>();

    /**
     * 每个缓存生效时间1小时
     */
    public static final long CACHE_HOLD_TIME_1H = 60*1000L;// 24 * 60 * 60 * 1000L 24小时


    /**
     * 存放一个缓存对象，默认保存时间1小时
     * @param cacheName
     * @param obj
     */
    public static void put(String cacheName, Object obj) {
        put(cacheName, obj, CACHE_HOLD_TIME_1H);
    }

    /**
     * 存放一个缓存对象，保存时间为holdTime
     * @param cacheName
     * @param obj
     * @param holdTime
     */
    public static void put(String cacheName, Object obj, long holdTime) {
        CACHE_MAP.put(cacheName, obj);
        CACHE_MAP.put(cacheName + "_HoldTime", System.currentTimeMillis() + holdTime);//缓存失效时间
    }

    /**
     * 取出一个缓存对象
     * @param cacheName
     * @return
     */
    public static Object get(String cacheName) {
        if (checkCacheName(cacheName)) {
            return CACHE_MAP.get(cacheName);
        }
        return null;
    }

    /**
     * 删除所有缓存
     */
    public static void removeAll() {
        CACHE_MAP.clear();
    }

    /**
     * 删除某个缓存
     * @param cacheName
     */
    public static void remove(String cacheName) {
        CACHE_MAP.remove(cacheName);
        CACHE_MAP.remove(cacheName + "_HoldTime");
    }

    /**LocalCacheUtil
     * 检查缓存对象是否存在，
     * 若不存在，则返回false
     * 若存在，检查其是否已过有效期，如果已经过了则删除该缓存并返回false
     * @param cacheName
     * @return
     */
    public static boolean checkCacheName(String cacheName) {
        Long cacheHoldTime = (Long) CACHE_MAP.get(cacheName + "_HoldTime");
        if (cacheHoldTime == null || cacheHoldTime == 0L) {
            return false;
        }
        System.out.println("当前时间"+cacheHoldTime+"是否小于过期时间"+System.currentTimeMillis()+"==="+(cacheHoldTime < System.currentTimeMillis()));
        if (cacheHoldTime < System.currentTimeMillis()) {
            remove(cacheName);
            return false;
        }
        return true;
    }

}

// 示例实现代码
public PaymentConfig getPaymentConfig(String bizCode,String channel){
        String catheName=bizCode+channel;
        if (!ConstantsCatheUtil.checkCacheName(catheName)){
            PaymentConfig paymentConfig=paymentConfigRepository.getPaymentConfig(bizCode,channel);
            ConstantsCatheUtil.put(catheName,paymentConfig);
            return paymentConfig;
        }
        return (PaymentConfig)ConstantsCatheUtil.get(catheName);
    }
    // 测试
    @Test
    public void testCathe(){
        while (true){
            Thread thread = new Thread(
                    (Runnable) () -> {
                        wxLitePayGateway.getPaymentConfig("zg", "wx_lite");
                        System.out.println("线程名" + Thread.currentThread().getName());
                    }
            );
            thread.start();
            try {
                thread.sleep(5*1000L);
            }catch (Exception e){

            }
        }
    }
```

### Guava缓存

```
   <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
        </dependency>
```

## 六、JVM

### 1. 监控不到本地JAVA程序

win+R 输入 %TMP%\

找到 hsperfdata_用户名，修改该文件夹权限 为 完全控制

JAVA程序线程才能输入到这个文件夹被监控到

项目启动打印JVM信息

-XX:+PrintGCDetails

-XX:+PrintGCDetails -Xmx512m -Xms512m 这两个最好一致，避免GC堆伸缩幅度太大，停顿

-Xmx默认为物理内存的1/4

-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

```
-XX:+PrintGCDetails
-Xmx1g
-Xms1g
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC   
```

parNew年轻代， CMS年老代上面配置

**visualVM监控工具**

### 2. GC日志

要查看gc日志，那么首先得把gc日志进行输出，在JVM启动的时候添加参数：

-XX:+PrintGCDetails 打印GC日志细节

-XX:+PrintGCTimeStamps 打印GC日志时间

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9EbnRjb3NPUVRUNWNHMXNBRmUxYmljSEUzUmd5T1RqOUM1UTAySndlbFluNFN6RkM4ZDc4eUo4QThuWnVPMW1mMndRRWdMa3RONVdmajhNTklBSGlhR1N3LzY0MA?x-oss-process=image/format,png)



#### 2.1 parNew+CMS日志

parNew年轻代， CMS年老代，这里年轻代 8:1:1:1  年轻代共300m，年老代700m，堆1g大

```
-XX:+PrintGCDetails
-Xmx1g
-Xms1g
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC   
```

**parNew**

```
[GC (Allocation Failure) [ParNew: 279616K->23307K(314560K), 0.0126343 secs] 279616K->23307K(1013632K), 0.0126996 secs] [Times: user=0.00 sys=0.02, real=0.01 secs] 
```

最前面的`2019-03-01T13:38:04.037+0800: 0.867:`是固定的，`2019-03-01T13:38:04.037+0800`表示GC发生的日期花间，`0.867`表示本次gc与JVM启动时的相对时间，单位为秒。

`[GC (Allocation Failure)`这里的`GC`表示这是一次垃圾回收，但并不能单凭这个就判断这是依次Minor GC，下文会说到CMS的标识为`[GC (CMS Initial Mark)`和`[GC (CMS Final Remark)`，同样是`GC`CMS的却是是Major GC；括号中的`Allocation Failure`表示gc的原因，新生代内存不足而导致新对象内存分配失败。

再后面的`[ParNew:`表示本次gc使用的垃圾收集器为ParNew，我们知道ParNew是针对新生代的垃圾收集器，从这可以看出本次gc是Minor GC。后面紧跟着的`34944K->4352K(39296K)`的含义是`GC前该内存区域已使用容量 -> GC后该内存区域已使用容量（该内存区域总容量）`，再后面的`0.0138186 secs`表示该内存区域GC所占用的时间，单位为秒。

再后面的`34944K->6355K(126720K), 0.0141834 secs`表示收集前后整个堆的使用情况，`0.0141834 secs`表示本次GC的总耗时，包括把年轻代的对象转移到老年代的时间。

最后的`[Times: user=0.06 sys=0.00, real=0.02 secs]`表示GC事件在不同维度的耗时，单位为秒。这里面的user、sys和real与Linux的time命令所输出的时间含义一致，分别表示用户态消耗的CPU时间、内核态消耗的CPU时间和操作从开始到结束所经过的等待耗时，例如等待磁盘I/O、等待线程阻塞，而CPU时间不包括这些耗时，但当系统有多CPU或者多核的话，多线程操作会叠加这些CPU时间，所以有时候user或sys时间超过real时间也是完全正确的。

**CMS**

老年代由CMS收集器执行的Major GC相对比较复杂，包括初始标记、并发标记、重新标记和并发清除4个阶段

**初始标记阶段（CMS initial mark）**

```
[GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(699072K)] 31207K(1013632K), 0.0015626 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

`[GC (CMS Initial Mark)`表示这是CMS开始对老年代进行垃圾圾收集的初始标记阶段，该阶段从垃圾回收的“根对象”开始，且只扫描直接与“根对象”直接关联的对象，并做标记，需要暂停用户线程（Stop The Word，下面统称为STW），速度很快。`104208K(126116K)`表示当前老年代的容量为126116K，在使用了104208K时开始进行CMS垃圾回收。可以计算下这个比例，104208 / 126116约等于0.83，可以大概推算出CMS收集器的启动内存使用阈值。

**并发标记阶段（CMS concurrent mark）**

```
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.378/0.492 secs] [Times: user=1.00 sys=0.13, real=0.49 secs] 

```

该阶段进行了细分，但都是和用户线程并发进行的
 `[CMS-concurrent-mark`表示并发标记阶段，会遍历整个年老代并且标记活着的对象，后面的`0.154/0.155 secs`表示该阶段持续的时间和时钟时间，耗时0.15秒，可见耗时是比较长的。
 由于该阶运行的过程中用户线程也在运行，这就可能会发生这样的情况，已经被遍历过的对象的引用被用户线程改变，如果发生了这样的情况，JVM就会标记这个区域为Dirty Card。

`[CMS-concurrent-preclean`阶段会把上一个阶段被标记为Dirty Card的对象以及可达的对象重新遍历标记，完成后清楚Dirty Card标记。另外，一些必要的清扫工作也会做，还会做一些final remark阶段需要的准备工作。

`[CMS-concurrent-abortable-preclean`并发预清理，这个阶段尝试着去承担接下来STW的Final Remark阶段足够多的工作，由于这个阶段是重复的做相同的事情直到发生aboart的条件（比如：重复的次数、多少量的工作、持续的时间等等）之一才会停止。这个阶段很大程度的影响着即将来临的Final Remark的停顿。
 从后面的`1.190/1.707 secs`显示这个阶段持续了1秒多的时间，相当的长。

**重新标记阶段（CMS remark）**

```
[GC (CMS Final Remark) [YG occupancy: 170287 K (314560 K)]
[Rescan (parallel) , 0.0116904 secs]
[weak refs processing, 0.0002737 secs]
[class unloading, 0.0021527 secs]
[scrub symbol table, 0.0028338 secs]
[scrub string table, 0.0002261 secs]
[1 CMS-remark: 0K(699072K)] 170287K(1013632K), 0.0177178 secs] [Times: user=0.17 sys=0.00, real=0.02 secs] 
```

该阶段同样被细分为多个子阶段

`[GC (CMS Final Remark)`表示这是CMS的重新标记阶段，会STW，该阶段的任务是完成标记整个年老代的所有的存活对象，尽管先前的pre clean阶段尽量应对处理了并发运行时用户线程改变的对象应用的标记，但是不可能跟上对象改变的速度，只是为final remark阶段尽量减少了负担。
 `[YG occupancy: 24305 K (39296 K)]`表示年轻代当前的内存占用情况，通常Final Remark阶段要尽量运行在年轻代是足够干净的时候，这样可以消除紧接着的连续的几个STW阶段。

`[Rescan (parallel) , 0.0103714 secs]`这是整个final remark阶段扫描对象的用时总计，该阶段会重新扫描CMS堆中剩余的对象，重新从“根对象”开始扫描，并且也会处理对象关联。本次扫描共耗时 0.0103714s。

`[weak refs processing, 0.0006267 secs]`第一个子阶段，表示对弱引用的处理耗时为0.0006267s。

`[class unloading, 0.0368915 secs]`第二个子阶段，表示卸载无用的类的耗时为0.0368915s。

`[scrub symbol table, 0.0486196 secs]`最后一个子阶段，表示清理分别包含类级元数据和内部化字符串的符号和字符串表的耗时。

`[1 CMS-remark: 108093K(126116K)]`表示经历了上面的阶段后老年代的内存使用情况。再后面的`132398K(165412K), 0.1005635 secs`表示final remark后整个堆的内存使用情况和整个final remark的耗时。

**并发清除阶段（CMS concurrent sweep）**

```
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.004/0.004 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

该阶段和用户线程并发执行，不会STW，作用是清除之前标记阶段没有被标记的无用对象并回收内存。整个过程分为两个子阶段。

`CMS-concurrent-sweep`第一个子阶段，任务是清除那些没有标记的无用对象并回收内存。后面的参数是耗时，不再多提。

`CMS-concurrent-reset`第二个子阶段，作用是重新设置CMS算法内部的数据结构，准备下一个CMS生命周期的使用。

### 3. 参数配置

#### **内存参数设置**

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9pbWcyMDE4LmNuYmxvZ3MuY29tL2Jsb2cvMTYzNzU4LzIwMTgxMS8xNjM3NTgtMjAxODExMDExMzEzMzExODktNzg4NjczOTMucG5n?x-oss-process=image/format,png)

- -Xms设置堆的最小空间大小。
- -Xmx设置堆的最大空间大小。
- -Xmn:设置年轻代大小
- -XX:NewSize设置新生代最小空间大小。
- -XX:MaxNewSize设置新生代最大空间大小。
- -XX:PermSize设置永久代最小空间大小。
- -XX:MaxPermSize设置永久代最大空间大小。
- -Xss设置每个线程的堆栈大小
- -XX:+UseParallelGC:选择垃圾收集器为并行收集器。此配置仅对年轻代有效。即上述配置下,年轻代使用并发收集,而年老代仍旧使用串行收集。
- -XX:ParallelGCThreads=20:配置并行收集器的线程数,即:同时多少个线程一起进行垃圾回收。此值最好配置与处理器数目相等。
- -XX:InitialHeapSize   初始化堆大小  = xms   即JVM进行GC垃圾回收时，会对Heap进行清理，会对Heap的内存进行缩容的操作，那么缩容最小是缩到多小，这个值就是缩容可以缩到的最小内存值。
- -XX:MaxHeapSize     最大堆大小  =xmx
- -XX:MaxTenuringThreshold    设置垃圾最大年龄。如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代。对于年老代比较多的应用，可以提高效率。如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象再年轻代的存活时间，
  增加在年轻代即被回收的概率

典型JVM参数配置参考:

- java-Xmx3550m-Xms3550m-Xmn2g-Xss128k
- -XX:ParallelGCThreads=20
- -XX:+UseConcMarkSweepGC-XX:+UseParNewGC

-Xmx3550m:设置JVM最大可用内存为3550M。

-Xms3550m:设置JVM促使内存为3550m。此值可以设置与-Xmx相同,以避免每次垃圾回收完成后JVM重新分配内存。

-Xmn2g:设置年轻代大小为2G。整个堆大小=年轻代大小+年老代大小+持久代大小。持久代一般固定大小为64m,所以增大年轻代后,将会减小年老代大小。此值对系统性能影响较大,官方推荐配置为整个堆的3/8。

-Xss128k:设置每个线程的堆栈大小。JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K。更具应用的线程所需内存大小进行调整。在相同物理内存下,减小这个值能生成更多的线程。但是操作系统对一个进程内的线程数还是有限制的,不能无限生成,经验值在3000~5000 左右。

### 4. 线程数

ThreadStackSize   JVMMemory           能创建的线程数
默认的325K       -Xms1024m -Xmx1024m    i = 2655
默认的325K       -Xms1224m -Xmx1224m    i = 2072
默认的325K       -Xms1324m -Xmx1324m    i = 1753
默认的325K       -Xms1424m -Xmx1424m    i = 1435
-Xss1024k        -Xms1424m -Xmx1424m    i = 452

查看线程大小

```
jinfo -flag ThreadStackSize 43512
或者
java -XX:+PrintFlagsFinal -version
```

配置要使用这个，Xss过时了

```
-XX:ThreadStackSize=512k
```

### 5. jstat

jstat主要用来查看当前java进程的各内存区域的使用情况以及GC的次数和总耗时。我最常用的是下面的命令：

jstat -gcutil <pid> [interval] [times]
可以用[interval]来控制每隔多少毫秒重复输出一次，并通过[times]参数来控制输出的总次数。这两个参数都是可以省略的，如果都省略的话，就只输出一次。

### 6. JvisualVM

#### 6.1 GC插件-idea

visualVM Launcher

visual gc  插件要在jdk目录里安装

https://www.cnblogs.com/seamy/p/15649609.html

下载插件 org-graalvm-visualvm-modules-visualgc

#### 6.1 线程状态含义

运行（runnable）：正在运行中的线程。

休眠（timed_waiting）：休眠线程，例如调用Thread.sleep方法。

等待（waiting）：等待唤醒的线程，可通过调用Object.wait方法获得这种状态，底层实现是基于对象头中的monitor对象。

驻留（park）：等待唤醒的线程，和等待状态类似，只不过底层的实现方式不同，处于这种状态的例子有线程池中的**空闲线程**，等待获取reentrantLock锁的线程，调用了reentrantLock的condition的await方法的线程等等，底层实现是基于Unsafe类的park方法，在AQS中有大量的应用。

监视（blocked）：等待获取monitor锁的线程，例如等待进入synchronize代码块的线程。

### 7. 计算一个对象的内存大小

##### 7.1 org.apache.lucene工具类

https://www.amazingkoala.com.cn/Lucene/gongjulei/2019/1212/117.html

```
        <dependency>
            <groupId>org.apache.lucene</groupId>
            <artifactId>lucene-core</artifactId>
            <version>8.7.0</version>
        </dependency>
```

```
System.out.println(RamUsageEstimator.shallowSizeOf(webClient)); .//对象
System.out.println(RamUsageEstimator.sizeOf(new int[] {1}));//基本类型
```

#### 7.2 jdk8自带API

```
System.setProperty("java.vm.name","Java HotSpot(TM) ");

System.out.println(ObjectSizeCalculator.getObjectSize(3L));
```

### 8. 实际优化案例

#### 8.1 restTemplate并发请求

背景：使用jemter进行压测

RestTemplate(new HttpComponentsClientHttpRequestFactory()) 5000请求1s， 并发吞吐量qps只有40-50/s，一个请求60K； >10000请求1s内直接报错

因为还没配置PoolingHttpClientConnectionManager，所以只使用了少部分线程，但是tomcat也新建了200个最大线程，浪费资源。

使用webclient能把吞吐量qps提升到  2500/s，内内存减少一半，支持十万以上并发

因为minor GC 的时候RestTemplate还在挂起的线程很多，因为处理得慢，所以每次minorGC都会增加十几M到

old年老代；

而weblient由于处理得快，每次到老年代没有或者更少

#### 8.2 GC每分钟几百次

原先的JVM参数

```
-XX:+PrintGCDetails
-XX:InitialHeapSize=1287651328
-XX:MaxHeapSize=1287651328
-XX:MaxNewSize=87244800
-XX:ThreadStackSize=512k
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC
```

没有按照8:1:1    

年轻代 8 M 两个survivor 各1M ， 老年代1.1G

年轻代和sucivor太小

-> 策略：尽量在年轻代就回收完，因为年轻代的回收暂停很短暂，所以扩大成8:1:1的模式

### 9. 工具

#### 9.1 内存分析工具MAT(Memory Analyzer Tool)

能够分析 dump文件可视化

## 七、并发测试

### 1. jmeter

参考  https://blog.csdn.net/hechurui/article/details/109158135

```
TestPlan	
	Thread Group
		HTTP请求
		查看结果树
		汇总聚合报告
		请求头
		/用户自定义变量
		/响应断言
		/响应断言结果
```

注意，一个TestPlan所有的线程组都会执行，所以可以部分disable Thread Group

#### 1.1 线程组参数解读

**Number of Threads (users)**：虚拟用户数（也就是线程数），一个虚拟用户占用一个进程或线程
**Ramp-Up Period(in seconds)**：准备时长，设置的虚拟用户数需要多长时间全部启动。
`例如：如果线程数为20，准备时长为2，那么需要2秒钟启动20个线程，也就是每秒钟启动10个线程`
**Loop Count**：循环次数每个线程发送请求的次数
`如果线程数为20，循环次数为100，那么每个线程发送100次请求。总请求数为20*100=2000 。如果勾选了“Forever”，那么所有线程会一直发送请求，一到选择停止运行脚本。`
**Delay Thread creation until needed**：直到需要时延迟线程的创建
**Scheduler**：调度器，设置线程组启动的开始时间和结束时间(配置调度器时，需要勾选循环次数为永远)
**Duration(Seconds)**：持续时间(秒)，测试持续时间，会覆盖结束时间
**Startup delay(Seconds)**：启动延迟（秒），测试延迟启动时间，会覆盖启动时间

#### 1.2 聚合报告参数解读

1. **Label**：每个 JMeter 的 element都有一个 Name 属性，这里显示的是 Name 属性的值
2. **#Samples**：请求数——表示这次测试中一共发出了多少个请求
   `如果模拟10个用户，每个用户迭代10次，那么这里显示100`
3. **Average**：平均响应时间——默认情况下是单个 Request 的平均响应时间
   `当使用了 Transaction Controller 时，以Transaction 为单位显示平均响应时间`
4. **Median**：中位数，也就是 50％ 用户的响应时间
5. **90% Line**：90％ 用户的响应时间
6. **99% Line**：99％ 用户的响应时间
7. **Min**：最小响应时间
8. **Max**：最大响应时间
9. **Error%**：错误率——错误请求数/请求总数
10. **Throughput**：**吞吐量**——默认情况下表示每秒完成的请求数（Request per Second）
    `当使用了 Transaction Controller 时，也可以表示类似 LoadRunner 的 Transaction per Second 数`
11. **KB/Sec**：每秒从服务器端接收到的数据量

在实际中我们需要关注的点只有——`#Samples 请求数，Average 平均响应时间，Min 最小响应时间，Max 最大响应时间，Error% 错误率和Throughput 吞吐量`

#### 1.3 并发端口bind问题

java.net.BindException: Address already in use: connect

windows提供给TCP/IP链接的端口为 1024-5000，并且要四分钟来循环回收它们，就导致我们在短时间内跑大量的请求时将端口占满了，导致如上报错

解决方案

1：http连接关闭keep live

2：修改注册表

1.cmd中输入regedit命令打开注册表；
2.在HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters右键Parameters；
3.添加一个新的DWORD，名字为MaxUserPort；
4.然后双击MaxUserPort，输入数值数据为65534，基数选择十进制；

### 2. ab.exe



## 八、idea使用相关

官网使用指南 

https://www.jetbrains.com/help/idea/2023.1/viewing-structure-and-hierarchy-of-the-source-code.html?utm_source=product&utm_medium=link&utm_campaign=IU&utm_content=2023.1

### 1. 插件

visualVm Launcher 另外加入gc插件

save actions

mybatisX

maven helper

alibaba java coding guidelines

codegalance2 -侧边栏

free mybatis plugin

leetcode editor

lombok

maven helper

save action

translation

visualVM Launcher

SequenceDiagram  在类的方法上右键选中能查看序列图

jclasslib **快速查看类的字节码文件**

### 2. 快捷键

win+e  最近使用的文件

alt+f7   方法名上使用快捷键 Alt + F7 来查看这个方法在哪些地方被调用过

ctrl + 7  查看类的数据结构

Ctrl + Alt + B/鼠标左键   方法名或者类名上，想直接跳转到某个方法/类的实现类

Ctrl  +  H   BeanDefinition 被哪些类实现

Ctrl + N  搜索

Ctrl + P  查看方法入参结构

ctrl + alt + 鼠标左键   点击类名， 相当于左侧的下箭头，能看到该类被哪个实现

ctrl + alt + < 回到上次光标初

Alt + insert  能查看接口的实现方法，简单的如toString() 添加测试类  也能查看覆写的方法 Ctrl+O 的内容

Ctrl + O  查看接口的实现方法，toString() hashcode()等

Ctrl + Alt + T  鼠标扩住一段代码，能实现一些方法，如包住  try catch

Ctrl + F12  在类中能查找类的方法啥的

Ctrl + Alt + M  抽取代码片段成方法

Ctrl + Q  查看方法，类的介绍

判断语句后直接      .if

一般语句直接转化为打印     .sout   

## 九、开发工具积累

### 1. 渗透测试工具Burp Suite

https://zhuanlan.zhihu.com/p/537832119

## 十、github精华Star

知乎总结： https://zhuanlan.zhihu.com/p/419823562

手写spring  

https://www.liaoxuefeng.com/wiki/1539348902182944

https://github.com/michaelliao/summer-framework

#### 后端技术官网

| 技术                 | 说明                | 官网                                                |
| -------------------- | ------------------- | --------------------------------------------------- |
| SpringBoot           | Web应用开发框架     | https://spring.io/projects/spring-boot              |
| SpringSecurity       | 认证和授权框架      | https://spring.io/projects/spring-security          |
| MyBatis              | ORM框架             | http://www.mybatis.org/mybatis-3/zh/index.html      |
| MyBatisGenerator     | 数据层代码生成器    | http://www.mybatis.org/generator/index.html         |
| Elasticsearch        | 搜索引擎            | https://github.com/elastic/elasticsearch            |
| RabbitMQ             | 消息队列            | https://www.rabbitmq.com/                           |
| Redis                | 内存数据存储        | https://redis.io/                                   |
| MongoDB              | NoSql数据库         | [https://www.mongodb.com](https://www.mongodb.com/) |
| LogStash             | 日志收集工具        | https://github.com/elastic/logstash                 |
| Kibana               | 日志可视化查看工具  | https://github.com/elastic/kibana                   |
| Nginx                | 静态资源服务器      | https://www.nginx.com/                              |
| Docker               | 应用容器引擎        | [https://www.docker.com](https://www.docker.com/)   |
| Jenkins              | 自动化部署工具      | https://github.com/jenkinsci/jenkins                |
| Druid                | 数据库连接池        | https://github.com/alibaba/druid                    |
| OSS                  | 对象存储            | https://github.com/aliyun/aliyun-oss-java-sdk       |
| MinIO                | 对象存储            | https://github.com/minio/minio                      |
| JWT                  | JWT登录支持         | https://github.com/jwtk/jjwt                        |
| Lombok               | Java语言增强库      | https://github.com/rzwitserloot/lombok              |
| Hutool               | Java工具类库        | https://github.com/looly/hutool                     |
| PageHelper           | MyBatis物理分页插件 | http://git.oschina.net/free/Mybatis_PageHelper      |
| Swagger-UI           | API文档生成工具     | https://github.com/swagger-api/swagger-ui           |
| Hibernator-Validator | 验证框架            | http://hibernate.org/validator                      |

#### 前端技术官网

| 技术       | 说明                  | 官网                                                  |
| ---------- | --------------------- | ----------------------------------------------------- |
| Vue        | 前端框架              | https://vuejs.org/                                    |
| Vue-router | 路由框架              | https://router.vuejs.org/                             |
| Vuex       | 全局状态管理框架      | https://vuex.vuejs.org/                               |
| Element    | 前端UI框架            | [https://element.eleme.io](https://element.eleme.io/) |
| Axios      | 前端HTTP框架          | https://github.com/axios/axios                        |
| v-charts   | 基于Echarts的图表框架 | https://v-charts.js.org/                              |
| Js-cookie  | cookie管理工具        | https://github.com/js-cookie/js-cookie                |
| nprogress  | 进度条控件            | https://github.com/rstacruz/nprogress                 |

#### 移动端技术官网

| 技术         | 说明             | 官网                                                  |
| ------------ | ---------------- | ----------------------------------------------------- |
| Vue          | 核心前端框架     | [https://vuejs.org](https://vuejs.org/)               |
| Vuex         | 全局状态管理框架 | [https://vuex.vuejs.org](https://vuex.vuejs.org/)     |
| uni-app      | 移动端前端框架   | [https://uniapp.dcloud.io](https://uniapp.dcloud.io/) |
| mix-mall     | 电商项目模板     | https://ext.dcloud.net.cn/plugin?id=200               |
| luch-request | HTTP请求框架     | https://github.com/lei-mu/luch-request                |

## 十一、多线程

一般多线程的题目有以下几种解法

sync(object) + wait + notify

lock  + condition

countdownlanch

cyclibarrier

semaphore

### 11.1 lock  + condition

https://blog.csdn.net/qq_35764295/article/details/108349483

管程这个概念，它能够解决并发编程领域的两大核心问题

1. 互斥：同一时刻只能有一个线程访问共享资源
2. 同步：线程之间的通信协作

而Java在1.5版本之前，是通过synchronized来实现。到了Java 1.5版本，Java大神Doug Lea的又在Java并发包增加了Lock和Condition两接口来解决这两个问题

为啥synchronized实现了管程，还要添加这两接口重复实现呢？答案很容易猜到，就是synchronized有缺陷，针对互斥与同步都存在缺陷：

1. 无法解决死锁问题
2. 最多使用一个条件变量

接下来我们就来看看Lock与Condition接口是如何解决这两个问题的

#### 11.1.1 lock接口

只有以下四个条件都发生时才会出现死锁：

1. 互斥：共享资源只能被一个线程占用
2. 不可抢占：其他线程不能强行另一个线程占有的资源
3. 占有且等待：线程T1已经取得共享资源X时，在等待共享资源Y时候，不释放共享资源X
4. 循环等待：线程T1等待线程T2占有的资源，线程T2等待线程T1占有的资源

所以，我们只要破坏其中一个，就可以成功避免死锁的发生，需要注意的是，互斥这个条件我们没办法破坏，因为我们用锁就是为了互斥，而其他三个是可以破坏掉。

Lock是通过下面三个方式来破坏掉 "不可抢占"这个条件的：

1. 非阻塞获取锁：尝试获取锁，如果失败了就立刻返回失败，这样就可以释放已经持有的锁
2. 响应中断：如果发生死锁后，此线程被其他线程中断，则会释放锁，解除死锁
3. 支持超时：一段时间内获取不到锁，就返回失败，这样就可以释放之前已经持有的锁

还需要额外注意的一点，当我们使用内置锁时，是不需要考虑释放锁的，而Lock作为一种显示锁，是需要手动释放的。我们一般通过try-finally或try-catch保护锁定时执行的所有代码，并确保在必要时释放锁定，作者也给我们提供了比较常用的使用的方式

#### 11.1.2 condition接口

Lock解决了synchronized不能处理死锁的问题，并对功能进行了扩展，而Condition接口就是解决synchronized只能有一个条件变量的缺点。

同一个synchronize内置锁只对应一个wait set，即当线程调用wait方法时，会把当前线程放入到同一个wait set中，当我们需要根据某些特定的条件来唤醒符合条件的线程时，我们只能先从wait set里唤醒一个线程后，再看是否符合条件，如果不符合条件，则需要将此线程继续wait，然后再去wait set中获取下一个线程再判断是否满足条件。这样会导致许多无意义的cpu开销。



所以Condition就是为了解决上面问题存在的。每一个锁都会对应多个Condition，每个Condtition都有一个容器来保存相应的等待线程，拿到锁的线程想唤醒某个等待特定条件的线程时，只需要去唤醒对应Condition容器中的线程即可。



获取一个Condition必须通过Lock的newCondition()方法。下面通过一个有界队列的示例来 深入了解Condition的使用方式。有界队列是一种特殊的队列，当队列为空时，队列的获取操作 将会阻塞获取线程，直到队列中有新增元素，当队列已满时，队列的插入操作将会阻塞插入线 程，直到队列出现“空位”，如代码所示。

       public class BoundedQueue<T> {
           private Object[] items;
           // 添加的下标，删除的下标和数组当前数量
           private int addIndex, removeIndex, count;
           private Lock lock = new ReentrantLock();
           private Condition notEmpty = lock.newCondition();
           private Condition notFull = lock.newCondition();
           
       public BoundedQueue(int size) {
           items = new Object[size];
       }
    
       // 添加一个元素，如果数组满，则添加线程进入等待状态，直到有"空位"
       public void add(T t) throws InterruptedException {
           lock.lock();
           try
    
           {
               while (count == items.length) notFull.await();
               items[addIndex] = t;
               if (++addIndex == items.length) addIndex = 0;
               ++count;
               notEmpty.signal();
           } finally
    
           {
               lock.unlock();
           }
       }
    
       // 由头部删除一个元素，如果数组空，则删除线程进入等待状态，直到有新添加元素 @SuppressWarnings("unchecked")
       public T remove() throws InterruptedException {
           lock.lock();
           try {
               while (count == 0) notEmpty.await();
               Object x = items[removeIndex];
               if (++removeIndex == items.length) removeIndex = 0;
               --count;
               notFull.signal();
               return (T) x;
           } finally {
               lock.unlock();
           }
       }

#### 11.1.3 Condition如何实现

ConditionObject是同步器AbstractQueuedSynchronizer的内部类，因为Condition的操作需要 获取相关联的锁，所以作为同步器的内部类也较为合理。每个**Condition对象都包含着一个队 列（以下称为等待队列**），该队列是Condition对象实现等待/通知功能的关键。 下面将分析Condition的实现，主要包括：等待队列、等待和通知，下面提到的Condition如 果不加说明均指的是ConditionObject。

## 十二、流式编程

对象嵌套太深：如何优雅判空获取里层对象
Optional.ofNullable(rspBody).map(TracePageCommRsp::getData).map(PageRspData::getList).orElseGet(Collections::emptyList);

### 12.1 Flux

```
public static void main(String[] args) {
    //just()：创建Flux序列，并声明数据流，
    Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4);//整形
    //subscribe()：订阅Flux序列，只有进行订阅后才回触发数据流，不订阅就什么都不会发生
    integerFlux.subscribe(System.out::println);

    Flux<String> stringFlux = Flux.just("hello", "world");//字符串
    stringFlux.subscribe(System.out::println);

    //fromArray(),fromIterable()和fromStream()：可以从一个数组、Iterable 对象或Stream 对象中创建Flux序列
    Integer[] array = {1,2,3,4};
    Flux.fromArray(array).subscribe(System.out::println);

    List<Integer> integers = Arrays.asList(array);
    Flux.fromIterable(integers).subscribe(System.out::println);

    Stream<Integer> stream = integers.stream();
    Flux.fromStream(stream).subscribe(System.out::println);

    Mono data = Mono.just("bole");


    data.subscribe(i -> System.out.println(i));
    Flux.just("Hello", "World").subscribe(System.out::println);
    Flux.fromArray(new Integer[]{1, 2, 3}).subscribe(System.out::println);
    Flux.empty().subscribe(System.out::println);
    Flux.range(1, 10).subscribe(System.out::println);
    Flux.interval(Duration.of(10, ChronoUnit.MINUTES)).subscribe(System.out::println);

    Flux.range(1, 100).buffer(20).subscribe(System.out::println);
    Flux.range(1, 10).bufferUntil(i -> i%2 == 0).subscribe(System.out::println);
    Flux.range(1, 10).bufferWhile(i -> i%2 == 0).subscribe(System.out::println);

}
```

