package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import cn.edu.fudan.se.multidependency.service.query.smell.BasicSmellQueryService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectServiceImpl implements ProjectService{
    @Autowired
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @Autowired
    private CloneRepository cloneRepository;

    @Autowired
    private CoChangeRepository coChangeRepository;

    @Autowired
    private BasicSmellQueryService basicSmellQueryService;

    private Map<Project, String> projectToAbsolutePath = new ConcurrentHashMap<>();


    @Override
    public Map<String, Boolean> getSelectedPcks() {
        return selectedPcks;
    }

    @Override
    public void setSelectedPcks(Map<String, Boolean> pcks) {
        selectedPcks = pcks;
    }

    private Map<String, Boolean> selectedPcks = new HashMap<>();

    @Override
    public boolean clearSelectedPcks(){
        selectedPcks.clear();
        return true;
    }

    @Override
    public String getAbsolutePath(Project project) {
        if(project == null) {
            return "";
        }
        return projectToAbsolutePath.getOrDefault(project, "");
    }

    @Override
    public void setAbsolutePath(Project project, String path) {
        this.projectToAbsolutePath.put(project, path);
    }

    @Override
    public JSONArray getMultipleProjectsGraphJson(JSONObject dataList, String type) {
        Map<String, Boolean> selectedPcks = new HashMap<>();
        JSONArray projectIds = dataList.getJSONArray("projectIds");

        boolean isFilter;
        if (getSelectedPcks().size() != 0) {
            selectedPcks = getSelectedPcks();
            isFilter = true;
        }else{
            isFilter = false;
        }

        JSONArray result = new JSONArray();
        JSONObject nodeJSON2 = new JSONObject();
        JSONObject nodeJSON4 = new JSONObject();
        JSONObject nodeJSON5 = new JSONObject();

        JSONObject projectJson = new JSONObject();

        if(projectIds.size() == 1){
            if(isFilter && Constant.PROJECT_STRUCTURE_COMBO.equals(type)){
                projectJson = joinMultipleProjectsGraphJson(projectIds.getJSONObject(0).getLong("id"), type, selectedPcks);
            }else{
                projectJson = joinMultipleProjectsGraphJson(projectIds.getJSONObject(0).getLong("id"), type);
            }
        }else{
            projectJson.put("name", "default");
            projectJson.put("id", "default");
            JSONArray multipleProjectsJson = new JSONArray();
            for(int i = 0; i < projectIds.size(); i++){
                JSONArray temp_array = new JSONArray();
                if(isFilter || Constant.PROJECT_STRUCTURE_COMBO.equals(type)) {
                    if(!isFilter){
                        temp_array = joinMultipleProjectsGraphJson(projectIds.getJSONObject(i).getLong("id"), type).getJSONArray("nodes");
                    }else{
                        temp_array = joinMultipleProjectsGraphJson(projectIds.getJSONObject(i).getLong("id"), type, selectedPcks).getJSONArray("nodes");
                    }
                    for(int j = 0; j < temp_array.size(); j++){
                        multipleProjectsJson.add(temp_array.getJSONObject(j));
                    }
                }else{
                    JSONObject temp = joinMultipleProjectsGraphJson(projectIds.getJSONObject(i).getLong("id"), type);

                    multipleProjectsJson.add(temp);
                }

            }
            if(isFilter || Constant.PROJECT_STRUCTURE_COMBO.equals(type)) {
                projectJson.put("nodes", multipleProjectsJson);
            }else{
                projectJson.put("children", multipleProjectsJson);
            }
        }

        nodeJSON2.put("result",projectJson);
        result.add(nodeJSON2);

        if(Constant.PROJECT_STRUCTURE_COMBO.equals(type)){
            JSONArray temp_allprojects = getAllProjectsLinksCombo(projectIds);
            nodeJSON4.put("links", temp_allprojects);
        }else{
            JSONObject temp_allprojects = getAllProjectsLinks();
            nodeJSON4.put("links", temp_allprojects);
        }
        result.add(nodeJSON4);

        if(Constant.PROJECT_STRUCTURE_TREEMAP.equals(type) || Constant.PROJECT_STRUCTURE_COMBO.equals(type)){
            JSONObject smellInfo = new JSONObject();
            for(int i = 0; i < projectIds.size(); i++){

                long projectId = projectIds.getJSONObject(i).getLong("id");
                smellInfo.put("projectId", projectId);
                smellInfo.put("project_smell_info", basicSmellQueryService.smellInfoToGraph(projectId));
            }
            nodeJSON5.put("smell_data", basicSmellQueryService.smellDataToGraph());
            nodeJSON5.put("smell_info", smellInfo);
            result.add(nodeJSON5);
        }

        return result;
    }

    private JSONObject joinMultipleProjectsGraphJson(long projectId, String type, Map<String, Boolean> selectedPcks){
        JSONObject result = new JSONObject();
        Project project = nodeService.queryProject(projectId);
        String language = project.getLanguage();
        ProjectStructure projectStructure = containRelationService.projectStructureInitialize(project);
        Package packageOfProject = projectStructure.getChildren().get(0).getPck();
        List<PackageStructure> childrenPackagesnew = new ArrayList<>();
        for(String pckPath : selectedPcks.keySet()){
            Package pck = nodeService.queryPackage(pckPath, language);
            if (pck != null) {
                if(selectedPcks.get(pckPath)){
                    childrenPackagesnew.add(containRelationService.packageStructureInitialize(pck,type));
                }else{
                    childrenPackagesnew.add(containRelationService.packageStructureInitializeWithNoSubPackages(pck,type));
                }
            }
        }

        if(!Constant.PROJECT_STRUCTURE_COMBO.equals(type)) {
            result.put("name", packageOfProject.getName());
            result.put("id", packageOfProject.getId().toString());
//            Collection<ProjectFile> clonefiles = basicCloneQueryService.findProjectContainCloneFiles(project);
            result.put("children", getPackageContainJson(childrenPackagesnew, type));
        }else{

            JSONArray combo = new JSONArray();

            for(PackageStructure pckstru2 : childrenPackagesnew){
                JSONObject temp = new JSONObject();
                List<PackageStructure> pckList = pckstru2.getChildrenPackages();
                JSONArray temp_children = new JSONArray();
                temp.put("name",pckstru2.getPck().getDirectoryPath());
                temp.put("depth",pckstru2.getPck().getDepth());
                temp.put("id", pckstru2.getPck().getId().toString());

                List<ProjectFile> fileList = pckstru2.getChildrenFiles();
                if(fileList.size() > 0){
                    for(ProjectFile profile : fileList){
                        JSONObject jsonObject2 = new JSONObject();
//                        jsonObject2.put("size",profile.getLoc());
                        jsonObject2.put("size", profile.getLoc() <= 500 ? 20 : (profile.getLoc() <= 1000 ? 25 : (profile.getLoc() <= 2000 ? 30 : 35)));
                        jsonObject2.put("long_name",profile.getPath());
                        jsonObject2.put("name",profile.getName());
                        jsonObject2.put("noc",profile.getNoc());
                        jsonObject2.put("nom",profile.getNom());
                        jsonObject2.put("loc",profile.getLoc());
                        jsonObject2.put("score",profile.getScore());
                        jsonObject2.put("id", profile.getId().toString());
                        temp_children.add(jsonObject2);
                    }
                }

                JSONArray children = getPackageContainJsonCombo(pckList);
                temp_children.addAll(children);
                temp.put("children", temp_children);

                combo.add(temp);
            }

            result.put("nodes", combo);
        }

        return result;
    }

    private JSONObject joinMultipleProjectsGraphJson(long projectId, String type){
        JSONObject result = new JSONObject();
        Project project = nodeService.queryProject(projectId);
        ProjectStructure projectStructure = containRelationService.projectStructureInitialize(project);
        Package packageOfProject = projectStructure.getChildren().get(0).getPck();

        List<PackageStructure> childrenPackages = containRelationService.packageStructureInitialize(packageOfProject,type).getChildrenPackages();
        List<PackageStructure> childrenPackagesnew = new ArrayList<>();

        for(PackageStructure pckstru : childrenPackages){
            PackageStructure pcknew = containRelationService.packageStructureInitialize(pckstru.getPck(),type);
            childrenPackagesnew.add(pcknew);
        }
        if(!Constant.PROJECT_STRUCTURE_COMBO.equals(type)) {
            result.put("name", packageOfProject.getName());
            result.put("id", packageOfProject.getId().toString());
//            Collection<ProjectFile> clonefiles = basicCloneQueryService.findProjectContainCloneFiles(project);
            result.put("children", getPackageContainJson(childrenPackagesnew, type));
        }else{

            JSONArray combo = new JSONArray();

            for(PackageStructure pckstru2 : childrenPackagesnew){
                JSONObject temp = new JSONObject();
                List<PackageStructure> pckList = pckstru2.getChildrenPackages();
                JSONArray temp_children = new JSONArray();
                temp.put("name",pckstru2.getPck().getDirectoryPath());
                temp.put("depth",pckstru2.getPck().getDepth());
                temp.put("id", pckstru2.getPck().getId().toString());

                List<ProjectFile> fileList = pckstru2.getChildrenFiles();
                if(fileList.size() > 0){
                    for(ProjectFile profile : fileList){
                        JSONObject jsonObject2 = new JSONObject();
//                        jsonObject2.put("size",profile.getLoc());
                        jsonObject2.put("size", profile.getLoc() <= 500 ? 20 : (profile.getLoc() <= 1000 ? 25 : (profile.getLoc() <= 2000 ? 30 : 35)));
                        jsonObject2.put("long_name",profile.getPath());
                        jsonObject2.put("name",profile.getName());
                        jsonObject2.put("noc",profile.getNoc());
                        jsonObject2.put("nom",profile.getNom());
                        jsonObject2.put("loc",profile.getLoc());
                        jsonObject2.put("score",profile.getScore());
                        jsonObject2.put("id", profile.getId().toString());
                        temp_children.add(jsonObject2);
                    }
                }

                JSONArray children = getPackageContainJsonCombo(pckList);
                temp_children.addAll(children);
                temp.put("children", temp_children);


                combo.add(temp);
            }

            result.put("nodes", combo);
        }

        return result;
    }

    @Override
    /**
     * 递归遍历项目中所有package的包含关系
     */
    public JSONArray getPackageContainJson(List<PackageStructure> childrenPackages,String type){
        JSONArray result = new JSONArray();
        for(PackageStructure pckstru :childrenPackages){
            List<PackageStructure> pckList = pckstru.getChildrenPackages();
            List<ProjectFile> fileList = pckstru.getChildrenFiles();
            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("name",pckstru.getPck().getName());
            jsonObject.put("name",pckstru.getPck().getDirectoryPath());
//			jsonObject.put("long_name",pckstru.getPck().getDirectoryPath());
            jsonObject.put("size",fileList.size());
            jsonObject.put("depth",pckstru.getPck().getDepth());
            jsonObject.put("id","id_" + pckstru.getPck().getId().toString());
            float cloneFilesInAllFiles = 0;
//            if(fileList.size() > 0){
//                for(ProjectFile profile : fileList){
//                    if(clonefiles.contains(profile)){
//                        cloneFilesInAllFiles += 1;
//                    }
//                }
//            }
//            if(fileList.size() > 0){
//                jsonObject.put("clone_ratio",cloneFilesInAllFiles / (float)(fileList.size()));
//            }else{
//                jsonObject.put("clone_ratio", 0);
//            }

            if(Constant.PROJECT_STRUCTURE_TREEMAP.equals(type)){
                JSONArray jsonArray = new JSONArray();
                if(fileList.size() > 0){
                    for(ProjectFile profile : fileList){
                        JSONObject jsonObject2 = new JSONObject();
                        jsonObject2.put("size",profile.getLoc());
//					jsonObject2.put("value",profile.getLoc());
                        jsonObject2.put("long_name",profile.getPath());
//                        if(clonefiles.contains(profile)){
//                            jsonObject2.put("clone",true);
//                        }else{
//                            jsonObject2.put("clone",false);
//                        }
                        jsonObject2.put("name",profile.getName());
                        jsonObject2.put("id", profile.getId().toString());
                        jsonArray.add(jsonObject2);
                    }
                }

                if(jsonArray.size() > 0){
                    jsonObject.put("children",jsonArray);
                }
            }

            if(pckList.size()>0){
                //如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
                jsonObject.put("children", getPackageContainJson(pckList,type));
            }
            result.add(jsonObject);
        }
        return result;
    }

    @Override
    public JSONArray getPackageContainJsonCombo(List<PackageStructure> childrenPackages){
        JSONArray result = new JSONArray();
        for(PackageStructure pckstru :childrenPackages){
            List<PackageStructure> pckList = pckstru.getChildrenPackages();
            List<ProjectFile> fileList = pckstru.getChildrenFiles();
//            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("name",pckstru.getPck().getName());
//            jsonObject.put("path",pckstru.getPck().getDirectoryPath());
//            jsonObject.put("depth",pckstru.getPck().getDepth());
//            jsonObject.put("id","id_" + pckstru.getPck().getId().toString());
            float cloneFilesInAllFiles = 0;

            if(fileList.size() > 0){
                for(ProjectFile profile : fileList){
                    JSONObject jsonObject2 = new JSONObject();
//                    jsonObject2.put("size",profile.getLoc());
                    jsonObject2.put("size", profile.getLoc() <= 500 ? 20 : (profile.getLoc() <= 1000 ? 25 : (profile.getLoc() <= 2000 ? 30 : 35)));
                    jsonObject2.put("long_name",profile.getPath());
                    jsonObject2.put("name",profile.getName());
                    jsonObject2.put("noc",profile.getNoc());
                    jsonObject2.put("nom",profile.getNom());
                    jsonObject2.put("loc",profile.getLoc());
                    jsonObject2.put("score",profile.getScore());
                    jsonObject2.put("id", profile.getId().toString());
                    result.add(jsonObject2);
                }
            }

            if(pckList.size()>0){
                //如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
                JSONArray temp_children = getPackageContainJsonCombo(pckList);
                result.addAll(temp_children);
            }
        }
        return result;
    }

    @Override
    public JSONObject getAllProjectsLinks(){
        List<HotspotPackagePair> cloneHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithFileCloneByParentId(-1, -1, "all");
        List<HotspotPackagePair> dependsonHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithDependsOn();
        List<HotspotPackagePair> cochangeHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithCoChange();
        return hotspotPackagesToJson(cloneHotspotPackageList, dependsonHotspotPackageList, cochangeHotspotPackageList);
    }

    @Override
    public JSONArray getAllProjectsLinksCombo(JSONArray projectIds){
        JSONArray result = new JSONArray();
        List<DependsOn> dependsOnList= new ArrayList<>();
        List<Clone> cloneList= new ArrayList<>();
        List<CoChange> coChangeList= new ArrayList<>();

        for(int i=0; i<projectIds.size(); i++){
            Project project = nodeService.queryProject(projectIds.getJSONObject(i).getLong("id"));
            cloneList.addAll(cloneRepository.findFileClonesInProjectByPathAndLanguage(project.getPath(), project.getLanguage()));
            coChangeList.addAll(coChangeRepository.findFileCoChangeInProjectByPathAndLanguage(project.getPath(), project.getLanguage()));
            dependsOnList.addAll(dependsOnRepository.findFileDependsInProjectByPathAndLanguage(project.getPath(), project.getLanguage()));
        }

//        List<DependsOn> dependsOnList= dependsOnRepository.findFileDepends();
//        List<Clone> cloneList= cloneRepository.findAllFileClones();
//        List<CoChange> coChangeList= coChangeRepository.findFileCoChange();

        for(DependsOn dependsOn : dependsOnList){
            JSONObject link = new JSONObject();
            link.put("type", "dependson");
            link.put("source_id", dependsOn.getStartNode().getId().toString());
            link.put("target_id", dependsOn.getEndNode().getId().toString());
            link.put("source_path", ((ProjectFile)dependsOn.getStartNode()).getPath());
            link.put("target_path", ((ProjectFile)dependsOn.getEndNode()).getPath());
            link.put("source_name", dependsOn.getStartNode().getName());
            link.put("target_name", dependsOn.getEndNode().getName());
            link.put("pair_id", dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());

            link.put("dependsOnTypes", dependsOn.getDependsOnType());
            link.put("dependsOnTimes", dependsOn.getTimes());
            link.put("dependsOnWeightedTimes", dependsOn.getWeightedTimes());

            JSONArray dependsOnTypesArray = new JSONArray();
            Map<String, Long> dependsOnTypesMap = dependsOn.getDependsOnTypes();
            for(Map.Entry<String, Long> entry: dependsOnTypesMap.entrySet()){
                JSONObject temp_dependsOnType = new JSONObject();
                temp_dependsOnType.put("dependsOnType", entry.getKey());
                temp_dependsOnType.put("dependsOnTime", entry.getValue());

                dependsOnTypesArray.add(temp_dependsOnType);
            }

            link.put("dependsOnTypesMap", dependsOnTypesArray);

            result.add(link);
        }

        for(Clone clone : cloneList){
            JSONObject link = new JSONObject();
            link.put("type", "clone");
            link.put("source_id", clone.getCodeNode1().getId().toString());
            link.put("target_id", clone.getCodeNode2().getId().toString());
            link.put("source_name", clone.getCodeNode1().getName());
            link.put("target_name", clone.getCodeNode2().getName());
            link.put("pair_id", clone.getCodeNode1().getId() + "_" + clone.getCodeNode2().getId());

            link.put("value", clone.getValue());
            link.put("cloneRelationType", clone.getCloneRelationType());
            link.put("cloneType", clone.getCloneType());
            result.add(link);
        }

        for(CoChange coChange : coChangeList){
            if(coChange.getTimes() >= 3){
                JSONObject link = new JSONObject();
                link.put("type", "cochange");
                link.put("source_id", coChange.getNode1().getId().toString());
                link.put("target_id", coChange.getNode2().getId().toString());
                link.put("source_name", coChange.getNode1().getName());
                link.put("target_name", coChange.getNode2().getName());
                link.put("pair_id", coChange.getNode1().getId() + "_" + coChange.getNode2().getId());

                link.put("coChangeTimes", coChange.getTimes());
                link.put("node1ChangeTimes", coChange.getNode1ChangeTimes());
                link.put("node2ChangeTimes", coChange.getNode2ChangeTimes());
                result.add(link);
            }
        }
        return result;
    }

    public JSONArray getSelectedPackageLinks(Map<String, Boolean> selectedPcks) {
        if (selectedPcks == null) {
            return new JSONArray();
        }
        Set<ProjectFile> files = new HashSet<>();
        for (Map.Entry<String, Boolean> entry : selectedPcks.entrySet()) {
            String packagePath = entry.getKey();
            Package selectedPck = nodeService.queryPackage(packagePath, "java");
            if (selectedPck != null) {
                Collection<ProjectFile> result = new ArrayList<>();
                if (entry.getValue()) {
                    result = containRelationService.findPackageContainAllFiles(selectedPck);
                } else {
                    result = containRelationService.findPackageContainFiles(selectedPck);
                }
                files.addAll(result);
            }
        }
        JSONArray links = new JSONArray();
        for (ProjectFile rowFile : files) {
            for (ProjectFile colFile : files) {
                DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenFiles(rowFile.getId(), colFile.getId());
                if (dependsOn != null) {
                    JSONObject dependsOnObject = new JSONObject();
                    dependsOnObject.put("source_name", rowFile.getName());
                    dependsOnObject.put("target_name", colFile.getName());
                    dependsOnObject.put("source_id", rowFile.getId().toString());
                    dependsOnObject.put("target_id", colFile.getId().toString());
                    dependsOnObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    dependsOnObject.put("dependsOnTypes", dependsOn.getDependsOnType());
                    dependsOnObject.put("type", "dependson");
                    links.add(dependsOnObject);
                }

                List<Clone> clones = cloneRepository.judgeCloneByFileId(rowFile.getId(), colFile.getId());
                if (clones.size() > 0) {
                    JSONObject cloneObject = new JSONObject();
                    cloneObject.put("source_name", rowFile.getName());
                    cloneObject.put("target_name", colFile.getName());
                    cloneObject.put("source_id", rowFile.getId().toString());
                    cloneObject.put("target_id", colFile.getId().toString());
                    cloneObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    cloneObject.put("type", "clone");
                    links.add(cloneObject);
                }

                CoChange coChange = coChangeRepository.findCoChangesBetweenTwoFiles(rowFile.getId(), colFile.getId());
                if (coChange != null) {
                    JSONObject coChangeObject = new JSONObject();
                    coChangeObject.put("source_name", rowFile.getName());
                    coChangeObject.put("target_name", colFile.getName());
                    coChangeObject.put("source_id", rowFile.getId().toString());
                    coChangeObject.put("target_id", colFile.getId().toString());
                    coChangeObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    coChangeObject.put("type", "cochange");
                    links.add(coChangeObject);
                }
            }
        }
        return links;
    }

    public JSONObject getSelectedPackageLinks() {
        Map<String, Boolean> selectedPcks = new HashMap<>();
        selectedPcks.put("/atlas/addons/", true);
        selectedPcks.put("/atlas/authorization/src/main/java/org/apache/atlas/authorize/", true);
        selectedPcks.put("/atlas/client/", true);
        selectedPcks.put("/atlas/common/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/graphdb/", true);
        selectedPcks.put("/atlas/intg/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/notification/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/plugin-classloader/src/main/java/org/apache/atlas/plugin/classloader/", true);
        selectedPcks.put("/atlas/repository/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/server-api/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/tools/", true);
        selectedPcks.put("/atlas/webapp/src/main/java/org/apache/atlas/", true);

        Set<Package> packages = new HashSet<>();
        for (Map.Entry<String, Boolean> pck : selectedPcks.entrySet()) {
            String pckPath = pck.getKey();
            if (pck.getValue()) {
                packages.add(nodeService.queryPackage(pckPath, "java"));
            }
        }
        JSONObject packageData = new JSONObject();
        JSONArray packageObjects = new JSONArray();
        JSONArray linkObjects = new JSONArray();
        Set<ProjectFile> files = new HashSet<>();
        for (Package pck : packages) {
            files.addAll(containRelationService.findPackageContainFiles(pck));
        }
        packages.clear();
        Map<Long, JSONArray> childrenMap = new HashMap<>();
        for (ProjectFile rowFile : files) {
            Package pck = containRelationService.findFileBelongToPackage(rowFile);
            packages.add(pck);
            JSONObject fileObject = new JSONObject();
            fileObject.put("id", rowFile.getId().toString());
            fileObject.put("name", rowFile.getName());
            fileObject.put("path", rowFile.getPath());
            JSONArray children = childrenMap.getOrDefault(pck.getId(), new JSONArray());
            children.add(fileObject);
            childrenMap.put(pck.getId(), children);
            for (ProjectFile colFile : files) {

                DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenFiles(rowFile.getId(), colFile.getId());
                if (dependsOn != null) {
                    JSONObject dependsOnObject = new JSONObject();
                    dependsOnObject.put("source_name", rowFile.getName());
                    dependsOnObject.put("target_name", colFile.getName());
                    dependsOnObject.put("source_id", rowFile.getId().toString());
                    dependsOnObject.put("target_id", colFile.getId().toString());
                    dependsOnObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    dependsOnObject.put("dependsOnTypes", dependsOn.getDependsOnType());
                    dependsOnObject.put("type", "dependson");
                    linkObjects.add(dependsOnObject);
                }

                List<Clone> clones = cloneRepository.judgeCloneByFileId(rowFile.getId(), colFile.getId());
                if (clones.size() > 0) {
                    JSONObject cloneObject = new JSONObject();
                    cloneObject.put("source_name", rowFile.getName());
                    cloneObject.put("target_name", colFile.getName());
                    cloneObject.put("source_id", rowFile.getId().toString());
                    cloneObject.put("target_id", colFile.getId().toString());
                    cloneObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    cloneObject.put("type", "clone");
                    linkObjects.add(cloneObject);
                }

                CoChange coChange = coChangeRepository.findCoChangesBetweenTwoFiles(rowFile.getId(), colFile.getId());
                if (coChange != null) {
                    JSONObject coChangeObject = new JSONObject();
                    coChangeObject.put("source_name", rowFile.getName());
                    coChangeObject.put("target_name", colFile.getName());
                    coChangeObject.put("source_id", rowFile.getId().toString());
                    coChangeObject.put("target_id", colFile.getId().toString());
                    coChangeObject.put("pair_id", rowFile.getId().toString() + "_" + colFile.getId().toString());
                    coChangeObject.put("type", "cochange");
                    linkObjects.add(coChangeObject);
                }
            }
        }
        for (Package pck : packages) {
            JSONObject packageObject = new JSONObject();
            packageObject.put("id", pck.getId().toString());
            packageObject.put("name", pck.getName());
            packageObject.put("depth", pck.getDepth());
            packageObject.put("children", childrenMap.getOrDefault(pck.getId(), new JSONArray()));
            packageObjects.add(packageObject);
        }
        packageData.put("nodes", packageObjects);
        packageData.put("links", linkObjects);
        System.out.println(packageData);
        return packageData;
    }

    private JSONObject hotspotPackagesToJson(List<HotspotPackagePair> cloneHotspotPackageList, List<HotspotPackagePair> dependsonHotspotPackageList, List<HotspotPackagePair> cochangeHotspotPackageList){
        JSONObject result = new JSONObject();

        result.put("clone_links", getLinksJson(cloneHotspotPackageList, "CLONE", "default"));
        result.put("dependson_links", getLinksJson(dependsonHotspotPackageList, "DEPENDS_ON", "default"));
        result.put("cochange_links", getLinksJson(cochangeHotspotPackageList, "CO_CHANGE", "default"));
        return result;
    }

    private JSONArray getLinksJson(List<HotspotPackagePair> hotspotPackagePairList, String linkType, String parentPairId){
        JSONArray result = new JSONArray();
        for(HotspotPackagePair hotspotPackagePair: hotspotPackagePairList){
            JSONObject link = new JSONObject();
            JSONObject link_common = new JSONObject();

            Project source_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
            Project target_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage2());

            link_common.put("source_id", hotspotPackagePair.getPackage1().getId().toString());
            link_common.put("target_id", hotspotPackagePair.getPackage2().getId().toString());
            link_common.put("depth", Math.max(hotspotPackagePair.getPackage1().getDepth(), hotspotPackagePair.getPackage2().getDepth()));
            link_common.put("pair_id", hotspotPackagePair.getPackage1().getId().toString() + "_" + hotspotPackagePair.getPackage2().getId().toString());
            link_common.put("parent_pair_id", parentPairId);
            link_common.put("source_name", hotspotPackagePair.getPackage1().getDirectoryPath());
            link_common.put("target_name", hotspotPackagePair.getPackage2().getDirectoryPath());
            link_common.put("source_projectBelong", source_projectBelong.getId().toString());
            link_common.put("target_projectBelong", target_projectBelong.getId().toString());

            switch (linkType) {
                case "CLONE":
                    CloneRelationDataForDoubleNodes<Node, Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    link.put("type", "clone");
                    link.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());
                    link.put("cloneNodesCount1", cloneRelationDataForDoubleNodes.getCloneNodesCount1());
                    link.put("cloneNodesCount2", cloneRelationDataForDoubleNodes.getCloneNodesCount2());
                    link.put("allNodesCount1", cloneRelationDataForDoubleNodes.getAllNodesCount1());
                    link.put("allNodesCount2", cloneRelationDataForDoubleNodes.getAllNodesCount2());
                    link.put("cloneMatchRate", cloneRelationDataForDoubleNodes.getCloneMatchRate());
                    link.put("cloneNodesLoc1", cloneRelationDataForDoubleNodes.getCloneNodesLoc1());
                    link.put("cloneNodesLoc2", cloneRelationDataForDoubleNodes.getCloneNodesLoc2());
                    link.put("allNodesLoc1", cloneRelationDataForDoubleNodes.getAllNodesLoc1());
                    link.put("allNodesLoc2", cloneRelationDataForDoubleNodes.getAllNodesLoc2());
                    link.put("cloneLocRate", cloneRelationDataForDoubleNodes.getCloneLocRate());
                    link.put("cloneNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
                    link.put("allNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
                    link.put("cloneCoChangeRate", cloneRelationDataForDoubleNodes.getCloneCoChangeRate());
                    link.put("cloneType1Count", cloneRelationDataForDoubleNodes.getCloneType1Count());
                    link.put("cloneType2Count", cloneRelationDataForDoubleNodes.getCloneType2Count());
                    link.put("cloneType3Count", cloneRelationDataForDoubleNodes.getCloneType3Count());
                    link.put("cloneType", cloneRelationDataForDoubleNodes.getCloneType());
                    link.put("cloneSimilarityValue", cloneRelationDataForDoubleNodes.getCloneSimilarityValue());
                    link.put("cloneSimilarityRate", cloneRelationDataForDoubleNodes.getCloneSimilarityRate());
                    if (cloneRelationDataForDoubleNodes.getClonePairs() == 0) {
                        link.put("bottom_package", false);
                    } else {
                        link.put("bottom_package", true);
                    }

                    result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
                    break;
                case "DEPENDS_ON":
                    DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = (DependsRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    link.put("type", "dependson");
                    link.put("dependsOnTypes", dependsRelationDataForDoubleNodes.getDependsOnTypes());
                    link.put("dependsByTypes", dependsRelationDataForDoubleNodes.getDependsByTypes());
                    link.put("dependsOnTimes", dependsRelationDataForDoubleNodes.getDependsOnTimes());
                    link.put("dependsByTimes", dependsRelationDataForDoubleNodes.getDependsByTimes());
                    link.put("dependsOnWeightedTimes", dependsRelationDataForDoubleNodes.getDependsOnWeightedTimes());
                    link.put("dependsByWeightedTimes", dependsRelationDataForDoubleNodes.getDependsByWeightedTimes());
                    link.put("dependsOnIntensity", dependsRelationDataForDoubleNodes.getDependsOnInstability());
                    link.put("dependsByIntensity", dependsRelationDataForDoubleNodes.getDependsByInstability());
                    link.put("bottom_package", !hotspotPackagePair.isAggregatePackagePair());

                    if(dependsRelationDataForDoubleNodes.getDependsOnTypes().equals("") ||
                            dependsRelationDataForDoubleNodes.getDependsByTypes().equals("")){
                        link.put("two_way", false);
                    }else{
                        link.put("two_way", true);
                    }

                    Map<String, Long> dependsOnTypesMap = dependsRelationDataForDoubleNodes.getDependsOnTypesMap();
                    Map<String, Long> dependsByTypesMap = dependsRelationDataForDoubleNodes.getDependsByTypesMap();
                    JSONArray dependsOnTypesArray = new JSONArray();
                    JSONArray dependsByTypesArray = new JSONArray();

                    for(Map.Entry<String, Long> entry: dependsOnTypesMap.entrySet()){
                        JSONObject temp_dependsOnType = new JSONObject();
                        temp_dependsOnType.put("dependsOnType", entry.getKey());
                        temp_dependsOnType.put("dependsOnTime", entry.getValue());

                        dependsOnTypesArray.add(temp_dependsOnType);
                    }

                    for(Map.Entry<String, Long> entry: dependsByTypesMap.entrySet()){
                        JSONObject temp_dependsByType = new JSONObject();
                        temp_dependsByType.put("dependsByType", entry.getKey());
                        temp_dependsByType.put("dependsByTime", entry.getValue());

                        dependsByTypesArray.add(temp_dependsByType);
                    }

                    link.put("dependsOnTypesMap", dependsOnTypesArray);
                    link.put("dependsByTypesMap", dependsByTypesArray);

                    result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));

                    break;
                case "CO_CHANGE":
                    CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = (CoChangeRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    if (coChangeRelationDataForDoubleNodes.getCoChangeTimes() >= 3) {
                        link.put("type", "cochange");
                        link.put("coChangeTimes", coChangeRelationDataForDoubleNodes.getCoChangeTimes());
                        link.put("node1ChangeTimes", coChangeRelationDataForDoubleNodes.getNode1ChangeTimes());
                        link.put("node2ChangeTimes", coChangeRelationDataForDoubleNodes.getNode2ChangeTimes());
                        link.put("bottom_package", !hotspotPackagePair.isAggregatePackagePair());
                        result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
                    }
                    break;
            }

            if(hotspotPackagePair.hasChildrenHotspotPackagePairs()){
//                System.out.println(hotspotPackagePair.getHotspotRelationType());
                result.addAll(getLinksJson(hotspotPackagePair.getChildrenHotspotPackagePairs(), linkType, hotspotPackagePair.getPackage1().getId().toString() + "_" + hotspotPackagePair.getPackage2().getId().toString()));
            }
        }
        return result;
    }
}

