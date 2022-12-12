package cn.edu.fudan.se.multidependency.service.query.data;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import lombok.Getter;

public class FunctionCallPropertionDetail {

	@Getter
	private Call functionCallFunction;
	
	/**
	 * 该函数调用在哪些测试用例下发生，发生几次
	 */
	private Map<TestCase, Integer> testCaseCallTimes = new HashMap<>();
	
	public void addTestCaseCall(TestCase testCase) {
		addTestCaseCall(testCase, 1);
	}
	
	public void addTestCaseCall(TestCase testCase, int addTimes) {
		Integer times = testCaseCallTimes.getOrDefault(testCase, 0);
		times += addTimes;
		testCaseCallTimes.put(testCase, times);
	}
	
	public Map<TestCase, Integer> getTestCaseCallTimes() {
		return new HashMap<>(testCaseCallTimes);
	}
	
}
