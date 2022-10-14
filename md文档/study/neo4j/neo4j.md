# **neo4j** - note

## 一、概述

### 1.链接

官网： https://neo4j.com/

官网document: https://neo4j.com/docs/getting-started/current/

官网下载： https://neo4j.com/download-center/

官网指令操作指南： https://neo4j.com/docs/cypher-manual/current/clauses/

文字教程： http://www.vue5.com/neo4j/neo4j_exe_environment_setup.html

w3school: https://www.w3cschool.cn/neo4j/  推荐这个文字教程

B站教程： https://www.bilibili.com/video/BV1HQ4y1h78j/?spm_id_from=333.337.search-card.all.click   讲得不太好，可以看文字教程即可

spring-data neo4j: https://spring.io/projects/spring-data-neo4j#learn

spring-data neo4j: https://neo4j.com/developer/spring-data-neo4j/  

### 2.介绍

百度百科的明星关系

银行欺诈检测

反洗钱

供应链管理

**与MYSQL对比**

| MYSQL    | NEO4J      |
| -------- | ---------- |
| 表       | 图         |
| 行       | 节点       |
| 列和数据 | 属性和数据 |
| 约束     | 关系       |

#### 数据模型

属性图模型规则

- 代表节点，关系和属性中的数据
- 节点和关系都包含属性
- 关系连接节点
- 属性是键值对
- 节点使用圆圈表示，关系用箭头键表示。
- 关系有方向：单向和双向。
- 每个关系包含“起始节点”或“从节点”和“到节点”或“结束节点”

**构件：**

- 节点
- 属性
- 关系
- 标签
- 数据浏览器，浏览器打开数据库

## 二、使用

### 1.环境搭建

如果neo4j.4.X + 的版本要当前jvm环境是  jdk 11 

下载3.x的最高版本

设置NEO4J_HOME = C： neo4j-community-2.1.3

设置PATH = C： neo4j-community-2.1.3 bin;

启动控制台

```
neo4j console  
```

desktop客户端也可以操作，推荐，客户端可以安装不同版本的 server

http://localhost:7474/

初始用户名和密码  neo4j

登录后会要求改密码  123456

### 2.CQL

| CQL命令/条      | 用法                         |
| :-------------- | ---------------------------- |
| CREATE 创建     | 创建节点，关系和属性         |
| MATCH 匹配      | 检索有关节点，关系和属性数据 |
| RETURN 返回     | 返回查询结果                 |
| WHERE 哪里      | 提供条件过滤检索数据         |
| DELETE 删除     | 删除节点和关系               |
| REMOVE 移除     | 删除节点和关系的属性         |
| ORDER BY以…排序 | 排序检索数据                 |
| SET 组          | 添加或更新标签               |

#### 1. 创建 Create

| 语法元素     | 描述                       |
| :----------- | :------------------------- |
| CREATE       | 它是一个Neo4j CQL命令。    |
| <node-name>  | 它是我们要创建的节点名称。 |
| <label-name> | 它是一个节点标签名称       |

```
CREATE (dept:Dept)   无属性
```

语法说明：

| 语法元素                              | 描述                                            |
| :------------------------------------ | :---------------------------------------------- |
| <node-name>                           | 它是我们将要创建的节点名称。                    |
| <label-name>                          | 它是一个节点标签名称                            |
| <Property1-name>...<Propertyn-name>   | 属性是键值对。 定义将分配给创建节点的属性的名称 |
| <Property1-value>...<Propertyn-value> | 属性是键值对。 定义将分配给创建节点的属性的值   |

```
带属性节点
CREATE (
   <node-name>:<label-name>
   { 	
      <Property1-name>:<Property1-Value>
      ........
      <Propertyn-name>:<Propertyn-Value>
   }
)

CREATE (dept:Dept {deptno:10,dname:"Accounting",location:"Hyderabad" })
CREATE (emp:Employee{id:123,name:"Lokesh",sal:35000,deptno:10})
CREATE (m:Dept:Employee{id:138})  #创建m节点，对应多个标签
```

#### 2. 查询Match

