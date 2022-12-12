在图数据库中创建克隆组

```cypher
// 文件级
match (n:ProjectFile) where n.suffix=".java" set n.language = "java";
match (n:ProjectFile) where n.suffix<>".java" set n.language = "cpp";

match (n:CloneGroup)-[r:CONTAIN]-() delete r;
match (n:CloneGroup) delete n;

CALL algo.unionFind.stream("ProjectFile", "CLONE")
YIELD nodeId,setId
with setId, collect(algo.getNodeById(nodeId)) AS files
where size(files) > 1
match (file:ProjectFile) where file in files set file.cloneGroupId = "file_group_" + setId;

match (file:ProjectFile) where file.cloneGroupId is not null with file.cloneGroupId as cloneGroupId, count(file) as count with cloneGroupId create (:CloneGroup{name: cloneGroupId, cloneLevel: "file", entityId: -1});

MATCH (n:CloneGroup) with n match (file:ProjectFile) where file.cloneGroupId = n.name create (n)-[:CONTAIN]->(file);

MATCH (n:CloneGroup) with n set n.size = size((n)-[:CONTAIN]->());
MATCH (n:CloneGroup)-[:CONTAIN]->(file:ProjectFile) where n.language is null with n, file set n.language = file.language;
    
// 方法级

```







# 基于图数据库的架构异味检测技术研究与实现

### Cypher语句

#### 创建cochange关系

```cypher
match (f1:ProjectFile)<-[:COMMIT_UPDATE_FILE]-(c:Commit)-[:COMMIT_UPDATE_FILE]->(f2:ProjectFile) where id(f1) < id(f2) with f1,f2,count(c) as times create (f1)-[:CO_CHANGE{times:times}]->(f2);
```

```cypher
match (f1:ProjectFile)<-[:COMMIT_UPDATE_FILE]-(c:Commit)-[:COMMIT_UPDATE_FILE]->(f2:ProjectFile) where id(f1) < id(f2) with f1,f2,c create (f1)-[:CO_CHANGE{commitId: c.commitId}]->(f2);


match ()-[r:CO_CHANGE]-() where r.times is null delete r;
```



#### 创建文件间DEPENDS_ON关系

根据继承关系、调用关系和访问属性关系创建

有次数的dependon

```cypher
match (:ProjectFile)-[r:DEPENDS_ON]-(:ProjectFile) delete r;
// EXTENDS
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:EXTENDS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// IMPLEMENTS
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:IMPLEMENTS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);

match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLEMENTS]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CALL
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);

match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CREATE
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
                                                        
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[c:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CAST
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[c:CAST]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[c:CAST]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// THROW
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[c:THROW]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// PARAMETER
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[c:PARAMETER]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
match (f1:ProjectFile)-[:CONTAIN*1..4]->(:Variable)-[c:PARAMETER]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// VARIABLE_TYPE
match (f1:ProjectFile)-[:CONTAIN*1..4]->(:Variable)-[c:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// ACCESS
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:ACCESS]->(:Variable)<-[:CONTAIN*1..4]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:ACCESS]->(:Variable)<-[:CONTAIN*1..4]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// IMPLLINK
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLLINK]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// ANNOTATION
match (f1:ProjectFile)-[:CONTAIN*1..4]->(:Variable)-[c:ANNOTATION]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[c:ANNOTATION]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[c:ANNOTATION]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);


match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) with f1,f2,count(r) as times create (f1)-[:DEPENDS_ON{times : times}]->(f2)

match (:ProjectFile)-[r:DEPENDS_ON]->() where r.times is null delete r;
```

简化版

```cypher
match (:ProjectFile)-[r:DEPENDS_ON]-(:ProjectFile) delete r;
// EXTENDS
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:EXTENDS]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// IMPLEMENTS
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:IMPLEMENTS]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CALL
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:CALL]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CREATE
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:CREATE]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// CAST
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[c:CAST]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// THROW
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[c:THROW]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// PARAMETER
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[c:PARAMETER]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// VARIABLE_TYPE
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[c:VARIABLE_TYPE]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// ACCESS
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:ACCESS]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// IMPLLINK
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:IMPLLINK]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);
// ANNOTATION
match (f1:ProjectFile)-[:CONTAIN*1..]->()-[c:ANNOTATION]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);


match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) with f1,f2,count(r) as times create (f1)-[:DEPENDS_ON{times : times}]->(f2)

match (:ProjectFile)-[r:DEPENDS_ON]->() where r.times is null delete r;
```



