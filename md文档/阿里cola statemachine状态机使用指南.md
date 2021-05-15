阿里cola statemachine状态机使用指南

相比Spring statemachine状态机等的复杂，功能多；我们更需要常用的功能，简单使用，所以这类就显得不简洁；再看cola-statemachine相比就是小巧、无状态、简单、轻量、性能极高的状态机DSL实现，解决业务中的状态流转问题。

##### 阿里cola statemachine

github:
[https://github.com/alibaba/COLA/tree/master/cola-components/cola-component-statemachine](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Falibaba%2FCOLA%2Ftree%2Fmaster%2Fcola-components%2Fcola-component-statemachine)

参考博客：

https://blog.csdn.net/significantfrank/article/details/104996419

概念：
State：状态

Event：事件，状态由事件触发，引起变化

Transition：流转，表示从一个状态到另一个状态

External Transition：外部流转，两个不同状态之间的流转

Internal Transition：内部流转，同一个状态之间的流转

Condition：条件，表示是否允许到达某个状态

Action：动作，到达某个状态之后，可以做什么

StateMachine：状态机

外部过程描述：起始状态STATE1，结束状态STATE2，当发生EVENT1时执行状态转移，当满足checkCondition()时，执行doAction，执行成功则返回状态STATE2，否则返回STATE1。

```
<dependency>
   <groupId>com.alibaba.cola</groupId>
   <artifactId>cola-component-statemachine</artifactId>
   <version>4.0.1</version>
</dependency>
```