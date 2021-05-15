## 书籍PDFspring源码

## DI源码

### 1.依赖注入发生的时间

当Spring loC容器完成了 Bean定义资源的定位、载入和解析注册以后,loC容器中已经管理类Bean 定义的相关数据，但是此时loC容器还没有对所管理的Bean进行依赖注入，依赖注入在以下两种情况 发生：

1.  、用户第一次调用getBean()方法时，loC容器触发依赖注入。 
2.  、当用户在配置文件中将＜bean＞元素配置了 lazy-init二false属性,即让容器在解析注册Bean定义 时进行预实例化，触发依赖注入。 



### 2.寻找获取Bean的入口

AbstractBeanFactory的 getBean()相关方法

在Spring中 ，如果Bean定义的单例 模式(Singleton), 则容器在创建之前先从缓存中查找，以确保整个容器中只存在一个实例对象。如果 Bean定义的是原型模式(Prototype)，则容器每次都会创建一个新的实例。除此之外,Bean定义 还可以扩展为指定其生命周期范围。

上面的源码只是定义了根据Bean定义的模式采取的不同创建Bean实例对象的策略 具体的Bean 实例对象的创建过程由实现了 ObjectFactory接口的匿名内部类的createBean()方 法 完 成 ， Object Factory使 用 委 派 模 式 , 具 体 的 Bean实 例 创 建 过 程 交 由 其 实 现 类 AbstractAutowireCapableBeanFactory完 成 ，我们继续分析 AbstractAutowireCapableBeanFactory 的 createBean。方法的源码，理解其创建Bean实例的具体实现过程。

### 3.开始实例化

AbstractAutowireCapableBeanFactory 类实现了 ObjectFactory 接口，创建容器指定的 Bean 实 例对象，同时还对创建的Bean实例对象进行初始化处理。

通过上面的源码注释，我们看至IJ具体的依赖注入实现其实就在以下两个方法中：

1.  、createBeanlnstance()方 法 ,生 成 Bean所包含的java对象实例。 
2.  、populateBean()方 法 ,对 Bean属性的依赖注入进行处理。 

下面继续分析这两个方法的代码实现。

### 4.选择Bean实例化策略

在createBeanlnstance方法中，根据指定的初始化策略，使用简单工厂、工厂方法或者容器的自 动装配特性生成Java实例对象

对使用工厂方法和自动装配特性的Bean的实例化相当比较清 楚 ，调用相应的工厂方法或者参数匹配的构造方法即可完成实例化对象的工作，但是对于我们最常使用 的默认无参构造方法就需要使用相应的初始化策略(JDK的反射机制或者CGLib)来进行初始化了 ,在方 法 getlnstantiationStrategy().instantiate()中就具体实现类使用初始策略实例化对象。

### 5.执行Bean实例化

在 使 用 默 认 的 无 参 构 造 方 法 创 建 Bean的 实 例 化 对 象 时 ， 方 法 getlnstantiationStrategy().instantiate()调用了 SimplelnstantiationStrategy 类中的实例化 Bean 的 方法

如果Bean有方法被覆盖了，则使用JDK的反射机制进行实例化, 否则,使用CGLib进行实例化。 instantiateWithMethodInjection()方 法 调 用 SimplelnstantiationStrategy 的 子 类 CGLibSubclassinglnstantiationStrategy 使用 CGLib 来进行初始化

CGLib是一个常用的字节码生成器的类库，它提供了一系列API实现Java字节码的生成和转换功 能。我们在学习JDK的动态代理时都知道，JDK的动态代理只能针对接口，如果一个类没有实现任何接 口 ,要对其进行动态代理只能使用CGLib。

### 6.准备依赖注入

在前面的分析中我们已经了解到Bean的 依 赖 注 入 主 要 分 为 两 个 步 骤 ，首先调用 createBeanlnstance。方法生成Bean所包含的Java对象实例。然后,调用populateBean()方法，对 Bean属性的依赖注入进行处理。

上面我们已经分析了容器初始化生成Bean所包含的Java实例对象的过程，现在我们继续分析生成 对象后，Spring loC容器是如何将Bean的属性依赖关系注入Bean实例对象中并设置好的，回到 AbstractAutowireCapableBeanFactory 的 populateBean方法，对属性依赖注入

