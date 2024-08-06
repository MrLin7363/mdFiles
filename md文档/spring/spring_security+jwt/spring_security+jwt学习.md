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
package com.lin.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // 盐
    private static String secret = "secret";

    public static void main(String[] args) {
        final String token = createToken(10, "666");
        System.out.println("token = " + token);
        parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0U3ViamVjdCIsIm5hbWUiOiJsaW4iLCJpYXQiOjE2NjY2MTQzMDUsImp0aSI6IjEifQ.WaUkIK5QLh1dbchDCJlLEeOLd6xXWe0n-Hc5rP44ls4");
    }

    public static String createToken(int time, String id) {
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
            .setId(id)
            // JWT的主体 {"sub": "jwtSubject"}
            .setSubject("testSubject")
            // 签发时间
            .setIssuedAt(new Date());
        String token = jwt.compact();
        return token;
    }

    public static String parseToken(String token) {
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
            return body.getId();
        } catch (Exception e) {
            throw new RuntimeException("无效Token");
        }
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

（1）自定义登录接口替换 UsernamePasswordAuthenticationFilter（不会调用这个了）

​		调用ProviderManager的方法进行认证，认证通过生产JWT

​		将用户信息存入缓存 redis/localcache中

（2）自定义UserDetailsService 替换 InMemoryUserDetailsManager.loadUserByUsername()

​		查询数据库用户信息

##### 校验：

（1）定义jwt认证过滤器  extends OncePerRequestFilter

​		获取token

​		解析token获取其中的userid

​		从redis中获取用户信息

​		存入SecurityContextHolder

登录

```mermaid
sequenceDiagram
	用户端->>登录LoginService: 用户名密码
    登录LoginService->>+ProviderManager: authenticate方法认证
    ProviderManager->>+DaoAuthenticationProvider: authenticate方法认证
    DaoAuthenticationProvider->>+自定义UserDetailService: loadUserByUsername方法查询用户
    自定义UserDetailService->>自定义UserDetailService: 在内存/数据库查询用户权限信息
    自定义UserDetailService-->>-DaoAuthenticationProvider: 对比用户名和密码是否正确，检验
    DaoAuthenticationProvider-->>-登录LoginService: 返回Authentication对象, SecurityContextHolder.getContext()setAuthentication方法存储该对象
    登录LoginService->>用户端: 用户id生产一个jwt,userId作为key,用户信息作为value 存入缓存redis
    用户端->>缓存: 用户信息存入缓存
```

token请求校验： 自定义认证过滤器

```mermaid
sequenceDiagram
	用户端->>JWT过滤认证器: token请求
	JWT过滤认证器->>JWT过滤认证器: 1.获取token <br /> 2.解析token <br/> 3.获取token <br/> 4. 封装Authentication对象存入SecurityContextHolder 
	JWT过滤认证器->>用户端: 如果token不存在或无效，返回403；登录接口除外，不需要认证
    JWT过滤认证器->>+缓存redis: 获取用户信息
    JWT过滤认证器->>用户端: 如果缓存不存在用户信息，说明未登陆，返回未登录
    JWT过滤认证器->>JWT过滤认证器: 用户信息存在，存入SecurityContextHolder   构造函数 super.setAuthenticated(true)设置为已认证，后续的认证拦截通过
    JWT过滤认证器->>用户端: 登录成功
```

### 7.具体实现-认证

#### (1) 默认登录

http://localhost:8080/login

```
/**
 * 数据库查询的用户/或者缓存
 */
@Data
@AllArgsConstructor
public class User {

    private String userName;

    private String password;
}
```

```
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * security的用户  定义一个类，实现UserDetails接口，把用户信息（从数据库中查询得到的信息）封装在其中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    /**
     * @return 返回权限信息
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * 获取密码
     *
     * @return
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户名
     *
     * @return
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }

    /**
     * 判断是否没过期，true没过期，false过期了
     * 改为true
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 是否帐户未锁定,改为true
     *
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证未过期，改为true
     *
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用，改为true
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

```
import com.lin.security.domain.LoginUser;
import com.lin.security.domain.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义userDetailService
 */
