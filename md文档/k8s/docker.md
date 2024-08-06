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

docker run  --name=c1  -it centos:7 /bin/bash

centos:7 镜像和版本

- **-i:** 以交互模式运行容器，通常与 -t 同时使用；  -i  必须加 /bin/bash
- **-t:** 为容器重新分配一个伪输入终端，通常与 -i 同时使用；
- **-d:** 后台运行容器，并返回容器ID；

docker run  --name=c2  -id centos:7 /bin/bash

-id 后台运行，exit退出不会关闭容器

**退出容器**

exit;   exit是退出xshell 注意

**进入容器**

docekr exec -it c2 /bin/bash

**启动**

docker start c2

**停止**

docker stop c2

**删除**

docker rm  c2 容器ID/容器名称

docker rm -f c2 容器ID/容器名称   删除正在运行的容器

docker rm 'docker ps -a' 删除所有容器

**查看**

docker inspect c2

#### 六、数据卷

外部机器 -> 宿主机(比如虚拟机) -> 容器

容器和外部机器不能交互，但可以通过宿主机交互

宿主机内部各个容器可以通过数据卷挂载进行数据交互

-i /bin/bash 必须加，交互式容器 

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
docker run --name=mysql -p 3307:3306    -v /root/mysql/conf:/etc/mysql/conf.d         -v /root/mysql/logs:/logs        -v /root/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456  -id mysql:8 /bin/bash
```

映射宿主机3307端口 到容器3306端口，外部通过3307连接

-v 挂载， 分别是配置目录，日志目录，数据目录

docker exec -it mysql /bin/bash

mysql -uroot -p

可以外部连接mysql通过宿主机的IP和端口 3307

mysql -h 1.14.195 -P3307 -uroot -p

##### Nginx部署

/bin/bash 一般和 -i 配合使用，不然容器已创建就exit

```
单独的nginx   注意最后不要加 /bin/bash  会把端口映射关闭，因为/bin/bash就是80端口
docker run --name nginx2 -p 8080:80 -id nginx
或者 docker run --name nginx2 -p 8080:80 -d nginx
curl localhost:8080  即可访问
docker run --name nginx25 -p 8027:80 -id nginx /bin/bash
docker run --name nginx26 -p 8028:80 -itd nginx /bin/bash
访问不了80端口
```

```
docker run  --name=nginx19 -p 8099:80    -v /root/nginx/conf/nginx.conf:/etc/nginx/nginx.conf   -v /root/nginx/logs:/var/log/nginx       -v /root/nginx/html:/usr/share/nginx/html -v /root/nginx/conf/conf.d:/etc/nginx/conf.d  -id nginx /bin/bash 
```

docker rm $(docker ps -a -q)  一次删除所有停止的容器。

kill -9 

容器的路径是**安装好nginx默认的目录**，想要宿主机挂载一些配置文件就先写好nginx.conf,default.conf等

nginx.conf 拷贝原始nginx的结构

分别 配置目录，日志目录

/bin/bash 不能丢

$PWD/nginx.conf  $PWD当前目录

http://1.14.195:80/ welcome to nginx!

可以修改 index.html 然后换成自己的展示页面

##### Tomcat部署

```
docker run --name tomcat -p 8080:8080 -v /root/tomcat:/usr/local/tomcat/webapps tomcat  -id /bin/bash
```

http://1.14.195:8080

直接访问什么都没有，自己创建个文件夹在根目录   test/index.html

http://1.14.195:8080/test/index.html

##### redis部署

```
docker run --name redis -p 6379:6379 -id redis /bin/bash
```

使用外部机器连接redis，进入redis 目录下

redis-cli.exe -h 1.14.195 -p 6379

#### 八、docker file-构建镜像

##### 1.发布spring-boot项目

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

##### 2.eruekaspring-boot项目安装

```
maven package
分别打包eureka-server   product-server 
java -jar eureka-server.jar
java-jar product-server.jar
和在idea 启动一样效果
```

2.jar上传到服务器-sftp

```
sftp:/root/software/docker> put eureka_server-1.0-SNAPSHOT.jar
sftp:/root/software/docker> put product_server-1.0-SNAPSHOT.jar
```

3.docker_file

root/software/docker_file_eureka

```
FROM java:8
MAINTAINER chenjunlin
ADD eureka_server-1.0-SNAPSHOT.jar  start.jar
CMD java -jar start.jar
```

4.构建镜像  

eruka_app 镜像名称

```
docker build -f ./docker_file_eureka -t  eureka_app .
```

-f路径名
. 上下文路径，将文件目录下打包给镜像，当前目录不易过多文件

5.运行容器

eureka项目访问是 8002端口，容器映射出来也8000，不过xshell连接服务器就是8000端口冲突换一个

```
docker run -id --name=eureka_server -p 8002:8000 eureka_app
```

启动后约2分钟部署完

http://1.14.195.48:8000/   访问到eruka服务

curl http://1.14.195.48:8002/  直接在容器上访问也行

6.启动product-server 容器，目前这个项目依赖mysql，所以单独启动运行不起来

一样的流程，查看是否注册

```
docker run -id --name=procut_server -p 9011:9011 product_app
```

```
http://1.14.195.48:9011/product/1
```

##### 3.构建自定义的centos容器

因为构建的容器一般没有 vim 编辑器，如nginx要在挂载目录写index.html

```
FROM centos:7
MAINTAINER chenjunlin
RUN yum install -y vim
WORKDIR /usr
CMD /bin/bash
```

#### 九、服务编排-compose

##### 安装

每个微服务部署一个示例很麻烦

先安装完docker再安装compose

查看版本

docker-compose -version

##### 编排nginx+spring-boot项目

如上面的app容器，需要nginx,mysql,redis容器

此处以 eruka_server + nginx 为例

1.创建docker-compose 目录

2.编写docker-compose.yml文件

```
version: '1'
services:
  nginx:
    image: nginx
    ports:
      - 80:80
    links:
      - eureka_app
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
  eureka_app:
    image: eureka_app
    expose:
      - "8080"
