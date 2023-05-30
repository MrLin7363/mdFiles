## 网址积累

官方网站   https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#function_coalesce

## MySQL-常见问题积累

### 1. mysql - Json 字段属性搜索

1.针对 JSON字符串

 字段->'$.json属性'进行查询条件

```
select * from table where data_json->'$.id' = 142;
```

使用 json_extract 函数查询，json_extract(字段, "$.json属性")

2.针对 JSON数组

根据json数组查询，用 `JSON_CONTAINS(字段, JSON_OBJECT('json属性', "内容"))`

```
select * from table and JSON_CONTAINS(json_data, JSON_OBJECT(#{queryParam.key},#{queryParam.value}))
json_data 是mysql json 属性的字段
select * from table and JSON_CONTAINS(json_data, JSON_OBJECT('width','43'))
搜索json_data中含有 width = 43 的数据
```

### 2. delete

delete使用别名时，必须在前面加入别名

```
delete a from table a from where a.status=2 and exists (select b.id from table2 b where a.wid=b.id)
```

来删除 `Person` 表中所有重复的电子邮箱，重复的邮箱里只保留 **Id** *最小* 的那个

```
DELETE p1 FROM Person p1,
    Person p2
WHERE
    p1.Email = p2.Email AND p1.Id > p2.Id
```

```
SELECT p1.*
FROM Person p1,
    Person p2
WHERE
    p1.Email = p2.Email AND p1.Id > p2.Id
```

### 3. dual 临时表实现插入存在不插入

```
    <insert id="insertNotExist">
        insert into t_notice(create_time, description, handler, status, type)
        SELECT #{notice.createTime},
               #{notice.description},
               #{notice.handler},
               #{notice.status},
               #{notice.type}
        FROM dual
        where not exists(select 1
                         from t_notice
                         where handler = #{notice.handler}
                           and type = #{notice.type})
    </insert>
```

### 4. sql的运行顺序

from -> where -> group by -> select -> order by -> limit

### 5. 索引优化积累

联合索引可能比单独的两个索引速度更快

## SQL积累

### 1. limit&offset

SQL查询语句中的 limit 与 offset 的区别：

1. limit y 分句表示: 读取 y 条数据
2. limit x, y 分句表示: 跳过 x 条数据，读取 y 条数据
3. limit y offset x 分句表示: 跳过 x 条数据，读取 y 条数据

### 2. @符号

- `@`后接变量名，用以定义一个变量，该变量的有效期为语句级，即再一次执行中始终有效，基本示例如下：

-- 由于通常情况下=被认为是等于比较运算符，因此赋值运算符一般使用:=
SELECT @lt:=1, @lt:=@lt+1, @lt:=@lt+1;

1 2 3



-- 由于tmp只有一行，当这两表进行**笛卡尔积链接**时，结果集实际上等同于增加了一列，而由于r变量的特性，每行都在原值的基础上在进行增加操作
SELECT a.BATCHNO, a.YEAR, @r:=@r+1 FROM m1 a, (SELECT @r:=0) tmp;



#### 例子：第N高的薪水例子

自定义变量实现按薪水降序后的数据排名，同薪同名不跳级，即3000、2000、2000、1000排名后为1、2、2、3；
对带有排名信息的临时表二次筛选，得到排名为N的薪水；
因为薪水排名为N的记录可能不止1个，用distinct去重

```
CREATE FUNCTION getNthHighestSalary(N INT) RETURNS INT
BEGIN
    # i 定义变量接收返回值
    DECLARE ans INT DEFAULT NULL;  
    # ii 执行查询语句，并赋值给相应变量
    SELECT 
        DISTINCT salary INTO ans
    FROM 
        (SELECT 
            salary, @r:=IF(@p=salary, @r, @r+1) AS rnk,  @p:= salary     #  当前薪水和上一个薪水比较，得出等级
        FROM  
            employee, (SELECT @r:=0, @p:=NULL)init    # 类似@r:=0  @p:=NULL 也是初始值啥的，初始列通过构造一张表来执行,表只有一行数据，不属于笛卡尔积   init 是表别名
        ORDER BY 
            salary DESC) tmp
    WHERE rnk = N;
    # iii 返回查询结果，注意函数名中是 returns，而函数体中是 return
    RETURN ans;
END
```