#### 创建包间DEPENDS_ON关系

```cypher
match (p1:Package)-[:CONTAIN]->(:ProjectFile)-[:DEPENDS_ON]->(:ProjectFile)<-[:CONTAIN]-(p2:Package) with p1,p2 where p1<>p2 and not (p1)-[:DEPENDS_ON]->(p2) create (p1)-[:DEPENDS_ON]->(p2)

match (p1:Package)-[r:DEPENDS_ON]->(p2:Package) with p1,p2,count(r) as times create (p1)-[:DEPENDS_ON{times : times}]->(p2)

match (:Package)-[r:DEPENDS_ON]->() where r.times is null delete r;
```

#### 找出文件间的循环依赖

```cypher
CALL algo.scc.stream("ProjectFile", "DEPENDS_ON")
YIELD nodeId, partition
with partition, collect(algo.getNodeById(nodeId)) AS files
where size(files) >= 2
return partition, files
ORDER BY size(files) DESC
```

#### 找出包间的循环依赖

```cypher
CALL algo.scc.stream("Package", "DEPENDS_ON")
YIELD nodeId, partition
with partition, collect(algo.getNodeById(nodeId)) AS packages
where size(packages) >= 2
return partition, packages
ORDER BY size(packages) DESC
```

```cypher
CALL algo.scc.stream("Package", "DEPENDS_ON")
YIELD nodeId, partition
with partition, collect(algo.getNodeById(nodeId)) AS packages
where size(packages) >= 2 
match result=(a:Package)-[r:DEPENDS_ON]->(b:Package) where a in packages and b in packages return result

CALL algo.scc.stream("Package", "DEPENDS_ON")
YIELD nodeId, partition where partition = 32
with partition, collect(algo.getNodeById(nodeId)) AS packages
match result=(a:Package)-[r:DEPENDS_ON]->(b:Package) where a in packages and b in packages return result
```

#### 计算文件FanOut

```cypher
match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) return f1, count(r) order by count(r) desc;
```

#### 计算文件FanIn

```cypher
match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) return f2, count(r) order by count(r) desc;
```

#### 计算文件的FanIn和FanOut

```cypher
MATCH (n:ProjectFile)
WITH size((n)-[:DEPENDS_ON]->()) as out, 
     size((n)<-[:DEPENDS_ON]-()) as in,
     n
RETURN  in,n,out order by(out+in) desc;
```

#### 计算文件各个指标

```cypher
MATCH (file:ProjectFile)
WITH size((file)-[:DEPENDS_ON]->()) as fanOut, 
     size((file)<-[:DEPENDS_ON]-()) as fanIn,
     size((file)<-[:COMMIT_UPDATE_FILE]-()) as changeTimes,
     size((file)-[:CONTAIN*1..3]->(:Function)) as nom,
     file.endLine as loc,
     file
RETURN  file,fanIn,fanOut,changeTimes,nom,loc order by(file.path) desc;

match (c:Commit)-[:COMMIT_UPDATE_FILE]->(f:ProjectFile) with f, c where size((c)-[:COMMIT_UPDATE_FILE]->(:ProjectFile)) > 1 return f, count(c) order by count(c) desc
```

引入协同修改次数（会丢失次数为0的文件）

```cypher
MATCH (file:ProjectFile)
with file
match (file)<-[r:COMMIT_UPDATE_FILE]-(c:Commit) with file, c where size((c)-[:COMMIT_UPDATE_FILE]->(:ProjectFile)) > 1 with file, count(c) as cochangeCommitTimes 
WITH size((file)-[:DEPENDS_ON]->()) as fanOut, 
     size((file)<-[:DEPENDS_ON]-()) as fanIn,
     size((file)<-[:COMMIT_UPDATE_FILE]-()) as changeTimes,
     size((file)-[:CONTAIN*1..3]->(:Function)) as nom,
     file.endLine as loc,
     file.score as score,
     cochangeCommitTimes,
     file
RETURN  file,fanIn,fanOut,changeTimes,cochangeCommitTimes,nom,loc order by(file.path) desc;
```

