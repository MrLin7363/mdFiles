mybatis常见问题

#### mybatis中的#{}和${}区别

1.#将传入的数据都当成一个字符串，会对自动传入的数据加一个单引号。如 where id = #{id}   解析成sql为  where id = '2'

2.$将传入的数据直接显示生成在sql中，如  order by ${create_time}  解析为 order by create_time

3.#方式能够很大程度防止sql注入

4.$方式无法防止Sql注入

5.$方式一般用于传入数据库对象，例如传入表名

6.一般能用#的就别用$

7.动态排序时用 $ 

#### xml 模糊查询

```
<if test="formListQry.companyName!=null">
            and company_name like "%"#{formListQry.companyName}"%"
</if>
```

```
and company_name like '%${formListQry.companyName}%'
```

```
and company_name like concat('%',#{formListQry.companyName},'%')  
```

concat()函数

concat(str1, str2,...) 返回结果为连接参数产生的字符串，如果有任何一个参数为null，则返回值为null。

#### 二，缓存 

推荐阅读：https://blog.csdn.net/Lotus_dong/article/details/116334317

**为什么使用缓存?**
缓存（即cache）的作用是为了减去数据库的压力，提高数据库的性能。缓存实现的原理是从数据库中查询出来的对象在使用完后不销毁，而是存储在内存（缓存）中，当再次需要获取该对象时，直接从内存中获取，不再向数据库执行select语句，减少对数据库的查询次数，提高了数据库的性能。缓存是使用Map集合存储数据。

**MyBatis缓存**
MyBatis有一级缓存和二级缓存之分。

一级缓存的作用域是同一个SqlSession，在同一个SqlSession中两次执行相同的sql语句，第一次执行完毕会将数据库查询的数据写到缓存（内存），第二次会从缓存中获取数据而不进行数据库查询，大大提高了查询效率。当一个SqlSession结束后该SqlSession中的一级缓存也就不存在了。MyBtais默认启动以及缓存。

二级缓存是多个SqlSession共享的，其作用域是mapper的同一个namespace，不同的sqlSession两次执行相同namespace下的sql语句且向sql中传递的参数也相同时，第一次执行完毕会将数据库中查询到的数据写到缓存（内存），第二次会直接从缓存中获取，从而提高了查询效率。MyBatis默认不开启二级缓存，需要在MyBtais全局配置文件中进行setting配置开启二级缓存。


MyBatis的缓存默认设置如下：

1. **一级缓存是默认开启的**，不需要任何配置
2. **二级缓存默认不开启**，需要在MyBatis全局配置文件中进行设置

##### 1.开启二级缓存

```
<configuration>
    <settings>
        <setting name="logImpl" value="STDOUT_LOGGING"/>
        <!--这个配置使全局的映射器(二级缓存)启用或禁用缓存-->
        <setting name="cacheEnabled" value="true" />
    </settings>
</configuration>
```

第二步

```
接口开启方法:使用@CacheNamespace注解
//通过注解开启二级缓存的分开关
@CacheNamespace
public interface EmpMapper {
```

或者

```
在xml中开启方法:添加"cache"标签
<mapper namespace="com.ssm.mapper.EmpMapper">
    <!--xml中二级缓存分开关-->
    <cache></cache>
```

##### 2.是否开启

由于使用了数据库连接池，默认每次查询完之后自动commite，这就导致两次查询使用的不是同一个sqlSessioin，根据一级缓存的原理，**它将永远不会生效**。
当我们开启了事务，两次查询都在同一个sqlSession中，从而让第二次查询命中了一级缓存。读者可以自行关闭事务验证此结论。



mybatis默认的session级别一级缓存，由于springboot中默认使用了hikariCP，所以基本没用，需要开启事务才有用。但一级缓存作用域仅限同一sqlSession内，无法感知到其他sqlSession的增删改，所以极易产生脏数据
二级缓存可通过cache-ref让多个mapper.xml共享同一namespace，从而实现缓存共享，但多表联查时配置略微繁琐。
所以生产环境建议将一级缓存设置为statment级别（即关闭一级缓存），如果有必要，可以开启二级缓存

