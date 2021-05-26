

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

```
// 根据快递表的id字段去映射成id字段的 List
List<Long> expressIds = expresses.stream().map(Express::getId).collect(Collectors.toList());
```

```
// 如果能确定expressId是唯一的可以这么写，不会建重复
Map<Long, List<Long>> expressTradeRelateMap = expressTradeRelates.stream()
        .collect(Collectors.toMap(ExpressTradeRelate::getExpressId, ExpressTradeRelate::getTradeIds));
```

```
// 这个会取第一个key作map，如果后面有重复或者不同的，则只取第一个元素作为map的value
        nameDescMap = users.stream().collect(Collectors.toMap(MyUser::getName,
                MyUser::getDesc,(k,v)-> k));
```

```
// shipmentGoodsList 是个list，取里面的List<Long> tradeId     去重 
shipmentDetail.setTradeIds(shipmentGoodsList.stream().map(ShipmentGoodsDO::getTradeId).distinct().collect(Collectors.toList()));
```

    public class ShipmentGoodsDO {
    /**
     * 发货单号
     */
    private String shipmentCode;
    /**
     * 交易单号
     */
    private Long tradeId;
    
    private Long orderId;// '订单号'
    ......
    }
```
List<ShipmentGoodsDO> shipmentGoodsDOList = orderMapper.selectShipmentGoodsByTradeId(tradeId, statusEnum.getCode());
// 根据里面的shipmentCode 分为  < shipmentCode,List<ShipmentGoodsDO> >
Map<String, List<ShipmentGoodsDO>> shipmentGoodsMap = shipmentGoodsDOList.stream()
                .collect(Collectors.groupingBy(ShipmentGoodsDO::getShipmentCode));
                
// 这个会取第一个key作map，如果后面有重复或者不同的，则只取第一个元素作为map的value   <shipmentCode,consigneeId>
Map<String, Long> shipmentConsigneeMap = shipmentGoodsMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getConsigneeId(), (c1, c2) -> c1));
        
// 
List<ConsigneeDO> consigneeDOS = consigneeMapper.selectByIdList(new ArrayList<>(shipmentConsigneeMap.values()));
Map<Long, ConsigneeDO> consigneeDOMap = consigneeDOS.stream().collect(Collectors.toMap(ConsigneeDO::getId,Function.identity()));
```