@Service
public class UserServiceImpl implements UserDetailsService {
  /**
     * 返回的user会和用户输入的比较
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 默认的DelegatingPasswordEncoder加密器如果无加密算法需要{noop}1234  这是存到数据库的形式
        User user = new User("user", "$2a$10$at7tErX8BzwVSTx9XoD3sewjrqWQRr.fAgqFNugclfvH.hg.g5TXa");
        // 根据用户名查询数据库,这里默认不查数据库了
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 后续加密的账号密码的USER和 用户输入的明文的USER密码账号 比较
        return new LoginUser(user);
    }}
```

#### (2) 密码加密存储

There is no PasswordEncoder mapped for the id "null"

如果用户的密码直接是明文存储，需要在密码前加{noop}。

在实际项目中我们不会把明文密码存储到数据库中，默认使用的PasswordEncoder(DelegatingPasswordEncoder)加密，要求密码格式为：{id}password。它会根据id去判断密码的加密方式。但是我们一般不会采用这种方式。所以要替换PasswordEncoder。

我们一般使用SpringSecurity为我们提供的BCryptPasswordEncoder。我们只需要把BCryptPasswordEncoder对象注入到Spring容器中，SpringSecurity就会使用该PasswordEncoder来进行加密和校验

步骤：

创建SecurityConfig类继承WebSecurityConfigurerAdapter
将 PasswordEncoder注入到Spring容器中

```
package com.lin.security.config;

import com.lin.security.filter.JwtAuthticationTokenFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * security的一些类未注入spring需要手动注入
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthticationTokenFilter jwtAuthticationTokenFilter;

    /**
     * Secutity的密码加密对象
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //链式调用
        http
            //关闭csrf
            .csrf().disable()
            //不通过Session获取SecurityContext
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 对于登录接口 允许匿名访问，该接口不需要认证
            .antMatchers("/user/login").anonymous()
            // 除上面外的所有请求全部需要鉴权认证 就是需要加上token
            .anyRequest().authenticated();
        // 自定义过滤器在这个之前，并且自定义过滤器能够设置为已认证
        http.addFilterBefore(jwtAuthticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```

#### (3) 自定义登录接口

替换 UsernamePasswordAuthenticationFilter    调用ProviderManager的方法进行认证，认证通过生产JWT

自定义登录接口，然后让SpringSecurity对这个接口放行,让用户访问这个接口的时候不用登录也能访问

 在接口中我们通过AuthenticationManager的authenticate方法来进行用户认证,所以需要在SecurityConfig中配置把AuthenticationManager注入容器

 认证成功的话要生成一个jwt，放入响应中返回。并且为了让用户下回请求时能通过jwt识别出具体的是哪个用户，我们需要把用户信息存入redis，可以把用户id作为key

```
@RestController
@RequestMapping(value = "/user", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class UserController {
    //注入BCryptPasswordEncoder
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private LoginService loginService;

    /**
     * 自定义登录接口，仅对该接口放行
     *
     * @param user user
     * @return user
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseResult test(@RequestBody User user) {
        return loginService.login(user);
    }
}
```

```
package com.lin.security.service;

import com.lin.security.domain.LoginUser;
import com.lin.security.domain.ResponseResult;
import com.lin.security.domain.User;
import com.lin.security.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
LoginService->UserServiceImpl
 */
@Service
public class LoginService {
    // 假设的redis缓存
    public static volatile ConcurrentMap<String, LoginUser> userCathe = new ConcurrentHashMap();

    @Autowired
    private AuthenticationManager authenticationManager;

    public ResponseResult login(User user) {
        //Authentication 调用authenticatie方法进行认证
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());//用户登录时的用户名和密码
        // 这个authticate里面的LoginUser 是 UserServiceImpl 返回的那个
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        //如果没有通过，返回提示
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("登录失败");
        }

        //如果验证通过了，使用userid生成jwt,再将jwt存入ResponseResult返回
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        Long id = loginUser.getUser().getId();//获取用户id
        String jwt = JwtUtil.createToken(30, id.toString());//生成jwt

        //将完整的用户信息存入redis中，userid作为key
        userCathe.put("key:" + id, loginUser);

        //将token返回给前端
        HashMap<String, String> data = new HashMap<>();
        data.put("token", jwt);

        return new ResponseResult(200, "登录成功", data);
    }
}
```

```
@Data
@AllArgsConstructor
public class ResponseResult {
    private int code;

    private String msg;

    HashMap<String, String> data;
}
```

#### (4) 自定义认证过滤器

```
package com.lin.security.filter;

