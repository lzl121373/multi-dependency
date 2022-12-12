package cn.edu.fudan.se.multidependency.service.query.dynamic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.data.FunctionCallPropertion;
import cn.edu.fudan.se.multidependency.service.query.data.FunctionCallPropertionDetail;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class TestCaseCoverageService {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
    
    @Autowired
    ContainRelationService containRelationService;
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(TestCase testCase) {
		return findFunctionCallFunctionDynamicCalled(testCase, null);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(TestCase testCase, Project project) {
		List<TestCase> list = new ArrayList<>();
		list.add(testCase);
		return findFunctionCallFunctionDynamicCalled(list, project);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(List<TestCase> testCases) {
		return findFunctionCallFunctionDynamicCalled(testCases, null);
	}

	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(List<TestCase> testCases, Project project) {
		System.out.println("findFunctionCallFunctionDynamicCalled " + testCases.size());
		// 所有静态调用
		Map<Function, List<Call>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		// 被动态调用的静态调用
		Map<Function, Map<Function, FunctionCallPropertionDetail>> dynamicCalls = dynamicAnalyseService.findFunctionCallFunctionDynamicCalled(testCases);
		if(project != null) {
			Set<Function> key = new HashSet<>(staticCalls.keySet());
			for(Function f : key) {
				Project fBelongToProject = containRelationService.findFunctionBelongToProject(f);
				if(!fBelongToProject.equals(project)) {
					staticCalls.remove(f);
					dynamicCalls.remove(f);
				}
			}
		}
		
		FunctionCallPropertion propertion = new FunctionCallPropertion(staticCalls, dynamicCalls);
		System.out.println(propertion.coverageOfDynamicCalls());
		return propertion;
	}
	
}
