package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.service.query.smell.BasicSmellQueryService;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/smell")
public class SmellController {
    @Autowired
    private BasicSmellQueryService basicSmellQueryService;

    @PostMapping("/treemap")
    @ResponseBody
    public JSONArray  smellsToTreemap() {
        return basicSmellQueryService.smellDataToGraph();
    }

    @GetMapping("/get_metric")
    @ResponseBody
    public Metric cytoscape(@RequestParam("smellId") long smellId) {
        return basicSmellQueryService.findMetricBySmellId(smellId);
    }

}