分析上述代码，我们可以看出，对属性的注入过程分以下两种情况:

1 )、属性值类型不需要强制转换时，不需要解析属性值，直接准备进行依赖注入。

2)、属性值需要进行类型强制转换时,如对其他对象的引用等，首先需要解析属性值，然后对解析后的 属性值进行依赖注入。

对属性值的解析是在 BeanDefinitionValueResolver 类中的 resolveValuelfNecessary()方法中进 行 的 ，对属性值的依赖注入是通过bw.setPropertyValues方法实现的，在分析属性值的依赖注入之前, 我们先分析一下对属性值的解析过程。

### 7.解析属性注入规则

当容器在对属性进行依赖注入时，如果发现属性值需要进行类型转换，如属性值是容器中另一个 Bean实例对象的引用,则容器首先需要根据属性值解析出所引用的对象,然后才能将该引用对象注入 到目标实例对象的属性上去，对属性进行解析的由resolveValuelfNecessary。

Spring是如何将引用类型，内部类以及集合类型等属性进行解析 的 ，属性值解析完成后就可以进行依赖注入了，依赖注入的过程就是Bean对象实例设置到它所依赖的 Bean对象属性上去。而真正的依赖注入是通过bw.setPropertyValues。方法实现的，该方法也使用了 委托模式,在 BeanWrapper接口中至少定义了方法声明，依赖注入的具体实现交由其实现类 BeanWrapperlmpI来完成，下面我们就分析依BeanWrapperlmpI中赖注入相关的源码。

### 8.注入赋值

BeanWrapperlmpI类主要是对容器中完成初始化的Bean实例对象进行属性的依赖注入，即把 Bean对象设置到它所依赖的另一个Bean的属性中去。然而，BeanWrapperlmpI中的注入方法实际 上由AbstractNestablePropertyAccessor来实现的

通过对上面注入依赖代码的分析，我们已经明白了 Spring loC容器是如何将属性的值注入到Bean 实例对象中去的:

1）、对于集合类型的属性，将其属性值解析为目标类型的集合后直接赋值给属性。

2）、对于非集合类型的属性，大量使用了 JDK的反射机制，通过属性的getter方法获取指定属性注入 以前的值，同时调用属性的setter。方法为属性设置注入后的值。看到这里相信很多人都明白了 Spring 的 setter注入原理。

至此Spring loC容器对Bean定义资源文件的定位，载入、解析和依赖注入已经全部分析完毕，现 在 Spring loC容器中管理了一系列靠依赖关系联系起来的Bean，程序不需要应用自己手动创建所需的 对象，Spring loC容器会在我们使用的时候自动为我们创建，并且为我们注入好相关的依赖，这就是 Spring核心功能的控制反转和依赖注入的相关功能。

### 9.loC容器中那些鲜为人知的细节

通过前面章节中对Spring loC容器的源码分析，我们已经基本上了解了 Spring loC容器对Bean 定义资源的定位、载入和注册过程,同时也清楚了当用户通过getBean()方法向loC容器获取被管理的 Bean时 ，loC容器对Bean进行的初始化和依赖注入过程,这些是Spring loC容器的基本功能特性。 Spring loC容器还有一些高级特性,如使用lazy-init属性对Bean预初始化、FactoryBean产生或者 修饰Bean对象的生成、loC容器初始化Bean过程中使用BeanPostProcessor后置处理器对Bean声 明周期事件管理等。

### 10.关于延时加载

通过前面我们对loC容器的实现和工作原理分析 我们已经知道loC容器的初始化过程就是对Bean 定义资源的定位、载入和注册，此时容器对Bean的依赖注入并没有发生,依赖注入主要是在应用程序 第一次向容器索取Bean时 ，通过getBean()方法的调用完成。

