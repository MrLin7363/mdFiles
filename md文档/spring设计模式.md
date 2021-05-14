spring设计模式

#### 观察者模式

JDK提供的观察者变量

```
// 问题
public class Question {
    private String userName;
    private String content;
}
```

```
// 观察者
public class Teacher implements Observer {
    private String name;
    public Teacher(String name){
        this.name = name;
    }
	// 重写的方法
    public void update(Observable o, Object arg) {
        GPer gper = (GPer)o;
        Question question = (Question)arg;
        System.out.println("===============================");
        System.out.println(name + "老师，你好！\n" +
        "您收到了一个来自“" + gper.getName() + "”的提问，希望您解答，问题内容如下：\n" +
        question.getContent() + "\n" +
        "提问者：" + question.getUserName());
    }
}
```

```
/**
 * JDK提供的一种观察者的实现方式，被观察者
 */
public class GPer extends Observable{

    private String name = "微信提问圈";
    private static GPer gper = null;
    private GPer(){}

    public static GPer getInstance(){
        if(null == gper){
            gper = new GPer();
        }
        return gper;
    }

    public String getName() {
        return name;
    }

    public void publishQuestion(Question question){
        System.out.println(question.getUserName() + "在" + this.name + "上提交了一个问题。");
        setChanged(); // 当前对象已更改
        notifyObservers(question); // 通知观察者,遍历观察者，回调观察者的update方法
    }
}
```

```
public class ObserverTest {
    public static void main(String[] args) {
        GPer gper = GPer.getInstance();
        Teacher tom = new Teacher("Tom");
        Teacher mic = new Teacher("Mic");

        Question question = new Question();
        question.setUserName("小明");
        question.setContent("观察者设计模式适用于哪些场景？");
        gper.addObserver(tom);
        gper.addObserver(mic);
        gper.publishQuestion(question);
    }
}
```

```
小明在微信提问圈上提交了一个问题。
===============================
Mic老师，你好！
您收到了一个来自“微信提问圈”的提问，希望您解答，问题内容如下：
观察者设计模式适用于哪些场景？
提问者：小明
===============================
Tom老师，你好！
您收到了一个来自“微信提问圈”的提问，希望您解答，问题内容如下：
观察者设计模式适用于哪些场景？
提问者：小明
```

#### 代理模式

##### 示例一-动态代理-媒婆

```
public class Girl implements Person {
    public void findLove() {
        System.out.println("高富帅");
        System.out.println("身高180cm");
        System.out.println("有6块腹肌");
    }
}
```

```
public class JDKMeipo implements InvocationHandler {

    private Object target;
    public Object getInstance(Object person) throws Exception{
        this.target = person;
        Class<?> clazz = target.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        Object obj = method.invoke(this.target,args);
        after();
       return obj;
    }

    private void before(){
        System.out.println("我是媒婆，我要给你找对象，现在已经确认你的需求");
        System.out.println("开始物色");
    }

    private void after(){
        System.out.println("OK的话，准备办事");
    }
}
```

```
 public static void main(String[] args) {
        try {
            Object obj = new JDKMeipo().getInstance(new Girl());
            Method method = obj.getClass().getMethod("findLove",null);
            method.invoke(obj);
            }
    }
```

```
我是媒婆，我要给你找对象，现在已经确认你的需求
开始物色
高富帅
身高180cm
有6块腹肌
OK的话，准备办事
```

##### 示例二-静态代理-父亲物色

```
public interface Person {

    void findLove();
}
```

```
public class Father implements Person {
    private Son person;

    public Father(Son person){
        this.person = person;
    }

    public void findLove(){
        System.out.println("父亲物色对象");
        this.person.findLove();
        System.out.println("双方父母同意，确立关系");
    }
  	public void findJob(){

    }
}
```

```
public class Son implements Person{

    public void findLove(){
        System.out.println("儿子要求：肤白貌美大长腿");
    }

    public void findJob(){

    }
}
```

```
public class FatherProxyTest {
    public static void main(String[] args) {
        Father father = new Father(new Son());
        father.findLove();
    }
}
```

