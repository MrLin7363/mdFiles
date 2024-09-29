## 一、zuul网关

github官网：https://github.com/Netflix/zuul

spring cloud netfix体系中使用文档，结合hystrix，ribbon,erueka：https://cloud.spring.io/spring-cloud-netflix/multi/multi__router_and_filter_zuul

单独的使用指南：https://www.baeldung.com/spring-rest-with-zuul-proxy

### 1. 单独使用指南

不结合hystrix，ribbon,erueka

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
    <version>2.2.10.RELEASE</version>
</dependency>
```

yaml

```
zuul:
  sensitive-headers: Access-Control-Allow-Origin
  ignored-headers: Access-Control-Allow-Origin
  host:
    connect-timeout-millis: 2000
    socket-timeout-millis: 300000
  max:
    host:
      connections: 500
  routes:
    project:
      path: /user/**         #   命中的uil
      url: http://..../..../api/      # 转发真实路径
```

过滤器

```
public class AddResponseHeaderFilter extends ZuulFilter {
	@Override
	public String filterType() {
		return "pre"; // 随便填
	}

	@Override
	public int filterOrder() {
		return 0; // 如果多个过滤器，按这个大小进行顺序过滤
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	// 过滤可以做的操作
	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
    	HttpServletResponse servletResponse = context.getResponse();
		servletResponse.addHeader("X-Sample", UUID.randomUUID().toString());
		return null;
	}
}
```

