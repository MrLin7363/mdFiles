# **springsecurity+jwt学习**

## 一、JWT

https://blog.csdn.net/qq_43380361/article/details/106117483

Json web token (JWT), 是为了在网络应用环境间传递声明而执行的一种基于JSON的开放标准.该token被设计为紧凑且安全的，特别适用于分布式站点的单点登录（SSO）场景。JWT的声明一般被用来在身份提供者和服务提供者间传递被认证的用户身份信息，以便于从资源服务器获取资源，也可以增加一些额外的其它业务逻辑所必须的声明信息，该token也可直接被用于认证，也可被加密。

### 1. jwt结构

JWT长什么样？
JWT是由三段信息构成的，将这三段信息文本用.链接一起就构成了Jwt字符串。就像这样:

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

JWT的构成
第一部分我们称它为***头部***（header),第二部分我们称其为***载荷***（payload, 类似于飞机上承载的物品)，第三部分是
签证（signature).

#### *header*

jwt的头部承载两部分信息：

声明类型，这里是jwt
声明[加密](https://so.csdn.net/so/search?q=加密&spm=1001.2101.3001.7020)的算法 通常直接使用 HMAC SHA256
完整的头部就像下面这样的JSON：

```
{
  'typ': 'JWT',
  'alg': 'HS256'
}
```

然后将头部进行base64加密（该加密是可以对称解密的),构成了第一部分.

eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9

#### playload

载荷就是存放有效信息的地方。这个名字像是特指飞机上承载的货品，这些有效信息包含三个部分

标准中注册的声明
公共的声明
私有的声明

标准中注册的声明 (建议但不强制使用) ：
iss: jwt签发者
sub: jwt所面向的用户
aud: 接收jwt的一方
exp: jwt的过期时间，这个过期时间必须要大于签发时间
nbf: 定义在什么时间之前，该jwt都是不可用的.
iat: jwt的签发时间
jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击。

公共的声明 ： 公共的声明可以添加任何的信息，一般添加用户的相关信息或其他业务需要的必要信息.但不建议添加敏感信息，因为该部分在客户端可解密.

私有的声明 ： 私有声明是提供者和消费者所共同定义的声明，一般不建议存放敏感信息，因为base64是对称解密的，意味着该部分信息可以归类为明文信息。

定义一个payload:
```
{
  "sub": "1234567890",
  "name": "John Doe",
  "admin": true
}
```

然后将其进行base64加密，得到JWT的第二部分。

eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9
signature

JWT的第三部分是一个签证信息，这个签证信息由三部分组成：

header (base64后的)
payload (base64后的)

#### secret

这个部分需要base64加密后的header和base64加密后的payload使用.连接组成的字符串，然后通过header中声明的加密方式进行加盐secret组合加密，然后就构成了jwt的第三部分。

```
1.var encodedString = base64UrlEncode(header) + '.' + base64UrlEncode(payload);

2.var signature = HMACSHA256(encodedString, 'secret'); // TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ
```

将这三部分用.连接成一个完整的字符串,构成了最终的jwt:

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

注意：secret是保存在服务器端的，jwt的签发生成也是在服务器端的，secret就是用来进行jwt的签发和jwt的验证，所以，它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。

如何应用
一般是在请求头里加入Authorization，并加上Bearer标注：

```
fetch('api/user/1', {
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
```

### 2. java代码

```
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    // 盐
    private static String secret = "secret";

    public static void main(String[] args) {
        final String token = createToken(10);
        System.out.println("token = " + token);
        parseToken(token);
    }

    private static String createToken(int time) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "lin");
        JwtBuilder jwt = Jwts.builder()
            // 设置头
            .setHeaderParam("typ", "JWT")
            .setHeaderParam("alg", "HS256")
            // 设置签名算法和加盐秘钥
            .signWith(SignatureAlgorithm.HS256, secret)
            .setExpiration(new Date(new Date().getTime() + time * 1000))
            // 自定义内容payload 接受一个map
            .setClaims(map)
            // 唯一id
            .setId("123")
            // JWT的主体 {"sub": "jwtSubject"}
            .setSubject("testSubject")
            // 签发时间
            .setIssuedAt(new Date());
        String token = jwt.compact();
        return token;
    }

    private static void parseToken(String token){
        System.out.println("====================开始解析JWT====================");

        System.out.println("token --> " + token);
        try {
            Claims body = Jwts.parser()
                // 签名秘钥
                .setSigningKey(secret)
                // 要解析的jwt
                .parseClaimsJws(token)
                .getBody();

            System.out.println("id --> " + body.getId());
            System.out.println("sub --> " + body.getSubject());
            System.out.println("自定义内容 name --> " + body.get("name"));
            System.out.println("iat 创建时间 --> " + body.getIssuedAt());
            Date expiration = body.getExpiration();
            System.out.print("过期时间 --> ");
            System.out.println(expiration);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("无效Token");
        }
        System.out.println("====================JWT解析结束====================");
    }
}
```

