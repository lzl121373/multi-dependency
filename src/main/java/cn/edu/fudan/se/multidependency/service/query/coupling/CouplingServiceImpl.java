package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class CouplingServiceImpl implements CouplingService {

    @Autowired
    public CouplingRepository couplingRepository;

    @Autowired
    public DependsOnRepository dependsOnRepository;

    @Autowired
    public ProjectFileRepository projectFileRepository;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private ContainRelationService containRelationService;

    @Override
    public Map<ProjectFile, Double> calGroupInstablity(List<Long> fileIdList){
        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOnByFileIds(fileIdList);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        Map<ProjectFile, Integer> instabilityInTimes = new HashMap<>();
        Map<ProjectFile, Integer> instabilityOutTimes = new HashMap<>();
        Map<ProjectFile, Double> instability = new HashMap<>();

        for(DependsOn dependsOn: GroupInsideDependsOns){
            ProjectFile startFile = (ProjectFile) dependsOn.getStartNode();
            ProjectFile endFile = (ProjectFile) dependsOn.getEndNode();
            Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                for (String type : dependsOnTypes.keySet()) {
                    if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                        if(instabilityInTimes.containsKey(startFile)){
                            instabilityInTimes.put(startFile, instabilityInTimes.get(startFile) + 10);
                        }else{
                            instabilityInTimes.put(startFile, 10);
                        }

                        if(instabilityOutTimes.containsKey(endFile)){
                            instabilityOutTimes.put(endFile, instabilityOutTimes.get(endFile) + 1);
                        }else{
                            instabilityOutTimes.put(endFile, 1);
                        }
                    }else if(type.equals("USE") || type.equals("CALL") ||  type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") ||  type.equals("CREATE")
                            || type.equals("MEMBER_VARIABLE")){
                        if(instabilityOutTimes.containsKey(startFile)){
                            instabilityOutTimes.put(startFile, instabilityOutTimes.get(startFile) + 1);
                        }else{
                            instabilityOutTimes.put(startFile, 1);
                        }

                        if(instabilityInTimes.containsKey(endFile)){
                            instabilityInTimes.put(endFile, instabilityInTimes.get(endFile) + 1);
                        }else{
                            instabilityInTimes.put(endFile, 1);
                        }
                    }
                }
        }

        for(Long fileId: fileIdList){
            ProjectFile file = projectFileRepository.findFileById(fileId);
            int allDependsOnTimes = instabilityInTimes.getOrDefault(file, 0) + instabilityOutTimes.getOrDefault(file, 0);
            int outDependsOnTimes = instabilityOutTimes.getOrDefault(file, 0);
            if(allDependsOnTimes == 0){
                instability.put(file, 0.0);
            }else{
                instability.put(file, ((double) outDependsOnTimes / (double) allDependsOnTimes));
            }
        }
        return instability;
    }

    private String getRelationTypeAndTimes(Map<String, Long> dependsOnTypes){
        StringBuilder typesAndTimes = new StringBuilder();
        Iterator<String> iterator = dependsOnTypes.keySet().iterator();
        while(iterator.hasNext()) {
            String type = iterator.next();
            if(iterator.hasNext()){
                typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")_");
            }else{
                typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")");
            }
        }
        return typesAndTimes.toString();
    }

    @Override
    public double calC1to2(int funcNum1, int funcNum2){
        return (2 * ((double)funcNum1 + 1) * ((double)funcNum2 + 1)) / ((double)funcNum1 + (double)funcNum2 + 2) - 1;
    }

    @Override
    public double calC(double C1, double C2){
        return Math.sqrt((double)(Math.pow(C1, 2) + Math.pow(C2, 2)));
    }

    @Override
    public double calU1to2(long dependsOntimes1, long dependsOntimes2){
        return ((double)dependsOntimes1 - (double)dependsOntimes2) / ((double)dependsOntimes1 + (double)dependsOntimes2);
    }

    @Override
    public double calI(long dependsOntimes1, long dependsOntimes2){
        return (2 * ((double)dependsOntimes1 + 1) * ((double)dependsOntimes2 + 1)) / ((double)dependsOntimes1 + (double)dependsOntimes2 + 2) - 1;
    }

    @Override
    public double calDISP(double C_AandB, long dependsOntimes1, long dependsOntimes2){
        return C_AandB / ((double)dependsOntimes1 + (double)dependsOntimes2);
    }

    @Override
    public double calDependsOnC(long file1Id, long file2Id){
        int funcNumAAtoB = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file1Id, file2Id);
        int funcNumBAtoB = couplingRepository.queryTwoFilesDependsByFunctionsNum(file1Id, file2Id);
        int funcNumABtoA = couplingRepository.queryTwoFilesDependsByFunctionsNum(file2Id, file1Id);
        int funcNumBBtoA = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file2Id, file1Id);

        return calC(calC1to2(funcNumAAtoB,funcNumBAtoB), calC1to2(funcNumABtoA,funcNumBBtoA));
    }

    @Override
    public double calDependsOnI(DependsOn dependsOnAtoB, DependsOn dependsOnBtoA){
        long dependsOntimesAtoB = 0;
        long dependsOntimesBtoA = 0;

        if(dependsOnAtoB != null) {
            Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();

            for (String type : dependsOnTypesAtoB.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN") || type.equals("CREATE")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
                        || type.equals("MEMBER_VARIABLE")) {
                    dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
                }
            }
        }

        if(dependsOnBtoA != null) {
            Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();

            for (String type : dependsOnTypesBtoA.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN") || type.equals("CREATE")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
                        || type.equals("MEMBER_VARIABLE")) {
                    dependsOntimesBtoA += dependsOnTypesBtoA.get(type);
                }
            }
        }

        return calI(dependsOntimesAtoB, dependsOntimesBtoA);
    }

    private List<Double> calGroupCI(List<DependsOn> dependsOnList){
        List<String> filePairs = new ArrayList<>();
        List<Double> result = new ArrayList<>();
        double CSum = 0.0, ISum = 0.0;

        for(DependsOn dependsOn: dependsOnList){
            long file1Id = dependsOn.getStartNode().getId();
            long file2Id = dependsOn.getEndNode().getId();
            ProjectFile file1 = (ProjectFile) dependsOn.getStartNode();
            ProjectFile file2 = (ProjectFile) dependsOn.getEndNode();
            String filesPair = file1Id + "_" + file2Id;
            String filesPairReverse = file2Id + "_" + file1Id;

            if(!filePairs.contains(filesPair) && !filePairs.contains(filesPairReverse)){
                filePairs.add(filesPair);
                DependsOn dependsOnAtoB = dependsOnRepository.findDependsOnBetweenFiles(file1Id, file2Id);
                DependsOn dependsOnBtoA = dependsOnRepository.findDependsOnBetweenFiles(file2Id, file1Id);

                String typesAndTimesAtoB = "";
                String typesAndTimesBtoA = "";

                if(dependsOnAtoB != null){
                    typesAndTimesAtoB = getRelationTypeAndTimes(dependsOnAtoB.getDependsOnTypes());
                }

                if(dependsOnBtoA != null){
                    typesAndTimesBtoA = getRelationTypeAndTimes(dependsOnBtoA.getDependsOnTypes());
                }

                double C = calDependsOnC(file1Id, file2Id);
                CSum += C;
                double I = calDependsOnI(dependsOnAtoB, dependsOnBtoA);
                ISum += I;
                System.out.println(file1Id + " " + file1.getPath() + " " + file2Id + " " + file2.getPath() + " "
                        + C + " " + I + " " + typesAndTimesAtoB + " " + typesAndTimesBtoA);
            }
        }

        result.add(CSum);
        result.add(ISum);
        return result;
    }

    @Override
    public JSONObject getCouplingValueByFileIds(List<Long> fileIds, Map<Long, Long> parentPckMap){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOnByFileIds(fileIds);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        Map<ProjectFile, Double> instability = calGroupInstablity(fileIds);

        for (ProjectFile projectFile : instability.keySet()) {
            JSONObject fileTmp = new JSONObject();
            fileTmp.put("id", projectFile.getId().toString());
            fileTmp.put("parentPckId", parentPckMap.get(projectFile.getId()).toString());
            fileTmp.put("name", projectFile.getName());
            fileTmp.put("label", projectFile.getName());
            fileTmp.put("path", projectFile.getPath());
            fileTmp.put("LOC", projectFile.getLoc());
            fileTmp.put("nodeType", "file");
            fileTmp.put("instability", instability.get(projectFile));
            nodes.add(fileTmp);
        }
        result.put("nodes", nodes);

        for(DependsOn dependsOn: GroupInsideDependsOns){
            JSONObject dependsOnTmp = new JSONObject();
            boolean flag = false;
            double i = 0.0;
            double dist = -1.0;

            Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
            for (String type : dependsOnTypes.keySet()) {
                if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                        type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                        || type.equals("MEMBER_VARIABLE")) {
                    flag = true;
                    break;
                }
            }
            if(flag){
//                    System.out.println(dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());
                Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                        , dependsOn.getEndNode().getId());
                i += coupling.getI();
                dist = coupling.getDist() > 0 ? coupling.getDist() : -1.0;
            }
            dependsOnTmp.put("I", i);
            dependsOnTmp.put("dist", dist);
            dependsOnTmp.put("id", dependsOn.getStartNode().getId().toString() + "_" + dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("source", dependsOn.getStartNode().getId().toString());
            dependsOnTmp.put("target", dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("dependsOnTypes", dependsOn.getDependsOnType());
            dependsOnTmp.put("isExtendOrImplements", dependsOnRepository.findDependsOnIsExtendOrImplements(dependsOn.getId()));
            dependsOnTmp.put("isTwoWayDependsOn", dependsOnRepository.findIsTwoWayDependsOn(dependsOn.getStartNode().getId(),
                    dependsOn.getEndNode().getId()));
            edges.add(dependsOnTmp);
        }
        result.put("edges", edges);

        return result;
    }

    @Override
    public JSONObject getCouplingValueByPcks(Map<Package, List<Package>> pckMap, Map<Long, Double> parentPcksInstability, boolean isTopLevel){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        List<Package> pckList = new ArrayList<>();

        for(Package parentPck: pckMap.keySet()){
            pckList.addAll(pckMap.get(parentPck));
        }

        Map<Map<Package, Package>, List<DependsOn>> dependsOnBetweenPackages = new HashMap<>();

        for(Package pck: pckList) {
            Double parentInstability = 0.0;
            Package parentPackage = new Package();

            for(Package parentPck: pckMap.keySet()){
                if(pckMap.get(parentPck).contains(pck)){
                    parentPackage = parentPck;
                    if(!isTopLevel) {
                        parentInstability = parentPcksInstability.get(parentPck.getId());
                    }
                }
            }

            JSONObject tmpPck = new JSONObject();
            String pckName = FileUtil.extractPackagePath(pck.getDirectoryPath(), isTopLevel);
            int pckContainsFilesNum = containRepository.findPackageContainAllFilesNum(pck.getId());
            int pckContainsFilesLOC = containRepository.findPackageContainAllFilesLOC(pck.getId());

            tmpPck.put("id", pck.getId().toString());
            tmpPck.put("path", pck.getDirectoryPath());
            tmpPck.put("name", pckName);
            tmpPck.put("LOF", pckContainsFilesNum);
            tmpPck.put("LOC", pckContainsFilesLOC);
            tmpPck.put("label", pckName);
            tmpPck.put("parentPckId", parentPackage.getId().toString());
            tmpPck.put("nodeType", "package");

            List<Map<Package, List<DependsOn>>> listTmp = getGroupInsideAndOutDependsOnByPackage(pck, pckList);

            Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = listTmp.get(0);
            Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = listTmp.get(1);
            int GroupInsideToOutDependsOnTimes = 0;
            int GroupOutToInsideDependsOnTimes = 0;

            if (GroupInsideToOutDependsOns.size() > 0) {
                for(Package endPackage: GroupInsideToOutDependsOns.keySet()){
                    for(DependsOn dependsOn: GroupInsideToOutDependsOns.get(endPackage)){
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupOutToInsideDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupInsideToOutDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(pck, endPackage);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            List<DependsOn> dependsOnsListTmp = new ArrayList<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

            if (GroupOutToInsideDependsOns.size() > 0) {
                for(Package startPackage: GroupOutToInsideDependsOns.keySet()){
                    for(DependsOn dependsOn: GroupOutToInsideDependsOns.get(startPackage)){
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupInsideToOutDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupOutToInsideDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(startPackage, pck);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            List<DependsOn> dependsOnsListTmp = new ArrayList<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

            int allDependsOnTimes = GroupInsideToOutDependsOnTimes + GroupOutToInsideDependsOnTimes;
            double finalInstability = 0.0;
            if (allDependsOnTimes != 0) {
                double pckInstability = (double) GroupInsideToOutDependsOnTimes / (double) allDependsOnTimes;
                if(parentInstability == 0.0){
                    finalInstability = pckInstability;
                }else{
                    finalInstability = (parentInstability - pckInstability) * (1 - parentInstability) + pckInstability;
                }
            }
            tmpPck.put("instability", finalInstability);
            nodes.add(tmpPck);
        }

        for(Map<Package, Package> map: dependsOnBetweenPackages.keySet()){
            JSONObject tmpEdge = new JSONObject();
            int DAtoB = 0;
            int DBtoA = 0;
            double dist = 0.0;
            double distSum = 0;

            for(Package pck: map.keySet()){
                tmpEdge.put("id", pck.getId().toString() + "_" + map.get(pck).getId().toString());
                tmpEdge.put("source", pck.getId().toString());
                tmpEdge.put("target", map.get(pck).getId().toString());
            }

            for(DependsOn dependsOn: dependsOnBetweenPackages.get(map)){
                boolean flag = false;
                Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                for (String type : dependsOnTypes.keySet()) {
                    if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                            type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                            || type.equals("MEMBER_VARIABLE")) {
                        flag = true;
                        break;
                    }
                }
                if(flag){
//                    System.out.println(dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());
                    Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                            , dependsOn.getEndNode().getId());
                    DAtoB += coupling.getDAtoB();
                    DBtoA += coupling.getDBtoA();
                    dist += coupling.getDist();
                    distSum  += 1;
                }
            }
            tmpEdge.put("I", calI(DAtoB, DBtoA));
            tmpEdge.put("dist", dist / distSum);
            tmpEdge.put("dependsOnNum", dependsOnBetweenPackages.get(map).size());
            edges.add(tmpEdge);
        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    private List<List<DependsOn>> getGroupInsideAndOutDependsOnByFileIds(List<Long> fileIdList){
        List<List<DependsOn>> result = new ArrayList<>();
        List<DependsOn> GroupInsideDependsOns = new ArrayList<>();
        List<DependsOn> GroupInsideToOutDependsOns = new ArrayList<>();
        List<DependsOn> GroupOutToInsideDependsOns = new ArrayList<>();


        for(long fileId : fileIdList){
            long endNodeId;
            boolean direction;
            List<DependsOn> allDependsOn = dependsOnRepository.findOneFileAllDependsOn(fileId);

            for(DependsOn dependsOn: allDependsOn){
                if(dependsOn.getStartNode().getId() == fileId){
                    endNodeId = dependsOn.getEndNode().getId();
                    direction = true;
                }else{
                    endNodeId = dependsOn.getStartNode().getId();
                    direction = false;
                }

                if(fileIdList.contains(endNodeId) && !GroupInsideDependsOns.contains(dependsOn)){
                    GroupInsideDependsOns.add(dependsOn);
                }else{
                    if(direction && !GroupInsideToOutDependsOns.contains(dependsOn)){
                        GroupInsideToOutDependsOns.add(dependsOn);
                    }else if(!GroupOutToInsideDependsOns.contains(dependsOn)){
                        GroupOutToInsideDependsOns.add(dependsOn);
                    }
                }
            }
        }
        result.add(GroupInsideDependsOns);
        result.add(GroupInsideToOutDependsOns);
        result.add(GroupOutToInsideDependsOns);
        return result;
    }

    private List<Map<Package, List<DependsOn>>> getGroupInsideAndOutDependsOnByPackage(Package mainPackage, List<Package> pckList){
        List<Map<Package, List<DependsOn>>> result = new ArrayList<>();
        Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = new HashMap<>();
        Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = new HashMap<>();

        for(Package pck: pckList){
            if(!pck.equals(mainPackage)){
                List<DependsOn> tmpInsideToOutDependsOn =
                        dependsOnRepository.findAllDependsOnBetweenPackages(mainPackage.getId(), pck.getId());
                List<DependsOn> tmpOutToInsideDependsOn =
                        dependsOnRepository.findAllDependsOnBetweenPackages(pck.getId(), mainPackage.getId());

                if(tmpInsideToOutDependsOn.size() != 0){
                    GroupInsideToOutDependsOns.put(pck, tmpInsideToOutDependsOn);
                }

                if(tmpOutToInsideDependsOn.size() != 0){
                    GroupOutToInsideDependsOns.put(pck, tmpOutToInsideDependsOn);
                }
            }
        }

        result.add(GroupInsideToOutDependsOns);
        result.add(GroupOutToInsideDependsOns);

        return result;
    }
}
