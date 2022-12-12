package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.service.query.data.TestCaseCoverageMicroServiceAPIs;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class APICoverageService {
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	public Map<MicroService, TestCaseCoverageMicroServiceAPIs> apiCoverage(Collection<TestCase> testCases) {
		Map<MicroService, TestCaseCoverageMicroServiceAPIs> result = new HashMap<>();
		
		for(MicroService ms : featureOrganizationService.allMicroServices()) {
			TestCaseCoverageMicroServiceAPIs coverage = new TestCaseCoverageMicroServiceAPIs();
			coverage.addTestCases(testCases);
			coverage.setMicroService(ms);
			List<RestfulAPI> apis = containRelationService.findMicroServiceContainRestfulAPI(ms);
			for(RestfulAPI api : apis) {
				coverage.addCallRestfulAPITimes(api, 0);
			}
			result.put(ms, coverage);
		}
		
		Iterable<Span> relatedSpans = featureOrganizationService.relatedSpan(testCases);
		int i = 0;
		for(Span span : relatedSpans) {
			MicroService microService = featureOrganizationService.spanBelongToMicroservice(span);
			assert(microService != null);
			TestCaseCoverageMicroServiceAPIs coverage = result.get(microService);
			assert(coverage != null);
			
			SpanInstanceOfRestfulAPI instanceOf = msService.findSpanBelongToAPI(span);
			if(instanceOf == null) {
				continue;
			}
			RestfulAPI api = instanceOf.getApi();
			coverage.addCallRestfulAPITimes(api, 1);
		}
		
		return result;
	}
	
}
