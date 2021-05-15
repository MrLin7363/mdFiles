spring容器实现

#### IOC

##### BeanFactory

三个重要子类

ListableBeanFactory接口表示这些Bean可列表化，而 HierarchicalBeanFactory表示这些Bean 是有继承关系的，也就是每个 Bean 可能有父 Bean。AutowireCapableBeanFactory 接口定义Bean的自动 装配规则

##### 高级的IoC容器

ApplicationContext

##### Bean对象及其相互关系

BeanDefinition

DispatcherServlet 无 init 方法，在其父类HttpServletBean中有

initServletBean

```
FrameworkServlet.configureAndRefreshWebApplicationContext
```

```.refresh()
ConfigurableApplicationContext.refresh()
```

```.
AbstractApplicationContext.refresh()  ->   this.onRefresh();
```

DispatcherServlet .initStrategies（）方法初始化Spring MVC的九大组

##### IOC启动

ClassPathXmlApplicationContext通过调用其父类 AbstractApplicationContext的refresh（）

refresh（）方法的主要作用是：在创建IoC容器前，如果已经有容 器存在，需要把已有的容器销毁和关闭，以保证在refresh（）方法之后 使用的是新创建的IoC容器。它类似于对IoC容器的重启，在新创建的容 器中对容器进行初始化，对Bean配置资源进行载入。

##### 基于注解的IOC容器

AnnotationConfigApplicationContext

**Spring对注解的处理分为以下两种方 式** （1）直接将注解Bean注册到容器中：可以在初始化容器时注册； 也可以在容器创建之后手动调用注册方法向容器注册，然后通过手动刷 新容器使容器对注册的注解Bean进行处理。 （2）通过扫描指定的包及其子包下的所有类处理：在初始化注解 容器时指定要自动扫描的路径，如果容器创建以后向给定路径动态添加 了注解Bean，则需要手动调用容器扫描的方法手动刷新容器，使容器对 所注册的注解Bean进行处理。

AnnotatedBeanDefinitionReader的register（）方法向容器注册指定的 注解Bean   doRegisterBean是真正的方法



#### DI自动装配之依赖注入

在BeanFactory中我们可以看到getBean（String...）方法，但它的具 体实现在AbstractBeanFactory中

**AbstractBeanFactory的getBean   doGetBean才是真正实现向IOC容器获取bean的功能，也就是依赖注入的时候**

**BeanFactory是管理Bean的对象，FactoryBean是创建对象的工厂Bean**

BeanFactory：Bean工厂，是一个工厂（Factory），Spring IoC容器 的最高层接口就是BeanFactory，它的作用是管理Bean，即实例化、定 位、配置应用程序中的对象及建立这些对象之间的依赖。 FactoryBean：工厂Bean，是一个Bean，作用是产生其他Bean实 例。这种Bean没有什么特别的要求，仅需要提供一个工厂方法，该方法 用来返回其他 Bean 实例。在通常情况下，Bean 无须自己实现工厂模 式，Spring容器担任工厂的角色；在少数情况下，容器中的Bean本身就 是工厂，其作用是产生其他Bean实例。



通过向IoC容器获取Bean的方法的分析，我们可以看到，在Spring 中如果Bean定义为单例模式（Singleton）的，则容器在创建之前先从缓 存中查找，以确保整个容器中只存在一个实例对象。如果Bean定义为原 型模式（Prototype）的，则容器每次都会创建一个新的实例对象。除此 之外，Bean定义还可以指定其生命周期范围

上面的源码只定义了根据 Bean 定义的不同模式采取的创建 Bean 实 例对象的不同策略，具体的Bean 实例对象的创建过程由实现了 ObjectFactory 接口的匿名内部类的 createBean（）方法完成， ObjectFactory 接口使 用 委 派 模 式，具体的 Bean 实例创建过程交由其 实现类AbstractAutowireCapableBeanFactory完成。下面我们继续分析 **AbstractAutowireCapableBeanFactory的createBean**方法的源码，理解 创建Bean实例的具体过程。

（1）createBeanInstance（）方法，生成Bean所包含的Java对象实 例。



 （2）populateBean（）方法，对Bean属性的依赖注入进行处理。