当 Bean定义资源的＜Bean＞元素中配置了 lazy-init=faIse属性时，容器将会在初始化的时候对所配置 的 Bean进行预实例化，Bean的依赖注入在容器初始化的时候就已经完成。这样，当应用程序第一次 向容器索取被管理的Bean时 ，就不用再初始化和对Bean进行依赖注入了，直接从容器中获取已经完 成依赖注入的现成Bean , 可以提高应用第一次向容器获取Bean的性能。

**refresh ()方法**

先从loC容器的初始化过程开始，我们知道loC容器读入已经定位的Bean定义资源是从refresh方法 开始的，我们首先从AbstractApplicationContext类的refresh。

在 refresh。方法中 ConfigurableListableBeanFactorybeanFactory=obtainFreshBeanFactory(); 启动了 Bean定义资源的载入、注册过程，而 finishBeanFactorylnitialization方法是对注册后的Bean 定义中的预实例化(lazy-init=false,Spring默认就是预实例化即为true)的 Bean进行处理的地方。

**finishBeanFactorylnitialization 处理预实例化 Bean**

当 Bean定义资源被载入loC容器之后，容器将Bean定义资源解析为容器内部的数据结构 BeanDefinition注册到容器中 AbstractApplicationContext类中的finishBeanFactorylnitialization方法对配置了预实例化属性的Bean进行预初始化过程

ConfigurableListableBeanFactory 是一个接口 , 其 prelnstantiateSingletons方法由其子类 DefaultListableBeanFactory 提供。

**DefaultListableBeanFactory 对配置 lazy-init 属性单态 Bean的预实例化**

通过对lazy-init处理源码的分析，我们可以看出，如果设置了 lazy-init属性，则容器在完成Bean 定义的注册之后，会通过getBean方法,触发对指定Bean的初始化和依赖注入过程，这样当应用第一 次向容器索取所需的Bean时 ，容器不再需要对Bean进行初始化和依赖注入，直接从已经完成实例化 和依赖注入的Bean中取一个现成的Bean ,这样就提高了第一次获取Bean的性能。

### 11.关于 FactoryBean 和 BeanFactory

在 Spring中 ,有两个很容易混淆的类：BeanFactory和 FactoryBean。 BeanFactory : Bean工厂 , 是一个工厂(Factory), 我 们 Spring loC容器的最顶层接口就是这个BeanFactory , 它的作用是管理Bean , 即实例化、定位、配置应用程序中的对象及建立这些对象间的 依赖。

Factory Bean : 工厂Bean , 是一个Bean , 作用是产生其他bean实例。通常情况下，这 种 Bean 没有什么特别的要求，仅需要提供一个工厂方法，该方法用来返回其他Bean实例。通常情况下，Bean 无须自己实现工厂模式，Spring容器担任工厂角色；但少数情况下,容器中的Bean本身就是工厂，其 作用是产生其它Bean实例。

当用户使用容器本身时可以使用转义字符来得到FactoryBean本身以区别通过FactoryBean 产生的实例对象和FactoryBean对象本身。在 BeanFactory中通过如下代码定义了该转义字符： String FACTORY BEAN PREFIX ="&";

如果 myJndiObject是一个 FactoryBean ,则使用 &myJndiObject 得到的是 myJndiObject 对 象 , 而不是myJndiObject产生出来的对象。

**FactoryBean 源 码 **

**AbstractBeanFactory 的 getBean()方法调用 FactoryBean **

在前面我们分析Spring loC容器实例化Bean并进行依赖注入过程的源码时，提到在getBean方法触发容器实例化Bean的时候会调用AbstractBeanFactory的 doGetBean()方法来进行实例化的过 程

在 上 面 获 取 给 定 Bean的 实 例 对 象 的 getObjectForBeanlnstance()方 法 中 ，会 调 用 FactoryBeanRegistrySupport 类的 getObjectFromFactoryBean()方 法 ,该方法实现了 Bean 工厂生 产 Bean实例对象。

Dereference(解引用):一个在C/C+ +中应用比较多的术语，在 C+ +中 ,” *“是解引用符号,而” & ”是引用符号，解引用是指变量指向的是所引用对象的本身数据，而不是引用对象的内存地址。

**AbstractBeanFactory 生产 Bean 实例对象**

BeanFactory接口调用其实现类的getObject方法来实现创 建 Bean实例对象的功能

