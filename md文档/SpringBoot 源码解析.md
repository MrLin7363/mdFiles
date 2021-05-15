**SpringBoot 源码启动解析**

### 启动源码分析

#### SpringApplicaiton对象

```
SpringApplication.run(Application.class, args);
```

```
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null");
		// 把SpringDemoApplication作为primarySources属性存储起来
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
		// 从classpath中推断是否为web应用
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
		// 获取启动加载器
		this.bootstrappers = new ArrayList<>(getSpringFactoriesInstances(Bootstrapper.class));
		// 设置初始化器（Initializer），最后会调用这些功能
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		// 设置监听器（Listener）
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		// 获取main方法所在的类
		this.mainApplicationClass = deduceMainApplicationClass();
	}
```

基本就是做如下几件事情：

1. 配置primarySources
2. 配置环境是否为web环境
3. 创建初始化构造器setInitializers
4. 创建应用监听器
5. 配置应用主方法所在类（就是main方法所在类）

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
	/**
	 * 运行spring应用程序，创建并刷新一个新的 {@link ApplicationContext}.
	 *
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext}
	 */
	public ConfigurableApplicationContext run(String... args) {
		// 计时工具
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// 创建启动上下文对象
		DefaultBootstrapContext bootstrapContext = createBootstrapContext();
		ConfigurableApplicationContext context = null;
		configureHeadlessProperty();
		// 第一步：获取并启动监听器
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting(bootstrapContext, this.mainApplicationClass);
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
			// 第二步：准备环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
			// 忽略环境因素
			configureIgnoreBeanInfo(environment);
			// 第三步：打印banner，就是启动的时候在console的spring图案
			Banner printedBanner = printBanner(environment);
			// 第四步：创建spring容器
			context = createApplicationContext();
			context.setApplicationStartup(this.applicationStartup);
			// 第五步：spring容器前置处理
			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
			// 第六步：刷新容器
			refreshContext(context);
			// 第七步：spring容器后置处理
			afterRefresh(context, applicationArguments);
			stopWatch.stop(); // 结束计时器并打印，这就是我们启动后console的显示的时间
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			// 发出启动结束事件
			listeners.started(context);
			// 执行runner的run方法
			callRunners(context, applicationArguments);
		} catch (Throwable ex) {
			// 异常处理，如果run过程发生异常
			handleRunFailure(context, ex, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		} catch (Throwable ex) {
			// 异常处理，如果run过程发生异常
			handleRunFailure(context, ex, null);
			throw new IllegalStateException(ex);
		}
		// 返回最终构建的容器对象
		return context;
	}
