MySQL-常见问题积累

#### mysql - Json 字段属性搜索

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

