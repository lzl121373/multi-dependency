package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.MultipleDependencyApp;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//@Slf4j
//@Transactional
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MultipleDependencyApp.class)
public class CyclicDependencyDetectorImplTest {

    private Set<ProjectFile> fileSet;

    private Set<DependsOn> relationSet;

    @BeforeEach
    void setUp() {
        //初始化节点集合
        this.fileSet = new HashSet<>();
        //创建对象
        ProjectFile fileA = new ProjectFile(1L, "A.java", "Demo/CyclicDependency/PackageA/A.java", ".java");
        ProjectFile fileB = new ProjectFile(2L, "B.java", "Demo/CyclicDependency/PackageA/B.java", ".java");
        ProjectFile fileC = new ProjectFile(3L, "C.java", "Demo/CyclicDependency/PackageA/C.java", ".java");
        ProjectFile fileD = new ProjectFile(4L, "D.java", "Demo/CyclicDependency/PackageA/D.java", ".java");
        ProjectFile fileE = new ProjectFile(5L, "E.java", "Demo/CyclicDependency/PackageA/E.java", ".java");
        //设置Id
        fileA.setId(1L);
        fileB.setId(2L);
        fileC.setId(3L);
        fileD.setId(4L);
        fileE.setId(5L);
        //加入集合
        this.fileSet.add(fileA);
        this.fileSet.add(fileB);
        this.fileSet.add(fileC);
        this.fileSet.add(fileD);
        this.fileSet.add(fileE);

        //初始化关系集合
        this.relationSet = new HashSet<>();
        Map<String, Long> dependsOnTypes = new HashMap<>();
        //创建对象
        DependsOn dependsOn1 = new DependsOn(fileA, fileB, "CREATE__CALL__RETURN__USE__PARAMETER");
        DependsOn dependsOn2 = new DependsOn(fileB, fileA, "CREATE__CALL__RETURN__USE__PARAMETER");
        //设置Id
        dependsOn1.setId(1L);
        dependsOn2.setId(2L);
        //设置DependsOnTypes
        dependsOnTypes.put(RelationType.str_CREATE, 3L);
        dependsOnTypes.put(RelationType.str_CALL, 10L);
        dependsOnTypes.put(RelationType.str_RETURN, 1L);
        dependsOnTypes.put(RelationType.str_USE, 7L);
        dependsOnTypes.put(RelationType.str_PARAMETER, 3L);
        dependsOn1.setDependsOnTypes(dependsOnTypes);
        dependsOnTypes.clear();
        dependsOnTypes.put(RelationType.str_CALL, 5L);
        dependsOnTypes.put(RelationType.str_USE, 8L);
        dependsOn2.setDependsOnTypes(dependsOnTypes);
        //加入集合
        this.relationSet.add(dependsOn1);
        this.relationSet.add(dependsOn2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void detectFileCyclicDependency() {
        for (ProjectFile file : this.fileSet) {
            System.out.println(file);
        }

        for (DependsOn dependsOn : relationSet) {
            System.out.println(dependsOn);
        }
    }
}