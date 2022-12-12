package cn.edu.fudan.se.multidependency.service.query.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import lombok.Data;

@Data
public class TestCaseCoverageMicroServiceAPIs implements Serializable {
	private static final long serialVersionUID = 2637149928699256151L;
	private MicroService microService;
	private List<TestCase> testCases = new ArrayList<>();;
	private Map<RestfulAPI, Integer> callAPITimes = new HashMap<>();
	
	public void addTestCase(TestCase testCase) {
		this.testCases.add(testCase);
	}
	
	public void addTestCases(Collection<TestCase> testCases) {
		this.testCases.addAll(testCases);
	}
	
	public void addCallRestfulAPITimes(RestfulAPI api, int times) {
		Integer previousTimes = callAPITimes.getOrDefault(api, 0);
		callAPITimes.put(api, previousTimes + times);
	}
}