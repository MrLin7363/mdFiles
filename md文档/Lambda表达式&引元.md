

## Lambda表达式

### 常用函数式接口

| 接口                         | 参数 | 返回    | 描述                                                         | 抽象方法 | 示例                    |
| ---------------------------- | ---- | ------- | ------------------------------------------------------------ | -------- | ----------------------- |
| Supplier<T> 提供者           | 无   | T       | 返回一个T类型的值                                            | get      | HashMap:new             |
| Consumer <T> 消费者          | T    | void    | 处理 一个 T 类型的值                                         | accept   | list.forEach            |
| Predicate <T>   ‘是’与‘不是’ | T    | boolean | 布尔值函数                                                   | test     | s.length()>12           |
| Function <T,R>               | T    | R       | 有一个 T 类型参数的函数                                      | apply    | Person::getName         |
| BinaryOperator<T,T>          | T    | T       | 二元操作符，二元（就是数学里二元一次方程那个二元,代表 2 个的意思）,双重的。即有两个操作数 例如求两个数的乘积(*) | apply    | (s, a) -> s + ", " + a) |
| UnaryOperator<T>             | T    | T       | 一元操作符，只有一个操作数 逻辑非(!)                         | apply    | (s) -> s + "值" )       |
| BiFunction<T, U, R>          | T,U  | R       | 有 T 和 U 类型参数的函数                                     | apply    |                         |

```
Function 

List<String> ss= names.stream().map(s->{
            if (s.equals("1234")) {
                return s;
            }
            return null;
        }).collect(Collectors.toList()); // 收集=1234的name
```

## Stream

Lambda表达式总结

```
tagsCreateCmd.getTageOptionValueList().stream().filter(
        e-> !valueSet.contains(e.getTagsValueKey()) 
);
```

```
tagsCreateCmd.getTageOptionValueList().stream().filter(
        (e) ->
        {
            return !valueSet.contains(e.getTagsValueKey());
        }
);
```

两种方式一样

```
  Map<String, String> phoneBook
         people.stream().collect(toMap(Person::getName,
                                       Person::getAddress,
                                       (s, a) -> s + ", " + a)); // BinaryOperator
```

