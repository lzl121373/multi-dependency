package cn.edu.fudan.se.multidependency.controller.ar;

import cn.edu.fudan.se.multidependency.service.query.ar.ClusterService;
import cn.edu.fudan.se.multidependency.service.query.ar.ShowService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ar")
public class ARController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ShowService showService;

    @GetMapping("/exportNeo4jCSV")
    @ResponseBody
    public void exportNeo4jCSV() {
        clusterService.exportNeo4jCSV("D:\\neo4j-community-3.5.16\\import\\guava-27.1-guava\\");
    }

    @GetMapping("/dependGraph/static")
    public String staticDependGraph() {
        System.out.println("test staticDependGraph");
        return "ar/static_depend_graph";
    }

    @GetMapping("/dependGraph/dynamic")
    public String dynamicDependGraph() {
        System.out.println("test dynamicDependGraph");
        return "ar/dynamic_depend_graph";
    }

    @GetMapping("/dependGraph/cochange")
    public String cochangeDependGraph() {
        System.out.println("test cochangeDependGraph");
        return "ar/cochange_depend_graph";
    }

    @GetMapping("/dependJson/static")
    @ResponseBody
    public JSONObject staicDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test staicDependJson");
        result.put("result", showService.staticDependGraph());
        return result;
    }

    @GetMapping("/dependJson/dynamic")
    @ResponseBody
    public JSONObject dynamicDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test dynamicDependJson");
        result.put("result", showService.dynamicDependGraph());
        return result;
    }

    @GetMapping("/dependJson/cochange")
    @ResponseBody
    public JSONObject cochangeDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test cochangedependJson");
        result.put("result", showService.cochangeDependGraph());
        return result;
    }

    @GetMapping("/clusterGraph/static")
    public String staticClusterGraph() {
        return "ar/static_cluster_graph";
    }

    @GetMapping("/clusterGraph/dynamic")
    public String dynamicClusterGraph() {
        return "ar/dynamic_cluster_graph";
    }

    @GetMapping("/clusterGraph/cochange")
    public String cochangeClusterGraph() {
        return "ar/cochange_cluster_graph";
    }

    @GetMapping("/clusterJson/static")
    @ResponseBody
    public JSONArray staticClusterJson() {
        String filePath = "C:\\Users\\SongJee\\Desktop\\static_alone.csv";
        return clusterService.getClusterJson(filePath);
    }

    @GetMapping("/clusterJson/dynamic")
    @ResponseBody
    public JSONArray dynamicClusterJson() {
        String filePath = "C:\\Users\\SongJee\\Desktop\\dynamic_alone.csv";
        return clusterService.getClusterJson(filePath);
    }

    @GetMapping("/clusterJson/cochange")
    @ResponseBody
    public JSONArray cochangeClusterJson() {
        String filePath = "C:\\Users\\SongJee\\Desktop\\history_alone.csv";
        return clusterService.getClusterJson(filePath);
    }
}