### 3、zuul网关实例总结-类似spring-security

zuul网关服务

1.继承 IZuulFilter，实现自定义的是否需要过滤，过滤就执行run()

```
package com.netflix.zuul;

public interface IZuulFilter {
    boolean shouldFilter();

    Object run();
}
```

run()里执行各种认证方式

如 JWT认证， cookies认证

### 4、JWT结合springboot项目

1.JWTValidationInterceptor extends HandlerInterceptorAdapter

执行了 jwt认证逻辑

2.SimpleJWTInterceptor implements ClientHttpRequestInterceptor

在restemplate调内部系统时中默认加上jwt的认证信息，不然内部系统间的调用不通

## 二、spring-security

### 1. 链接

官方文档 ：   https://spring.io/projects/spring-security#learn

B站视频： https://www.bilibili.com/video/BV1mm4y1X7Hc/?spm_id_from=333.337.search-card.all.click

参考文章： https://blog.csdn.net/GJS200010/article/details/124638546

### 2.介绍

认证：验证用户是不是当前系统的用户，并确认具体是哪个用户 

授权：判断当前用户能进行什么权限的操作

### 3.环境搭建

spring boot 版本 2.3.6.RELEASE

security 版本  5.3.5.RELEASE

```
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
```

项目引入pom依赖后，默认访问controller会跳转登录页面(可自定义换掉)

用户名： user

密码 - spring启动时日志里有 : Using generated security password: 72c2917d-7262-4662-adca-c7d6ff3f2503

### 4.常规项目登录校验流程

```mermaid
sequenceDiagram
    前端->>服务器: 1.携带用户密码访问登录端口
    服务器->>服务器: 2.数据库校验账号密码，使用获取userID生产一个JWT
    服务器->>前端: 3.jwt响应给前端
    前端->>服务器: 4.登陆后访问请求在请求头中携带token
    服务器->>服务器: 5.获取token进行解析，获取userID，进行权限校验，返回响应资源给前端
    服务器-->>前端: 6.响应response
```

### 5.spring-security 原理

springsecurity 原理就是一个过滤器链路，内部包含各种功能的过滤器

```mermaid
graph LR; 
	客户端-->UsernamePasswordAuthenticationFilter;
    UsernamePasswordAuthenticationFilter-->ExceptionTranslationFilter;
    ExceptionTranslationFilter-->FilterSecurityInteceptor;
    FilterSecurityInteceptor-->API;
    API-->FilterSecurityInteceptor;
    FilterSecurityInteceptor-->ExceptionTranslationFilter;
    ExceptionTranslationFilter-->UsernamePasswordAuthenticationFilter;
    UsernamePasswordAuthenticationFilter-->客户端;
```

|                Filter                |                             作用                             |
| :----------------------------------: | :----------------------------------------------------------: |
| UsernamePasswordAuthenticationFilter |      负责处理我们在登陆页面填写了用户名密码后的登陆请求      |
|      ExceptionTranslationFilter      | 处理过滤器链中抛出的任何AccessDeniedException和AuthenticationException |
|       FilterSecurityInteceptor       |                     负责权限校验的过滤器                     |

总共15个过滤器，上面三个是主要的

##### (1)debug技巧查看15个过滤器

启动类第二行debug，SpringApplication.run其实返回值其实是个容器

```
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("Application start success!");
```

debug后 alt+f8  evaluate expression 可以获取当前对象，且能实现对象的方法获取返回值

```
context.getBean(DefaultSecurityFilterChain.class)
```

注意：低版本不是spring bean所以获取为空对象，直接debug  DefaultSecurityFilterChain 的初始化能看到15个filter

