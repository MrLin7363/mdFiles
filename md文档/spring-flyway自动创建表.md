```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ums_bd1?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  flyway:
    baselineOnMigrate: false   # 是否启动项目时创建表创建

```

![image-20210413174616245](C:\Users\cool\AppData\Roaming\Typora\typora-user-images\image-20210413174616245.png)

```
  <dependency>
  	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-core</artifactId>
  </dependency>
```