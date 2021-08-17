JAVA核心技术卷-书籍

#### 面向对象

封装：JAVA对象的属性

继承：

多态：对象变量是多态的。 一个 Employee 变量既可以引用一个
Employee 类对象， 也可以引用一个 Employee 类的任何一个子类的对象（例如， Manager、
Executive、 Secretary 等）  

类之间关系：

依赖： use a

聚合：has a

继承：is a

注意：方法入参，不能改变引用对象的引用

```
class Employee
{
private static int nextld;
private int id;

// object initialization block 初始化块在Employee对象被实例化的时候执行，和static块不一样
//为了避免循环定义，不要读取在块后面初始化的域
{
id = nextld;
nextld++;
}

```



#### 反射

Class<?> 任意一个类

Class<?>[] 任意一个接口

#### 泛型

```
// 第一个T是方法参数限定，表示（）内的T必须是T类型的
// 第二个T是方法的返回类型
// 第三个T和第一个T对应
public static <T extends Comparable & Serializable> T getMin(T...a){
```