# JSPKiller

![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/ASM-9.2-blue)
![](https://img.shields.io/badge/Java-8-red)

## 简介

一个JSP Webshell检测工具

主要是基于污点分析来做，依靠ASM解析字节码，然后模拟栈帧在JVM指令执行中的变化实现数据流分析

具体的原理参考先知社区文章：https://xz.aliyun.com/t/10622

## Quick Start

目前只做了普通反射JSP马的检测，其他方式后续更新

命令：

`java -jar JSPKiller.jar -f 1.jsp`

如果发生空指针异常或编译报错，参考以下

注意：
1. `JSPKiller.jar`目录下必须有`lib.jar`文件
2. 测试的三种反射JSP马已经提供（在JSP目录下）
3. 确保配置了正确的环境变量`JAVA_HOME`
4. 确保`java`命令是`JDK`下的而不是`JRE`下的（例如环境变量`Path`中配置`C:\Program Files\Java\jdk1.8.0_131\bin`为第一个）这样做的原因是：在`JRE`环境中无法获得编译器对象（JavaCompiler）来进行动态编译，只有`JDK`有这样的功能

## 效果

![](/img/01.png)