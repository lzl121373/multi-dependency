package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.service.query.metric.*;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellMetricCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/metric")
public class MeasureController {

    @Autowired
    ModularityCalculatorImplForFieldMethodLevel modularityCalculator;

    @Autowired
    MetricCalculatorService metricCalculatorService;

    @Autowired
    SmellMetricCalculatorService smellMetricCalculatorService;

    @Autowired
    MetricShowService metricShowService;
    
    @Autowired
    NodeService nodeService;

    @GetMapping(value= {"", "/", "/index"})
    public String index() {
        return "metric";
    }

    @GetMapping(value= {"/smell"})
    public String smellmetric() {
        return "smellmetric";
    }
    
    @GetMapping("/excel/package")
    @ResponseBody
    public void printPackageMetric(HttpServletRequest request, HttpServletResponse response) {
		try {
	        response.addHeader("Content-Disposition", "attachment;filename=package_metrics.xlsx");  
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); 
			OutputStream stream = response.getOutputStream();

			metricShowService.printPackageMetricExcel(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    @GetMapping("/excel/file")
    @ResponseBody
    public void printFileMetric(HttpServletRequest request, HttpServletResponse response) {
		try {
	        response.addHeader("Content-Disposition", "attachment;filename=file_metrics.xlsx");  
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); 
			OutputStream stream = response.getOutputStream();

			metricShowService.printFileMetricExcel(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    
    @GetMapping("/file")
    @ResponseBody
    public Object getFileMetrics() {
    	return metricShowService.getProjectFileMetrics();
    }
    
    @GetMapping("/project")
    @ResponseBody
    public Collection<NodeMetric> getProjectMetrics() {
    	return metricShowService.getProjectMetrics();
    }
    
    @GetMapping("/project/modularity")
    @ResponseBody
    public double projectModularity(@RequestParam("projectId") long id) {
    	Project project = nodeService.queryProject(id);
    	if(project == null) {
    		return -1;
    	}
    	return metricCalculatorService.calculateProjectModularity(project);
    }
    
    @GetMapping("/project/commitTimes")
    @ResponseBody
    public int projectCommitTimes(@RequestParam("projectId") long id) {
    	Project project = nodeService.queryProject(id);
    	if(project == null) {
    		return -1;
    	}
    	return metricCalculatorService.calculateProjectCommits(project);
    }
    
    @GetMapping("/package")
    @ResponseBody
    public Object getPackageMetrics() {
    	return metricShowService.getProjectPackageMetrics();
    }

    @GetMapping("/smellMetric")
    @ResponseBody
    public Object calculateSmellMetric() {
        return metricShowService.getProjectSmellMetricsInFileLevel();
    }

    @GetMapping("/modularityMetricQ")
    @ResponseBody
    public JSONObject modularityMetricQ() {
        JSONObject result = new JSONObject();

        Map<String, Double> metricQs = new HashMap<>();
        for (Project project:nodeService.allProjects()){
            metricQs.put(project.getName(),modularityCalculator.calculate(project).getValue());
        }

        try {
            result.put("result", "success");
            result.put("value", metricQs);
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