import com.lin.security.domain.LoginUser;
import com.lin.security.jwt.JwtUtil;
import com.lin.security.service.LoginService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义过滤器
 */
@Component
public class JwtAuthticationTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            // 放行,如果是登录接口/user/login 执行用户密码校验的逻辑，登录逻辑 -> LoginService->UserServiceImpl
            // 如果其他接口 则 403
            filterChain.doFilter(request, response);
            // 拦截结束还是会到这里，所以return
            return;
        }
        // 跳过用户密码校验，直接访问走这里
        // 解析token
        String id = JwtUtil.parseToken(token);
        // 从缓存中获取用户信息
        LoginUser loginUser = LoginService.userCathe.get("key:" + id);
        if (Objects.isNull(loginUser)) {
            throw new RuntimeException("用户未登陆");
        }
        // 存入SecurityContextHolder   构造函数 super.setAuthenticated(true)设置为已认证
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(loginUser,null,null);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        // 放行，否则拦截在这里了 -> LoginService->UserServiceImpl
        filterChain.doFilter(request, response);
    }
}
```

#### (5) 退出登录

```
    /**
     * 通过token去注销
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseResult logout() {
        return loginService.logout();
    }
```

```
@Service
public class LoginService {
	public ResponseResult logout() {
        // 获取SecurityContextHolder中的用户信息,因为会经过过滤器，注销接口实际已经设置了SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authenticationToken.getPrincipal();

        // 缓存中删除用户信息
        userCathe.remove("key:" + loginUser.getUser().getId());

        return new ResponseResult(200, "注销成功", null);
    }
}
```

### 8. 授权

不同的用户只能看到不同信息， 如果只是前端判断，如果别人知道接口地址可以直接访问，所以后台也进行接口判断，用户具有某些权限才能访问

##### 授权基本流程

springSecurity 默认在 FilterSecurityInteceptor中认证权限，从SecurityContextHolder中获取用户权限信息



###### （1）UserDetailsService 查询用户权限信息，封装到token中

```
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 默认的DelegatingPasswordEncoder加密器如果无加密算法需要{noop}1234
    User user = new User(1L, "user", "$2a$10$at7tErX8BzwVSTx9XoD3sewjrqWQRr.fAgqFNugclfvH.hg.g5TXa");
    // 根据用户名查询数据库,这里默认不查数据库了
    if (user == null) {
        throw new RuntimeException("用户名或密码错误");
    }
    // 根据用户名查询权限信息，设置到LoginUser中
    List<String> rolesList = new ArrayList<>(Arrays.asList("nromal", "admin"));
    // 后续加密的账号密码的USER和 用户输入的明文的USER密码账号 比较
    return new LoginUser(user, rolesList);
}
```

```
public class LoginUser implements UserDetails {

    private User user;

    private List<String> permissions;

    // 只用转换一次,同时不用再序列化到redis流中(安全考虑啥的)
    @JSONField(serialize = false)
    private List<SimpleGrantedAuthority> authorities;

    public LoginUser(User user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /**
     * @return 返回权限信息
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 把permissions中String信息封装成SimpleGrantedAuthority对象
        if (authorities == null) {
            authorities = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        return authorities;
    }
}
```

###### （2）tokenFilter中解析权限信息，设置到SecurityContextHolder中

```
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(loginUser,null, loginUser.getAuthorities());
```

###### （3）通过注解设置接口权限

Application  添加 @EnableGlobalAuthentication

controller方法上添加@PreAuthorize

```
    // spring读到注解的时候把这个字符串当作一个表达式,当作方法去执行
    // 'test'是什么权限能访问的意思 会去比较LoginUser的SimpleGrantedAuthority
    @PreAuthorize("hasAnyAuthority('test','nromal')")
    public ResponseResult password() {
        return new ResponseResult(200, "访问成功", new HashMap<>());
    }
```

另一种设置权限方式（一般用于静态资源）：基于配置的权限控制

 我们也可以在配置类中使用使用配置的方式对资源进行权限控制。

.authorizeRequests()

.antMatchers(“接口访问过路径”).hasAuthority(“权限”)

###### （4）FilterSecurityInteceptor根据权限认证过滤



##### RDBC权限模型（Role-Based Access Control）

五张核心表组成该模型

**user表**   一对多    menu权限表

|      | user_id | username....等 |
| ---- | ------- | -------------- |
| 1    | 1       |                |
| 2    | 2       |                |

 **menu权限(菜单)表**

| menu_id | 权限名       | perm_key权限关键字 |
| ------- | ------------ | ------------------ |
| 1       | 删除图书     | sys: book:delete   |
| 2       | 查看图书列表 | sys: book:list     |

**role角色表**    

| role_id | 角色名       | role_key |
| ------- | ------------ | -------- |
| 1       | 删除图书     | admin    |
| 2       | 查看图书列表 | nromal   |

**角色和权限关联表**  多对多

| role_id | menu_id |
| ------- | ------- |
| 1       | 1       |

**用户角色管线表**   多对多

| role_id | user_id |
| ------- | ------- |
| 1       | 1       |

```sql
USE `sg_security`;

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(64) NOT NULL DEFAULT 'NULL' COMMENT '菜单名',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` int(11) DEFAULT '0' COMMENT '是否删除（0未删除 1已删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `role_key` varchar(100) DEFAULT NULL COMMENT '角色权限字符串',
  `status` char(1) DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
  `del_flag` int(1) DEFAULT '0' COMMENT 'del_flag',
  `create_by` bigint(200) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint(200) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `role_id` bigint(200) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `menu_id` bigint(200) NOT NULL DEFAULT '0' COMMENT '菜单id',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(64) NOT NULL DEFAULT 'NULL' COMMENT '用户名',
  `nick_name` varchar(64) NOT NULL DEFAULT 'NULL' COMMENT '昵称',
  `password` varchar(64) NOT NULL DEFAULT 'NULL' COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `phonenumber` varchar(32) DEFAULT NULL COMMENT '手机号',
  `sex` char(1) DEFAULT NULL COMMENT '用户性别（0男，1女，2未知）',
  `avatar` varchar(128) DEFAULT NULL COMMENT '头像',
  `user_type` char(1) NOT NULL DEFAULT '1' COMMENT '用户类型（0管理员，1普通用户）',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人的用户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int(11) DEFAULT '0' COMMENT '删除标志（0代表未删除，1代表已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint(200) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `role_id` bigint(200) NOT NULL DEFAULT '0' COMMENT '角色id',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

```
    <select id="selectPermsByUserId" resultType="java.lang.String">
        select distinct perms
        from sys_user_role as sur
                 left join sys_role as sr on sur.role_id = sr.id
                 left join sys_role_menu as srm on sur.role_id = srm.role_id
                 left join sys_menu as sm on srm.menu_id = sm.id
        where sur.user_id = #{userId}
          and sr.`status` = 0
          and sm.`status` = 0
    </select>
```

### 9.异常处理-自定义

在SpringSecurity中，如果我们在认证或者授权的过程中出现了异常会被ExceptionTranslationFilter捕获到。在ExceptionTranslationFilter中会去判断是认证失败还是授权失败出现的异常。

 如果是**认证**过程中出现的异常会被封装成AuthenticationException然后调用**AuthenticationEntryPoint**对象的方法去进行异常处理。

 如果是**授权**过程中出现的异常会被封装成AccessDeniedException然后调用**AccessDeniedHandler**对象的方法去进行异常处理。

 所以如果我们需要自定义异常处理，我们只需要自定义AuthenticationEntryPoint和AccessDeniedHandler然后配置给SpringSecurity即可。

#### （1）认证失败处理

```
/**
 * 认证失败异常处理 JwtAuthticationTokenFilter或者登陆认证
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
        AuthenticationException e) throws IOException, ServletException {
        ResponseResult responseResult = new ResponseResult(HttpStatus.UNAUTHORIZED.value(), "认证失败，请重新登录", null);
        String s = JSON.toJSONString(responseResult);
        WebUtils.renderString(httpServletResponse, s);
    }
}
```

401认证失败

#### （2）授权 ，权限不足处理

403权限不足失败

```
/**
 * 授权失败，403 针对用户权限
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
        AccessDeniedException e) throws IOException, ServletException {
        ResponseResult responseResult = new ResponseResult(HttpStatus.FORBIDDEN.value(), "权限不足", null);
        String data = JSON.toJSONString(responseResult);
        WebUtils.renderString(httpServletResponse, data);
    }
}
```

```
    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

   // 自定义异常处理器
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler);
```

### 10. 跨域

 浏览器出于安全的考虑，使用 XMLHttpRequest对象发起 HTTP请求时必须遵守同源策略，否则就是跨域的HTTP请求，默认情况下是被禁止的。 同源策略要求源相同才能正常进行通信，即协议、域名、端口号都完全一致。

 前后端分离项目，前端项目和后端项目一般都不是同源的，所以肯定会存在跨域请求的问题。

 所以我们就要处理一下，让前端能进行跨域请求。

**spring-boot配置**

```
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
            // 设置允许跨域请求的域名
            .allowedOrigins("*")
            // 是否允许cookie
            .allowCredentials(true)
            // 设置允许的请求方式
            .allowedMethods("GET", "POST", "DELETE", "PUT")
            // 设置允许的header属性
            .allowedHeaders("*")
            // 跨域允许时间
            .maxAge(3600);
    }
}
```

**spring-security配置**

```
        http
            // 允许跨域
            .cors().and()
```

## 三、spring-security遗留问题

### 1. 权限校验方法

看源码，一般自定义权限校验方法

```
    @PreAuthorize("hasAnyAuthority('admin','normal')")
//    @PreAuthorize("hasAuthority('admin')")
//    @PreAuthorize("hasRole('admin')")  会比较 ROLE_admin 少用
```

### 2. 自定义权限校验方法

#### (1) 实现校验方法

```
/**
 * 重写自定义权限检验方法 SecurityExpressionRoot
 */
