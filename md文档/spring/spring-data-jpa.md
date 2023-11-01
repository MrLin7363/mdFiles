spring-data-jpa

官方文档  https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods

### 1. sql相关

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
            // in
            if (CollectionUtils.isNotEmpty(serviceList)) {
            CriteriaBuilder.In<Object> in = builder.in(root.get("service"));
            for (Integer service : serviceList) {
                in.value(service);
            }
            predicates.add(in);
        	}
            return b.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

// 如果要 and or 条件

```
List<Predicate> predicates = new ArrayList<>();
predicates.add(builder.between(root.get("start"), reqVo.getBeginTime(), reqVo.getEndTime()));
if (StringUtils.isNotBlank(reqVo.getSystem())) {
    predicates.add(builder.equal(root.get("System"), reqVo.getSystem()));
}
// and (status=200 or status=300)
if (isAvoid) {
    List<Predicate> statusPredicate = new ArrayList<>();
    statusPredicate.add(builder.equal(root.get("status"), 200));
    statusPredicate.add(builder.equal(root.get("status"), 300));
    predicates.add(builder.or(statusPredicate.toArray(new Predicate[0])));
}
return builder.and(predicates.toArray(new Predicate[0]));
```

// 如果要用排序CriteriaQuery

```
public static Specification<ProxySystem> getSystemSpec(List<Integer> serviceList) {
        return (root, query, builder) -> getPredicate(serviceList,query, root, builder);
}
    
private static Predicate getPredicate(List<Integer> serviceList, CriteriaQuery<?> query, Root<ProxySystem> root,
    CriteriaBuilder builder) {
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("is_delete"), 0));
    if (CollectionUtils.isNotEmpty(serviceList)) {
        CriteriaBuilder.In<Object> in = builder.in(root.get("service"));
        for (Integer type : serviceList) {
            in.value(type);
        }
        predicates.add(in);
    }
    Predicate[] searchParam = predicates.toArray(new Predicate[0]);
    query.where(searchParam);
    // 排序
    query.orderBy(builder.asc(root.get("service")));
    return query.getRestriction();
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

##### 7. 传入对象参数

```
@Query(value = "select * from table where start>:#{#vo.beginTime} "
    + " and start<:#{#vo.endTime} ", nativeQuery = true)
List<xxxxx> getAvoidDetail(@Param("vo") AvoidDetailReqVo vo); // 注意param中名字要和方法名vo一致
```

### 2. 注解相关

#### 2.1 @MappedSuperclass

**1.**@MappedSuperclass注解只能标准在类上：@Target({java.lang.annotation.ElementType.TYPE})

**2.**标注为@MappedSuperclass的类将不是一个完整的[实体类](https://so.csdn.net/so/search?q=实体类&spm=1001.2101.3001.7020)，他将不会映射到数据库表，但是他的属性都将映射到其**子类**的数据库字段中。

**3.**标注为@MappedSuperclass的类不能再标注@Entity或@Table注解，也无需实现序列化接口。

一般用户公共属性，子类继承此 BaseEntity

```
@Data
@ToString
@MappedSuperclass
public abstract class BaseEntity {
    protected long creatorId;

    protected String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    protected Date createTime;

    protected long updaterId;

    protected String updater;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    protected Date updateTime;
}
```

#### 2.2 复合主键



### 3. spring jpa接入redis配置

需要关闭 enabled: false

```
  data:
    redis:
      repositories:
        enabled: false
  redis:
    cluster:
      nodes: ...
    password: ...
    lettuce:
      pool:
        min-idle: 5
        max-idle: 50
        max-wait: 5s
        time-between-eviction-runs: 2s
      cluster:
        refresh:
          period: 30s
          adaptive: true
    timeout: 5s
```

### 4. jpa与hibernate

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
