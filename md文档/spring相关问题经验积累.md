spring相关问题经验积累

##### spring-boot-启动显示已加载的类

```
@Bean
public CommandLineRunner run(ApplicationContext applicationContext){
    return args -> {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).sorted().forEach(System.out::println);
    };
}
```

##### 通过名称反射调用加载的类

```
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext=applicationContext;
    }

    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

}
```

```
 // 普通接口不会注册为bean,通过类型找bean需开头小写，然后是具体的实现类
Object WorkflowRepository = springContextUtil.getBean("workflowServiceImpl");
// Feign接口会注册为bean，但是名称是全路径名
Object feign = springContextUtil.getBean("com.coolpad.basic.infrastructure.outconfig.gatewayimpl.AppConfigFeign");
Method method = ReflectionUtils.findMethod(feign.getClass(), "queryConfig", ConfigQryGo.class);
ConfigQryGo configQryGo = new ConfigQryGo();
// 方法,对象Bean,入参
Object singleResponse = ReflectionUtils.invokeMethod(method, feign, configQryGo);
```

