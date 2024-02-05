Linux系统操作笔记

## 一、Linux

### 1.快捷键

#### 1. 命令行

ctrl+a ctrl+e 分别代表把管标移动到最前和最后

ctrl+u ctrl+k 分别代表光标处往前和光标处往后*删除*

shift+insert / 右键  = 粘贴

右键 = 复制粘贴到命令行

### 1、命令

#### ps

```
ps -aux --width=300          # width能显示出所有内容
```

https://www.cnblogs.com/qiuyu666/p/12580110.html

## 二、ubuntu

### 1. 安装下载环境

CentOS 使用 yum 管理器，Ubuntu 使用 apt 管理器，所以在 Ubuntu 上安装软件，用的是 apt 命令，而不是 yum 命令。apt 命令语法和具体使用如下：

sudo apt-get update

安装 ping:       sudo apt-get install iputils-ping 

### 2. 禁止ip访问

sudo apt-get install iptables

```
sudo iptables -A INPUT -s 7.256.zz.zzz -j DROP
sudo iptables -A INPUT -s 7.256.zz.zz -j DROP

解除限制
sudo iptables -D INPUT -s x.x.x.x -j DROP
 
sudo iptables-save
```

### 3. ubuntu没有systemctl

```
System has not been booted with systemd as init system (PID 1). Can't operate.
Failed to connect to bus: Host is down
```

**为什么会出现这样的报错信息呢？**

原因是当你尝试使用 systemd 命令来管理 Linux 系统上的服务的时候，但是系统中根本就没有使用 systemd，而是（很可能）使用的 SysV init (sysvinit)。

这是怎么回事呢？

如果你是在 windows 中通过 WSL 使用的 Ubuntu，默认情况下系统使用的是 SysV 而不是 systemd。当你使用 systemctl 命令（适用于有 systemd init 的系统）的时候，系统自然会报错。

那么怎样查看到底用的是哪个 init 系统呢？可以使用如下命令来检查 PID 为 1 的进程（即系统运行的第一个进程）名称：

复制

```
ps -p 1 -o comm=1.
```

它应该在输出中显示 init 或 sysv（或类似的东西）。如果你看到的是 init，那么你的系统就没有使用 systemd，应该使用 init 命令。

**如何修复 System has not been booted with systemd 报错信息？**

最简单的方式就是不使用 systemctl 命令，而是使用 sysvinit 命令。

sysvinit 也不复杂，它与 systemctl 命令的语法相似。如下表格为两个命令的对比：

| Systemd command                | Sysvinit command             |
| ------------------------------ | ---------------------------- |
| systemctl start service_name   | service service_name start   |
| systemctl stop service_name    | service service_name stop    |
| systemctl restart service_name | service service_name restart |
| systemctl status service_name  | service service_name status  |
| systemctl enable service_name  | chkconfig service_name on    |
| systemctl disable service_name | chkconfig service_name off   |

大家在初始学习的时候，如果遇到类似的错误，可以尝试使用上面表格中等效的命令，就不会看到 "System has not been booted with systemd as init system" 这样的报错信息了。