#### 例子：连续出现的数字

https://leetcode.cn/problems/consecutive-numbers/solution/bian-xie-yi-ge-sql-cha-xun-cha-zhao-suo-you-zhi-sh/

```
输入：
Logs 表：
+----+-----+
| Id | Num |
+----+-----+
| 1  | 1   |
| 2  | 1   |
| 3  | 1   |
| 4  | 2   |
| 5  | 1   |
| 6  | 2   |
| 7  | 2   |
+----+-----+
输出：
Result 表：
+-----------------+
| ConsecutiveNums |
+-----------------+
| 1               |
+-----------------+
解释：1 是唯一连续出现至少三次的数字。
```

| Id   | Num  | Id   | Num  | Id   | Num  |
| ---- | ---- | ---- | ---- | ---- | ---- |
| 1    | 1    | 2    | 1    | 3    | 1    |

注意：前两列来自 l1 ，接下来两列来自 l2 ，最后两列来自 l3 。

然后我们从上表中选择任意的 *Num* 获得想要的答案。同时我们需要添加关键字 `DISTINCT` ，因为如果一个数字连续出现超过 3 次，会返回重复元素。

常规题解：三表连接，通过id的连续性

```
select distinct l1.num as ConsecutiveNums from Logs l1,Logs l2, Logs l3 
where l1.Id=l2.Id-1
and l2.Id=l3.Id-1
and l1.num=l2.num
and l2.num=l3.num
```

@符题解

```
#首先遍历一遍整张表，找出每个数字的连续重复次数
#具体方法为：
    #初始化两个变量，一个为pre，记录上一个数字；一个为count，记录上一个数字已经连续出现的次数。
    #然后调用if()函数，如果pre和当前行数字相同，count加1极为连续出现的次数；如果不同，意味着重新开始一个数字，count重新从1开始。
    #最后，将当前的Num数字赋值给pre，开始下一行扫描。
    select 
        Num,    #当前的Num 数字
        if(@pre=Num,@count := @count+1,@count := 1) as nums, #判断 和 计数
        @pre:=Num   #将当前Num赋值给pre
    from Logs as l ,
        (select @pre:= null,@count:=1) as pc #这里需要别名
    #上面这段代码执行结果就是一张三列为Num,count as nums,pre的表。

#②将上面表的结果中，重复次数大于等于3的数字选出，再去重即为连续至少出现三次的数字。
    select 
        distinct Num as ConsecutiveNums 
    from  
        (select Num,
                if(@pre=Num,@count := @count+1,@count := 1) as nums,
                @pre:=Num
            from Logs as l ,
                (select @pre:= null,@count:=1) as pc
        ) as n
    where nums >=3;

#注意：pre初始值最好不要赋值为一个数字，因为不确定赋值的数字是否会出现在测试表中。
# 表的别名一个不能少
```

#### 例子：部门工资前三高的员工

https://leetcode.cn/problems/department-top-three-salaries/

注意点 : 部门薪水前三高包含了相同薪水下排名相同的意思

@题解

```
select dep.name as Department,emp.name as Employee,emp.salary as Salary
from
(
## (部门,薪水)去重,再 部门(升),薪水(降) 排序 , 这里不能查出名字，只能后面内连接关联出多条同薪水的
select ta.departmenId,ta.salary,
(case 
when @p=ta.departmentId then @r:=@r+1   # 如果不等于部门ID，默认到下面语句初始化等级，每个部门从1开始
when @p:=ta.departmentId then @r:=1   
end) as rank 
from 
(select @r:=0,@p:=null) init,
(
select departmentId,salary from employee group by departmentId,salary
order by departmentId asc,salary desc
)ta
) t
inner join department dep on t.departmentId=dep.Id
inner join employee emp on t.departmentId=emp.departmentId and t.salary=emp.salary and t.rank<=3
order by t.departmentId asc,t.salary desc
```

