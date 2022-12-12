package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/coupling")
public class CouplingController {

    @Autowired
    private CouplingService couplingService;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private PackageRepository packageRepository;


    /**
     * 返回一个包下的所有子包（打平）的耦合数据
     * @param requestBody
     * @return
     */
    @PostMapping("/group/all_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getAllChildPackagesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray parentPcks = new JSONArray();
        Map<Long, Double> parentPcksInstability = new HashMap<>();
        Map<Package, List<Package>> pckMap = new HashMap<>();

        JSONArray fileIdsArray = requestBody.getJSONArray("pcks");
        List<Long> pckIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            pckIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(fileIdsArray.getJSONObject(i).getLong("id"),
                    fileIdsArray.getJSONObject(i).getDouble("instability"));
        }

        for(Long pckId: pckIds){
            Package parentPackage = packageRepository.findPackageById(pckId);

            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", parentPackage.getId().toString());
            parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
            parentPckJson.put("name", parentPackage.getName());
            parentPcks.add(parentPckJson);

            List<Package> pckList = new ArrayList<>(packageRepository.findAllChildPackagesById(pckId));
            pckMap.put(parentPackage, pckList);
        }

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, false);
        result.put("parentPackage", parentPcks);

        return result;
    }

    /**
     * 返回一个包下的第一层子包的耦合数据
     * @param requestBody
     * @return
     */
    @PostMapping("/group/one_step_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getOneStepChildPackagesCouplingValue(@RequestBody JSONObject requestBody){
//        JSONArray parentPcks = new JSONArray();
        Map<Long, Double> parentPcksInstability = new HashMap<>();
        Map<Package, List<Package>> pckMap = new HashMap<>();

        JSONArray otherPcks = requestBody.getJSONArray("otherPcks");
        JSONArray unfoldPcks = requestBody.getJSONArray("unfoldPcks");

        List<Long> otherPckIds = new ArrayList<>();
        List<Long> unfoldPckIds = new ArrayList<>();
        for(int i = 0; i < otherPcks.size(); i++){
            otherPckIds.add(otherPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(otherPcks.getJSONObject(i).getLong("id"),
                    otherPcks.getJSONObject(i).getDouble("instability"));
        }

        for(int i = 0; i < unfoldPcks.size(); i++){
            unfoldPckIds.add(unfoldPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(unfoldPcks.getJSONObject(i).getLong("id"),
                    unfoldPcks.getJSONObject(i).getDouble("instability"));
        }

        for(Long pckId: otherPckIds){
            Package pck = packageRepository.findPackageById(pckId);

            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", pck.getId().toString());
            parentPckJson.put("directoryPath", pck.getDirectoryPath());
            parentPckJson.put("name", pck.getName());
            parentPckJson.put("label", pck.getName());
//            parentPcks.add(parentPckJson);

            List<Package> tmpList = new ArrayList<>();
            tmpList.add(pck);
            pckMap.put(pck, tmpList);
        }

        for(Long pckId: unfoldPckIds){
            Package parentPackage = packageRepository.findPackageById(pckId);

            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", parentPackage.getId().toString());
            parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
            parentPckJson.put("name", parentPackage.getName());
//            parentPcks.add(parentPckJson);

            List<Package> pckList = new ArrayList<>(packageRepository.findOneStepPackagesById(pckId));
            if(pckList.size() == 0){
                JSONObject failJson = new JSONObject();
                failJson.put("code", -1);
                failJson.put("pck", parentPckJson);
                return failJson;
            }
            pckMap.put(parentPackage, pckList);
        }

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, false);
        result.put("code", 200);

        return result;
    }

    /**
     * 返回顶层模块的耦合值
     * @param
     * @return
     */
    @GetMapping("/group/top_level_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getTopLevelPackagesCouplingValue(){
        List<Package> topLevelPackages = packageRepository.findPackagesAtDepth1();
        Map<Package, List<Package>> pckMap = new HashMap<>();
        Map<Long, Double> parentPcksInstability = new HashMap<>();

        pckMap.put(containRepository.findPackageInPackage(topLevelPackages.get(0).getId()), topLevelPackages);

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, true);
        result.put("code", 200);

        return result;
    }

    @PostMapping("/group/files_of_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getPackagesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray fileIdsArray = requestBody.getJSONArray("pckIds");
        Map<Long, Long> parentPckMap = new HashMap<>();

        List<Long> pckIds = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            pckIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
        }

        for(Long pckId: pckIds){
            List<ProjectFile> fileList = containRepository.findPackageContainAllFiles(pckId);
            for(ProjectFile projectFile: fileList){
                parentPckMap.put(projectFile.getId(), pckId);
                fileIds.add(projectFile.getId());
            }
        }

        return couplingService.getCouplingValueByFileIds(fileIds, parentPckMap);
    }
}
