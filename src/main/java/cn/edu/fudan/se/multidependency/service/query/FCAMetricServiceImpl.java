package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.DependsRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FCAMetricServiceImpl implements FCAMetricService{
    @Autowired
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @Autowired
    private CloneRepository cloneRepository;

    public void getFCAPackageDependsOnMetric() throws IOException {
        List<HotspotPackagePair> dependsList = hotspotPackagePairDetector.getHotspotPackagePairWithDependsOn();
        Map<Package, Map<String, Package>> MetricMap = new HashMap<>();
        Map<Package, Integer> EntityMap = new HashMap<>();
        Integer entityNum = 0;

        for(HotspotPackagePair hotspotPackagePair : dependsList){
            DependsRelationDataForDoubleNodes dependsRelationDataForDoubleNodes = (DependsRelationDataForDoubleNodes)hotspotPackagePair.getPackagePairRelationData();

            if(!MetricMap.containsKey(hotspotPackagePair.getPackage1())){
                Map<String, Package> package1Map = new HashMap<>();
                Map<String, Long> dependsOnTypesMap = dependsRelationDataForDoubleNodes.getDependsOnTypesMap();
                for(String key: dependsOnTypesMap.keySet()){
                    if(key.equals(RelationType.str_EXTENDS) || key.equals(RelationType.str_IMPLEMENTS)){
                        package1Map.put(key, hotspotPackagePair.getPackage2());
                    }
                }
                MetricMap.put(hotspotPackagePair.getPackage1(), package1Map);
                EntityMap.put(hotspotPackagePair.getPackage1(), entityNum);
                entityNum++;
            }

            if(!MetricMap.containsKey(hotspotPackagePair.getPackage2())){
                Map<String, Package> package2Map = new HashMap<>();
                Map<String, Long> dependsByTypesMap = dependsRelationDataForDoubleNodes.getDependsByTypesMap();
                for(String key: dependsByTypesMap.keySet()){
                    if(key.equals(RelationType.str_EXTENDS) || key.equals(RelationType.str_IMPLEMENTS)){
                        package2Map.put(key, hotspotPackagePair.getPackage1());
                    }
                }
                MetricMap.put(hotspotPackagePair.getPackage2(), package2Map);
                EntityMap.put(hotspotPackagePair.getPackage2(), entityNum);
                entityNum++;
            }
        }

        String inputStringAll = "";
        String inputEntity = "";
        String inputAttr = "";
        String inputMetric = "";

        for(Map.Entry<Package, Map<String, Package>> entry : MetricMap.entrySet()){
            inputEntity += entry.getKey().getDirectoryPath() + " ";
            inputAttr += "EXTENDS_" + entry.getKey().getDirectoryPath() + " IMPL_" + entry.getKey().getDirectoryPath() + " ";
            ArrayList<Integer> relationAttr = new ArrayList<>();
            for(Map.Entry<String, Package> entry2: entry.getValue().entrySet()){
                if(entry2.getKey().equals(RelationType.str_EXTENDS)){
                    relationAttr.add(EntityMap.get(entry2.getValue()) * 2 + 1);
                }else if(entry2.getKey().equals(RelationType.str_IMPLEMENTS)){
                    relationAttr.add(EntityMap.get(entry2.getValue()) * 2 + 2);
                }
            }

            for(int i = 0; i < MetricMap.size() * 2; i++){
                if(!relationAttr.contains(i)){
                    inputMetric += "0 ";
                }else {
                    inputMetric += "1 ";
                }
            }
            inputMetric += "\r\n";
        }

        inputStringAll = MetricMap.size() + "\r\n" + MetricMap.size() * 2 + "\r\n"
                + inputEntity + "\r\n" + inputAttr + "\r\n" + inputMetric;

        File file = new File("./metric.txt");
        if(!file.getParentFile().exists()){ //如果文件的目录不存在
            file.getParentFile().mkdirs(); //创建目录

        }

        OutputStream output = new FileOutputStream(file);

        byte data[] = inputStringAll.getBytes();
        output.write(data);
        output.close();
    }

    @Override
    public void getFCAFileDependsOnMetric() throws IOException{
        List<DependsOn> dependsOnList = dependsOnRepository.findFileDepends();
//        System.out.println(dependsOnList);
        List<Clone> cloneList = cloneRepository.findAllFileClones();
//        System.out.println(cloneList);
        Map<ProjectFile, Map<String, ProjectFile>> MetricMap = new HashMap<>();
        ArrayList<ProjectFile> EntityList = new ArrayList<>();
        Integer entityNum = 0;
        int dependsNum = 0;

        for(DependsOn dependsOn : dependsOnList){
            ProjectFile startNode = (ProjectFile) dependsOn.getStartNode();
            ProjectFile endNode = (ProjectFile) dependsOn.getEndNode();
            if(!MetricMap.containsKey(startNode)){
                Map<String, ProjectFile> file1Map = new HashMap<>();
                Map<String, Long> dependsOnTypesMap = dependsOn.getDependsOnTypes();
                for(String key: dependsOnTypesMap.keySet()){
                    if(key.equals(RelationType.str_EXTENDS) || key.equals(RelationType.str_IMPLEMENTS)
                            || key.equals(RelationType.str_IMPLLINK) || key.equals(RelationType.str_CALL)){
//                    if(key.equals(RelationType.str_EXTENDS) || key.equals(RelationType.str_IMPLEMENTS)
//                        || key.equals(RelationType.str_ASSOCIATION) || key.equals(RelationType.str_CALL)){
                        file1Map.put(key, endNode);
                    }
                }
                MetricMap.put(startNode, file1Map);

                if(!EntityList.contains(startNode)){
                    EntityList.add(startNode);
                }
//                System.out.println("dependsNum:" + dependsNum + "entityNum:" + entityNum);
            }

            if(!EntityList.contains(endNode)){
                EntityList.add(endNode);
//                System.out.println("dependsNum:" + dependsNum + "entityNum:" + entityNum);
            }
            dependsNum++;
        }

//        System.out.println(cloneList);
//        System.out.println(EntityMap.size());
        for(Clone clone : cloneList){
            if(EntityList.contains((ProjectFile)clone.getStartNode()) && EntityList.contains((ProjectFile)clone.getEndNode())){
                if(MetricMap.containsKey((ProjectFile)clone.getStartNode())){
                    MetricMap.get((ProjectFile)clone.getStartNode()).put("CLONE", (ProjectFile)clone.getEndNode());
                }else{
                    Map<String, ProjectFile> file1Map = new HashMap<>();
                    file1Map.put("CLONE", (ProjectFile)clone.getEndNode());
                    MetricMap.put((ProjectFile) clone.getStartNode(), file1Map);
                }

                if(MetricMap.containsKey((ProjectFile)clone.getEndNode())){
                    MetricMap.get((ProjectFile)clone.getEndNode()).put("CLONE", (ProjectFile)clone.getStartNode());
                }else{
                    Map<String, ProjectFile> file1Map = new HashMap<>();
                    file1Map.put("CLONE", (ProjectFile)clone.getStartNode());
                    MetricMap.put((ProjectFile) clone.getEndNode(), file1Map);
                }
            }
        }

        System.out.println("CLONE end");

        String inputStringAll = "";
        String inputEntity = "";
        String inputAttr = "";
        String inputMetric = "";
        Integer MetricNum = 0;

        FileWriter fw = new FileWriter("./metric.txt",true);
//        fw.write(EntityList.size() + "\r\n" + EntityList.size() * 5 + "\r\n");
//        fw.write(EntityList.size() + "\r\n" + EntityList.size() * 3 + "\r\n");
        fw.write(EntityList.size() + "\r\n" + (EntityList.size() * 2 + 1) + "\r\n");

        for(ProjectFile projectFile: EntityList){
            fw.write(projectFile.getPath() + " ");
        }

        fw.write("\r\n");

        int temp_num = 1;
        for(ProjectFile projectFile: EntityList){
//            fw.write(temp_num + "_EXTENDS_" + projectFile.getPath() + " " + (temp_num + 1) + "_IMPL_" + projectFile.getPath()+ " " + (temp_num + 2) + "_ASSOCIATION_" + projectFile.getPath()+ " " + (temp_num + 3) + "_CALL_" + projectFile.getPath() + " " + (temp_num + 4) +"_CLONE_" + projectFile.getPath() + " ");
//            temp_num += 5;
//            fw.write(temp_num + "_EXTENDS_" + projectFile.getPath() + " " + (temp_num + 1) + "_IMPL_" + projectFile.getPath() +  " " + (temp_num + 2) +"_CLONE_" + projectFile.getPath() + " ");
            fw.write(temp_num + "_EXTENDS_" + projectFile.getPath() + " " + (temp_num + 1) + "_IMPL_" + projectFile.getPath() +  " ");
            temp_num += 2;
        }
        fw.write(temp_num  +"_CLONE");
        fw.write("\r\n");

        ArrayList<Integer> relationAttr = new ArrayList<>();
        for(ProjectFile projectFile: EntityList){
            relationAttr.clear();
            if(MetricMap.containsKey(projectFile)){
                for(Map.Entry<String, ProjectFile> entry2: MetricMap.get(projectFile).entrySet()){
                    if(entry2.getKey().equals(RelationType.str_EXTENDS)){
                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 2 + 1));
                    }else if(entry2.getKey().equals(RelationType.str_IMPLEMENTS)){
                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 2 + 2));
                    }else if(entry2.getKey().equals("CLONE")){
//                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 3 + 3));
                        relationAttr.add(EntityList.size() * 2 + 1);
                    }

//                    if(entry2.getKey().equals(RelationType.str_EXTENDS)){
//                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 5 + 1));
//                    }else if(entry2.getKey().equals(RelationType.str_IMPLEMENTS)){
//                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 5 + 2));
//                    }else if(entry2.getKey().equals(RelationType.str_ASSOCIATION)){
//                        relationAttr.add(EntityList.indexOf(entry2.getValue()) * 5 + 3);
//                    }else if(entry2.getKey().equals(RelationType.str_CALL)){
//                        relationAttr.add(EntityList.indexOf(entry2.getValue()) * 5 + 4);
//                    }else if(entry2.getKey().equals("CLONE")){
//                        relationAttr.add((EntityList.indexOf(entry2.getValue()) * 5 + 5));
//                    }
                }
            }

            inputMetric = "";
//            for(int i = 1; i <= EntityList.size() * 5; i++){
//            for(int i = 1; i <= EntityList.size() * 3; i++){
            for(int i = 1; i <= (EntityList.size() * 2 + 1); i++){
                if(!relationAttr.contains(i)){
                    inputMetric += "0 ";
                }else {
                    inputMetric += "1 ";
                }
            }
            inputMetric += "\r\n";
            fw.write(inputMetric);

            MetricNum++;
            System.out.println(MetricNum);
        }

        fw.close();
    }

    private void generateMetric(Map<Package, Map<String, Package>> MetricMap, Map<Package, Integer> EntityMap) throws IOException{

    }

}