第二种题解

公司里前 3 高的薪水意味着有不超过 3 个工资比这些值大

```
SELECT
    d.Name AS 'Department', e1.Name AS 'Employee', e1.Salary
FROM
    Employee e1
        JOIN
    Department d ON e1.DepartmentId = d.Id
WHERE
    3 > (SELECT
            COUNT(DISTINCT e2.Salary)
        FROM
            Employee e2
        WHERE
            e2.Salary > e1.Salary
                AND e1.DepartmentId = e2.DepartmentId
        )
;
```

### 3. 流控制语句 `CASE | IF`

https://leetcode.cn/problems/tree-node/solution/shu-jie-dian-by-leetcode/

给定一个表 tree，id 是树节点的编号， p_id 是它父节点的 id 。

+----+------+
| id | p_id |
+----+------+
| 1  | null |
| 2  | 1    |
| 3  | 1    |
| 4  | 2    |
| 5  | 2    |
+----+------+
树中每个节点属于以下三种类型之一：

叶子：如果这个节点没有任何孩子节点。
根：如果这个节点是整棵树的根，即没有父节点。
内部节点：如果这个节点既不是叶子节点也不是根节点。

#### UNION 解题

```mysql
select id,'Root' as Type from tree
where p_id is null   # 根节点

union
select id,'Inner' as Type from tree
where p_id is not null  # 非根节点
and id in
(select distinct p_id from tree where p_id is not null) # 非叶子节点

union
select id,'Leaf' as Type from tree
where p_id is not null # 非根节点
and id not in
(select distinct p_id from tree where p_id is not null) # 非叶子节点
order by id
```

#### CASE WHEN THEN解题

用法   https://blog.csdn.net/weixin_44487203/article/details/124793889

```
用法一：
CASE input_expression
WHEN when_expression THEN
    result_expression [...n ] [
ELSE
    else_result_expression
END

用法二：
CASE
WHEN search_condition THEN statement_list
[WHEN search_condition THEN statement_list] ...
[ELSE statement_list]
END 
```

```mysql
SELECT
    id AS `Id`,
    CASE
        WHEN tree.id = (SELECT atree.id FROM tree atree WHERE atree.p_id IS NULL) 
          THEN 'Root'
        WHEN tree.id IN (SELECT atree.p_id FROM tree atree) # 有父节点的除了根节点就是内部节点，  根节点在第一个when已经检查过是否是父节点了
          THEN 'Inner'
        ELSE 'Leaf'
    END AS Type
FROM
    tree
ORDER BY `Id`;
```

换座位  https://leetcode.cn/problems/exchange-seats/

```
# 初始版
select 
(case when mod(id,2)!=0 and (select count(*) from Seat)!=id   # 奇数+1
then id+1
when mod(id,2)!=0 and (select count(*) from Seat)=id    # 最后一个奇数不变
then id
else id-1   # 偶数固定是-1
end) as id,
student
from Seat
order by id asc

# 增强版
select 
(case when mod(id,2)!=0 and counts!=id   # 奇数+1
then id+1
when mod(id,2)!=0 and counts=id    # 最后一个奇数不变
then id
else id-1   # 偶数固定是-1
end) as id,
student
from Seat,(select count(*) as counts from seat ) as seat2   # 新增了一列，counts, 作为变量名，seat2作为表名不可缺失,这个counts只用查询一次
order by id asc
```

#### IF 解题

```
SELECT
    atree.id,
    IF(ISNULL(atree.p_id),
        'Root',
        IF(atree.id IN (SELECT p_id FROM tree), 'Inner','Leaf')) Type 
FROM
    tree atree # 别名可以去掉 
ORDER BY atree.id
```

### 4. Coalesce 函数

```
mysql> SELECT COALESCE(NULL,1);  # 按顺序取出非null值
        -> 1
mysql> SELECT COALESCE(NULL,NULL,NULL);
        -> NULL
```

### 5. join函数

```
SELECT
     a.NAME AS Employee
FROM Employee AS a JOIN Employee AS b    # 和这个效果一致，都是全连接笛卡尔积 Employee a, Employee b    
     ON a.ManagerId = b.Id
     AND a.Salary > b.Salary
;
```

