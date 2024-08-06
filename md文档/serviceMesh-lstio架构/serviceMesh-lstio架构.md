视频：https://www.bilibili.com/video/BV1Lf4y1x7j8?p=56&spm_id_from=pageDriver&vd_source=0ee4a5fcc4ed2246ba902aa714c4428b

CSDN文章（了解背景啥的）：https://blog.csdn.net/luanpeng825485697/article/details/84560659

### lstio架构

springcloud（注册中心，链路监控，熔断降级限流，高可用负载均衡），需要在业务代码里，加入maven依赖，配置等，微服务是多语言调用，维护成本高。非业务代码各个语言，维护成本高

docker->k8s容器的编排平台-> lstio增加k8s的服务治理功能，与业务代码解耦

sideCar: 负载，限流，服务发现，统一语言问题



![](asserts/lstio架构图.png)

### **lstio组件**

**Pilot**: 服务发现和路由规则

**Mixer**: 策略控制，如服务调用限速

**citadel**: 安全作用，保障服务与服务之间安全通信

**galley** : 配置中心

**sidecar-injector**: 负责自动注入的组件，开启后，创建pod时会自动调用。生成sidecar容器的描述，自动插入原pod配置信息中。在pod中除了业务容器，还有sidecar容器

**envoy**: 处理服务的流量，包含以下两个进程

pilot-agent：生成envoy配置；负责启动envoy进程；监控envoy状态，重启等

envoy：拦截pod流量；从控制平面polit获取配置和服务发现；上报数据给控制平面组件mixer

**ingress-gateway**: 网关，从网格外访问服务就通过这里

在 Kubernetes 中，Gateway API 和 Ingress 都是用于管理网络流量的工具

gateway是进阶，ingress初级

gateway API大概率也会成为主流

### lstio安装

#### 注入sidecar容器

每个pod需要注入sidecar容器

**手动注入**：加指令手动注入

**自动注入**：创建命名空间，设置命名空间下pod自动创建sidecar容器

sidecar-injector=enabled

#### **k8s组件回顾 :**

docker是启动容器的，一个node需要安装docker

一个node多个pod
一个pod代表一个基本的引用，而这个pod需要启动服务，就是启动容器，pod是一组容器的组合，所以需要docker

**deployment**是pod控制器

**service**是统一的ip入口，用于pod之间通信，因为每个pod  IP都可能扩容变化，所以需要service
Service: clusterIP类型：用于集群内访问；   nodePort：用于集群外访问集群
缺点：占用物理机的端口  ----》 演化出ingress 

node节点有单独的IP，node节点下每一个pod也都有单独的IP

微服务制作镜像推送到容器，通过启动pod   让docker 启动容器

K8s一般一主多从，新建pod会在负载较低的node新建pod

**Ingress** 配置域名和转发规则

### lstio实战

#### 监控功能

**prometheus** : 存储服务和监控数据，数据来自于mixer组件

**grafana**: 开源数据可视化工具，展示prometheus监控数据

#### 项目案例bookinfo

官网有

首先一个项目最好创建一个命名空间

##### 流量控制

版本:  所有的流量切换到某个版本

权重：百分之多少的流量到哪个版本

用户：A用户v1版本，B用户v2版本

##### 故障注入

配置文件配置，如果请求头不包含啥的，请求延迟2s等

##### 流量迁移-金丝雀发布

流量逐渐迁移到另一个版本

等v3版本稳定后，再吧100%的流量都迁移到v3

