MySQL数据类型 - json 格式

#### JSON格式

- 使用 `字段->'$.json属性'` 进行查询条件
- 使用 json_extract 函数查询，`json_extract(字段, "$.json属性")`
- 根据json数组查询，用 `JSON_CONTAINS(字段, JSON_OBJECT('json属性', "内容"))`

```
SQL语句
SELECT * FROM core_process where data -> '$.name' = '陈俊霖'
```

如果是Mybatis框架，要把 > 符号转义，像这样用 `<![CDATA[ ]]>`

```
<![CDATA[   Form_Value_ -> '$.endDate' < #{endDate,jdbcType=VARCHAR}    ]]>
```