**如果应用是是分布式部署，由于二级缓存存储在本地，必然导致查询出脏数据，所以，分布式部署的应用不建议开启**

#### 三，拦截器

##### 1.  四种拦截器

拦截器是一种基于 AOP（面向切面编程）的技术，它可以在目标对象的方法执行前后插入自定义的逻辑。MyBatis 定义了四种类型的拦截器，分别是：

Executor：拦截**执行器**的方法，例如 update、query、commit、rollback 等。可以用来实现缓存、事务、分页等功能。

ParameterHandler：拦截**参数处理器**的方法，例如 setParameters 等。可以用来转换或加密参数等功能。

ResultSetHandler：拦截**结果集处理器**的方法，例如 handleResultSets、handleOutputParameters 等。可以用来转换或过滤结果集等功能。

StatementHandler：拦截**JDBC语句处理器**的方法，例如 prepare、parameterize、batch、update、query 等。可以用来修改 SQL 语句、添加参数、记录日志等功能。

**拦截的执行顺序是Executor->StatementHandler->ParameterHandler->ResultHandler** 

##### 2. 实现拦截器 

定义一个实现 `org.apache.ibatis.plugin.Interceptor `接口的拦截器类，并重写其中的 `intercept`、`plugin` 和 `setProperties` 方法。

```
public interface Interceptor {
    Object intercept(Invocation var1) throws Throwable;
    Object plugin(Object var1);
    void setProperties(Properties var1);
}
```

```
@Component
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DynamicSqlInterceptor implements Interceptor {
 ...
}
```

##### 3.  应用-切换特定字符

根据方法是否包含动态切换的注解标识，替换sql中包含的信息

**yml**

定 xml 文件中需要替换的占位符标识：@dynamicSql 以及待替换日期条件。

```
# 动态sql配置
dynamicSql:
  placeholder: "@dynamicSql"
  date: "2023-07-31"
```

**自定义注解**

```
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicSql {
}
```

**DAO层**

```
public interface DynamicSqlMapper  {
    @DynamicSql
    Long count();

    Long save();
}
```

**xml**

```
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.zysheep.mapper.DynamicSqlMapper">
    <select id="count" resultType="java.lang.Long">
        select count(1) from t_order_1 where create_time > @dynamicSql
    </select>
</mapper>
```

拦截器核心代码

```
@Component
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class,
                method = "prepare", args = {Connection.class, Integer.class})
})
public class DynamicSqlInterceptor implements Interceptor {

    @Value("${dynamicSql.placeholder}")
    private String placeholder;

    @Value("${dynamicSql.date}")
    private  String dynamicDate;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 获取 StatementHandler 对象也就是执行语句
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 2. MetaObject 是 MyBatis 提供的一个反射帮助类，可以优雅访问对象的属性，这里是对 statementHandler 对象进行反射处理，
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        // 3. 通过 metaObject 反射获取 statementHandler 对象的成员变量 mappedStatement
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        // mappedStatement 对象的 id 方法返回执行的 mapper 方法的全路径名，如cn.zysheep.mapper.DynamicSqlMapper.count
        String id = mappedStatement.getId();
        // 4. 通过 id 获取到 Dao 层类的全限定名称，然后反射获取 Class 对象
        Class<?> classType = Class.forName(id.substring(0, id.lastIndexOf(".")));
        // 5. 获取包含原始 sql 语句的 BoundSql 对象
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        log.info("替换前---sql：{}", sql);
        // 拦截方法
        String mSql = null;
        // 6. 遍历 Dao 层类的方法
        for (Method method : classType.getMethods()) {
            // 7. 判断方法上是否有 DynamicSql 注解，有的话，就认为需要进行 sql 替换
            if (method.isAnnotationPresent(DynamicSql.class)) {
                mSql = sql.replaceAll(placeholder, String.format("'%s'", dynamicDate));
                break;
            }
        }
        if (StringUtils.isNotBlank(mSql)) {
            log.info("替换后---mSql：{}", mSql);
            // 8. 对 BoundSql 对象通过反射修改 SQL 语句。
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, mSql);
        }
        // 9. 执行修改后的 SQL 语句。
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        // 使用 Plugin.wrap 方法生成代理对象
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 获取配置文件中的属性值
    }
}
```

