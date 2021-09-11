redis的设计与实现

## redis数据库

默认reids有16个数据库，在 db[] 数组里

数据，以字典的方式保存

```
flushdb 清空数据库
dbsize  该数据库健数量
exists  key 
```