@Component
public class MyExpressionRoot {

    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return loginUser.getPermissions().contains(authority);
    }
}
```

```
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @PreAuthorize("@myExpressionRoot.hasAuthority('nromal')")
    public ResponseResult hello() {
        return new ResponseResult(200, "访问成功", new HashMap<>());
    }
```

#### (2) spel表达式

spel https://blog.csdn.net/weixin_42645678/article/details/125414902

自定义注解 https://blog.csdn.net/qq_27304827/article/details/126137980

### 3. 基于配置的权限控制

```
 http
            .authorizeRequests()
            // 基于配置的权限控制，这个接口需要admin权限才能访问
            .antMatchers("/config").hasAuthority("nromal")
```

### 4. csrf

 SpringSecurity去防止CSRF攻击的方式就是通过csrf_token。后端会生成一个csrf_token，前端发起请求的时候需要携带这个csrf_token,后端会有过滤器进行校验，如果没有携带或者是伪造的就不允许访问。

 我们可以发现CSRF攻击依靠的是cookie中所携带的认证信息。但是在前后端分离的项目中我们的认证信息其实是token，而token并不是存储中cookie中，并且需要前端代码去把token设置到请求头中才可以（而攻击者的网站拿不到这个前端服务器中的token只能拿到cookie所以能防御），所以CSRF攻击也就不用担心了。所以要关闭csrf不然会携带csrf_token
```
http
      // 允许跨域
      .cors().and()
      //关闭csrf没必要打开，因为前后端分离的项目就是现在访问要带的token,天然是抵抗csrf的。如果不关闭会去检验csrf_token，这里请求头没携带
      .csrf().disable()
