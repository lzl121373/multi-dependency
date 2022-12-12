package cn.edu.fudan.se.multidependency.service.insert.dynamic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.exception.ScenarioIdErrorException;
import cn.edu.fudan.se.multidependency.exception.TestCaseIdErrorException;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunctionByTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class FeatureAndTestCaseFromJSONFileForMicroserviceInserter extends ExtractorForNodesAndRelationsImpl {
	
	private Map<Integer, Feature> features = new HashMap<>();
	
	public FeatureAndTestCaseFromJSONFileForMicroserviceInserter(String featureConfigPath) {
		this.featureConfigPath = featureConfigPath;
	}
	
	private String featureConfigPath;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		JSONObject featureJsonFile = JSONUtil.extractJSONObject(new File(featureConfigPath));
		JSONArray featuresArray = featureJsonFile.getJSONArray("features");
		JSONArray testcasesArray = featureJsonFile.getJSONArray("testcases");
		JSONArray scenariosArray = featureJsonFile.getJSONArray("scenarios");
		if(featuresArray != null) {
			for(int i = 0; i < featuresArray.size(); i++) {
				JSONObject featureTemp = featuresArray.getJSONObject(i);
				Feature feature = new Feature();
				feature.setEntityId(generateEntityId());
				Integer featureId = featureTemp.getInteger("id");
				if(featureId == null || featureId < 0) {
					throw new Exception("featureId错误");
				}
				feature.setFeatureId(featureId);
				feature.setName(featureTemp.getString("name"));
				feature.setDescription(featureTemp.getString("description"));
				Integer parentFeatureId = featureTemp.getInteger("parentId");
				if(parentFeatureId == null) {
					feature.setParentFeatureId(-1);
				} else {
					feature.setParentFeatureId(parentFeatureId);
				}
				features.put(featureId, feature);
				addNode(feature, null);
			}
			for(Feature feature : features.values()) {
				if(feature.getParentFeatureId() == null || feature.getParentFeatureId() < 0) {
					continue;
				}
				Feature parentFeature = features.get(feature.getParentFeatureId());
				if(parentFeature == null) {
					throw new Exception("feature的parentId错误，没有找到id为 " + feature.getParentFeatureId() + " 的Feature");
				}
				Contain featureContainFeature = new Contain(parentFeature, feature);
				addRelation(featureContainFeature);
			}
		}
		
		if(testcasesArray != null) {
			for(int i = 0; i < testcasesArray.size(); i++) {
				JSONObject testcaseTemp = testcasesArray.getJSONObject(i);
				TestCase testcase = new TestCase();
				testcase.setEntityId(generateEntityId());
				Integer testcaseId = testcaseTemp.getInteger("id");
				if(testcaseId == null || testcaseId < 0) {
					throw new TestCaseIdErrorException(testcaseId);
				}
				testcase.setTestCaseId(testcaseId);
				testcase.setInputContent(testcaseTemp.get("input").toString());
				testcase.setSuccess(testcaseTemp.getBooleanValue("success"));
				testcase.setName(testcaseTemp.getString("name"));
				testcase.setDescription(testcaseTemp.getString("description"));
				String group = testcaseTemp.getString("group") == null ? TestCase.DEFAULT_GROUP : testcaseTemp.getString("group");
				testcase.setGroup(group);
				addNode(testcase, null);
				
				JSONArray featureIds = testcaseTemp.getJSONArray("features");
				for(int j = 0; j < featureIds.size(); j++) {
					Integer featureId = featureIds.getInteger(j);
					Feature feature = this.getNodes().findFeatures().get(featureId);
					if(feature == null) {
						throw new Exception("featureId " + featureId + " 不存在");
					}
					TestCaseExecuteFeature testCaseExecuteFeature = new TestCaseExecuteFeature(testcase, feature);
					addRelation(testCaseExecuteFeature);
				}
				JSONArray traceIds = testcaseTemp.getJSONArray("traces");
				for(int j = 0; j < traceIds.size(); j++) {
					String traceId = traceIds.getString(j);
					Trace trace = this.getNodes().findTraces().get(traceId);
					if(trace == null) {
						throw new Exception("traceId " + traceId + " 不存在");
					}
					TestCaseRunTrace testcaseRunTrace = new TestCaseRunTrace(testcase, trace);
					addRelation(testcaseRunTrace);
					
					List<DynamicCallFunctionByTestCase> calls = this.getRelations().findDynamicCallFunctionsByTraceId(traceId);
					calls.forEach(call -> {
						call.setTestCaseId(testcaseId);
					});
				}
				
			}
		}
		
		if(scenariosArray != null) {
			for(int i = 0; i < scenariosArray.size(); i++) {
				JSONObject scenarioTemp = scenariosArray.getJSONObject(i);
				Scenario scenario = new Scenario();
				scenario.setEntityId(generateEntityId());
				Integer scenarioId = scenarioTemp.getInteger("id");
				if(scenarioId == null || scenarioId < 0) {
					throw new ScenarioIdErrorException(scenarioId);
				}
				scenario.setScenarioId(scenarioId);
				scenario.setName(scenarioTemp.getString("name"));
				scenario.setDescription(scenarioTemp.getString("description"));
				addNode(scenario, null);
				
				JSONArray testcaseIds = scenarioTemp.getJSONArray("testcases");
				if(testcaseIds == null) {
					continue;
				}
				for(int j = 0; j < testcaseIds.size(); j++) {
					Integer testcaseId = testcaseIds.getInteger(j);
					TestCase testCase = this.getNodes().findTestCases().get(testcaseId);
					if(testCase == null) {
						throw new Exception("TestCaseId " + testcaseId + " 不存在");
					}
					ScenarioDefineTestCase relation = new ScenarioDefineTestCase(scenario, testCase);
					addRelation(relation);
				}
			}
		}
	}

}