#### 计算项目各个指标

```cypher
MATCH (project:Project)-[:CONTAIN*2]->(file:ProjectFile)
WITH project, sum(file.endLine) as loc
WITH size((project)-[:CONTAIN]->(:Package)) as nop, 
     size((project)-[:CONTAIN*2]->(:ProjectFile)) as nof,
     size((project)-[:CONTAIN*3..5]-(:Function)) as nom,
     loc,
     project
RETURN project, nop, nof, nom, loc order by(project.name) desc;
      
      
match (project:Project)-[:CONTAIN*2]->(:ProjectFile)<-[:COMMIT_UPDATE_FILE]-(c:Commit)
with count(distinct c) as commitTimes, project
MATCH (project)-[:CONTAIN*2]->(file:ProjectFile)
WITH project, sum(file.endLine) as loc, commitTimes
WITH size((project)-[:CONTAIN]->(:Package)) as nop, 
     size((project)-[:CONTAIN*2]->(:ProjectFile)) as nof,
     size((project)-[:CONTAIN*3..5]-(:Function)) as nom,
     loc,
     project,
     commitTimes
RETURN project, nop, nof, nom, loc, commitTimes order by(project.name) desc;
```

#### 计算包各个指标

```cypher
MATCH (pck:Package)-[:CONTAIN]->(file:ProjectFile)
WITH pck, sum(file.endLine) as loc
WITH size((pck)-[:CONTAIN]->(:ProjectFile)) as nof, 
     size((pck)-[:CONTAIN*2..4]-(:Function)) as nom,
     size((pck)-[:DEPENDS_ON]->()) as fanOut, 
     size((pck)<-[:DEPENDS_ON]-()) as fanIn,
     loc,
     pck
RETURN pck, loc, nof, nom, fanIn, fanOut order by(pck.directoryPath) desc;
```

#### 网页排名算法

根据节点上链接的邻近节点和链接到这些邻近节点的节点，来估算一个节点的重要性

```cypher
CALL algo.pageRank.stream('ProjectFile', 'DEPENDS_ON', {iterations:20, dampingFactor:0.85})
YIELD nodeId, score
RETURN algo.getNodeById(nodeId).path AS page, score
ORDER BY score DESC
```

```cypher
CALL algo.pageRank.stream('Package', 'DEPENDS_ON', {iterations:20, dampingFactor:0.85})
YIELD nodeId, score
RETURN algo.getNodeById(nodeId).directoryPath AS page, score
ORDER BY score DESC
```

在文件上新加上分数

```cypher
CALL algo.pageRank.stream('ProjectFile', 'DEPENDS_ON', {iterations:20, dampingFactor:0.85})
YIELD nodeId, score
with algo.getNodeById(nodeId) AS file, score 
set file.score=score
RETURN file
ORDER BY score DESC
```

#### 在commit节点上添加提交文件数量属性

```cypher
match (c:Commit) set c.commitFilesSize = size((c)-[:COMMIT_UPDATE_FILE]->(:ProjectFile)) return c
```

#### 一次提交文件数超过1个的Commit次数

```cypher
match p=(c:Commit) with c, size((c)-[:COMMIT_UPDATE_FILE]->(:ProjectFile)) as size where size > 0 return count(c);
```

```cypher
match (c:Commit) where c.commitFilesSize > 1 return count(c)
```

#### 文件被修改次数

```cypher
match (c:Commit)-[:COMMIT_UPDATE_FILE]->(f:ProjectFile) return f, count(c) order by count(c) desc
```

```cypher
match (c:Commit)-[:COMMIT_UPDATE_FILE]->(f:ProjectFile) where f.path="" return count(c)
```

```cypher
match (c:Commit)-[:COMMIT_UPDATE_FILE]->(f:ProjectFile) with f, c where size((c)-[:COMMIT_UPDATE_FILE]->(:ProjectFile)) > 1 return f, count(c) order by count(c) desc
```

#### 网页排名算法与文件被修改次数结合