或者项目启动时 打印了日志：  Creating filter chain：

```
0 = {WebAsyncManagerIntegrationFilter@6047} 
1 = {SecurityContextPersistenceFilter@6048} 
2 = {HeaderWriterFilter@6049} 
3 = {CsrfFilter@6050} 
4 = {LogoutFilter@6051} 
5 = {UsernamePasswordAuthenticationFilter@6052} 
6 = {DefaultLoginPageGeneratingFilter@6053} 
7 = {DefaultLogoutPageGeneratingFilter@6054} 
8 = {BasicAuthenticationFilter@6055} 
9 = {RequestCacheAwareFilter@6056} 
10 = {SecurityContextHolderAwareRequestFilter@6057} 
11 = {AnonymousAuthenticationFilter@6058} 
12 = {SessionManagementFilter@6059} 
13 = {ExceptionTranslationFilter@6060} 
14 = {FilterSecurityInterceptor@6061} 
```

UsernamePasswordAuthenticationFilter源码认证流程

UsernamePasswordAuthenticationFilter    类继承AbstractAuthenticationProcessingFilter类

ProviderManager   实现AuthenticationManager接口

DaoAuthenticationProvider    实现AbstractUserDetailsAuthenticationProvider类

inMemoryUserDetailsManager    实现UserDetailsService接口

```mermaid
sequenceDiagram
	用户端->>UsernamePasswordAuthenticationFilter: 用户名密码
	UsernamePasswordAuthenticationFilter->>UsernamePasswordAuthenticationFilter: 用户名密码
    UsernamePasswordAuthenticationFilter->>+ProviderManager: authenticate方法认证
    ProviderManager->>+DaoAuthenticationProvider: authenticate方法认证
    DaoAuthenticationProvider->>+inMemoryUserDetailsManager: loadUserByUsername方法查询用户
    inMemoryUserDetailsManager->>inMemoryUserDetailsManager: 在内存中根据用户名查询用户对于的权限信息loadUserByUsername，封装userDetails对象返回
    inMemoryUserDetailsManager-->>-DaoAuthenticationProvider: 对比用户名和密码是否正确，讲userDetails权限信息设置到Authentication对象中
    DaoAuthenticationProvider-->>-UsernamePasswordAuthenticationFilter: 返回Authentication对象, SecurityContextHolder.getContext()setAuthentication方法存储该对象
```

### 6.spring-security项目登录完整流程

##### 登录：

（1）自定义登录接口替换 UsernamePasswordAuthenticationFilter    

​		调用ProviderManager的方法进行认证，认证通过生产JWT

​		将用户信息存入缓存 redis/localcache中

（2）自定义UserDetailsService 替换

​		查询数据库用户信息

##### 校验：

（1）定义jwt认证过滤器

​		获取token

​		解析token获取其中的userid

​		从redis中获取用户信息

​		存入SecurityContextHolder

登录

```mermaid
sequenceDiagram
	用户端->>登录接口Controller: 用户名密码
    登录接口Controller->>+ProviderManager: authenticate方法认证
    ProviderManager->>+DaoAuthenticationProvider: authenticate方法认证
    DaoAuthenticationProvider->>+自定义UserDetailService: loadUserByUsername方法查询用户
    自定义UserDetailService->>自定义UserDetailService: 在内存/数据库查询用户权限信息
    自定义UserDetailService-->>-DaoAuthenticationProvider: 对比用户名和密码是否正确，检验
    DaoAuthenticationProvider-->>-登录接口Controller: 返回Authentication对象, SecurityContextHolder.getContext()setAuthentication方法存储该对象
    登录接口Controller->>用户端: 用户id生产一个jwt,userId作为key,用户信息作为value 存入缓存redis
    用户端->>缓存: 用户信息存入缓存
```

token请求校验： 自定义认证过滤器

```mermaid
sequenceDiagram
	用户端->>JWT过滤认证器: token请求
	JWT过滤认证器->>JWT过滤认证器: 1.获取token <br /> 2.解析token <br/> 3.获取token <br/> 4. 封装Authentication对象存入SecurityContextHolder
    JWT过滤认证器->>+缓存redis: 获取用户信息
    JWT过滤认证器->>其他过滤器: 如果缓存不存在用户信息，走原始流程
```

### 7.具体实现





