## MAVEN

### MAVEN问题积累

#### 1.本地仓库直接删除jar包 transfer failed for  pom.lastUpdataed

关于本地仓库直接删除jar包文件夹，点击idea 刷新maven不行，需要执行clean -> compile    编译阶段会重新去拉取包

直接拉的话会生成一个 xxx.pom.lastUpdated  里面有错误  transfer failed 拉取不到包 ，可能由于 https的原因



idea compile指令  默认去掉ssl验证  -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

#### 2.idea maven插件,跳过测试

由于某些原因，启动项目要设置VM参数和环境参数，测试类跑不起来

package打不了包， 插件上方有图标  Toggle 'Skip Test' mode 



#### 3.查看解决冲突-idea

项目能跑起来，但是右侧插件报红，一般是冲突或重复

冲突会选择一个版本依赖，如果这个版本没有找到class  一般会报ClassNotFoundException

点击idea 右侧maven 插件   ctrl+shit+alt+u

https://blog.csdn.net/weixin_41435451/article/details/126351598

ctrl+f  搜索报错冲突的包，然后找到红线，分析上游，在上游第一层pom  exclude

双击能找到冲突的包的依赖

**idea右侧插件的爆红行，不会实时刷新**

**(1) 排除包**

```
            <exclusions>
                <exclusion>
                    <groupId>commons-beanutils</groupId>
                    <artifactId>commons-beanutils</artifactId>
                </exclusion>
            </exclusions>
```

**(2) 父模块重新定义包版本**

```
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-extension</artifactId>
                <version>3.4.1</version>
                <scope>compile</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

```

**（3）推荐:Maven Helper插件检测冲突**

https://blog.csdn.net/GyaoG/article/details/120599475

#### 4.关于打包区别(可执行/依赖)

https://blog.csdn.net/u010406047/article/details/110492505

使用spring-boot-maven-plugin插件打包时，默认生成两个包，以打jar包为例，生成的是*.jar和*.jar.original。

这是因为spring-boot-maven-plugin的rapackage目标，是在 mvn package 执行之后，再次打包生成可执行的 jar包。repackage生成jar包的名称与 mvn package 生成的原始 jar/war包名称相同，而原始 jar包被重命名为 *.origin。

这样生成的*.jar可直接运行，但不能被其他项目模块依赖。这是因为repackage将项目的class都放在了jar包 BOOT-INF/classes 文件夹中，导致其他模块不能加载jar包中的class。


1.带有BOOT-INF的jar包，是编译后的，可执行jar包,可以使用 java -jar  启动

```
		<plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <--引入 spring-boot-starter-parent 的项目就默认带着可去掉-->
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

**如何解决spring-boot项目使用maven打包时出现BOOT-INF文件夹的问题?**

(1)不使用`spring-boot-maven-plugin`打包

- 将`spring-boot-maven-plugin`注释掉

- 将`spring-boot-maven-plugin`的repackage目标跳过，即设置skip为true

  ```
  		<plugin>
  				<groupId>org.springframework.boot</groupId>
  				<artifactId>spring-boot-maven-plugin</artifactId>
  				<executions>
  					<execution>
  						<configuration>
  							<skip>true</skip>
  						</configuration>
  					</execution>
  				</executions>
  			</plugin>
  ```

- 使用命令行跳过`spring-boot-maven-plugin`的repackage

  ```
  mvn clean package -Dspring-boot.repackage.skip=true
  ```

(2)官方方法

在spring-boot-maven-plugin插件上配置classifier参数，指定可执行jar包的名称后缀，例如将classifier设置为exec，则mvn package 生成的原始 jar包不被rapackage目标重命名，可执行jar包的名称变为*-exec.jar。原始 jar包就可以被其他项目依赖啦。

*.jar：普通jar包，可被依赖
*-exec.jar：可执行

```
		<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<executions>
					<execution>
						<configuration>
							<classifier>exec</classifier>
						</configuration>
					</execution>
				</executions>
			</plugin>
