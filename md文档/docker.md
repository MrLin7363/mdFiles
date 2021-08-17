docker -> docker compose -> k8s

#### 一、安装docker

```
1.yum包更新到最新
yum update
2.安装所需的软件包
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
3.设置稳定仓库
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
4.安装CE版本docker
yum install -y docker-ce
5.查看docker版本
docker -v
6.卸载
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```

#### 二、配置镜像加速器

阿里云控制台-镜像加速器

```
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://1y61n11d.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

#### 三、docker相关命令

```
systemctl start docker
systemctl stop docker
systemctl status docker
systemctl restart docker
systemctl enable docker  开启启动docker
```

#### 四、镜像相关命令

远程仓库，镜像，容器

查看镜像

docker images

搜索镜像

docker search redis

下载redis镜像

docker pull redis:3.2   :3.2不写默认拉取最新版本

**hub.docker.com** 可以查看对应镜像的版本号

删除镜像

docker rmi  image_id 

docker rmi redis:lasted 通过软件和版本号删除

docker images -q 查看所镜像ID

docekr rmi 'docker images -q'  删除所有镜像

#### 五、容器相关命令

**查看容器**

docker ps -a 查看所有容器

docker ps 查看正在运行的容器

**创建容器**

docker run -it --name=c1 centos:7 /bin/bash

centos:7 镜像和版本

-it 交互式容器

docker run -id --name=c2 centos:7 /bin/bash

-id 后台运行，exit退出不会关闭容器

**退出容器**

exit 

**进入容器**

docekr exec -it c2 /bin/bash

**启动**

docker start c2

**停止**

docker stop c2

**删除**

docker rm c2 容器ID/容器名称

docker rm 'docker ps -a' 删除所有容器

**查看**

docker inspect c2

#### 六、数据卷

外部机器 -> 宿主机(比如虚拟机) -> 容器

容器和外部机器不能交互，但可以通过宿主机交互

宿主机内部各个容器可以通过数据卷挂载进行数据交互

**配置数据卷**

目录必须是绝对路径

如果目录不存在，自动创建

**挂载单个目录**

```
docker run -id --name=volume-test -v ~/data1:/root/data_container1 centos:centos7 /bin/bash
```

宿主机这个文件下，创建的文件，容器这个目录也会同步。反过来也一样

**挂载多个数据卷**

```
docker run -id --name=volume-test -v ~/data1:/root/data_container1 -v  ~/data2:/root/data_container2 centos:centos7 /bin/bash
```

~只能在宿主机写，表示当前目录

**多个容器挂载同一个数据卷，实现数据同步**

#### 七、docker应用部署

##### MySQL部署

docker search mysql

docker pull mysql:8.0

cd / 

```
docker run -id --name=mysql -p 3307:3306    -v /root/mysql/conf:/etc/mysql/conf.d         -v /root/mysql/logs:/logs        -v /root/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 mysql:8
```

映射宿主机3307端口 到容器3306端口，外部通过3307连接

-v 挂载， 分别是配置目录，日志目录，数据目录

docker exec -it mysql /bin/bash

mysql -uroot -p

可以外部连接mysql通过宿主机的IP和端口 3307

mysql -h 1.14.195 -P3307 -uroot -p

##### Nginx部署

```
docker run -id --name nginx -p 80:80    -v $pwd/nginx/nginx.conf:/etc/nginx/nginx.conf   -v $pwd/nginx/logs:/var/log/nginx       -v $pwd/nginx/html:/usr/share/nginx/html   nginx
```

容器的路径是安装好nginx默认的目录，想要宿主机挂载一些配置文件就先写好nginx.conf等

分别 配置目录，日志目录

$PWD/nginx.conf  $PWD当前目录

http://1.14.195:80   welcome to nginx!

可以修改 index.html 然后换成自己的展示页面

##### Tomcat部署

```
docker run -id --name tomcat -p 8080:8080 -v /root/tomcat:/usr/local/tomcat/webapps tomcat /bin/bash
```

http://1.14.195:8080

直接访问什么都没有，自己创建个文件夹在根目录   test/index.html

http://1.14.195:8080/test/index.html

##### redis部署

```
docker run -id --name redis -p 6379:6379 redis /bin/bash
```

使用外部机器连接redis，进入redis 目录下

redis-cli.exe -h 1.14.195 -p 6379

#### 八、docker file

发布spring-boot项目

1.maven package 打包项目

2.java -jar  ....-snapshot.jar 运行jar包没问题

3.下载java 8 镜像   定义父镜像

FROM java:8

4.定义作者信息

MAINTAINER chenjunlin

5.将jar包添加到容器

ADD springboot.jar app.jar

6.定义容器启动执行的命令

CMD java -jar  app.jar

file里命名为app.jar

7.通过dockerfile 构建镜像

docker build -f dockerfile文件路径 -t  镜像名称：版本

vim springboot_dockerfile

```
FROM java:8
MAINTAINER chenjunlin
ADD  文件名称.jar  app.jar
CMD java -jar app.jar
```

建造一个容器

docker build -f ./springboot_dockerfile -t app .

.默认最新版本   app为容器名称

docker run -id -p 9000:8080 app

8080为springboot访问的端口

#### 九、服务编排-compose

##### 安装

每个微服务部署一个示例很麻烦

先安装完docker再安装compose

查看版本

docker-compose -version

##### 使用

如上面的app容器，需要nginx,mysql,redis容器

vim 

#### 十、docker常见问题

usr/bin/docker permission denied 

这个文件没有删除修改权限

1. a：让文件或目录仅供附加用途。
2. b：不更新文件或目录的最后存取时间。
3. c：将文件或目录压缩后存放。
4. d：将文件或目录排除在倾倒操作之外。
5. i：不得任意更动文件或目录。
6. s：保密性删除文件或目录。
7. S：即时更新文件或目录。
8. u：预防意外删除。

lsattr docker 查看文件属性

chattr -ia docker 取消文件属性限制

chmod 777 docker  赋予文件读写执行权限

docker就可访问

