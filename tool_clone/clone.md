---
typora-copy-images-to: imgs_clone
typora-root-url: imgs_clone
---

# 克隆维度

## 数据准备

### 项目准备

一个有多个java、C/C++项目的文件夹。

![1593410660655](/1593410660655.png)

### 运行克隆工具获取数据

见“检测工具使用说明”。设置好config.properties（包括粒度、语言、文件后缀）后，在命令行运行

```shell
java -jar SACloneDetector.jar 项目路径（如D:\multiple-dependency-project\train-ticket）
```

然后在python目录下运行：

```shell
python calculate_similarity-(语言).py
```

目前只需file级别和method级别，跑出数据后，在多维度依赖项目的配置的“clone”里复制三个路径：

- name_path：result/MeasureIndex.csv
- result_path：similarity.csv
- group_path：type123_method_group_result.csv

## 数据插入

### 数据模型

通过静态分析，运行depends获取代码的各个实体后，在多维度依赖项目内的节点类型为：

- MicroService 

  目前每个项目（大文件夹下的每个文件夹）都默认为一个微服务项目

- Project

  由于华为的试点项目中存在一个微服务项目内有java和c++两种语言，因此对每种语言都生成一个Project节点，一个MicroService下最多包含两个Project

- Package

  目录，一个Project有多个Package，Package和Package之间在图里没有上下级关系

- ProjectFile（ implements CodeNode）

  文件

- Namespace（ implements CodeNode）

  命名空间，C++特有，不需关注

- Type（ implements CodeNode）

  类型，java：class、interface、enum、annotation等；c/c++：class、struct、enum等。（在静态分析中记录了起止行号）

- Function（ implements CodeNode）

  方法/函数（在静态分析中记录了起止行号）

- Variable（ implements CodeNode）

  变量，包括属性，局部变量等

- Snippet（ implements CodeNode）

  片段，代码片段

与克隆相关的节点：

- CloneGroup

  克隆组，一个克隆组可以包含至少2个CodeNode类型的节点。

与克隆相关的关系：

- Contain

  CloneGroup -[:Contain]->CodeNode

- Clone

  CodeNode -[:Clone]- CodeNode，在数据库中存了关系的方向，但从逻辑上讲是不分方向的。

### 克隆数据映射

#### result_path

在result_path中记载的是克隆数据的第一列id与第二列id之间有克隆关系，第三行为克隆比率，最后一行（similarity文件）为克隆类型（type1、type2、type3）

#### name_path

result_path中的id具体对应到name_path中的值，name_path每一行为：

- id

- 文件（对应数据库的ProjectFile，对应方法见：**cn.edu.fudan.se.multidependency.model.node.Nodes.findFileByPathRecursion(String)**

  因为在数据库中ProjectFile的路径为项目路径的相对路径，而不是绝对路径，如文件D:\multiple-dependency-project\train-ticket\ts-admin-route-service\src\main\java\adminroute\AdminRouteApplication.java，在数据库中存的路径为：/ts-admin-route-service/src/main/java/adminroute/AdminRouteApplication.java，

  而name_path里的路径为绝对路径，所以通过每次去掉一个 \ ... 来找ProjectFile节点；

- 开始行数与结尾行数。如果是文件级克隆，则开始行数是1，结尾行数为文件的行数，直接通过路径找ProjectFile，如果是方法级克隆，行数为代码段所在的位置，由于他们克隆组的工具的方法级是找  {}，因此不全为方法，有可能是Struct、class、代码块等，因此通过结尾代码行（由于使用工具的差异导致通过首行查找出现不一致的情况）来找Type和Function，如果没找到，则为对应行数的代码块生成一个Snippet节点。

#### group_path

如果存在克隆关系的多个节点形成一个组，那么生成一个“克隆组CloneGroup”节点包含这些节点

### 主要代码文件

cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserter

cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserterForFile

cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserterForMethod

## 数据查询

查询主要是以克隆组为单位查询。

### 主要代码文件

cn.edu.fudan.se.multidependency.controller.CloneGroupController

cn.edu.fudan.se.multidependency.service.spring.clone包下的所有文件

multi-dependency\src\main\resources\templates\clonegroup.html

multi-dependency\src\main\resources\static\js\clonegroup.js