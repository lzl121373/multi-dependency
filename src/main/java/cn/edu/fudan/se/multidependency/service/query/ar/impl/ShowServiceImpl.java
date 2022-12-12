package cn.edu.fudan.se.multidependency.service.query.ar.impl;

import cn.edu.fudan.se.multidependency.service.query.ar.DependencyMatrix;
import cn.edu.fudan.se.multidependency.service.query.ar.ShowService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private DependencyMatrix dependencyMatrix;

    public JSONArray staticDependGraph() {
        dependencyMatrix.init();
        return dependGraph(dependencyMatrix.getStaticDependGraph());
    }

    public JSONArray dynamicDependGraph() {
        dependencyMatrix.init();
        return dependGraph(dependencyMatrix.getDynamicDependGraph());
    }

    public JSONArray cochangeDependGraph() {
        dependencyMatrix.init();
        return dependGraph(dependencyMatrix.getCochangeDependGraph());
    }

    private JSONArray dependGraph(Map<String, Set<String>> dependGraph) {
        JSONArray result = new JSONArray();
        for (Map.Entry<String, Set<String>> entry : dependGraph.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", entry.getKey());
            JSONArray imports = new JSONArray();
            imports.addAll(entry.getValue());
            jsonObject.put("imports", imports);
            result.add(jsonObject);
        }
        return result;
    }
}
