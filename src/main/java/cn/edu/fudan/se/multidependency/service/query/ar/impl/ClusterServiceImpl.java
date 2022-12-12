package cn.edu.fudan.se.multidependency.service.query.ar.impl;


import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.ar.ClusterService;
import cn.edu.fudan.se.multidependency.service.query.ar.DependencyMatrix;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private DependencyMatrix dependencyMatrix;

    public void exportNeo4jCSV(String dirPath) {
        dependencyMatrix.init();
        String nodeFileName = "nodes.csv";
        String relationFileName = "relations.csv";
        final String CSV_COL_SPR = ",";
        final String CSV_ROW_SPR = "\r\n";

        StringBuffer nodeBuf = new StringBuffer();
        nodeBuf.append("file_id" + CSV_COL_SPR + "name" + CSV_ROW_SPR);

        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        for (ProjectFile projectFile : dependencyMatrix.getProjectFiles()) {
            nodeBuf.append(i + CSV_COL_SPR + projectFile.getPath() + CSV_ROW_SPR);
            map.put(projectFile.getPath(), i);
            i++;
        }
        FileUtil.exportToFile(dirPath + nodeFileName, nodeBuf.toString());

        StringBuffer relationBuf = new StringBuffer();
        relationBuf.append("file1_id" + CSV_COL_SPR + "file2_id" + CSV_COL_SPR + "weight" + CSV_ROW_SPR);

        Map<String, Map<String, Double>> adjacencyList = dependencyMatrix.getAdjacencyList();
        for (String file1 : adjacencyList.keySet()) {
            for (Map.Entry<String, Double> entry : adjacencyList.get(file1).entrySet()) {
                relationBuf.append(map.get(file1) + CSV_COL_SPR + map.get(entry.getKey())
                        + CSV_COL_SPR + entry.getValue() + CSV_ROW_SPR);
            }
        }
        FileUtil.exportToFile(dirPath + relationFileName, relationBuf.toString());
    }

    public JSONArray getClusterJson(String filePath) {
        List<Set<String>> list = readClusterResultCsv(filePath);
        JSONArray result = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("name", "");
        JSONArray jsonArray1 = new JSONArray();
        for (Set<String> cluster : list) {
            JSONArray jsonArray2 = new JSONArray();

            for(String path : cluster) {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("size", 1);
                jsonObject2.put("name", FileUtil.extractFilePathName(path));
//                jsonObject2.put("name", path);
                jsonArray2.add(jsonObject2);
            }

            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("name", "");
            jsonObject3.put("children", jsonArray2);

            jsonArray1.add(jsonObject3);
        }

        jsonObject1.put("children", jsonArray1);
        jsonObject.put("result", jsonObject1);

        result.add(jsonObject);
        return result;
    }

    private List<Set<String>> readClusterResultCsv(String filePath)  {
        Map<Integer, Set<String>> map = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            String line;
            boolean flag = false;
            while((line = reader.readLine()) != null) {
                if("".equals(line.trim()) || !flag) {
                    flag = true;
                    continue;
                }
                String[] values = line.split(",");
                if(values.length < 3) {
                    continue;
                }
                String path = values[0];
                int cluster = Integer.parseInt(values[1]);
                if (!map.containsKey(cluster)) {
                    map.put(cluster, new HashSet<>());
                } else {
                    map.get(cluster).add(path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(map.values());
    }

}
