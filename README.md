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

注意：
1. `JSPKiller.jar`目录下必须有`lib.jar`文件
2. 测试的三种反射JSP马已经提供（在JSP目录下）
3. 如果立即退出无法检测，请将`JDK/lib/tools.jar`放入`JDK/jre/lib和JRE/lib/`下（例如`C:\Program Files\Java\jdk1.8.0_131\lib\tool.jar`复制到`C:\Program Files\Java\jdk1.8.0_131\jre\lib`和`C:\Program Files\Java\jre1.8.0_131\lib`）
