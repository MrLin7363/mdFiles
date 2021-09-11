MySQL-常见问题积累

#### mysql - Json 字段属性搜索

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

#### SQL

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