```
@SpringBootTest(classes = DmApplication.class)
public class DynamicTest {

    @Autowired
    private DynamicSqlMapper dynamicSqlMapper;

    @Test
    public void test() {
        Long count = dynamicSqlMapper.count();
        Assert.notNull(count, "count不能为null");
    }
}
```

##### 4. 应用-拦截参数

```
 
@Component
@Intercepts({@Signature(type = ParameterHandler.class,
                        method = "setParameters",
                        args = {PreparedStatement.class})})
public class ParameterPlugin implements Interceptor {
 
        ParameterHandler  parameterHandler = (ParameterHandler) invocation.getTarget();
        Object parameterObject = parameterHandler.getParameterObject();
        //第一种，性能高
        // if(parameterObject instanceof BaseModel){
        //  BaseModel baseModel = (BaseModel) parameterObject;
        //  baseModel.setLastUpdateBy(LocalUserUtil.getLocalUser().getNickName());
        // }
        //第二种使用反射处理，扒光撕开
        Field lastUpdateBy = ReflectUtil.getField(parameterObject.getClass(), "lastUpdateBy");
        if (lastUpdateBy != null) {
            ReflectUtil.setFieldValue(parameterObject,lastUpdateBy,LocalUserUtil.getLocalUser().getNickName());
        }
        return invocation.proceed();
 
}
```



##### 7. 应用场景

1、SQL 语句执行监控：可以拦截执行的 SQL 方法，打印执行的 SQL 语句、参数等信息，并且还能够记录执行的总耗时，可供后期的 SQL 分析时使用。
2、SQL 分页查询：MyBatis 中使用的 RowBounds 使用的内存分页，在分页前会查询所有符合条件的数据，在数据量大的情况下性能较差。通过拦截器，可以在查询前修改 SQL 语句，提前加上需要的分页参数。
3、公共字段的赋值：在数据库中通常会有 createTime ， updateTime 等公共字段，这类字段可以通过拦截统一对参数进行的赋值，从而省去手工通过 set 方法赋值的繁琐过程。
4、数据权限过滤：在很多系统中，不同的用户可能拥有不同的数据访问权限，例如在多租户的系统中，要做到租户间的数据隔离，每个租户只能访问到自己的数据，通过拦截器改写 SQL 语句及参数，能够实现对数据的自动过滤。
5、SQL 语句替换：对 SQL 中条件或者特殊字符进行逻辑替换。(也是本文的应用场景)

6. 对某些字段过滤，如订单的某些信息过滤









### 四, mybatis多数据源

#### 1.mybatis-Configuration形式

参考文章：  https://www.cnblogs.com/SweetCode/p/15591792.html

##### 连接配置

既然有多个数据源，因为数据库用户名密码可能不相同，所以是需要配置多个数据源信息的，直接在 `properties/yml` 中配置即可。这里要注意根据配置的属性名进行区分，同时因为数据源要有一个默认使用的数据源，最好在名称上有所区分（这里使用 **primary** 作为主数据源标识）。

```
########################## 主数据源 ##################################
spring.datasource.primary.jdbc-url=jdbc:mysql://127.0.0.1:3306/demo1?characterEncoding=utf-8&serverTimezone=GMT%2B8
spring.datasource.primary.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.primary.username=root
spring.datasource.primary.password=

########################## 第二个数据源 ###############################
spring.datasource.datasource2.jdbc-url=jdbc:mysql://127.0.0.1:3306/demo2?characterEncoding=utf-8&serverTimezone=GMT%2B8
spring.datasource.datasource2.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.datasource2.username=root
spring.datasource.datasource2.password=

# mybatis  这里需要扫描全部
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.wdbyte.domain
```

注意，配置中的数据源连接 url 末尾使用的是 `jdbc-url`.

