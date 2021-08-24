JVM

##### 监控不到本地JAVA程序

win+R  输入 %TMP%\

找到 hsperfdata_用户名，修改该文件夹权限 为 完全控制

JAVA程序线程才能输入到这个文件夹被监控到

项目启动打印JVM信息

-XX:+PrintGCDetails

-XX:+PrintGCDetails -Xmx512m -Xms512m  这两个最好一致，避免GC堆伸缩幅度太大，停顿

-Xmx默认为物理内存的1/4

-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

##### visualVM监控工具