```

3.**读取所有的环境变量**

configurationProperties

servletContextInitParams

systemProperties

systemEnvironment

random

applicationConfig: [classpath:/application.yml]

##### 获取并启动监听器

public interface SpringApplicationRunListener {

	/**
	 * 当调用run方法后会立即调用，可以用于非常早期的初始化
	 */
	default void starting(ConfigurableBootstrapContext bootstrapContext) {
		starting();
	}
	
	/**
	 * 环境准备好之后调用
	 */
	default void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
									 ConfigurableEnvironment environment) {
		environmentPrepared(environment);
	}
	
	/**
	 * 在加载资源之前，ApplicationContex准备好之后调用
	 */
	default void contextPrepared(ConfigurableApplicationContext context) {
	}
	
	/**
	 * 在加载应用程序上下文但在其刷新之前调用
	 */
	default void contextLoaded(ConfigurableApplicationContext context) {
	}
	
	/**
	 * 上下文已经刷新且应用程序已启动且所有{@link CommandLineRunner commandLineRunner}
	 * 和{@link ApplicationRunner ApplicationRunners}未调用之前调用
	 */
	default void started(ConfigurableApplicationContext context) {
	}
	
	/**
	 * 当应用程序上下文被刷新并且所有{@link CommandLineRunner commandLineRunner}
	 * 和{@link ApplicationRunner ApplicationRunners}都已被调用时，在run方法结束之前立即调用。
	 */
	default void running(ConfigurableApplicationContext context) {
	}
	
	/**
	 * 在启动过程发生失败时调用
	 */
	default void failed(ConfigurableApplicationContext context, Throwable exception) {
	}
##### 准备环境

	/**
	 * 创建并配置SpringBooty应用j将要使用的Environment
	 *
	 * @param listeners
	 * @param bootstrapContext
	 * @param applicationArguments
	 * @return
	 */
	private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
													   DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
		// 根据不同的web类型创建不同实现的Environment对象
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		// 配置环境
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		ConfigurationPropertySources.attach(environment);
		// 发送环境已准备完成事件
		listeners.environmentPrepared(bootstrapContext, environment);
		DefaultPropertiesPropertySource.moveToEnd(environment);
		// 根据命令行参数中spring.profiles.active属性配置Environment对象中的activeProfile（比如dev、prod、test）
		configureAdditionalProfiles(environment);
		// 绑定环境中spring.main属性绑定到SpringApplication对象中
		bindToSpringApplication(environment);
		// 如果用户使用spring.main.web-application-type属性手动设置了webApplicationType
		if (!this.isCustomEnvironment) {
			// 将环境对象转换成用户设置的webApplicationType相关类型，他们是继承同一个父类，直接强转
			environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
					deduceEnvironmentClass());
		}
		ConfigurationPropertySources.attach(environment);
		return environment;
	}
这里主要有如下过程：

1. 创建配置环境 ConfigurableEnvironment
2. 加载属性文件资源
3. 配置监听

##### **忽略环境因素**

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

##### 打印banner

spring启动的图案

##### 创建spring容器

```
/**
	 * Spring容器准备
	 */
	private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
								ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
								ApplicationArguments applicationArguments, Banner printedBanner) {
		// 设置上下文环境  
		context.setEnvironment(environment);
		// 获取到bean工厂
		postProcessApplicationContext(context);
		// 执行所有ApplicationContextInitializer对象的initialize方法（这些对象是通过读取spring.factories加载）
		applyInitializers(context);
		// 发布上下文准备完成事件到所有监听器
		listeners.contextPrepared(context);
		bootstrapContext.close(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		 // 创建DefaultListableBeanFactory bean工厂
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		// 加载bean到上下文    // load扫描加载@controller @component这些注解的类
		load(context, sources.toArray(new Object[0]));
		// 发送上下文加载完成事件
		listeners.contextLoaded(context);
	}
```

##### 刷新容器【关键】

		/**
		 * 刷新应用程序上下文
		 *
		 * @param context
		 */
		private void refreshContext(ConfigurableApplicationContext context) {
			// 注册一个关闭钩子，在jvm停止时会触发，然后退出时执行一定的退出逻辑
			if (this.registerShutdownHook) {
				try {
					// 添加：Runtime.getRuntime().addShutdownHook()
					// 移除：Runtime.getRuntime().removeShutdownHook(this.shutdownHook)
					context.registerShutdownHook();
				} catch (AccessControlException ex) {
					// Not allowed in some environments.
				}
			}
			// ApplicationContext真正开始初始化容器和创建bean的阶段
			refresh((ApplicationContext) context);
		}
调用应用上下文对象的refresh()方法，接下来我i门到ConfigurableApplicationContext类中去看下这个方法

```
 * Refresh the underlying {@link ApplicationContext}.
 * @param applicationContext the application context to refresh
 */
protected void refresh(ApplicationContext applicationContext) {
   Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
   ((AbstractApplicationContext) applicationContext).refresh();
}

```

```
protected void refresh(ApplicationContext applicationContext) {
		Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
		((AbstractApplicationContext) applicationContext).refresh();
	}
```

 AbstractApplicationContext 是一个抽象类，其余两个类都继承了它，我们来看看这个抽象类的代码

	@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");
	
			// 第一步：准备更新上下时的预备工作
			prepareRefresh();
	
			// 第二步：获取上下文内部BeanFactory
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
	
			// 第三步：对BeanFactory做预备工作
			prepareBeanFactory(beanFactory);
	
			try {
				// 第四步：允许在上下文子类中对bean工厂进行post-processing
				postProcessBeanFactory(beanFactory);
	
				StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
				// 第五步：调用上下文中注册为bean的工厂 BeanFactoryPostProcessor
				invokeBeanFactoryPostProcessors(beanFactory);
	
				// 第六步：注册拦截bean创建的拦截器
				registerBeanPostProcessors(beanFactory);
				beanPostProcess.end();
	
				// 第七步：初始化MessageSource(国际化相关)
				initMessageSource();
	
				// 第八步：初始化容器事件广播器(用来发布事件)
				initApplicationEventMulticaster();
	
				// 第九步：初始化一些特殊的bean
				onRefresh();
	
				// 第十步：将所有监听器注册到前两步创建的事件广播器中
				registerListeners();
	
				// 第十一步：结束bean的初始化工作（主要将所有单例BeanDefinition实例化）
				finishBeanFactoryInitialization(beanFactory);
	
				// 第十二步：afterRefresh（上下文刷新完毕，发布相应事件）
				finishRefresh();
			} catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}
	
				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();
	
				// Reset 'active' flag.
				cancelRefresh(ex);
	
				// Propagate exception to caller.
				throw ex;
			} finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
				contextRefresh.end();
			}
		}
	}
其中，我们这里是web应用，所以实现类是ServletWebServerApplicationContext，我们看下这个类refresh()的代码：

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