```
select a.system,
case when h.health_level is null then 0 else h.health_level end as health_level,
case when request10minute is null then 0 else request10minute end as request10minute,
case when request1hour is null then 0 else request1hour end  as request1hour,
case when request12hour is null then 0 else request12hour end  as request12hour from
(select system from proxy_system where is_delete=0) as a 

inner join 
(select proxy_system,count(*) as request12hour from proxy_statistic where proxy_start>1683316800000 group by proxy_system)as d
on a.system=d.proxy_system

left join 
(select proxy_system,count(*) as request1hour from proxy_statistic where proxy_start>1683356400000 group by proxy_system)as b
on a.system=b.proxy_system

left join  
(select proxy_system,count(*) as request10minute from proxy_statistic where proxy_start>1683359400000 group by proxy_system)as c
on a.system=c.proxy_system

left join system_health h

on a.system=h.system 
```

### 6. 困难题积累

#### 行程和用户

https://leetcode.cn/problems/trips-and-users/

连接排除

```
select t.request_at as 'Day',
round(
	sum(if(t.status='completed',0,1))/count(t.status),
	2     # 小数位两位
) AS `Cancellation Rate`
from Trips t
inner join Users u1 on t.client_id=u1.users_id and u1.banned='No'
inner join Users u2 on t.driver_id=u2.users_id and u2.banned='No'
where request_at between '2013-10-01' and '2013-10-03'
group by request_at
```

not in排除

```
SELECT T.request_at AS `Day`, 
	ROUND(
			SUM(
				IF(T.STATUS = 'completed',0,1)
			)
			/ 
			COUNT(T.STATUS),
			2
	) AS `Cancellation Rate`
FROM trips AS T
WHERE 
T.Client_Id NOT IN (
	SELECT users_id
	FROM users
	WHERE banned = 'Yes'
)
AND
T.Driver_Id NOT IN (
	SELECT users_id
	FROM users
	WHERE banned = 'Yes'
)
AND T.request_at BETWEEN '2013-10-01' AND '2013-10-03'
GROUP BY T.request_at
```

#### 体育馆的人流量

题解

https://leetcode.cn/problems/human-traffic-of-stadium/solution/ti-yu-guan-de-ren-liu-liang-by-little_bird/

```
SELECT distinct a.*
FROM stadium as a,stadium as b,stadium as c
where ((a.id = b.id-1 and b.id+1 = c.id) or   -- a b c    a为三个连续值中 最小值
       (a.id-1 = c.id and c.id+2 = b.id) or   -- c a b    a为 中间值
       (a.id-1 = b.id and b.id-1 = c.id))     -- c b a     a为 最大值
  and (a.people>=100 and b.people>=100 and c.people>=100)
order by a.id;
```

### 7. count(条件)

注意要加 or null  ， count只有在值为null时才不统计数据，而为false时是会统计数据的

```
SELECT COUNT(*) AS request,count(status between 200 and 299 or null)/count(*)*100 as successRate FROM `xxx` WHERE start between 1684740023639 and 1684740623639 AND system = 'Test' and uri like concat('/','%')
```

### 8.分页性能优化

```
-- 传统limit，文件扫描
[SQL]SELECT * FROM tableName ORDER BY id LIMIT 500000,2;
受影响的行: 0
时间: 5.371s

-- 子查询方式，索引扫描    如果这里有其他查询条件，则外层的select也要带查询条件不是很推荐这种
[SQL]
SELECT * FROM tableName
WHERE id >= (SELECT id FROM tableName ORDER BY id LIMIT 500000 , 1)
LIMIT 2;
受影响的行: 0
时间: 0.274s

-- JOIN分页方式
[SQL]
SELECT *
FROM tableName AS t1
JOIN (SELECT id FROM tableName ORDER BY id desc LIMIT 500000, 1) AS t2
WHERE t1.id <= t2.id ORDER BY t1.id desc LIMIT 2;
受影响的行: 0
时间: 0.278s
```