- 从数据库获取有关节点和属性的数据
- 从数据库获取有关节点，关系和属性的数据

```
MATCH 
(
   <node-name>:<label-name>
)
```

```
#获取25个节点
MATCH (n) RETURN n LIMIT 25

# 查询Dept下的内容
MATCH (dept:Dept) return dept

# 查询Employee标签下 id=123，name="Lokesh"的节点
MATCH (p:Employee {id:123,name:"Lokesh"}) RETURN p

## 查询Employee标签下name="Lokesh"的节点，使用（where命令）
MATCH (p:Employee)
WHERE p.name = "Lokesh"
RETURN p

## 返回lin的关联关系及节点
match p=(:Employee{name:"lin"}) -[r:HAVE]->() return p 

## 查询neo4j帮我们自动生成的<id>
MATCH (n:Dept) where id(n)=42 RETURN n LIMIT 25
```

#### 3. Return

```
需要搭配查询语句match，不可单独用
RETURN 
   <node-name>.<property1-name>,
   ........
   <node-name>.<propertyn-name>
   
return n 返回整个节点
return n.deptno 就返回n的属性  

MATCH (dept: Dept) RETURN dept.deptno,dept.dname,dept.location
```

#### 4. 关系基础-创建

基于方向性，Neo4j关系被分为两种主要类型。

- 单向关系
- 双向关系

在以下场景中，我们可以使用Neo4j CQL CREATE命令来创建两个节点之间的关系。 这些情况适用于Uni和双向关系。

- 在两个现有节点之间创建无属性的关系
- 在两个现有节点之间创建有属性的关系
- 在两个新节点之间创建无属性的关系
- 在两个新节点之间创建有属性的关系
- 在具有WHERE子句的两个退出节点之间创建/不使用属性的关系

```
CREATE (cc:CreditCard{id:"5001",number:"1234567890",cvv:"888",expiredate:"20/17"})
CREATE (e:Customer{id:"1001",name:"Abc",dob:"01/10/1982"})
```

```
MATCH (<node1-label-name>:<nodel-name>),(<node2-label-name>:<node2-name>)
CREATE  
	(<node1-label-name>)-[<relationship-label-name>:<relationship-name>{<define-properties-list>}]->(<node2-label-name>)
RETURN <relationship-label-name>
```

##### (1) 现有节点之间创建无属性的关系

客户指向信用卡

```
MATCH (e:Customer),(cc:CreditCard) 
CREATE (e)-[r:DO_SHOPPING_WITH ]->(cc) 
```

查询

```
MATCH (e)-[r:DO_SHOPPING_WITH ]->(cc)  RETURN r   #返回单纯的关系

MATCH p=()-[r:DO_SHOPPING_WITH]->() RETURN p  #返回这个关系所连接的所有节点
```

##### (2) 现有节点之间创建有属性的关系

```
MATCH (cust:Customer{name:"lin"}),(cc:CreditCard) 
CREATE (cust)-[r:DO_SHOPPING_WITH_PROP{shopdate:"12/12/2014",price:55000}]->(cc) 
RETURN r
```

```
MATCH (cust:Customer{name:"lin"}),(cc:CreditCard),(r:Relation) 
where r.form=cust.name
CREATE (cust)-[r:DO_SHOPPING_WITH_PROP{shopdate:"12/12/2014",price:55000}]->(cc) 
RETURN r
```

##### (3) 新节点之间创建无属性的关系

```
# LIKES关系标签  like关系名称，一般写r即可
CREATE (fb1:FaceBookProfile1)-[like:LIKES]->(fb2:FaceBookProfile2) 
```

查询关系

```
MATCH (fb1:FaceBookProfile1)-[like:LIKES]->(fb2:FaceBookProfile2) 
RETURN like

MATCH (e)-[r:LIKES ]->(cc)  RETURN r
```

##### (4) 新节点之间创建有属性的关系

```
CREATE (video1:YoutubeVideo1{title:"Action Movie1",updated_by:"Abc",uploaded_date:"10/10/2010"})
-[movie:ACTION_MOVIES{rating:1}]->
(video2:YoutubeVideo2{title:"Action Movie2",updated_by:"Xyz",uploaded_date:"12/12/2012"}) 
```

