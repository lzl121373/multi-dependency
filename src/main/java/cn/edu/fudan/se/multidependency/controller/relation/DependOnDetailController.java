package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping("/dependon")
public class DependOnDetailController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @Autowired
    private CoChangeRepository coChangeRepository;

    @GetMapping("")
    public String index(HttpServletRequest request, @RequestParam("pck1")Long id1, @RequestParam("pck2")Long id2){
        request.setAttribute("pck1", id1);
        request.setAttribute("pck2", id2);
        return"/relation/dependonpair";
    }

    @GetMapping("/packagepair/metrics")
    @ResponseBody
    public JSONObject depemdOnAndNone(@RequestParam("id1")Long id1, @RequestParam("id2")Long id2){
        Package pck1 = nodeService.queryPackage(id1);
        Package pck2 = nodeService.queryPackage(id2);
        JSONObject result = new JSONObject();
        Collection<ProjectFile> Files1 = containRelationService.findPackageContainFiles(pck1);
        Collection<ProjectFile> Files2 = containRelationService.findPackageContainFiles(pck2);
        Map<String,Long> dependon1 = new HashMap<>();
        Map<String,Long> dependon2 = new HashMap<>();
        int i = 0;
        for(ProjectFile file1 : Files1){
            int j = 0;
            for(ProjectFile file2 : Files2) {
                if (dependsOnRepository.findDependsOnBetweenFiles(file1.getId(), file2.getId()) != null) {
                    DependsOn dependOn = dependsOnRepository.findDependsOnBetweenFiles(file1.getId(), file2.getId());
                    Map<String,Long> dependOn1Types = dependOn.getDependsOnTypes();
                    for (String key:
                            dependOn1Types.keySet()) {
                        if(dependon1.containsKey(key)){
                            dependon1.put(key, dependon1.get(key) + dependOn1Types.get(key));
                        }else{
                            dependon1.put(key, dependOn1Types.get(key));
                        }
                    }
                }
                if (dependsOnRepository.findDependsOnBetweenFiles(file2.getId(), file1.getId()) != null) {
                    DependsOn dependOn = dependsOnRepository.findDependsOnBetweenFiles(file2.getId(), file1.getId());
                    Map<String,Long> dependOn2Types = dependOn.getDependsOnTypes();
                    for (String key:
                            dependOn2Types.keySet()) {
                        if(dependon2.containsKey(key)){
                            dependon2.put(key, dependon2.get(key) + dependOn2Types.get(key));
                        }else{
                            dependon2.put(key, dependOn2Types.get(key));
                        }
                    }
                }
                j++;
            }
            i++;
        }
        result.put("path1", pck1.getDirectoryPath());
        result.put("path2", pck2.getDirectoryPath());
        result.put("loc1", pck1.getLoc());
        result.put("loc2", pck2.getLoc());
        result.put("pck1num", Files1.size());
        result.put("pck2num", Files2.size());
        result.put("dependon1types", printDependsOnTypes(dependon1));
        result.put("dependon2types", printDependsOnTypes(dependon2));
        if(coChangeRepository.findPackageCoChangeByPackageId(id1, id2) != null){
            result.put("cochange", coChangeRepository.findPackageCoChangeByPackageId(id1, id2).getTimes());
        }
        return result;
    }

    @GetMapping("/packagepair/matrix")
    @ResponseBody
    public JSONObject getPackagePairMatrix(@RequestParam("id1")Long id1, @RequestParam("id2")Long id2){
        Package pck1 = nodeService.queryPackage(id1);
        Package pck2 = nodeService.queryPackage(id2);
        Collection<ProjectFile> allFiles = containRelationService.findPackageContainFiles(pck1);
        int numOfFile1 = allFiles.size();
        allFiles.addAll(containRelationService.findPackageContainFiles(pck2));
        String[][] dependsOnMatrix = new String[allFiles.size()][allFiles.size()];
        int i = 0;
        for(ProjectFile file1 : allFiles){
            int j = 0;
            for(ProjectFile file2 : allFiles) {
                if (dependsOnRepository.findDependsOnBetweenFiles(file1.getId(), file2.getId()) != null) {
                    DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenFiles(file1.getId(), file2.getId());
                    Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                    for (String key:
                            dependsOnTypes.keySet()) {
                        if(dependsOnMatrix[i][j] == null){
                            dependsOnMatrix[i][j] = "";
                        }
                        if(!dependsOnMatrix[i][j].equals("")){
                            dependsOnMatrix[i][j] += "/";
                        }
                        dependsOnMatrix[i][j] += RelationType.relationAbbreviation.get(RelationType.valueOf(key));
                        dependsOnMatrix[i][j] += "(" + dependsOnTypes.get(key).toString() + ")";
                    }
                    if(dependsOnMatrix[i][j] != null && !"".equals(dependsOnMatrix[i][j])){
                        double weightedTimes = dependsOn.getWeightedTimes() ;
                        BigDecimal newWeightedTimes  =  new BigDecimal(weightedTimes);
                        newWeightedTimes = newWeightedTimes.setScale(2, RoundingMode.HALF_UP);
                        dependsOnMatrix[i][j] = "(" +  dependsOn.getTimes() + ", " + newWeightedTimes.doubleValue() + "): " + dependsOnMatrix[i][j];
                    }
                }
                j++;
            }
            i++;
        }
        JSONObject result = new JSONObject();
        result.put("allfiles",allFiles);
        result.put("numofpck1",numOfFile1);
        result.put("matrix",dependsOnMatrix);
        return result;
    }

    private String printDependsOnTypes(Map<String, Long> dependsOnTypes){
        String result = new String("");
        for (String key:
                dependsOnTypes.keySet()) {
            if(!result.equals("")){
                result += "/";
            }
            result += RelationType.relationAbbreviation.get(RelationType.valueOf(key));
            result += "(" + dependsOnTypes.get(key).toString() + ")";
        }
        return result;
    }
}
