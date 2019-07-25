# 安装Spring引导应用程序 - Spring Boot应用的后台运行配置

## 1.修改maven配置

要使用Maven创建一个“完全可执行的”jar，请使用以下插件配置
```
<plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
                <executable>true</executable>
        </configuration>
</plugin>
```
这个时候就可以使用 /opt/exec/executable.jar 运行自己的JAR了.

## 2.添加操作系统支持
### 2.1安装为 init.d 服务
#### 2.1.1创建连接
```
sudo ln -s /opt/exec/executable.jar /etc/init.d/executable
```

#### 2.1.2 这个时候就可以用下面的命令启动服务了
```
service executable start 
```
#### 2.1.3 确保 init.d 服务的安全
修改所属
```
chown bootapp:bootapp executable.jar
```
您还应该采取步骤防止修改应用程序的jar文件。首先，配置其权限，使其不能被写入，只能被其所有者读取或执行，如下例所示:
修改权限
```
chmod 500 executable.jar 
```
其次，如果您的应用程序或正在运行它的帐户受到损害，您还应该采取步骤来限制损害。
如果攻击者确实获得了访问权限，他们可以使jar文件可写并更改其内容。
防止这种情况的一种方法是使用chattr使其不可变，如下面的例子所示:
```
锁定文件
$ sudo chattr +i executable.jar
取消锁定
$ sudo chattr -i executable.jar
```
使用管理员权限锁定文件 executable.jar.这将阻止任何用户(包括root用户)修改jar。


### 2.2安装为systemd服务

#### 2.2.1 
systemd是System V init系统的继承者，现在被许多现代Linux发行版使用。
可以继续通过systemd 使用 init.d 脚本，还可以使用systemd 'service' 脚本启动Spring引导应用程序。

假设您在 /opt/exec/ 中安装了一个Spring Boot应用程序，
要将Spring引导应用程序安装为systemd服务，
创建一个名为 executable.service 的脚本。并将其放在/etc/systemd/system目录中。
下面的脚本提供了一个例子:

```
[Unit]
# 服务描述
Description=executable
# 指定了在systemd在执行完那些target之后再启动该服务
Documentation=https://www.baidu.com
After=syslog.target

[Service]
User=bootapp
ExecStartexecutable.jar run
ExecReload=/opt/exec/executable.jar restart
ExecStop=/opt/exec/executable.jar stop
SuccessExitStatus=143
Restart=always
PrivateTmp=true=/opt/exec/

[Install]
# 多用户
WantedBy=multi-user.target
```

修改用户所属
```
chown bootapp:bootapp executable.jar
```

设置开机启动
```
systemctl enable executable.service
```

## 3.添加启动参数文件

除了JARFILE和APP_NAME之外，可以使用.conf文件配置上一节中列出的设置。
该文件应该放在jar文件旁边，并且具有相同的名称，但是后缀是.conf而不是.jar。
例如，一个名为/opt/exec/executable.jar 的jar 使用名为 /opt/exec/executable.jar 的配置文件。
如下例所示:
executable.conf 

序号|名称|说明
:---:|:---:|---
1|MODE|操作的“模式”。默认值取决于构建jar的方式，但通常是自动的(这意味着它试图通过检查init.d目录中的符号链接来猜测它是否是init脚本)。您可以显式地将其设置为service，以便stop|start|status|restart命令可以工作，或者如果您想在前台运行脚本，可以运行该命令。
2|USE_START_STOP_DAEMON|当start-stop-daemon命令可用时，是否应该使用它来控制进程。默认值为true。
3|PID_FOLDER|pid文件夹的根名称(默认/var/run)。
4|LOG_FOLDER|存放日志文件的文件夹的名称(默认情况下是/var/log)。
5|CONF_FOLDER|要从中读取.conf文件的文件夹的名称(默认情况下与jar-file相同)。
6|LOG_FILENAME|LOG_FOLDER中的日志文件的名称(默认情况下<appname>.log)。
7|APP_NAME|如果jar从符号链接运行，脚本将猜测应用程序的名称。如果它不是一个符号链接，或者您想显式地设置应用程序名称，这将非常有用。
8|RUN_ARGS|传递给程序(Spring Boot应用程序)的参数。
9|JAVA_HOME|默认情况下，通过使用路径可以发现java可执行文件的位置，但是如果在$JAVA_HOME/bin/java中有一个可执行文件，则可以显式地设置它。
10|JAVA_OPTS|启动JVM时传递给JVM的选项。
11|JARFILE|jar文件的显式位置，以防脚本用于启动一个实际上没有嵌入的jar。
12|DEBUG|如果不是空的，则在shell进程上设置-x标志，使其更容易看到脚本中的逻辑。
13|STOP_WAIT_TIME|在强制关闭应用程序之前等待的时间(默认为60秒)，以秒为单位。

示例
```
APP_NAME=executable
JAVA_HOME=/opt/soft/jdk1.8
# pid文件夹的根名称(默认/var/run)。
PID_FOLDER=/opt/exec/
# LOG_FOLDER中的日志文件的名称(默认<appname>.log)
LOG_FOLDER=/opt/exec/
#后台运行
MODE=service
JAVA_OPTS="-server -Xmx100m -Xms100m -Xloggc:/opt/exec/pandora_gc.log -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=utf-8"
#RUN_ARGS="--server.port=3011 --spring.profiles.active=debug --spring.pid.file=/tmp/logs/blog.pid"
```

## 4.查看服务的日志
```
journalctl -u executable.service
```


