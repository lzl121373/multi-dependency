package cn.edu.fudan.se.multidependency.service.insert.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;

@Deprecated
public class FeatureAndTestCaseFromCodeForMicroserviceInserter extends ExtractorForNodesAndRelationsImpl {
	
	private List<Feature> features = new ArrayList<>();
	
	private List<TestCase> testcases = new ArrayList<>();
	
	private List<TestCaseExecuteFeature> testcaseExecuteFeatures = new ArrayList<>();
	
	private List<TestCaseRunTrace> testcaseRunTraces = new ArrayList<>();
	
	private final Map<String, Trace> traces = this.getNodes().findTraces();
	
	public void addFeature(Feature feature) {
		this.features.add(feature);
	}
	
	public void addTestCase(TestCase testcase) {
		this.testcases.add(testcase);
	}
	
	public void addTestCaseExecuteFeature(TestCase testcase, Feature feature) {
		testcaseExecuteFeatures.add(new TestCaseExecuteFeature(testcase, feature));
	}
	
	public void addTestCaseRunTrace(TestCase testcase, String traceId) throws Exception {
		Trace trace = traces.get(traceId);
		if(trace == null) {
			throw new Exception("traceId为 " + traceId + " 的trace不存在");
		}
		testcaseRunTraces.add(new TestCaseRunTrace(testcase, trace));
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		for(Feature feature : features) {
			addNode(feature, null);
		}
		for(TestCase testcase : testcases) {
			addNode(testcase, null);
		}
		for(TestCaseExecuteFeature testcaseExecuteFeature : testcaseExecuteFeatures) {
			if(!this.getNodes().existNode(testcaseExecuteFeature.getTestCase())
					|| !this.getNodes().existNode(testcaseExecuteFeature.getFeature())) {
				throw new Exception("节点不存在，不能保存关系");
			}
			addRelation(testcaseExecuteFeature);
		}
		for(TestCaseRunTrace testcaseRunTrace : testcaseRunTraces) {
			if(!this.getNodes().existNode(testcaseRunTrace.getTestCase())
					|| !this.getNodes().existNode(testcaseRunTrace.getTrace())) {
				throw new Exception("节点不存在，不能保存关系");
			}
			addRelation(testcaseRunTrace);
		}
	}

}