```
MATCH p=()-[r:ACTION_MOVIES]->() RETURN p LIMIT 25

MATCH (video1)-[r:ACTION_MOVIES]->(video2) 
RETURN video1,video2
```

##### (5) 检索关系节点的详细信息 用这个

```
MATCH (cust)-[r:DO_SHOPPING_WITH]->(cc) 
RETURN cust,cc
```

#### 5. where子句

AND RO NOT

```
MATCH (emp:Employee) 
WHERE emp.name = 'Abc' OR emp.name = 'Xyz'
RETURN emp
```

```
# WHERE子句在两个现有节点之间创建了一个NEW关系
MATCH (cust:Customer),(cc:CreditCard) 
WHERE cust.id = "1001" AND cc.id= "5001" 
CREATE (cust)-[r:DO_SHOPPING_WITH{shopdate:"12/12/2014",price:55000}]->(cc) 
RETURN r
```

#### 6. delete删除（节点和关系）

- 删除节点。
- 删除节点及相关节点和关系。

删除节点

```
MATCH (e: Employee) DELETE e
```

删除节点和关系

```
MATCH (cc: FaceBookProfile1)-[rel]-(c:FaceBookProfile2) 
DELETE cc,c,rel
```

删除关系

```
MATCH (cc: CreditCard)-[rel]-(c:Customer) 
DELETE rel
```

#### 7. remove删除（标签和属性）

- 删除节点或关系的标签
- 删除节点或关系的属性

```
CREATE (book:Book {id:122,title:"Neo4j Tutorial",pages:340,price:250}) 

MATCH (book : Book)
RETURN book
```

```
删除属性
MATCH (book { id:122 })
REMOVE book.price
RETURN book

类似于SQL 
ALTER TABLE BOOK REMOVE COLUMN PRICE;
SELECT * FROM BOOK WHERE ID = 122;
```

```
删除多标签节点 中的一个标签
MATCH (m:Employee) where m.id=138
REMOVE m:Dept
```

#### 8. set子句更新

向现有节点或关系添加新属性

```
MATCH (n:YoutubeVideo1)
set n.updated_by='lin'
return n
```

#### 9. roder/limit/union/skip

order by

```
MATCH (emp:Employee)
RETURN emp.empid,emp.name,emp.salary,emp.deptno
ORDER BY emp.name DESC
```

union 它将两组结果中的公共行组合并返回到一组结果中。 它不从两个节点返回重复的行。

```
MATCH (cc:CreditCard)
RETURN cc.id as id,cc.number as number,cc.name as name,
   cc.valid_from as valid_from,cc.valid_to as valid_to
UNION
MATCH (dc:DebitCard)
RETURN dc.id as id,dc.number as number,dc.name as name,
   dc.valid_from as valid_from,dc.valid_to as valid_to
```

union all  它结合并返回两个结果集的所有行成一个单一的结果集。它还返回由两个节点重复行。

```
MATCH (cc:CreditCard)
RETURN cc.id as id,cc.number as number,cc.name as name,
   cc.valid_from as valid_from,cc.valid_to as valid_to
UNION ALL
MATCH (dc:DebitCard)
RETURN dc.id as id,dc.number as number,dc.name as name,
   dc.valid_from as valid_from,dc.valid_to as valid_to
```

limit skip

```
MATCH (emp:Employee) 
RETURN emp
LIMIT/SKIP 2
```

#### 10.合并

```
MERGE = CREATE + MATCH
```

 MERGE命令检查该节点在数据库中是否可用。 如果它不存在，它创建新节点。 否则，它不创建新的

```
MATCH  (gp2:GoogleProfile2) 
RETURN gp2.Id,gp2.Name
```

#### 11.NULL值 / IN

```
MATCH (e:Employee) 
WHERE e.id IS NULL
RETURN e.id,e.name,e.sal,e.deptno
```

```
MATCH (e:Employee) 
WHERE e.id IN [123,124]
RETURN e.id,e.name,e.sal,e.deptno
```

### 3.函数

#### 字符串函数

