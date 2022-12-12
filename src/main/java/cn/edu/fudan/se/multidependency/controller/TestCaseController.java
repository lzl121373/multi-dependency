package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.service.query.APICoverageService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.data.FunctionCallPropertion;
import cn.edu.fudan.se.multidependency.service.query.data.MicroServiceCallWithEntry;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.TestCaseCoverageService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Controller
@RequestMapping("/testcase")
public class TestCaseController {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private TestCaseCoverageService testCaseCoverageService ;
	
	@Autowired
	private APICoverageService apiCoverageService;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/detail")
	public String detail(HttpServletRequest request, @RequestParam("testcases") String testCaseIds, @RequestParam("project") Long projectId) {
		String[] testCasesIdsArray = testCaseIds.split(",");
		List<TestCase> testCases = new ArrayList<>();
		for(String testCaseId : testCasesIdsArray) {
			try {
				TestCase testCase = featureOrganizationService.findTestCase(Integer.parseInt(testCaseId));
				if(testCase == null) {
					continue;
				}
				testCases.add(testCase);
			} catch (Exception e) {
			}
		}
		Project project = nodeService.queryProject(projectId);
		if(testCases.size() == 0 || project == null) {
			request.setAttribute("error", "ERROR!");
			return "error";
		}
		FunctionCallPropertion propertion = testCaseCoverageService.findFunctionCallFunctionDynamicCalled(testCases, project);
		request.setAttribute("treeview", propertion.treeView());
		request.setAttribute("coverage", propertion.coverageOfDynamicCalls());
		request.setAttribute("testCases", testCases);
		request.setAttribute("project", project);
		System.out.println(propertion.treeView());
		return "testcase/detail";
	}
	
	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request, @RequestParam(value="testCaseList", required=false) int[] testCaseList) {
		Map<String, List<TestCase>> allTestCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		List<Integer> testCaseIds = new ArrayList<>();
		if(testCaseList != null) {
			for(Integer testCaseId : testCaseList) {
				testCaseIds.add(testCaseId);
			}
		}
		Iterable<Feature> features = featureOrganizationService.allFeatures();
		Map<TestCase, List<Feature>> testCaseToFeatures = new LinkedHashMap<>();
		Map<Feature, Boolean> isFeatureExecuted = new LinkedHashMap<>();
		List<TestCase> testCasesSortByGroup = new ArrayList<>();
		for(Collection<TestCase> groups : allTestCases.values()) {
			for(TestCase testCase : groups) {
				if(testCaseIds.size() != 0 && !testCaseIds.contains(testCase.getTestCaseId())) {
					continue;
				}
				testCasesSortByGroup.add(testCase);
				List<Feature> hasExecute = new ArrayList<>();
				List<Feature> executeFeatures = featureOrganizationService.findTestCaseExecutionFeatures(testCase);
				for(Feature feature : features) {
					if(executeFeatures.contains(feature)) {
						isFeatureExecuted.put(feature, true);
						hasExecute.add(feature);
					} else {
						if(isFeatureExecuted.get(feature) == null) {
							isFeatureExecuted.put(feature, false);
						}
						hasExecute.add(null);
					}
				}
				testCaseToFeatures.put(testCase, hasExecute);
			}
		}
		List<Feature> executeFeatures = new ArrayList<>(isFeatureExecuted.keySet().size());
		for(Feature key : isFeatureExecuted.keySet()) {
			if(isFeatureExecuted.getOrDefault(key, false)) {
				executeFeatures.add(key);
			} else {
				executeFeatures.add(null);
			}
		}
		request.setAttribute("features", features);
		request.setAttribute("testCaseToFeatures", testCaseToFeatures);
		request.setAttribute("executeFeatures", executeFeatures);
		request.setAttribute("testCases", allTestCases);

		Iterable<Project> projects = nodeService.allProjects();
		
		Map<String, List<CoverageData>> testCaseToPercent = new LinkedHashMap<>();
		List<CoverageData> mergeCoverages = new ArrayList<>();
		mergeCoverages.add(new CoverageData(testCaseCoverageService.findFunctionCallFunctionDynamicCalled(testCasesSortByGroup).coverageOfDynamicCalls(), null, testCasesSortByGroup));
		for(Project project : projects) {
			mergeCoverages.add(new CoverageData(testCaseCoverageService.findFunctionCallFunctionDynamicCalled(testCasesSortByGroup, project).coverageOfDynamicCalls(), project, testCasesSortByGroup));
		}
		testCaseToPercent.put("all", mergeCoverages);
		
		for(TestCase testCase : testCasesSortByGroup) {
			List<CoverageData> percents = new ArrayList<>();
			percents.add(new CoverageData(testCaseCoverageService.findFunctionCallFunctionDynamicCalled(testCase).coverageOfDynamicCalls(), null, testCase));
			for(Project project : projects) {
				percents.add(new CoverageData(testCaseCoverageService.findFunctionCallFunctionDynamicCalled(testCase, project).coverageOfDynamicCalls(), project, testCase));
			}
			testCaseToPercent.put(testCase.getTestCaseId() + ":" + testCase.getName(), percents);
		}
		
		request.setAttribute("projects", projects);
		request.setAttribute("testCaseToCoverages", testCaseToPercent);
		
		Map<String, JSONObject> testCaseToMicroserviceCytoscape = new LinkedHashMap<>();
		Set<Trace> traces = featureOrganizationService.findRelatedTracesForTestCases(testCasesSortByGroup);
		Trace[] traceArray = new Trace[traces.size()];
		traces.toArray(traceArray);
		JSONObject value = featureOrganizationService.microServiceToCytoscape(true, traceArray);
		testCaseToMicroserviceCytoscape.put("all", value);
		for(TestCase testCase : testCasesSortByGroup) {
			traces = featureOrganizationService.findRelatedTracesForTestCases(testCase);
			traceArray = new Trace[traces.size()];
			traces.toArray(traceArray);
			value = featureOrganizationService.microServiceToCytoscape(true, traceArray);
			testCaseToMicroserviceCytoscape.put(testCase.getTestCaseId() + ":" + testCase.getName(), value);
		}
		request.setAttribute("testCaseToMicroserviceCytoscape", testCaseToMicroserviceCytoscape);
		return "testcase";
	}
	
	@GetMapping(value = "/microservice")
	public String microservice(HttpServletRequest request) {
		Map<String, List<TestCase>> allTestCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		request.setAttribute("testCases", allTestCases);
		return "testcase/microservice";
	}
	
	@PostMapping(value = "/microservice/query/entry/edges")
	@ResponseBody
	public JSONObject getMicroServiceEntryWithTestCaseId(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			List<String> idsStr = (List<String>) params.get("ids");
			List<Long> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Long.parseLong(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			List<TestCase> selectTestCases = new ArrayList<>();
			for(TestCase testCase :allTestCases) {
				Long id = testCase.getId();
				if(ids.contains(id)) {
					selectTestCases.add(testCase);
				}
			}
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			result.put("result", "success");
			result.put("edges", callsWithEntry.relatedEdgeIds());
			result.put("nodes", callsWithEntry.relatedMicroServiceIds());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/microservice/query/union")
	@ResponseBody
	public JSONObject getMicroservice(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		List<String> idsStr = (List<String>) params.get("ids");
		List<Integer> ids = new ArrayList<>();
		for(String idStr : idsStr) {
			ids.add(Integer.parseInt(idStr));
		}
		Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
		List<TestCase> selectTestCases = new ArrayList<>();
		for(TestCase testCase :allTestCases) {
			int id = testCase.getTestCaseId();
			if(ids.contains(id)) {
				selectTestCases.add(testCase);
			}
		}
		
		try {
			result.put("result", "success");
			result.put("testCases", selectTestCases);
			result.put("coverageValue", featureOrganizationService.microServiceToCytoscapeUnion(selectTestCases, allTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping(value = "/microservice/api")
	@ResponseBody
	public JSONObject getTestCasesCallAPIs(HttpServletRequest request, @RequestParam(value="testCaseList", required=false) int[] testCaseList) {
		JSONObject result = new JSONObject();
		List<Integer> testCaseIds = new ArrayList<>();
		if(testCaseList != null) {
			for(Integer testCaseId : testCaseList) {
				testCaseIds.add(testCaseId);
			}
		}
		Collection<TestCase> allTestCases = featureOrganizationService.allTestCases();
		List<TestCase> selectTestCases = new ArrayList<>();
		for(TestCase testCase :allTestCases) {
			int id = testCase.getTestCaseId();
			if(testCaseIds.contains(id)) {
				selectTestCases.add(testCase);
			}
		}
		try {
			result.put("result", "success");
			result.put("testCases", allTestCases);
			result.put("value", apiCoverageService.apiCoverage(allTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/microservice/query/intersection")
	@ResponseBody
	public JSONObject getMicroserviceIntersection(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		List<String> idsStr = (List<String>) params.get("ids");
		List<Integer> ids = new ArrayList<>();
		for(String idStr : idsStr) {
			ids.add(Integer.parseInt(idStr));
		}
		Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
		List<TestCase> selectTestCases = new ArrayList<>();
		for(TestCase testCase :allTestCases) {
			int id = testCase.getTestCaseId();
			if(ids.contains(id)) {
				selectTestCases.add(testCase);
			}
		}
		
		try {
			result.put("result", "success");
			result.put("testCases", selectTestCases);
			result.put("coverageValue", featureOrganizationService.microServiceToCytoscapeIntersection(selectTestCases, allTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@Data
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class CoverageData {
		private double coverage;
		private Project project;
		private String testCaseIds;
		public CoverageData(double coverage, Project project, TestCase testCase) {
			this.coverage = coverage;
			this.project = project;
			this.testCaseIds = testCase.getTestCaseId() + "";
		}
		public CoverageData(double coverage, Project project, List<TestCase> testCases) {
			this.coverage = coverage;
			this.project = project;
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < testCases.size(); i++) {
				builder.append(testCases.get(i).getTestCaseId());
				if(i != testCases.size() - 1) {
					builder.append(",");
				}
			}
			this.testCaseIds = builder.toString();
		}
	}
	
}
