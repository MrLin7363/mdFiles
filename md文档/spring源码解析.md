spring源码解析

#### IOC

configAndRefreshWebApplicationContext（）方法中调用了 refresh（）方 法，这是真正启动 IoC 容器的入口

AbstractApplicationContext  refresh() 方法启动容器

```
public void refresh() throws BeansException, IllegalStateException {
    synchronized(this.startupShutdownMonitor) {
    	// 1.调用容器准备刷新的方法，获取容器当前时间，给容器设置同步标识
        this.prepareRefresh();
        // 2.告诉子类启动refreshBeanFactory()方法，Bean定义资源文件的载入从子类的refreshBeanFactory()方法启动
        ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
        // 3.为BeanFactory 配置容器特性，例如类加载器，事件处理器
        this.prepareBeanFactory(beanFactory);

        try {
        	// 4.为容器某些子类指定特殊的POST时间处理器
            this.postProcessBeanFactory(beanFactory);
            // 5.调用所在注册的BeanFactoryPostProcessor的Bean
            this.invokeBeanFactoryPostProcessors(beanFactory);
            // 6.为u=
            this.registerBeanPostProcessors(beanFactory);
            // 7.初始化国际化信息源
            this.initMessageSource();
            // 8.初始化容器事件传播器
            this.initApplicationEventMulticaster();
            // 9.调用子类某些特殊Bean的初始化方法
            this.onRefresh();
            // 10.为事件床勃起注册时间监听器
            this.registerListeners();
            // 11.初始化所有剩余的单例Bean
            this.finishBeanFactoryInitialization(beanFactory);
            // 12. 初始化容器的
            this.finishRefresh();
        } catch (BeansException var9) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
            }

            this.destroyBeans();
            this.cancelRefresh(var9);
            throw var9;
        } finally {
            this.resetCommonCaches();
        }

    }
}
```