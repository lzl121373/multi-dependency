package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

public interface MicroserviceService {
	
	long countOfAllMicroServices();
	
	List<ZTreeNode> queryMicroServiceContainProjectsZTree(Iterable<MicroService> mss);
	
	List<MicroService> queryAllMicroServicesByPage(int page, int size, String... sortByProperties);
	
	MicroService findMicroServiceById(Long id);
	
	Collection<MicroService> findAllMicroService();
	
	Trace findTraceByFeature(Feature feature);
	
	List<SpanCallSpan> findSpanCallSpans(Span span);
	
	MicroServiceCreateSpan findMicroServiceCreateSpan(Span span);
	
	SpanCallSpan findSpanCallSpanById(Long id);

	SpanStartWithFunction findSpanStartWithFunctionByTraceIdAndSpanId(String requestTraceId, String requestSpanId);
	
	Span findSpanById(Long id);

	List<Span> findSpansByMicroserviceAndTraceId(MicroService ms, String traceId);
	
	Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls();
	
	Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns();
	
	boolean isMicroServiceCall(MicroService start, MicroService end);
	
	boolean isMicroServiceDependOn(MicroService start, MicroService end);
	
	Map<MicroService, List<RestfulAPI>> microServiceContainsAPIs();
	
	Iterable<MicroServiceCallMicroService> findAllMicroServiceCallMicroServices();
	
	void deleteAllMicroServiceCallMicroService();
	
	void saveMicroServiceCallMicroService(MicroServiceCallMicroService call);

	SpanInstanceOfRestfulAPI findSpanBelongToAPI(Span span);
	
	Map<Span, SpanInstanceOfRestfulAPI> findAllSpanInstanceOfRestfulAPIs();
	
	
	/**
	 * 找出Project调用了哪些三方
	 * @param project
	 * @return
	 */
	CallLibrary<MicroService> findMicroServiceCallLibraries(MicroService ms);
	
	Iterable<CallLibrary<MicroService>> findAllMicroServiceCallLibraries();
	
	/**
	 * 从动态运行下的微服务之间的依赖中找出某个微服务的FanIn和FanOut
	 * @return
	 */
	Fan_IO<MicroService> microServiceDependencyFanIOInDynamicCall(MicroService ms);

}
