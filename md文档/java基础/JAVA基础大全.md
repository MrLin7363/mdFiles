## 一、基础类

### DecimalFormat

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

### 面向对象

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

### 反射

Class<?> 任意一个类

Class<?>[] 任意一个接口

### 泛型

```
// 第一个T是方法参数限定，表示（）内的T必须是T类型的
// 第二个T是方法的返回类型
// 第三个T和第一个T对应
public static <T extends Comparable & Serializable> T getMin(T...a){
```

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

#### 监控不到本地JAVA程序

win+R 输入 %TMP%\

找到 hsperfdata_用户名，修改该文件夹权限 为 完全控制

JAVA程序线程才能输入到这个文件夹被监控到

项目启动打印JVM信息

-XX:+PrintGCDetails

-XX:+PrintGCDetails -Xmx512m -Xms512m 这两个最好一致，避免GC堆伸缩幅度太大，停顿

-Xmx默认为物理内存的1/4

-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

**visualVM监控工具**