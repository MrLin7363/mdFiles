**SpringBoot 源码启动解析**

### 启动源码分析

#### SpringApplicaiton对象

```
SpringApplication.run(Application.class, args);
```

```
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        this.sources = new LinkedHashSet();
        this.bannerMode = Mode.CONSOLE;
        this.logStartupInfo = true;
        this.addCommandLineProperties = true;
        this.addConversionService = true;
        this.headless = true;
        this.registerShutdownHook = true;
        this.additionalProfiles = new HashSet();
        this.isCustomEnvironment = false;
        this.resourceLoader = resourceLoader;
        Assert.notNull(primarySources, "PrimarySources must not be null");
        
        this.primarySources = new LinkedHashSet(Arrays.asList(primarySources)); //把主启动类放到一个hahset中，因为后面有很多地方需要用到
        
        this.webApplicationType = WebApplicationType.deduceFromClasspath();//设置应用程序的类型，这里应用程序的类型总的会有三种
        this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));//设置初始化器
        this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
        this.mainApplicationClass = this.deduceMainApplicationClass();
    }
```

#### deduceFromClasspath-设置应用程序的类型

设置应用程序的类型，这里应用程序的类型总的会有三种

```
if (ClassUtils.isPresent("org.springframework.web.reactive.DispatcherHandler", (ClassLoader)null) && !ClassUtils.isPresent("org.springframework.web.servlet.DispatcherServlet", (ClassLoader)null) && !ClassUtils.isPresent("org.glassfish.jersey.servlet.ServletContainer", (ClassLoader)null)) {
```

#### this.setInitializers-设置初始化器

```
this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
```

```
Set<String> names = new LinkedHashSet(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
```

```
return (List)loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
```

```
private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
    MultiValueMap<String, String> result = (MultiValueMap)cache.get(classLoader);
    if (result != null) {
        return result;
    } else {
        try {
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
            LinkedMultiValueMap result = new LinkedMultiValueMap();

            while(urls.hasMoreElements()) {
                URL url = (URL)urls.nextElement();
                UrlResource resource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                Iterator var6 = properties.entrySet().iterator();

                while(var6.hasNext()) {
                    Entry<?, ?> entry = (Entry)var6.next();
                    String factoryClassName = ((String)entry.getKey()).trim();
                    String[] var9 = StringUtils.commaDelimitedListToStringArray((String)entry.getValue());
                    int var10 = var9.length;

                    for(int var11 = 0; var11 < var10; ++var11) {
                        String factoryName = var9[var11];
                        result.add(factoryClassName, factoryName.trim());
                    }
                }
            }

            cache.put(classLoader, result);
            return result; // 这步读取spring的配置文件
        } catch (IOException var13) {
            throw new IllegalArgumentException("Unable to load factories from location [META-INF/spring.factories]", var13);
        }
    }
}
```

总结下来就是去读取了spring.factories 配置的监听器和初始化器，初始化器一共7个，监听器11个，注意：这里这是取出了类的完全限定名(包名+类名)，并不是对象，可以方便以后通过反射获取到对象。

```
private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args, Set<String> names) {
    List<T> instances = new ArrayList(names.size());
    Iterator var7 = names.iterator();

    while(var7.hasNext()) {
        String name = (String)var7.next();

        try {
            Class<?> instanceClass = ClassUtils.forName(name, classLoader);
            Assert.isAssignable(type, instanceClass);
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
            
            T instance = BeanUtils.instantiateClass(constructor, args); //到这里就已经把全限类名实例化了，这里有一个点需要注意，如果你的项目里面也有spring.factories 文件，那项目启动的时候也会去读你的配置文件
            
            instances.add(instance);
        } catch (Throwable var12) {
            throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, var12);
        }
    }
    return instances;
}
```

#### deduceMainApplicationClass

判断当前是否一个web应用，从多个配置类中找到有main方法的主配置类，然后进行启动

```
private Class<?> deduceMainApplicationClass() {
    try {
        StackTraceElement[] stackTrace = (new RuntimeException()).getStackTrace();
        StackTraceElement[] var2 = stackTrace;
        int var3 = stackTrace.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StackTraceElement stackTraceElement = var2[var4];
            if ("main".equals(stackTraceElement.getMethodName())) {
                return Class.forName(stackTraceElement.getClassName()); 
            }
        }
    } catch (ClassNotFoundException var6) {
    }

    return null;
}
```

至此完成

#### SpringApplicaiton初始化完成

1.计时操作

2.启动监听器，监听自己负责的事件，发布了一个事件，所有监听器判断是否自己需要处理，需要就处理自己对应的逻辑