| 描述      |                                  |
| :-------- | -------------------------------- |
| UPPER     | 它用于将所有字母更改为大写字母。 |
| LOWER     | 它用于将所有字母改为小写字母。   |
| SUBSTRING | 它用于获取给定String的子字符串。 |
| REPLACE   | 它用于替换一个字符串的子字符串。 |

```
MATCH (e:Employee) 
RETURN e.id,UPPER(e.name),e.sal,e.deptno

MATCH (e:Employee) 
RETURN e.id,SUBSTRING(e.name,0,2),e.sal,e.deptno
```

#### 聚合函数

| 聚集功能 | 描述                                    |
| :------- | :-------------------------------------- |
| COUNT    | 它返回由MATCH命令返回的行数。           |
| MAX      | 它从MATCH命令返回的一组行返回最大值。   |
| MIN      | 它返回由MATCH命令返回的一组行的最小值。 |
| SUM      | 它返回由MATCH命令返回的所有行的求和值。 |
| AVG      | 它返回由MATCH命令返回的所有行的平均值。 |

```
MATCH (e:Employee) RETURN COUNT(*)

MATCH (e:Employee) 
RETURN MAX(e.sal),MIN(e.sal)

MATCH (e:Employee) 
RETURN SUM(e.sal),AVG(e.sal)
```

#### 关系函数

| 描述      |                                          |
| :-------- | ---------------------------------------- |
| STARTNODE | 它用于知道关系的开始节点。               |
| ENDNODE   | 它用于知道关系的结束节点。               |
| ID        | 它用于知道关系的ID。                     |
| TYPE      | 它用于知道字符串表示中的一个关系的TYPE。 |

```
MATCH (a)-[movie:ACTION_MOVIES]->(b) 
RETURN STARTNODE(movie)

MATCH (a)-[movie:ACTION_MOVIES]->(b) 
RETURN ENDNODE(movie)

MATCH (a)-[movie:ACTION_MOVIES]->(b) 
RETURN ID(movie),TYPE(movie)
```

### 4.索引

```
CREATE INDEX ON :<label_name> (<property_name>)

CREATE INDEX ON :Customer (name)
```

```
DROP INDEX ON :<label_name> (<property_name>)

DROP INDEX ON :Customer (name)
```

### 5.UNIQUE约束

```
CREATE CONSTRAINT ON (cc:CreditCard)
ASSERT cc.number IS UNIQUE
```

```
DROP CONSTRAINT ON (cc:CreditCard)
ASSERT cc.number IS UNIQUE
```

## 三、代码运用

### 1. spring data neo4j

参考spring官网

```
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-neo4j</artifactId>
             <version>2.7.0</version>
</dependency>
```

新版本替换了下面的注解

| Old                                       | New                                                         |
| :---------------------------------------- | :---------------------------------------------------------- |
| `org.neo4j.ogm.annotation.NodeEntity`     | `org.springframework.data.neo4j.core.schema.Node`           |
| `org.neo4j.ogm.annotation.GeneratedValue` | `org.springframework.data.neo4j.core.schema.GeneratedValue` |
| `org.neo4j.ogm.annotation.Id`             | `org.springframework.data.neo4j.core.schema.Id`             |
| `org.neo4j.ogm.annotation.Property`       | `org.springframework.data.neo4j.core.schema.Property`       |
| `org.neo4j.ogm.annotation.Relationship`   | `org.springframework.data.neo4j.core.schema.Relationship`   |

```
public interface DeptRepository extends Neo4jRepository<Dept,Long>
```

```
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Dept") // 标签：默认就是类名
@Data
public class Dept {
    @Id // 声明主键
    @GeneratedValue  // 声明该字段为neo4j自动生成的<id>映射的字段
    private Long id;

    @Property("deptno") // 如果定义属性名称不一样可用
    private Integer deptno;

    private String dname;

    private String location;
    
	@Relationship(type = "ACTED_IN", direction = Direction.INCOMING) 
	private List<Roles> actorsAndRoles;

	@Relationship(type = "DIRECTED", direction = Direction.INCOMING)
	private List<PersonEntity> directors = new ArrayList<>();
}
```