**工厂Bean的实现类getObject方法创建Bean实例对象 **

Factory Bea n 的实现类有非常多，比 如 ：Proxy、RMI、JNDI、ServletContextFactoryBean 等等 FactoryBean接口为Spring容器提供了一个很好的封装机制，具体的getObject有不同的实现类根 据不同的实现策略来具体提供,我们分析一个最简单的AnnotationTestFactoryBean的实现源码

其他的Proxy , RMI , JNDI等 等 ,都是根据相应的策II略提供getObjectQ的实现。这里不做一一分 析 ,这已经不是Spring的核心功能，感兴趣的小伙可以再去深入研究。

### 12.再述 autowiring

Spring loC容器提供了两种管理Bean依赖关系的方式:

1)、显式管理:通过BeanDefinition的属性值和构造方法实现Bean依赖关系管理。

2)、autowiring :Spring loC容器的依赖自动装配功能，不需要对Bean属性的依赖关系做显式的声明，

只需要在配置好autowiring属性,loC容器会自动使用反射查找属性的类型和名称，然后基于属性 的类型或者名称来自动匹配容器中管理的Bean,从而自动地完成依赖注入。

通过对autowiring自动装配特性的理解，我们知道容器对Bean的自动装配发生在容器对Bean依 赖注入的过程中。在前面对Spring loC容器的依赖注入过程源码分析中，我们已经知道了容器对Bean 实例对象的属性注入的处理发生在AbstractAutoWireCapableBeanFactory类中的populateBean() 方法中，我们通过程序流程分析autowiring的实现原理:

**AbstractAutoWireCapableBeanFactory 对 Bean 实例进行属性依赖注入**

应用第一次通过getBean方法(配置了 lazy-init预实例化属性的除外)向loC容器索取Bean时 ， 容 器 创 建 Bean实 例 对 象 ，并 且 对 Bean实 例 对 象 进 行 属 性 依 赖 注 入 ， AbstractAutoWireCapableBeanFactory 的 populateBean()方法就是实现 Bean 属性依赖注入的功 能

**Spring loC容器根据Bean名称或者类型进行autowiring自动依赖注入**

通过属性名进行自动依赖注入的相对比通过属性类型进行自 动依赖注入要稍微简单一些，但是真正实现属性注入的是DefaultSingletonBeanRegistry类的 registerDependentBean方法。

**DefaultSingletonBeanRegistry 的 registerDependentBean()方法对属性注入**

通过对autowiring的源码分析，我们可以看出，autowiring的实现过程:

a、对 Bean的属性代调用getBean()方法,完成依赖Bean的初始化和依赖注入。

b、 将依赖Bean的属性引用设置到被依赖的Bean属性上。

c、将依赖Bean的名称和被依赖Bean的名称存储在loC容器的集合中。

Spring loC容器的autowiring属性自动依赖注入是一个很方便的特性，可以简化开发时的配置, 但是凡是都有两面性，自动属性依赖注入也有不足,首先，Bean的依赖关系在配置文件中无法很清楚 地看出来，对于维护造成一定困难。其次,由于自动依赖注入是Spring容器自动执行的,容器是不会 智能判断的，如果配置不当，将会带来无法预料的后果，所以自动依赖注入特性在使用时还是综合考虑。

@Autoware

容器对 Bean
实例对象的依赖属性注入发生在AbstractAutoWireCapableBeanFactory类
的populateBean（）  

真正实现属性注入的是 DefaultSingletonBeanRegistry 类的registerDependentBean（）方法  

## AOP源码

Spring AOP是由接入BeanPostProcessor后置处理器开始的  

### BeanPostProcessor  

这个Bean后置处理器是一个监听
器，可以监听容器触发的Bean声明周期事件。向容器注册后置处理器以后，容器中管理的Bean就具备了接收IoC容器回调事件的能力  