```

在conf.d目录下编写  nginx.conf  加上

```
 server {
        listen       80;
        server_name  nginx;
        
        access_log  off;

        location / {
            proxy_pass  http://eruka_app:8080;
        }
```



#### 十、docker私有仓库

##### 1.私有仓库搭建

**拉取仓库镜像**

docker pull registry

**启动私有仓库容器**

docker run -id --name registry -p 5000:5000 registry

http://服务器IP:5000/v2/_catalog  看到{"repositories":[]}搭建成功

**修改daemon.json**

vim /etc/docker/daemon.json

添加docker信任私有仓库

```
{"insecure-registries":"服务器ID:5000"}
```

重启docker

systemctl restart docker

docker start registry

##### 2.将镜像上传至私有仓库

标记镜像为私有仓库镜像

```
docker tag centos:7 私有仓库IP:5000/centos:7
```

上传标记的镜像

docker push 私有仓库IP:5000/centos:7

##### 3.从私有仓库拉取镜像

docker rmi 原先上传的容器

docker pull  服务器IP:5000/centos:7


#### 十、docker常见问题

###### usr/bin/docker permission denied 

docker运行指令如果不行，就会重置隐藏属性

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

chmod u=rwxr,g=xr,o=x docker  上面777权限太高容易被恢复

docker就可访问

dockerd也要赋予权限否则启动不了docker

###### 容器删除端口释放

重启docker会将所有容器关闭，但是端口映射的代理不会关闭，下次启动容器会端口被占用

`docker-proxy`作用是提供端口映射，以便外部可以访问容器内部

###### systemctl start docker启动不起来

/root/usr/bin/dockerd  这个文件权限没赋予

###### 删除容器停止等不行

systemctl restart docker

可能是挂载目录没有取消关联，重启docekr默认关闭容器

###### docker-proxy

exec: "docker-proxy": executable file not found in $PATH

/root/usr/bin docker-proxy文件权限没了

###### 重启docker后

docker restart 容器名

不要用docker start 容器名

###### nginx启动访问不了80端口问题

 Recv failure: Connection reset by peer

/bin/bash   docker run 的时候如果加这个命令会把http端口映射关掉