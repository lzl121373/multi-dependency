package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.data.MicroServiceCallWithEntry;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseServiceImpl;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeEdge;

@Controller
@RequestMapping("/multiple/all")
public class MDAllController {

    @Autowired
    private GitAnalyseServiceImpl gitAnalyseService;

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private CloneAnalyseService cloneAnalyse;
	
	@Autowired
	private CloneValueService cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@GetMapping("")
	public String multipleMicroServiceAll(HttpServletRequest request) {
		Map<String, List<TestCase>> allTestCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		request.setAttribute("testCases", allTestCases);
		Iterable<Scenario> allScenarios = featureOrganizationService.allScenarios();
		request.setAttribute("scenarios", allScenarios);
		Iterable<Feature> allFeatures = featureOrganizationService.allFeatures();
		request.setAttribute("features", allFeatures);
		return "structure_testcase_microservice/multiple_microservice_all";
	}
	
	@PostMapping(value = "")
	@ResponseBody
	public JSONObject all(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
			boolean showCntOfDevUpdMs = (boolean) params.getOrDefault("showCntOfDevUpdMs", true);
			int showClonesMinPair = Integer.parseInt((String) params.getOrDefault("showClonesMinPair", "0"));
			System.out.println(showStructure + " " + showClonesInMicroService + " " + showMicroServiceCallLibs + " " + showCntOfDevUpdMs + " " + showClonesMinPair);
			MicroServiceCallWithEntry temp = testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, showCntOfDevUpdMs, showClonesMinPair, featureOrganizationService.allTestCases());
			result.put("result", "success");
			result.put("value", temp.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/{TestcaseOrFeatureOrScenario}")
	@ResponseBody
	public JSONObject allTestCaseOrFeatureOrScenario(@PathVariable("TestcaseOrFeatureOrScenario") String testCaseOrFeatureOrScenario, 
			@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
			boolean showCntOfDevUpdMs = (boolean) params.getOrDefault("showCntOfDevUpdMs", false);
			int showClonesMinPair = Integer.parseInt((String) params.getOrDefault("showClonesMinPair", "0"));
			System.out.println(showStructure + " " + showClonesInMicroService + " " + showMicroServiceCallLibs + " " + showCntOfDevUpdMs + " " + showClonesMinPair);
			List<Integer> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Integer.parseInt(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			Iterable<TestCase> selectTestCases = null;
			
			switch(testCaseOrFeatureOrScenario) {
			case "testcase":
				List<TestCase> temp = new ArrayList<>();
				for(TestCase testCase :allTestCases) {
					int id = testCase.getTestCaseId();
					if(ids.contains(id)) {
						temp.add(testCase);
					}
				}
				selectTestCases = temp;
				break;
			case "feature":
				List<Feature> features = new ArrayList<>();
				for(Feature feature : featureOrganizationService.allFeatures()) {
					if(ids.contains(feature.getFeatureId())) {
						features.add(feature);
					}
				}
				selectTestCases = featureOrganizationService.relatedTestCaseWithFeatures(features);
				break;
			case "scenario":
				List<Scenario> scenarios = new ArrayList<>();
				for(Scenario scenario : featureOrganizationService.allScenarios()) {
					if(ids.contains(scenario.getScenarioId())) {
						scenarios.add(scenario);
					}
				}
				selectTestCases = featureOrganizationService.relatedTestCaseWithScenarios(scenarios);
				break;
			}
			MicroServiceCallWithEntry temp = testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, showCntOfDevUpdMs, showClonesMinPair, selectTestCases);
			result.put("result", "success");
//			result.put("value", temp.testCaseEdges());
			result.put("value", temp.toCytoscapeWithStructure());
			result.put("cloneDetail", temp.cloneDetails());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	private MicroServiceCallWithEntry testCaseEdges(boolean showStructure, boolean showClonesInMicroService, 
			boolean showMicroServiceCallLibs, boolean showCntOfDevUpdMs, int showClonesMinPair,
			Iterable<TestCase> selectTestCases) {
		MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
		callsWithEntry.setAllTestCases(featureOrganizationService.allTestCases());
		callsWithEntry.setAllFeatures(featureOrganizationService.allFeatures());
		callsWithEntry.setAllMicroServices(msService.findAllMicroService());
		callsWithEntry.setAllScenarios(featureOrganizationService.allScenarios());
		
		callsWithEntry.setMsDependOns(msService.msDependOns());
		callsWithEntry.setShowStructure(showStructure);
		callsWithEntry.setShowMicroServiceCallLibs(showMicroServiceCallLibs);
		callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
		callsWithEntry.setShowCntOfDevUpdMs(showCntOfDevUpdMs);
		if(showClonesInMicroService) {
//			Iterable<MicroService> allMicroServices = msService.findAllMicroService();
//			Map<MicroService, CloneLineValue<MicroService>> msCloneValues = cloneAnalyse.msCloneLineValuesGroup(allMicroServices, CloneGroup.allGroup(CloneLevel.file), CloneLevel.file, false, false);
//			callsWithEntry.setMsToCloneLineValue(msCloneValues);
			callsWithEntry.setClonesInMicroServiceFromFileClone(cloneValueService.findMicroServiceCloneFromFileClone(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE)));
			callsWithEntry.setShowClonesMinPair(showClonesMinPair);
		}
		if(showMicroServiceCallLibs) {
			callsWithEntry.setMicroServiceCallLibraries(msService.findAllMicroServiceCallLibraries());
		}
		if(showCntOfDevUpdMs) {
			callsWithEntry.setCntOfDevUpdMs(gitAnalyseService.cntOfDevUpdMsList());
		}
		return callsWithEntry;
	}
	
	@PostMapping(value = "/microservice/query/edges")
	@ResponseBody
	public JSONObject getMicroServiceCallChainWithTestCaseId(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.get("ids");
			List<Long> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Long.parseLong(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			List<TestCase> selectTestCases = new ArrayList<>();
			for(Long selectId : ids) {
				for(TestCase testCase :allTestCases) {
					Long id = testCase.getId();
					if(selectId.equals(id)) {
						selectTestCases.add(testCase);
						break;
					}
				}
			}
			assert(selectTestCases.size() <= 2);
			JSONArray nodes = null;
			JSONArray edges = null;
			if(selectTestCases.size() < 2) {
				MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
				nodes = callsWithEntry.relatedMicroServiceIds();
				edges = callsWithEntry.relatedEdgeIds();
			} else {
				MicroServiceCallWithEntry callsWithEntry1 = featureOrganizationService.findMsCallMsByTestCases(selectTestCases.get(0));
				MicroServiceCallWithEntry callsWithEntry2 = featureOrganizationService.findMsCallMsByTestCases(selectTestCases.get(1));
				List<CytoscapeEdge> edges1 = callsWithEntry1.relatedEdgeObjs();
				List<CytoscapeEdge> edges2 = callsWithEntry2.relatedEdgeObjs();
				nodes = new JSONArray();
				edges = new JSONArray();
				for(CytoscapeEdge edge1 : edges1) {
					if(edges2.contains(edge1)) {
						edges2.get(edges2.indexOf(edge1)).setType("NewEdges_Edge1_Edge2");
						edge1.setType("NewEdges_Edge1_Edge2");
						edges.add(edge1.toJSONDataContent());
					} else {
						edge1.setType("NewEdges_Edge1");
						edges.add(edge1.toJSONDataContent());
					}
				}
				for(CytoscapeEdge edge2 : edges2) {
					if(!edges1.contains(edge2)) {
						edge2.setType("NewEdges_Edge2");
						edges.add(edge2.toJSONDataContent());
					}
				}
			}
			result.put("result", "success");
			result.put("edges", edges);
			result.put("nodes", nodes);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
}