**2.AbstractAutowireCapabIeBeanFactory类的doCreateBean（）**方
法
BeanPostProcessor后置处理器的调用发生在Spring IoC容器完成Bean
实例对象的创建和属性的依赖注入之后，在对Spring依赖注入的源码分
析中我们知道，当应用程序第一次调用getBean（）方法（lazy-init预实
例化除外）向Spring IoC容器索取指定Bean时，触发Spring IoC容器创建
Bean实例对象并进行依赖注入。其实真正实现创建 Bean 对象并进行依
赖注入的方法是

**Bean 实例对象添加 BeanPostProcessor 后置处理器的入口是initializeBean（）  **方法

**3.initiaIizeBean（）方法**  

在AbstractAutowireCapableBeanFactory类中initializeBean（）方法实
现为容器创建的Bean实例对象添加BeanPostProcessor后置处理器  

4.一个创建AOP代理对象的子类AbstractAutoProxyCreator，该类重写了postProcessAfterInitialization（）  

一个创建AOP代理对象的子类AbstractAutoProxyCreator，该类重写了
postProcessAfterInitialization（） 

选择代理策略postProcessAfterInitialization（）方法，它调用了一个非常核心的方法—wrapIfNecessary（）  

5.整个过程最终调用的是proxyFactory.getProxy（）方法。到这里，
proxyFactory有JDK和CGLib两种，我们该如何选择呢？使用
DefaultAopProxyFactory的createAopProxy（）方法：  

### AOP - JDK 代理

JdkDynamicAopProxy    -> 

InvocationHandler 是 JDK 动态代理的核心，生成的代理对象的方法
调用都会委派到 invoke（）方法。  

主要实现思路为：先获取应用到此方法上的拦截器链（Interceptor
Chain）。如果有拦截器，则应用拦截器，并执行连接点（JoinPoint）；
如果没有拦截器，则直接反射执行连接点。这里的关键是拦截器链是如
何获取的，以及它又是如何执行的。下面来逐一分析。  

**拦截器链是通过Advised.getInterceptorsAndDynamicInterceptionAdvice（）  获得**

可以看到，获取拦截器其实是由AdvisorChainFactory的
**getInterceptorsAndDynamicInterception-Advice（）**方法  

Advised中配置的能够应用到连接点
（JoinPoint）或者目标对象（Target Object）的Advisor全部被转化成 MethodInterceptor

如果得到的拦截器链为空，则直接反射调用目标方法，否则创建MethodInvocation，调用其proceed（）方法，触发拦截器链的执行  

### 触发通知

在为AopProxy代理对象配置拦截器的实现中，有一个取得拦截器的
配置过程，这个过程是由DefaultAdvisorChainFactory 实现的。这个工厂
类负责生成拦截器链，在它的 **getInterceptorsAndDynamicInterceptionAdvice（）**方法中，有一个适配和注册过程，通过配置Spring 预先设计好的拦截器，加入了AOP实现  

GlobalAdvisorAdapterRegistry类起到了适配器和单例模式的作用，
提供了一个DefaultAdvisor-AdapterRegistry类来完成各种通知的适配和注册过程

Spring AOP为了实现Advice的织入，设计了特定的拦截器对这些功能进行封装。我们接着看**MethodBeforeAdviceInterceptor**类是如何完成封装的

对目标对象的增强是通过拦截器实现的  

## spring MVC源码

容器初始化时会建立所有URL和Controller中方法的对应关系，保存
到Handler Mapping中，用户请求时根据请求的 URL 快速定位到
Controller 中的某个方法。在 Spring 中先将 URL 和Controller的对应关系
保存到Map＜url， Controller＞中。 Web容器启动时会通知Spring初始化
容器（加载Bean的定义信息和初始化所有单例Bean），然后Spring
MVC会遍历容器中的Bean，获取每一个Controller中的所有方法访问的
URL，将URL和Controller保存到一个Map中  

### 九大组件

初始化

首先找到DispatcherServlet类，寻找init（）方法。我们发现init（）方法其实在父类HttpServletBean中 

-> initServletBean（）  

上面这段代码主要就是初始化IoC容器，最终会调用refresh（）方
法，前面的章节对IoC容器的初始化已经讲得很详细，在此不再赘述。
我们看到，在IoC容器初始化之后，又调用了onRefresh（）方法，它是
在DisptcherServlet类中实现的，来看源码：  