```cypher
CALL algo.pageRank.stream('ProjectFile', 'DEPENDS_ON', {iterations:20, dampingFactor:0.85})
YIELD nodeId, score
with algo.getNodeById(nodeId).path AS page, score
match (c:Commit)-[r:COMMIT_UPDATE_FILE]->(f:ProjectFile) where f.path = page return f.path As file, score, count(c) AS updateTimes
ORDER BY score DESC
```

#### 网页排名算法与文件被修改次数和文件入度出度结合

```cypher
CALL algo.pageRank.stream('ProjectFile', 'DEPENDS_ON', {iterations:20, dampingFactor:0.85})
YIELD nodeId, score
with algo.getNodeById(nodeId).path AS page, score
match (c:Commit)-[r:COMMIT_UPDATE_FILE]->(f:ProjectFile) where f.path = page with size((f)-[:DEPENDS_ON]->()) as out, size((f)<-[:DEPENDS_ON]-()) as in, f,score, c return f.path As file, score, count(c) AS updateTimes, in, out
ORDER BY score DESC
```





Package之间的HAS关系

```cypher
match ()-[r:HAS]-() delete r;
match (p1:Package), (p2:Package) where p2.directoryPath =~ (p1.directoryPath + ".+")  create (p1)-[:HAS]->(p2);
match (a:Package)-[r:HAS]->(b:Package) where (a)-[:HAS*2..]->(b) delete r;
                                                   match (p1:Package)-[r1:HAS*2..]->(p3:Package) where size((p1)-[:HAS]->()) = 1 and size((p3)-[:HAS]->()) = 1 and size((p1)-[:CONTAIN]->()) = 0 and size((p3)-[:CONTAIN]->()) = 0 create (p1)-[:HAS]->(p3);

match (p1:Package)-[r1:HAS]->(p2:Package)-[r2:HAS]->(p3:Package) where (p1)-[:HAS]->(p3) delete r1, r2;

match (project:Project)-[r:CONTAIN]->(pck:Package) where not (pck)-[:HAS]-() delete r, pck;

match (project:Project)-[r1:CONTAIN]->(pck1:Package)-[r2:HAS]->(pck2:Package) where size((pck1)-[:CONTAIN]->(:ProjectFile)) = 0 and size((pck2)-[:CONTAIN]->(:ProjectFile)) = 0 delete r1, r2, pck1;
```





## 



## AS及其计算步骤

### Unused Component

#### 描述

未被使用的包或文件

#### 计算

找出没有出入度关系的文件或包

### Cyclic Dependency

#### 描述

组件间产生循环依赖

#### 计算

强连通图算法，找出环

### HubLike Component

#### 描述

某个文件依赖数和被依赖数都很高

### Unstable Dependency

#### 描述

在软件中具有重要影响力的文件（依赖该文件的其它文件的数量很多）应该保持稳定	

### Implicit Cross-module Dependency（Logic Coupling）

#### 描述

当属于不同模块（模块的修改应该是独立的，与其它模块无关）下的文件（相互没有依赖关系）发生共同修改时，显示了文件间的隐藏的依赖

#### 计算

找出cochange的两个文件，检测是否在同一模块（如Package）下

### Similar Component



### Multiple Architectural Smell



### Architecture Violation ?



## fastjson



## Guava

9b94fb3965c6869b0ac47420958a4bbae0b2d54c

开始出现android



d8e921fd2df52184708d6eb9516e4869f548d841



## Cassandra

### 3.11

#### 3.11.1

983c72a84ab6628e09a78ead9e20a0c323a005af

### 3.0

#### 3.0.21

e39d1da325f5853ab3a64d92ecf52f8271239b9e

#### 3.0.15

b32a9e6452c78e6ad08e371314bf1ab7492d0773

#### 3.0.13

91661ec296c6d089e3238e1a72f3861c449326aa

#### 3.0.11

338226e042a22242645ab54a372c7c1459e78a01

### 2.2

#### 2.2.12

1602e606348959aead18531cb8027afb15f276e7

#### 2.2.9

70a08f1c35091a36f7d9cc4816259210c2185267

### 2.1

#### 2.1.20

b2949439ec62077128103540e42570238520f4ee

#### 2.1.17

943db2488c8b62e1fbe03b132102f0e579c9ae17