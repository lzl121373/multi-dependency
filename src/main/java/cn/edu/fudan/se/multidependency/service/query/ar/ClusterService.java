package cn.edu.fudan.se.multidependency.service.query.ar;

import com.alibaba.fastjson.JSONArray;


public interface ClusterService {
    void exportNeo4jCSV(String dirPath);

    JSONArray getClusterJson(String filePath);
}