```

### 5. 认证成功处理器-不推荐

实际上在UsernamePasswordAuthenticationFilter进行登录认证的时候，如果登录成功了是会调用AuthenticationSuccessHandler的方法进行认证成功后的处理的。AuthenticationSuccessHandler就是登录成功处理器。

 我们也可以自己去自定义成功处理器进行成功后的相应处理。

上面的例子  UsernamePasswordAuthenticationFilter已经不调用了

```
@Component
public class SGSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("认证成功了");
    }
}
```

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().successHandler(successHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```

### 6.认证失败处理器-不推荐

 实际上在UsernamePasswordAuthenticationFilter进行登录认证的时候，如果认证失败了是会调用AuthenticationFailureHandler的方法进行认证失败后的处理的。AuthenticationFailureHandler就是登录失败处理器。

 我们也可以自己去自定义失败处理器进行失败后的相应处理。

```
@Component
public class SGFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.out.println("认证失败了");
    }
}
```

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
//                配置认证成功处理器
                .successHandler(successHandler)
//                配置认证失败处理器
                .failureHandler(failureHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```

### 7.注销成功处理器-不推荐

```
@Component
public class SGLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("注销成功");
    }
}
```

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
//                配置认证成功处理器
                .successHandler(successHandler)
//                配置认证失败处理器
                .failureHandler(failureHandler);

        http.logout()
                //配置注销成功处理器
                .logoutSuccessHandler(logoutSuccessHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```