```



不带BOOT-INF的jar依赖包：首目录是 com.jun....

其他项目直接通过pom引入文件使用 

不需要引入上面的springboot maven打包插件

#### 7.maven指令

##### **mvn compile **

命令会在根目录生成target文件

##### mvn clean

根目录下生成的target文件移除

##### mvn install

将整个项目打包成jar安装到本地仓库，后面再部署成依赖 给其他项目引用依赖

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

#### 8.plugins出现红线错误的解决

插件未成功下载到本地仓库

1.删除所有以lastUpdated结尾的文件

2.重新点击reimport即可

#### 9.子项目引用父项目版本，子项目需要不同版本时

直接子项目加上依赖

```
 <dependencies>
        <!--覆盖了父类项目的2.3.6的spring-boot版本 ，引用新的2.7.0,maven依赖同时存在以下的一些spring-boot多个版本配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>2.7.0</version>
            <scope>compile</scope>
        </dependency>
 <dependencies>        
```

#### 10. lastUpdated问题

项目使用[maven](https://so.csdn.net/so/search?q=maven&spm=1001.2101.3001.7020)管理jar包，很容易因为各种原因(网速慢、断网)导致jar包下载不下来，出现很多.lastUpdated文件。这些文件一个一个删除太麻烦。下面是全部删除的方法。 .lastUpdated文件会导致即使有这个jar包，刷新maven时会报cannot resolve，最好删除，才能重新拉包。   不删除如果本地有idea还是能访问到这个jar包

```
windows系统下，cd到本地仓库目录下，运行命令
for /r %i in (*.lastUpdated) do del %i
```

## MAVEN知识点

### scope

compile(默认)
如果没有指定scope,那么该元素的默认值为compile。被依赖项目需要参与到项目的编译、测试、打包、运行等阶段，打包时通常会包含被依赖项目，是比较强的依赖。

provided
被依赖项目理论上可以参与到项目的编译、测试、运行等阶段，当时在打包时进行了exclude动作。
应用场景：例如我们在开发一个web项目，在编译的时候我们需要依赖servlet-api.jar,但在运行时我们不需要这个jar，因为它已由应用服务器提供，这是我们就需要用provided来修饰这个依赖包。

runtime
顾名思义，表示该依赖项目无需参与到项目的编译，但会参与到测试与运行阶段。
应用场景：例如在编译时我们不需要JDBC API的jar，但在运行时才需要JDBC的驱动包。

test
表示该依赖项目仅会参与到项目的测试阶段。
应用场景：例如，Junit 测试。system

与provided类似，但是被依赖项不会从maven仓库查找依赖，而是从本地系统中获取，systemPath元素用于指定依赖在系统中jar的路径。

import
它只使用在dependencyManagement中，我们知道maven和java只能单继承，作用是管理依赖包的版本，一般用来保持当前项目的所有依赖版本统一。
例如：项目中有很多的子项目，并且都需要保持依赖版本的统一，以前的做法是创建一个父类来管理依赖的版本，所有的子类继承自父类，这样就会导致父项目的pom.xml非常大，而且子项目不能再继承其他项目。

import为我们解决了这个问题，可以把dependencyManagement放到一个专门用来管理依赖版本的pom中，然后在需要用到该依赖配置的pom中使用scope import就可以引用配置。

### mirror和pom

官网：https://maven.apache.org/guides/mini/guide-mirror-settings.html

- `*` = everything
- `external:*` = everything not on the localhost and not file based.
- `repo,repo1` = repo or repo1
- `*,!repo1` = everything except repo1

```
<mirrorOf>central,!rdc-releases,!rdc-snapshots</mirrorOf>
含义: 镜像拦截了 远端仓库central, 但不拦截 rdc-releases 和 rdc-snapshots
```

B仓库的ID会映射到URL去下载依赖

```
<mirror> 
<id>A仓库的id</id> 
<name>xxx</name> 
<url>A仓库的url</url> 
<mirrorOf>B仓库的id</mirrorOf> 
</mirror>
```

