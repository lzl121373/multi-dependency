package cn.edu.fudan.se.multidependency.service.query.dynamic;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.service.query.data.FunctionCallPropertionDetail;

public interface DynamicAnalyseService {
	
	Map<Function, Map<Function, FunctionCallPropertionDetail>> findFunctionCallFunctionDynamicCalled(Iterable<TestCase> testcases);

	List<DynamicCall> findFunctionDynamicCallsByTraceAndMicroService(Trace trace, MicroService ms);
	
	List<DynamicCall> findFunctionDynamicCallsByTrace(Trace trace);
	
	List<DynamicCall> findFunctionDynamicCallsByTraceAndSpan(Trace trace, Span span);

	List<DynamicCall> findFunctionCallsByTraceIdAndSpanId(String traceId, String spanId);
	
	List<TestCase> findTestCasesByFeatureName(String featureName);
	
	Iterable<Feature> findAllFeatures();
	
	Iterable<TestCase> findAllTestCases();
	
	Iterable<Scenario> findAllScenarios();
	
	Map<TestCase, List<TestCaseExecuteFeature>> findAllTestCaseExecuteFeatures();
	
	Map<Feature, List<TestCaseExecuteFeature>> findAllFeatureExecutedByTestCases();
	
	Map<TestCase, List<TestCaseRunTrace>> findAllTestCaseRunTraces();
	
	Map<Scenario, List<ScenarioDefineTestCase>> findAllScenarioDefineTestCases();

	Map<Feature, Feature> findAllFeatureToParentFeature();
	
	List<Feature> findFeaturesByFeatureId(Integer... featureIds);

	List<DynamicCall> findFunctionDynamicCallsByMicroService(MicroService ms);
	
	List<DynamicCall> findFunctionDynamicCallsByProject(Project project);

	TestCase findTestCaseByTestCaseId(Integer testCaseId);

	Feature findFeatureByFeatureId(Integer featureId);
	
	TestCase findTestCaseById(Long id);

	Feature findFeatureById(Long id);

	List<DynamicCall> findFunctionDynamicCallFunctionRelations(Project project, boolean isTraceRunForTestCase);

	Iterable<DynamicCall> findAllFunctionDynamicCallFunctionRelations(boolean b);
	
	Trace findTraceByTraceId(String traceId);

	Trace findTraceById(Long id);
}
