github SSH登录

git bash 

1.进入当前用户根目录

cd ~

2.删除已经存在的 .ssh目录

rm -r .ssh

3.运行命令生成.ssh 密钥目录 (注意:这里-C 这个参数是大写的 C):

ssh-keygen -t rsa -C m138779@163.com (你的github邮箱地址)

一直回车创建

4.查看生产的.ssh文件

cd .ssh

id_rsa  私钥  id_rsa.pub 公钥

cat id_rsa.pub

复制 id_rsa.pub 文件内容，登录 GitHub，点击用户头像→Settings→SSHandGPG keys



IDEA配置

重新设置远程连接改为 ssh 方式

git remote set-url origin git@github.com:1387/leetcode-Lin.git

git remote -v 查看

git push 就可以了