```
父亲物色对象
儿子要求：肤白貌美大长腿
双方父母同意，确立关系
```

##### 示例三-动态代理-数据源

```
public class Order {

    private Object orderInfo;
    
    //订单创建时间进行按年分库
    private Long createTime;
    private String id;
    
}
```

```
public class OrderDao {
    public int insert(Order order){
        System.out.println("OrderDao创建Order成功!");
        return 1;
    }
}
```

```
public class OrderService implements IOrderService {
    private OrderDao orderDao;

    public OrderService(){
        //如果使用Spring应该是自动注入的
        //我们为了使用方便，在构造方法中将orderDao直接初始化了
        orderDao = new OrderDao();
    }

    public int createOrder(Order order) {
        System.out.println("OrderService调用orderDao创建订单");
        return orderDao.insert(order);
    }
}
```

```
public interface IOrderService {
    int createOrder(Order order);
}
```

```
public class OrderServiceDynamicProxy implements InvocationHandler {

    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    Object proxyObj;
    public Object getInstance(Object proxyObj) {
        this.proxyObj = proxyObj;
        Class<?> clazz = proxyObj.getClass();
        return Proxy.newProxyInstance(new GPClassLoader(),clazz.getInterfaces(),this);
    }

    // method OrderDao , args[0] Order , proxy OrderServiceDynamicProxy
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(args[0]);
        Object object = method.invoke(proxyObj,args); // 调用 dao.create方法
        after();
        return object;
    }

    private void after() {
        System.out.println("Proxy after method");
        //还原成默认的数据源
        DynamicDataSourceEntity.restore();
    }

    //target 应该是订单对象Order
    private void before(Object target) {
        try {
            //进行数据源的切换
            System.out.println("Proxy before method");

            //约定优于配置
            Long time = (Long) target.getClass().getMethod("getCreateTime").invoke(target);
            Integer dbRouter = Integer.valueOf(yearFormat.format(new Date(time)));
            System.out.println("静态代理类自动分配到【DB_" + dbRouter + "】数据源处理数据");
            DynamicDataSourceEntity.set(dbRouter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```

```
public class DbRouteProxyTest {
    public static void main(String[] args) {
        try {
            Order order = new Order();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date date = sdf.parse("2017/02/01");
            order.setCreateTime(date.getTime());
            
            // 代理了Order实现OrderService的动态代理去设置数据源，其他Service也能动态代理
            IOrderService orderService = (IOrderService)new OrderServiceDynamicProxy().getInstance(new OrderService());
            orderService.createOrder(order); // 这行开始执行 OrderServiceDynamicProxy.invoke
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
```

```
Proxy before method
静态代理类自动分配到【DB_2017】数据源处理数据
OrderService调用orderDao创建订单
OrderDao创建Order成功!
Proxy after method
```

2.手写实现JDK动态代理 

不仅知其然，还得知其所以然。既然JDK动态代理功能如此强大， 那么它是如何实现的呢？我们现在来探究一下原理，并模仿JDK动态代 理动手写一个属于自己的动态代理。 我们都知道JDK动态代理采用字节重组，重新生成对象来替代原始 对象，以达到动态代理的目的。JDK动态代理生成对象的步骤如下： 

（1）获取被代理对象的引用，并且获取它的所有接口，反射获 取。 

（2）JDK动态代理类重新生成一个新的类，同时新的类要实现被 代理类实现的所有接口。 

（3）动态生成Java代码，新加的业务逻辑方法由一定的逻辑代码 调用（在代码中体现）。 

（4）编译新生成的Java代码.class文件。 

（5）重新加载到JVM中运行。 以上过程就叫字节码重组。

JDK中有一个规范，在ClassPath下只要 是$开头的.class文件，一般都是自动生成的。那么我们有没有办法看到 代替后的对象的“真容”呢？做一个这样测试，我们将内存中的对象字节 码通过文件流输出到一个新的.class文件，然后利用反编译工具查看其源 代码。

