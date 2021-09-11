## maven

##### **mvn compile **

命令会在根目录生成target文件

##### mvn clean

根目录下生成的target文件移除

##### mvn install

将整个项目打包成jar，后面再部署成依赖 给其他项目引用依赖

##### mvn test

执行test顺利通过

##### mvn deploy

打包到本地仓库

```
mvn deploy:deploy-file -Dfile=C:\Users\cool\Desktop\泛微资料\workflow-restful-demo\lib\RSA-0.0.1-SNAPSHOT.jar -DgroupId=com.fangwei -DartifactId=rsa -Dversion=0.0.1 -Dpackaging=jar -Durl=file:D:\maven-repository
```

```
   <dependency>
            <groupId>com.fangwei</groupId>
            <artifactId>rsa</artifactId>
            <version>0.0.1</version>
   </dependency>
```

打包到远程私服仓库 maven settings.xml  里面的repository

```
mvn deploy:deploy-file -Dfile=jar包 -DgroupId=groupID -DartifactId=artifacid -Dversion=0.0.1-SNAPSHOT  -Dpackaging=jar -Durl=http://ip:port/nexus/content/repositories/thirdparty/ -DrepositoryId=thirdparty
```

-DrepositoryId 远程仓库的仓库标识，如果有多个仓库

注：maven通过install将本地工程打包成jar包，放入到本地仓库中，再通过pom.xml配置依赖引入到当前工程。

　　pom.xml中引入的坐标首先在本地maven仓库中查找，若没有则去maven的网上中央仓库查找，并放到本地仓库供项目使用。