因为使用了 Mybatis 框架，所以 Mybatis 框架的配置信息也是少不了的，指定扫描目录 `mapper` 下的`mapper xml` 配置文件。

##### 多数据源配置

上面你应该看到了，到目前为止和 Mybatis 单数据源写法唯一的区别就是 Mapper 接口使用不同的目录分开了，那么这个不同点一定会在数据源配置中体现。

##### 主数据源

开始配置两个数据源信息，先配置主数据源，配置扫描的 `MapperScan` 目录为 `com.wdbyte.mapper.primary`

```
@Configuration
@MapperScan(basePackages = {"com.wdbyte.mapper.primary"}, sqlSessionFactoryRef = "sqlSessionFactory")
public class PrimaryDataSourceConfig {

    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // xml文件可以添加文件夹路径加以区分
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

和单数据源不同的是这里把

- `dataSource`
- `sqlSessionFactory`
- `transactionManager`
- `sqlSessionTemplate`

都单独进行了配置，简单的 bean 创建，下面是用到的一些注解说明。

- `@ConfigurationProperties(prefix = "spring.datasource.primary")`：使用spring.datasource.primary 开头的配置。
- `@Primary` ：声明这是一个主数据源（默认数据源），多数据源配置时**必不可少**。
- `@Qualifier`：显式选择传入的 Bean。

##### 第二个数据源

第二个数据源和主数据源唯一不同的只是 `MapperScan` 扫描路径和创建的 Bean 名称，同时没有 `@Primary` 主数据源的注解。

```
@Configuration
@MapperScan(basePackages = {"com.wdbyte.mapper.datasource2"}, sqlSessionFactoryRef = "sqlSessionFactory2")
public class SecondDataSourceConfig {

    @Bean(name = "dataSource2")
    @ConfigurationProperties(prefix = "spring.datasource.datasource2")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sqlSessionFactory2")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource2") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "transactionManager2")
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource2") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate2")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory2") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

注意：因为已经在两个数据源中分别配置了扫描的 Mapper 路径，如果你之前在 SpringBoot 启动类中也使用了 Mapper 扫描注解，**需要删掉**。

#### 2.mybatis-plus@DS注解形式

##### 导入依赖

```
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.2</version>
</dependency>
```

##### yaml 配置不同的数据源

```
spring:
  datasource:
    dynamic:
      primary: master #设置默认的数据源或者数据源组,默认值即为master
      strict: false #严格匹配数据源,默认false. true未匹配到指定数据源时抛异常,false使用默认数据源
      datasource:
        master:
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver # 3.2.0开始支持SPI可省略此配置
        slave_1:
          url: jdbc:mysql://xx.xx.xx.xx:3307/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
        slave_2:
          url: ENC(xxxxx) # 内置加密,使用请查看详细文档
          username: ENC(xxxxx)
          password: ENC(xxxxx)
          driver-class-name: com.mysql.jdbc.Driver
       #......省略
       #以上会配置一个默认库master，一个组slave下有两个子库slave_1,slave_2
```

注：多数据源配置规范

```
# 多主多从                      纯粹多库（记得设置primary）                   混合配置
spring:                               spring:                               spring:
  datasource:                           datasource:                           datasource:
    dynamic:                              dynamic:                              dynamic:
      datasource:                           datasource:                           datasource:
        master_1:                             mysql:                                master:
        master_2:                             oracle:                               slave_1:
        slave_1:                              sqlserver:                            slave_2:
        slave_2:                              postgresql:                           oracle_1:
        slave_3:                              h2:                                   oracle_2:
```

##### 使用 **@DS** 切换数据源

**@DS** 可以注解在方法上或类上，**同时存在就近原则 方法上注解 优先于 类上注解**。

```
@Service
@DS("slave")
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List selectAll() {
    return  jdbcTemplate.queryForList("select * from user");
  }
  
  @Override
  @DS("slave_1")
  public List selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }
}
```

#### 3.Spring AOP+目录结构+切数据源

cool项目中用的
