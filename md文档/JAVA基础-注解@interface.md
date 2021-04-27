### JDK自带注解

***\*@Override 表示当前方法覆盖了父类的方法
@Deprecation 表示方法已经过时,方法上有横线，使用时会有警告。
@SuppviseWarnings 表示关闭一些警告信息(通知java编译器忽略特定的编译警告)\****





### **自定义@Interface注解**

1.可以使用default为成员指定一个默认值，如上所示

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

### 元注解

​     何为元注解？就是注解的注解，就是给你自己定义的注解添加注解，你自己定义了一个注解，但你想要你的注解有什么样的功能，此时就需要用元注解对你的注解进行说明了。

​    元注解有4个

#### @Retention

1. 用@Retention(RetentionPolicy.CLASS)修饰的注解，表示注解的信息被保留在class文件(字节码文件)中当程序编译时，但不会被虚拟机读取在运行的时候；
2. 用@Retention(RetentionPolicy.SOURCE)修饰的注解,表示注解的信息会被编译器抛弃，不会留在class文件中，注解的信息只会留在源文件中；
3. 用@Retention(RetentionPolicy.RUNTIME)修饰的注解，表示注解的信息被保留在class文件(字节码文件)中，当程序编译时，会被虚拟机保留在运行时。

#### **@Target**

即注解的作用域，用于说明注解的使用范围（即注解可以用在什么地方，比如类的注解，方法注解，成员变量注解等等）

**取值：**

​     ElemenetType.CONSTRUCTOR----------------------------构造器声明 
​     ElemenetType.FIELD --------------------------------------域声明（包括 enum 实例） 
​     ElemenetType.LOCAL_VARIABLE------------------------- 局部变量声明 
​     ElemenetType.METHOD ----------------------------------方法声明 
​     ElemenetType.PACKAGE --------------------------------- 包声明 
​     ElemenetType.PARAMETER ------------------------------参数声明 
​     ElemenetType.TYPE--------------------------------------- 类，接口（包括注解类型）或enum声明

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

#### @Inherited

1. 是一个标记注解，没有成员，表示允许子类继承该注解，也就是说如果一个使用了@Inherited修饰的注解被用于一个class时，则这个注解将被该class的子类继承拥有

2. 使用了@Inherited修饰的注解只能被子类所继承，并不可以从它所实现的接口继承

3. 子类继承父类的注解时，并不能从它所重载的方法继承注解

#### @**Documented**

指定被标注的注解会包含在javadoc中。

#### @FunctionalInterface

函数式接口 (Functional Interface) 其实就是一个只具有一个方法的普通接口。

```
@FunctionalInterface
public interface Runnable {
    void run();
}
```

### Spring注解

#### @Bean

Spring的@Bean注解用于告诉方法，产生一个Bean对象，然后这个Bean对象交给Spring管理。(和xml配置中的bean标签的作用是一样的)

@Bean是一个方法级别上的注解，主要用在@Configuration注解的类里，也可以用在@Component注解的类里。添加的bean的id为方法名

```
@Configuration
public class AppConfig {
    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl();
    }
}
```

这个配置就等同于之前在xml里的配置

```
<beans>
    <bean id="transferService" class="com.acme.TransferServiceImpl"/>
</beans>
```

bean的依赖

@bean 也可以依赖其他任意数量的bean，如果TransferService 依赖 AccountRepository，我们可以通过方法参数实现这个依赖

```
@Configuration
public class AppConfig {
    @Bean // 创建名为方法名的bean "transferService"
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}
```

示例

```
@Configuration
public class MyJasyptConfigure {

    @Bean("kmsEncryptUtils")
    public  KmsEncryptUtils initkmsEncryptUtils() throws Exception {
        return new KmsEncryptUtils("AKIDFgyrUqPtKDsHcNuBUkkLjOlumtQiu7KC", "qPM7crbHelajfLkeUDgsCIKpJWidKwRg");
    }
    
    @Bean("kmsStringEncryptor") // 注入名为 kmsEncryptUtils 的bean ，上面已经创建
    public KmsStringEncryptor init(KmsEncryptUtils kmsEncryptUtils) {
        return new KmsStringEncryptor(kmsEncryptUtils);
    }

}
```

```
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class KmsStringEncryptor implements StringEncryptor {

    private KmsEncryptUtils kmsEncryptUtils;

    @Override
    public String encrypt(String s){
        return null;
    }

    @Override
    public String decrypt(String s) {
        String res = null;
        try{
            res = kmsEncryptUtils.decryptData(s);
        }catch (Exception e){
            log.error("decrypt" ,e);
        }
        return res;
    }

}
```

```
 //默认情况下bean的名称和方法名称相同，你也可以使用name属性来指定
 @Bean(name = "myFoo")
    public Foo foo() {
        return new Foo();
    }

 // bean的命名支持别名
 @Bean(name = { "dataSource", "subsystemA-dataSource", "subsystemB-dataSource" })
    public DataSource dataSource() {
        // instantiate, configure and return DataSource bean...
    }
// bean的描述
 @Bean
    @Description("Provides a basic example of a bean")
    public Foo foo() {
        return new Foo();
    }
```

