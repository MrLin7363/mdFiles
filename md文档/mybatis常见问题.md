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