**Jasypt加密指南**

### 默认组件

```
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>2.1.2</version>
</dependency>
```

```
    @Test
    public void encrypt() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("abc"); // 加密所需要的盐，后面通过盐来解密
//        String enc = encryptor.encrypt("123456");
//        System.out.println(enc);
        String res = encryptor.decrypt("Pfyeg6EQnvDffZX2olwRKA==");
        System.out.println(res);
    }
```

配置文件设置密文，启动应用时解密 = 123456

```  
spring:
  application:
    name: service-provider
  datasource: 
    driver-class-name: com.mysql.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/mydb?autoReconnect=true&failOverReadOnly=false&createDatabaseIfNotExist=true&useSSL=false&useUnicode=true&characterEncoding=utf8
    username: root
    password: ENC(Pfyeg6EQnvDffZX2olwRKA==)
```

以下方式二选一设置盐去解密

配置文件-不安全

```
jasypt:
  encryptor:
    password: abc # 盐
```

启动项目的VM参数  VM-options:

```text
-Djasypt.encryptor.password=abc
```

### 自定义

```
jasypt:
  encryptor:
    bean: stringEncryptor
    property:
      prefix: "TTT(" #自定义前缀
      suffix: ")"
```

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/d_marketing?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    username: root
    password: TTT(k0qvoQ94kJQikqWjCRUzNg==://+EEnPI/UO7lMD7jUuxl8Qq5laKqakpj9s4xq7dnHJvY6vMhqmRFjbx)
```

```
@Configuration
@Component("stringEncryptor")
@Slf4j
public class KmsStringEncryptor implements StringEncryptor {

 	// 自定义加解密算法
    @Override
    public String encrypt(String s) {
        return "test";
    }

    @Override
    public String decrypt(String s) {   
        return "123456";
    }

}
```

```
Jasypt 配置详解
1、Jasypt 默认使用 StringEncryptor 解密属性，如果在 Spring 上下文中找不到自定义的 StringEncryptor，则使用如下默认值：

配置属性	是否必填项	默认值
jasypt.encryptor.password	True	-
jasypt.encryptor.algorithm	False	PBEWITHHMACSHA512ANDAES_256
jasypt.encryptor.key-obtention-iterations	False	1000
jasypt.encryptor.pool-size	False	1
jasypt.encryptor.provider-name	False	SunJCE
jasypt.encryptor.provider-class-name	False	null
jasypt.encryptor.salt-generator-classname	False	org.jasypt.salt.RandomSaltGenerator
jasypt.encryptor.iv-generator-classname	False	org.jasypt.iv.RandomIvGenerator
jasypt.encryptor.string-output-type	False	base64
jasypt.encryptor.proxy-property-sources	False	false
jasypt.encryptor.skip-property-sources	False	empty list
2、唯一需要的属性是 jasypt.encryptor.password ，其余的可以使用默认值。虽然所有这些属性都可以在属性文件中声明，但为了安全 password 属性官方不推荐存储在属性文件中，而应作为系统属性、命令行参数或环境变量传递。
```

