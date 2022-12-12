package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.repository.relation.code.CallRepository;
import cn.edu.fudan.se.multidependency.service.query.DependencyOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.utils.GraphvizUtil;
import cn.edu.fudan.se.multidependency.utils.GraphvizUtil.GraphvizTreeNode;

@Controller
@RequestMapping("/function")
public class FunctionController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private MicroserviceService msService;

	@Autowired
	private FeatureOrganizationService organizationService;
	
	@Autowired
	private DependencyOrganizationService dependencyOrganizationService;
	
	@Autowired
	private CallRepository functionCallFunctionRepository;
	
	@GetMapping("/testfanout/{fileId}")
	@ResponseBody
	public void testFanout(@PathVariable("fileId") long fileId) {
//		functionCallFunctionRepository.queryTest(fileId);
//		System.out.println(functionCallFunctionRepository.queryTest(fileId));
//		List<Object> result = functionCallFunctionRepository.queryTest(fileId);
//		for(Object r : result) {
//			System.out.println(r.getClass());
//		}
	}
	
	private List<Function> test(Function startFunction, List<DynamicCall> calls, Long depth) {
		List<Function> result = new ArrayList<>();
		List<DynamicCall> temp = new ArrayList<>();
		
		for(DynamicCall call : calls) {
			if(call.getFunction().equals(startFunction) && call.getFromDepth().equals(depth)) {
				temp.add(call);
			}
		}
		temp.sort(new Comparator<DynamicCall>() {
			@Override
			public int compare(DynamicCall o1, DynamicCall o2) {
				return o1.getToOrder().compareTo(o2.getToOrder());
			}
		});
		for(DynamicCall t : temp) {
			result.add(t.getCallFunction());
		}
		return result;
	}
	
	private JSONObject test1(Function f, List<DynamicCall> calls, Long depth) {
		JSONObject result = new JSONObject();
		result.put("text", f.getName() + " " + f.getParametersIdentifies());
		JSONArray tagsArray = new JSONArray();
		tagsArray.add("function");
		result.put("tags", tagsArray);
		List<Function> callFunctions = test(f, calls, depth);
		if(callFunctions.size() == 0) {
			return result;
		}
		JSONArray nodes = new JSONArray();
		for(Function callFunction : callFunctions) {
			nodes.add(test1(callFunction, calls, depth + 1));
		}
		result.put("nodes", nodes);
		return result;
	}
	
	@GetMapping("/graphviz/span")
	@ResponseBody
	public JSONObject dynamicFunctionToGraphviz(@RequestParam("spanGraphId") Long spanGraphId) {
		JSONObject result = new JSONObject();
		try {
			Span span = msService.findSpanById(spanGraphId);
			List<DynamicCall> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			GraphvizTreeNode root = GraphvizUtil.generate(spanFunctionCalls);
			GraphvizUtil.print(root.toGraphviz(), "D:\\testfunctioncall.png");
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/treeview/span")
	@ResponseBody
	public JSONObject dynamicFunctionToTreeView(@RequestParam("spanGraphId") Long spanGraphId) {
		JSONObject result = new JSONObject();
		try {
			JSONArray functionArray = new JSONArray();
			
			Span span = msService.findSpanById(spanGraphId);
			SpanStartWithFunction spanStartWithFunction = msService.findSpanStartWithFunctionByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			List<DynamicCall> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			functionArray.add(test1(spanStartWithFunction.getFunction(), spanFunctionCalls, 0L));
			result.put("result", "success");
			result.put("value", functionArray);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/cytoscape/file")
	@ResponseBody
	@Deprecated
	public JSONObject dynamicFileCallFromMicroServiceInOneTraceToCatoscape(
			@RequestParam("microserviceGraphId") Long microserviceGraphId, 
			@RequestParam(required=false, name="traceId") String traceId,
			@RequestParam(required=false, name="callType") String type) {
		JSONObject result = new JSONObject();
		try {
			System.out.println(microserviceGraphId);
			MicroService ms = msService.findMicroServiceById(microserviceGraphId);
			if(ms == null) {
				throw new Exception("没有id为 " + microserviceGraphId + " 的MicroService");
			}
			List<DynamicCall> calls = new ArrayList<>();
			if(traceId == null) {
				calls = dynamicAnalyseService.findFunctionDynamicCallsByMicroService(ms);
			} else {
				Trace trace = dynamicAnalyseService.findTraceByTraceId(traceId);
				calls = dynamicAnalyseService.findFunctionDynamicCallsByTraceAndMicroService(trace, ms);
			}
			dependencyOrganizationService.dynamicCallDependency(calls);
			result.put("result", "success");
			if(type == null || "file".equals(type)) {
				result.put("value", dependencyOrganizationService.fileCallToCytoscape());
			} else if("package".equals(type)) {
				result.put("value", dependencyOrganizationService.directoryCallToCytoscape());
			} else if("function".equals(type)) {
				result.put("value", dependencyOrganizationService.functionCallToCytoscape());
			} else if("fileAndPackage".equals(type)) {
				result.put("value", dependencyOrganizationService.packageAndFileToCytoscape());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/treeview")
	@ResponseBody
	@Deprecated
	public JSONObject toTreeView() {
		JSONObject result = new JSONObject();
		try {
			JSONArray featureArray = new JSONArray();
			Iterable<Feature> features = organizationService.allFeatures();
			for(Feature feature : features) {
				JSONArray featureTags = new JSONArray();
				featureTags.add("feature");
				JSONObject featureJson = new JSONObject();
				featureJson.put("text", feature.getName());
				featureJson.put("tags", featureTags);
				featureJson.put("href", feature.getFeatureId());
				
				Set<MicroService> relatedMicroServices = organizationService.findRelatedMicroServiceForFeatures(feature);
				JSONArray msArray = new JSONArray();
				for(MicroService ms : relatedMicroServices) {
					JSONObject microservice = new JSONObject();
					microservice.put("text", ms.getName());
					JSONArray microserviceTags = new JSONArray();
					microserviceTags.add("microservice");
					microservice.put("tags", microserviceTags);
					microservice.put("href", ms.getId());
					JSONArray spanArray = new JSONArray();
//					Set<Trace> traces = organizationService.findRelatedTracesForFeature(features);
//					List<Span> spans = organizationService.findMicroServiceCreateSpansInTraces(ms, feature);
//					for(Span span : spans) {
//						JSONObject spanJson = new JSONObject();
//						spanJson.put("text", span.getOperationName());
//						JSONArray spanTags = new JSONArray();
//						spanTags.add("span");
//						spanJson.put("tags", spanTags);
//						spanJson.put("href", span.getId());
//						spanArray.add(spanJson);
//					}
					microservice.put("nodes", spanArray);
					msArray.add(microservice);
				}
				
				featureJson.put("nodes", msArray);
				featureArray.add(featureJson);
			}
			
			result.put("result", "success");
			result.put("value", featureArray);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
}
