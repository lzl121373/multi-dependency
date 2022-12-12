package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.service.query.dynamic.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;

@Controller
@RequestMapping("/feature")
public class FeatureController {

	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private FeatureOrganizationService featureOrganizationService;

	@GetMapping("/all")
	@ResponseBody
	public Iterable<Feature> findAllFeatures() {
		return featureOrganizationService.allFeatures();
	}

	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request) {
		request.setAttribute("features", featureOrganizationService.allFeatures());
		request.setAttribute("testcases", featureOrganizationService.allTestCases());
		return "feature";
	}
	
	@PostMapping("/trace/cytoscape")
	@ResponseBody
	public JSONObject traceToCytoscape(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			System.out.println("/trace/cytoscape");
			System.out.println(params);
			List<String> idsStr = (List<String>) params.get("ids");
			List<Long> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Long.parseLong(idStr));
			}
			if(ids.size() != 1) {
				throw new Exception("只能选一条trace");
			}
			Iterable<Trace> allTraces = featureOrganizationService.allTraces();
			Trace selectTrace = null;
			for(Trace trace : allTraces) {
				if(trace.getId().equals(ids.get(0))) {
					selectTrace = trace;
					break;
				}
			}
			System.out.println(selectTrace.getTraceId());
			JSONObject data = featureOrganizationService.restfulAPIToCytoscape(selectTrace, false);
			
			result.put("result", "success");
			result.put("value", data);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	
	@GetMapping("/testcase/cytoscape")
	@ResponseBody
	public JSONObject executedByTestCasesToCytoscape() {
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			JSONObject value = featureOrganizationService.featureExecuteTestCasesToCytoscape();
			System.out.println(value);
			result.put("value", value);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/testcaseToFeature/treeview")
	@ResponseBody
	public JSONObject executeFeaturesToTreeView() {
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			result.put("value", featureOrganizationService.testcaseExecuteFeaturesToTreeView());
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/testcase/treeview")
	@ResponseBody
	public JSONObject executedByTestCasesToTreeView() {
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			result.put("value", featureOrganizationService.featureExecutedByTestCasesToTreeView());
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@GetMapping("/microservicecall/all")
	@ResponseBody
	public JSONObject toCytoscapeAll() {
		JSONObject result = new JSONObject();
		try {
			Set<Trace> traces = featureOrganizationService.findRelatedTracesForFeature(featureOrganizationService.allFeatures());
			Trace[] traceArray = new Trace[traces.size()];
			traces.toArray(traceArray);
			JSONObject value = featureOrganizationService.microServiceToCytoscape(true, traceArray);
			result.put("result", "success");
			result.put("removeUnuseMicroService", true);
			result.put("value", value.getJSONObject("value"));
			result.put("microservice", value.getJSONArray("microservice"));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/microservicecall/feature")
	@ResponseBody
	public JSONObject toCytoscapeForFeature(@RequestParam("featureGraphId") Long id) {
		JSONObject result = new JSONObject();
		try {
			Feature feature = dynamicAnalyseService.findFeatureById(id);
			Set<Trace> traces = featureOrganizationService.findRelatedTracesForFeature(feature);
			Trace[] traceArray = new Trace[traces.size()];
			traces.toArray(traceArray);
			JSONObject value = featureOrganizationService.microServiceToCytoscape(true, traceArray);
			result.put("result", "success");
			result.put("removeUnuseMicroService", true);
			result.put("value", value.getJSONObject("value"));
			result.put("microservice", value.getJSONArray("microservice"));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/microservicecall/testcase")
	@ResponseBody
	public JSONObject toCytoscapeForTestCase(@RequestParam("testcaseGraphId") Long id) {
		JSONObject result = new JSONObject();
		try {
			TestCase testcase = dynamicAnalyseService.findTestCaseById(id);
			Set<Trace> traces = featureOrganizationService.findRelatedTracesForTestCases(testcase);
			Trace[] traceArray = new Trace[traces.size()];
			traces.toArray(traceArray);
			JSONObject value = featureOrganizationService.microServiceToCytoscape(true, traceArray);
			result.put("result", "success");
			result.put("removeUnuseMicroService", true);
			result.put("value", value.getJSONObject("value"));
			result.put("microservice", value.getJSONArray("microservice"));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/microservicecall/trace")
	@ResponseBody
	public JSONObject toCytoscapeForTrace(@RequestParam("traceGraphId") Long id) {
		JSONObject result = new JSONObject();
		try {
			Trace trace = dynamicAnalyseService.findTraceById(id);
			JSONObject value = featureOrganizationService.microServiceToCytoscape(true, trace);
			result.put("detail", value.getJSONObject("detail"));
			result.put("result", "success");
			result.put("removeUnuseMicroService", true);
			result.put("value", value.getJSONObject("value"));
			result.put("microservice", value.getJSONArray("microservice"));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
}
