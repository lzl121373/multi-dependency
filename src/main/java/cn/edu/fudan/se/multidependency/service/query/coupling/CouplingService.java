package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CouplingService {

    Map<ProjectFile, Double> calGroupInstablity(List<Long> fileIdList);

    double calC1to2(int funcNum1, int funcNum2);

    double calC(double C1, double C2);

    double calU1to2(long dependsOntimes1, long dependsOntimes2);

    double calI(long dependsOntimes1, long dependsOntimes2);

    double calDISP(double C_AandB, long dependsOntimes1, long dependsOntimes2);

    double calDependsOnC(long file1Id, long file2Id);

    double calDependsOnI(DependsOn dependsOnAtoB, DependsOn dependsOnBtoA);

    JSONObject getCouplingValueByFileIds(List<Long> fileIds, Map<Long, Long> parentPckMap);

    JSONObject getCouplingValueByPcks(Map<Package, List<Package>> pckMap, Map<Long, Double> parentPcksInstability, boolean isTopLevel);
}