```
public ConfigurableApplicationContext run(String... args) {
    StopWatch stopWatch = new StopWatch();
    
    stopWatch.start(); // 开始计时
    ConfigurableApplicationContext context = null;
    Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList();
    this.configureHeadlessProperty();
    SpringApplicationRunListeners listeners = this.getRunListeners(args);
    listeners.starting(); // 刚才对象里面有一堆监听器，当调用这个方法时，判断执行的东西是否与监听器有关，有关就执行

    Collection exceptionReporters;
    try {
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        // 将传进来的参数进行封装，并开始准备环境
        // 读取所有的环境变量
        ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
        
        
        this.configureIgnoreBeanInfo(environment);
        // 打印上下文
        Banner printedBanner = this.printBanner(environment);
        
        // 创建上下文
        context = this.createApplication();
        
        exceptionReporters = this.getSpringFactoriesInstances(SpringBootExceptionReporter.class, new Class[]{ConfigurableApplicationContext.class}, context);
        
        // 开始处理上下文对象
        this.prepareContext(context, environment, listeners, applicationArguments, printedBanner);
        
        this.refreshContext(context);
        this.afterRefresh(context, applicationArguments);
        stopWatch.stop(); // 计时结束
        
        
        if (this.logStartupInfo) {
            (new StartupInfoLogger(this.mainApplicationClass)).logStarted(this.getApplicationLog(), stopWatch);
        }

        listeners.started(context);
        this.callRunners(context, applicationArguments);
    } catch (Throwable var10) {
        this.handleRunFailure(context, var10, exceptionReporters, listeners);
        throw new IllegalStateException(var10);
    }

    try {
        listeners.running(context);
        // 读取所有的环境变量
        return context;
    } catch (Throwable var9) {
        this.handleRunFailure(context, var9, exceptionReporters, (SpringApplicationRunListeners)null);
        throw new IllegalStateException(var9);
    }
}
```

3.**读取所有的环境变量**

configurationProperties

servletContextInitParams

systemProperties

systemEnvironment

random

applicationConfig: [classpath:/application.yml]

4.**忽略环境因素**

   this.configureIgnoreBeanInfo(environment)

```
private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
    if (System.getProperty("spring.beaninfo.ignore") == null) {
    // 如果配置了这个就忽略对应的环境属性
        Boolean ignore = (Boolean)environment.getProperty("spring.beaninfo.ignore", Boolean.class, Boolean.TRUE);
        System.setProperty("spring.beaninfo.ignore", ignore.toString());
    }
}
```

5.**打印**banner

Banner printedBanner = printBanner(environment);

> 扩展：**什么叫上下文？**
>
> 某个作用域里面所需的一些属性，信息，叫做上下文。就是为了获取某个作用域的一些对象

6.**创建上下文**：**首先判断需要创建什么类型的上下文**

```
protected ConfigurableApplicationContext createApplicationContext() {
    Class<?> contextClass = this.applicationContextClass;
    if (contextClass == null) {
        try {
            switch(this.webApplicationType) {
            case SERVLET:
                contextClass = Class.forName("org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext");
                break;
            case REACTIVE:
                contextClass = Class.forName("org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext");
                break;
            default:
                contextClass = Class.forName("org.springframework.context.annotation.AnnotationConfigApplicationContext");
            }
        } catch (ClassNotFoundException var3) {
            throw new IllegalStateException("Unable create a default ApplicationContext, please specify an ApplicationContextClass", var3);
        }
    }
    return (ConfigurableApplicationContext)BeanUtils.instantiateClass(contextClass);
}
```

获取到对应上下文环境的属性值、也可以往上下文中设置属性和参数

**开始处理上下文对象****

postProcessApplicationContext获取到bean工厂，设置conversionService——还是进行参数设置的

什么叫conversionService？

类型转换服务，比如我们写了一个abc=123，他是如何帮我们转换成一个整数的，就是这个conversionService干的事。

```
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment, SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
    context.setEnvironment(environment);
    
    // 获取到bean工厂
    this.postProcessApplicationContext(context);
    
    this.applyInitializers(context);
    
    // 发送上下文初始化完成事件
    listeners.contextPrepared(context);
    if (this.logStartupInfo) {
        this.logStartupInfo(context.getParent() == null);
        this.logStartupProfileInfo(context);
    }

    // 创建DefaultListableBeanFactory bean工厂
    ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
    
    beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
    if (printedBanner != null) {
        beanFactory.registerSingleton("springBootBanner", printedBanner);
    }

    if (beanFactory instanceof DefaultListableBeansFactory) {
        ((DefaultListableBeanFactory)beanFactory).setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
    }

    Set<Object> sources = this.getAllSources();
    Assert.notEmpty(sources, "Sources must not be empty");
    
    // load扫描加载@controller @component这些注解的类
    this.load(context, sources.toArray(new Object[0]));
    listeners.contextLoaded(context);
}
```

prepareContext就结束了，就是为了向上下文对象中设置一系列的属性

## 自动配置原理@SpringBootApplication注解



