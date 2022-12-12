package cn.edu.fudan.se.multidependency.service.insert.dynamic;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.DynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.JavaDynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TraceRunWithFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.utils.TimeUtil;
import cn.edu.fudan.se.multidependency.utils.query.DynamicUtil;

public class JavassistDynamicInserter extends DynamicInserterForNeo4jService {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavassistDynamicInserter.class);

	protected Map<Project, Map<String, List<Function>>> projectToFunctions;
	
	protected Map<String, List<DynamicFunctionExecution>> javaExecutionsGroupByProject;
	
	public JavassistDynamicInserter(File[] dynamicFunctionCallFiles) {
		super(dynamicFunctionCallFiles);
		projectToFunctions = new HashMap<>();
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		javaExecutionsGroupByProject = executionsGroupByLanguageAndProject.get(Language.java);
		LOGGER.info("提取微服务间的调用");
		extractMicroServiceCall();
		LOGGER.info("提取函数调用");
		extractDynamicFunctionCalls();
	}
	
	private Map<Trace, List<Span>> traceToSpans = new HashMap<>();

	private void addSpanToTrace(Trace trace, Span span) {
		List<Span> spans = traceToSpans.getOrDefault(trace, new ArrayList<>());
		if (!spans.contains(span)) {
			spans.add(span);
		}
		traceToSpans.put(trace, spans);
	}

	private Map<Span, String> spanToCallMethod = new HashMap<>();

	private Map<String, String> spanIdToParentSpanId = new HashMap<>();

	private void extractMicroServiceCall() throws Exception {
		// 单体应用执行该函数不会有操作，因为没有微服务间的调用，没有Span的概念
		for(String projectName : this.javaExecutionsGroupByProject.keySet()) {
			for(DynamicFunctionExecution execution : javaExecutionsGroupByProject.get(projectName)) {
				JavaDynamicFunctionExecution javaExecution = (JavaDynamicFunctionExecution) execution;
				Trace trace = null;
				String traceId = execution.getTraceId();
				if(!execution.isTraceIdBlank()) {
					trace = this.getNodes().findTraces().get(traceId);
					if (trace == null) {
						trace = new Trace();
						trace.setEntityId(generateEntityId());
						trace.setTraceId(traceId);
						trace.setMicroServiceTrace(execution.isCallBetweenMicroService());
						addNode(trace, null);
					}
				}
				String spanId = execution.getSpanId();
				String parentSpanId = execution.getParentSpanId();
				Long order = javaExecution.getOrder();
				Long depth = javaExecution.getDepth();
				if(!execution.isCallBetweenMicroService() || order != 0 || depth != 0) {
					continue;
				}
				Span span = this.getNodes().findSpanBySpanId(spanId);
				if (span == null) {
					span = new Span();
					span.setSpanId(spanId);
					span.setTraceId(traceId);
					span.setEntityId(generateEntityId());
					String functionName = execution.getFunctionName();
					span.setApiFunctionName(functionName);
					String operationName = "";
					if(functionName.contains(".")) {
						operationName = functionName.substring(functionName.lastIndexOf(".") + 1);
					} else {
						operationName = functionName;
					}
					span.setOperationName(operationName);
					span.setTime(TimeUtil.changeTimeStrToLong(execution.getTime()));
					spanToCallMethod.put(span, execution.getCallForm());
					
					Project project = this.getNodes().findProject(projectName, Language.java);
					if(project == null) {
						throw new Exception("error: span的serviceName不是一个项目 " + projectName);
					}
					MicroService microService = getNodes().findMicroServiceByName(project.getMicroserviceName());
					if(microService == null) {
						throw new Exception("error: span的serviceName不是一个项目 " + projectName);
					}
					addNode(span, project);

					Contain contain = new Contain(trace, span);
					addRelation(contain);
					addSpanToTrace(trace, span);

					MicroServiceCreateSpan projectCreateSpan = new MicroServiceCreateSpan(microService, span);
					addRelation(projectCreateSpan);
					
					RestfulAPI api = this.getNodes().findRestfulAPIByProjectAndSimpleFunctionName(project, operationName);
					if(api == null) {
						api = new RestfulAPI();
						api.setEntityId(this.generateEntityId());
						api.setApiFunctionSimpleName(operationName);
						addNode(api, project);
						Contain projectContainAPI = new Contain(project, api);
						addRelation(projectContainAPI);
						Contain microServiceContainAPI = new Contain(microService, api);
						addRelation(microServiceContainAPI);
					} 
					api.setApiFunctionName(functionName);
					
					SpanInstanceOfRestfulAPI spanInstanceOfMicroServiceAPI = new SpanInstanceOfRestfulAPI();
					spanInstanceOfMicroServiceAPI.setSpan(span);
					spanInstanceOfMicroServiceAPI.setApi(api);
					addRelation(spanInstanceOfMicroServiceAPI);
				}
				spanIdToParentSpanId.put(spanId, parentSpanId);
			}
		}

		for (String spanId : spanIdToParentSpanId.keySet()) {
			String parentSpanId = spanIdToParentSpanId.get(spanId);
			if ("-1".equals(parentSpanId)) {
				continue;
			}
			Span parentSpan = this.getNodes().findSpanBySpanId(parentSpanId);
			if (parentSpan == null) {
				throw new Exception("SpanId为 " + parentSpanId + " 的span不存在");
			}
			Span span = this.getNodes().findSpanBySpanId(spanId);
			SpanCallSpan spanCallSpan = new SpanCallSpan(parentSpan, span);
			spanCallSpan.setHttpRequestMethod(spanToCallMethod.get(span));
			addRelation(spanCallSpan);
		}

		for (Trace trace : traceToSpans.keySet()) {
			List<Span> sortedSpans = traceToSpans.get(trace);
			sortedSpans.sort(new Comparator<Span>() {
				@Override
				public int compare(Span o1, Span o2) {
					return o1.getTime().compareTo(o2.getTime());
				}
			});
			for (int i = 0; i < sortedSpans.size(); i++) {
				Span span = sortedSpans.get(i);
				span.setOrder(i);
			}
		}
	}

	private void extractDynamicFunctionCalls() {
		for(String projectName : javaExecutionsGroupByProject.keySet()) {
			// 分项目
			Project project = this.getNodes().findProject(projectName, Language.java);
			List<DynamicFunctionExecution> executions = javaExecutionsGroupByProject.get(projectName);
			// traceId, spanId, depth
			Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> executionsGroupByTrace = new HashMap<>();
			for(DynamicFunctionExecution execution : executions) {
				assert(execution.getClass() == JavaDynamicFunctionExecution.class);
				JavaDynamicFunctionExecution javaExecution = (JavaDynamicFunctionExecution) execution;
				if(execution.isTraceIdBlank()) {
					// traceId若为空，则不记录
					continue;
				}
				String traceId = execution.getTraceId();
				String spanId = execution.getSpanId();
				if(execution.isCallBetweenSingleSystem()) {
					spanId = traceId;
				}
				Long depth = javaExecution.getDepth();
				Map<String, Map<Long, List<JavaDynamicFunctionExecution>>> executionsGroupBySpan = executionsGroupByTrace.getOrDefault(traceId, new HashMap<>());
				Map<Long, List<JavaDynamicFunctionExecution>> depthResult = executionsGroupBySpan.getOrDefault(spanId, new HashMap<>());
				List<JavaDynamicFunctionExecution> executionsGroupByDepth = depthResult.getOrDefault(depth, new ArrayList<>());
				executionsGroupByDepth.add(javaExecution);
				depthResult.put(depth, executionsGroupByDepth);
				executionsGroupBySpan.put(spanId, depthResult);
				executionsGroupByTrace.put(traceId, executionsGroupBySpan);
			}
			extractDynamicFunctionCalls(executionsGroupByTrace, project);
		}
	}

	private void extractDynamicFunctionCalls(
			Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> allDynamicFunctionGroupByTrace, Project project) {
		for (String traceId : allDynamicFunctionGroupByTrace.keySet()) {
			for (String spanId : allDynamicFunctionGroupByTrace.get(traceId).keySet()) {
				Map<Long, List<JavaDynamicFunctionExecution>> depthResult = allDynamicFunctionGroupByTrace.get(traceId).get(spanId);
				for (Long depth : depthResult.keySet()) {
					List<JavaDynamicFunctionExecution> executions = depthResult.get(depth);
					for (JavaDynamicFunctionExecution execution : executions) {
						Map<String, List<Function>> functionsWithSameFunctionName = projectToFunctions.get(project);
						if (functionsWithSameFunctionName == null) {
							functionsWithSameFunctionName = this.getNodes().findFunctionsInProject(project);
							projectToFunctions.put(project, functionsWithSameFunctionName);
						}
						if (execution.getDepth() == 0) {
							List<Function> calledFunctions = functionsWithSameFunctionName.get(execution.getFunctionName());
							if (calledFunctions == null) {
								continue;
							}
							if(execution.isCallBetweenSingleSystem()) {
								Function calledFunction = findFunctionWithDynamic(execution, calledFunctions);
								Trace trace = this.getNodes().findTraces().get(execution.getTraceId());
								if(trace != null && calledFunction != null) {
									TraceRunWithFunction traceRunWithFunction = new TraceRunWithFunction(trace, calledFunction);
									traceRunWithFunction.setOrder(execution.getOrder() + "");
									addRelation(traceRunWithFunction);
								}
							}
							if (execution.getOrder() == 0) {
								// 是某段程序入口
								if(execution.isCallBetweenMicroService()) {
									Function calledFunction = findFunctionWithDynamic(execution, calledFunctions);
									Span span = this.getNodes().findSpanBySpanId(execution.getSpanId());
									if (span != null && calledFunction != null) {
										SpanStartWithFunction spanStartWithFunction = new SpanStartWithFunction(span, calledFunction);
										addRelation(spanStartWithFunction);
									}
								}
							}
							continue;
						}
						// 根据order和depth找到调用calledDynamicFunction的函数
						List<Long> list = new ArrayList<>();
						for (JavaDynamicFunctionExecution temp : depthResult.get(execution.getDepth() - 1)) {
							list.add(temp.getOrder());
						}
						JavaDynamicFunctionExecution callerDynamicFunction = null;
						int callerIndex = DynamicUtil.find(execution.getOrder(), list);
						if (callerIndex != -1) {
							callerDynamicFunction = depthResult.get(execution.getDepth() - 1).get(callerIndex);
						} else {
							continue;
						}
						// 找出在静态分析中对应的calledFunction和callerFunction
						// 可能存在方法名相同，通过参数精确判断出哪个方法
						List<Function> calledFunctions = functionsWithSameFunctionName.get(execution.getFunctionName());
						List<Function> callerFunctions = functionsWithSameFunctionName.get(callerDynamicFunction.getFunctionName());
						if (calledFunctions == null || callerFunctions == null) {
							continue;
						}
						Function calledFunction = findFunctionWithDynamic(execution, calledFunctions);
						Function callerFunction = findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
						if (calledFunction == null || callerFunction == null) {
							continue;
						}
						addRelation(generateFunctionDynamicCall(callerFunction, calledFunction, callerDynamicFunction,execution));
					}
				}
			}
		}
	}

	/**
	 * 从同名的Function中找到与dynamicFunction的参数对应的Function
	 * 
	 * @param dynamicFunction
	 * @param functions
	 * @return
	 */
	private Function findFunctionWithDynamic(JavaDynamicFunctionExecution dynamicFunction, List<Function> functions) {
		// Function根据参数排序
		functions.sort(new Comparator<Function>() {
			@Override
			public int compare(Function o1, Function o2) {
				if (o1.getParameters() == null || o2.getParameters() == null) {
					return -1;
				}
				return o1.getParameters().size() - o2.getParameters().size();
			}
		});
		for (Function function : functions) {
			// 方法名是否相同
			if (!dynamicFunction.getFunctionName().equals(function.getName())) {
				continue;
			}
			// 方法参数数量是否相同
			if (function.getParameters().size() != dynamicFunction.getParameters().size()) {
				continue;
			}
			// 方法名相同且只有一个参数，直接返回此Function
			if (functions.size() == 1) {
				return function;
			}
			// 参数一一对应
			boolean flag = false;
			for (int i = 0; i < function.getParameters().size(); i++) {
				// 动态分析得到的参数的类型是完整的
				if (dynamicFunction.getParameters().get(i).indexOf(function.getParameters().get(i)) < 0) {
					// 参数没有对应
					flag = true;
					break;
				}
			}
			if (flag) {
				continue;
			}
			return function;
		}
		return null;
	}

	private DynamicCall generateFunctionDynamicCall(Function callerFunction, Function calledFunction,
			JavaDynamicFunctionExecution callerDynamicFunction, JavaDynamicFunctionExecution calledDynamicFunction) {
		DynamicCall relation = new DynamicCall(callerFunction, calledFunction,
				calledDynamicFunction.getProject(), calledDynamicFunction.getLanguage().toString());
		relation.setTraceId(callerDynamicFunction.getTraceId());
		relation.setSpanId(callerDynamicFunction.getSpanId());
		relation.setOrder(callerDynamicFunction.getOrder() + ":" + callerDynamicFunction.getDepth() + " -> "
				+ calledDynamicFunction.getOrder() + ":" + calledDynamicFunction.getDepth());
		relation.setFromOrder(callerDynamicFunction.getOrder());
		relation.setToOrder(calledDynamicFunction.getOrder());
		relation.setFromDepth(callerDynamicFunction.getDepth());
		relation.setToDepth(calledDynamicFunction.getDepth());
		return relation;
	}
	
}
