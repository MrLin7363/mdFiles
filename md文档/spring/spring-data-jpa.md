spring-data-jpa

官方文档  https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods

#### sql相关

##### 1. **like**

```****
and proxy_uri like concat(:proxyUri,'%')
```

##### 2. **in list**

```
and a.access_level in :accessLevel

@Param("accessLevel") List accessLevel
```

##### 3. 自定义sql 

```
List<Map<String, Object>> getSystemStatistic(long ago10minute, long ago1hour, long ago12hour);
```

查到后再代码里转

```
List<Map<String, Object>> res = repository.getSystem(ago10minute, ago1hour, ago12hour);
List<ResVo> res1 = JSON.parseObject(JSON.toJSONString(res),
new TypeReference<List<ResVo>>() { });
```

##### 4. 动态表名

jpa@query 无法传递动态表名，会在sql 里  from ?  无法知道哪张表

##### 5. EntityManager-原生SQL查询

实体管理器

实体类 ResVo 要使用@Entity 和 @Id 注解，否则会报错

```
        Query query = em.createNativeQuery(sql, ResVo.class);
        query.setParameter("ago10minute", ago10minute);
        query.setParameter("ago1hour", ago1hour);
        query.setParameter("ago12hour", ago12hour);
        List<ResVo> resList = query.getResultList();
```

```
@Component
public class BalanceTargetInfoRepository {
    //注入的是实体管理器,执行持久化操作
    @PersistenceContext 
    EntityManager entityManager;
 
    public  List<Object[]> getTarget(String tableName, long memberId){
        String sql=" SELECT " +
                "id AS targetId, " +
                "state AS state,\n" +
                "current_value AS currentValue,\n" +
                "DATEDIFF(end_date, CURRENT_DATE()) AS remainingDay,\n" +
                "notification AS notification FROM "+tableName+" WHERE member_id ="+memberId+" ORDER BY update_time DESC;";
        List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
        return list;
    }
}
```

1.Caused by: java.sql.SQLSyntaxErrorException: ORA-01747: invalid user.table.column, table.column, or column specification

 查询sql的字段名与数据库关键字冲突了，仔细查看sql 是因为where t.like 写错了

2.javax.persistence.PersistenceException: org.hibernate.MappingException: Unknown entity: com.mx.JpaDemo.entity.RiskUnit
需要在实体类RiskUnit上加上@Entity注解 

3.Caused by: org.hibernate.AnnotationException: No identifier specified for entity: com.mx.JpaDemo.entity.RiskUnit

需要在实体类加上@Id注解 

##### 6. SpecificationExecutor

JpapecificationExecutor

https://blog.csdn.net/Rookie_cc/article/details/114261285

https://www.cnblogs.com/hanliukui/p/16842755.html

**动态条件查询**

```
public interface UserInfoRepository extends JpaRepository<UserInfo, Long>, JpaSpecificationExecutor<UserInfo> {
}
```

```
public class StatisticSpecification {

    public static Specification<Statistic> getUriCount(long beginTime, long endTime, String system,
        String uri, Integer status) {
        return  (root, query, b) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(userCode)) {
                //equal等于
                predicates.add(b.equal(r.get("userCode"), userCode));
            }
            if (StringUtils.isNotBlank(sex)) {
                //notEqual不等于
                predicates.add(b.notEqual(r.get("sex"), sex));
            }
            //isNotNull非空
            predicates.add(b.isNotNull(r.get("id")));
            //isNull空
            predicates.add(b.isNull(r.get("nikeName")));
            // like
            Predicate predicate = b.like(name.as(String.class), "%赵%");
            // between and
            Predicate timePredicate = b.between(root.get("start"), beginTime, endTime);
            return b.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

service中调用

```
UserInfoRepository.findAll(StatisticSpecification.getUriCount());
```

 **引入字段名**

```
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-jpamodelgen</artifactId>
            <version>5.6.14.Final</version>
        </dependency>
```

```
r.get("id")
```

上面root.get("字段名")如果字段名变了，容易出问题

引入这个包后，compile后会生成classes    实体类_ID  访问字段名称

**分页+排序**

```
public void queryUserInfo(QueryUserRequest request) {
    //排序
    Sort sort = Sort.by(Sort.Order.desc("id"));
    //构造分页请求参数，PageIndex当前页码，PageSiz每页大小
    PageRequest pageRequest = PageRequest.of(request.getPageIndex(), request.getPageSize(), sort);
    Page<UserInfoEntity> userInfoPage = envConfigureRepository.findAll((Specification<UserInfoEntity>) (r, q, b) -> {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(request.getDescription())) {
            predicates.add(b.equal(r.get("sex"), request.getSex()));
        }
        return b.and(predicates.toArray(new Predicate[0]));
    }, pageRequest);
    //获取总页数
    Integer totalPages = userInfoPage.getTotalPages();
    //获取数据总条数
    Long totalElements = userInfoPage.getTotalElements();
    //获取查询内容
    List<UserInfoEntity> = userInfoPage.getContent();
}
```

#### spring jpa接入redis配置

```
  data:
    redis:
      repositories:
        enabled: false
  redis:
    cluster:
      nodes: ${REDIS-CLUSTER-NODES:127.0.0.1:6379}
      max-redirects: 2
      timeout: 5000
      max-attempts: 3
    password: ${REDIS_PASSWORD:XXX}
```

#### jpa与hibernate

JPA是规范，Hibernate是框架，JPA是持久化规范，而Hibernate实现了JPA
JPA的主要API都定义在javax.persistence包中。如果你熟悉Hibernate，可以很容易做出对应：

| **org.hibernate** | **javax.persistence** | **说明**                              |
| ----------------- | --------------------- | ------------------------------------- |
| cfg.Configuration | Persistence           | 读取配置信息                          |
| SessionFactory    | EntityManagerFactory  | 用于创建会话/实体管理器的工厂类       |
| Session           | EntityManager         | 提供实体操作API，管理事务，创建查询   |
| Transaction       | EntityTransaction     | 管理事务                              |
| HQL               | JPQL                  | 静态查询，非类型安全（即存在SQL注入） |
| Criteria          | Criteria              | 动态查询，类型安全                    |

Hibernate查询语言（HQL）和Java持久性查询语言（JPQL）都是与SQL类似的面向对象模型的查询语言。JPQL是受HQL影响很大的子集。所有的JPQL查询都是有效的HQL查询，但反过来并不正确。

HQL和JPQL都是非类型安全的方式来执行查询操作。Criteria动态查询提供了一种查询类型安全的方法。
