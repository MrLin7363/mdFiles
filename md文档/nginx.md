#### nginx配置文件

**复制配置文件的时候前两个字母不会被复制到注意**，先按 i 再粘贴

##### docker-nginx配置

```
user  nginx;
worker_processes  1;
# /var等路径都是docker-nginx创建会有的，和本地的nginx不一样
error_log  /var/log/nginx/error.log warn; 

pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
}


http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    
	log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '

                               '$status $body_bytes_sent "$http_referer" '

                               '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    keepalive_timeout  65;

    include /etc/nginx/conf.d/*.conf # 创建nginx容器，默认的配置文件，里面会有server

}
```



