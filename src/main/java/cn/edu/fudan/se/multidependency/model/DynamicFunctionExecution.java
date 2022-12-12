package cn.edu.fudan.se.multidependency.model;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public abstract class DynamicFunctionExecution {
	protected String sentence;
	protected String time;
	protected String project;
	protected String inFile;
	protected String functionName;
	protected String traceId;
	protected String spanId;
	protected String parentSpanId;
	protected String callForm;
	protected JSONObject remarks;
	
	public static final String TRACE_START_PARENT_SPAN_ID = "-1";
	
	public abstract Language getLanguage();

	/**
	 * 三者都不空的情况下为微服务间的调用引起的函数调用
	 * @return
	 */
	public boolean isCallBetweenMicroService() {
		return StringUtils.isNoneBlank(traceId, spanId, parentSpanId);
	}
	
	public boolean isTraceIdBlank() {
		return StringUtils.isBlank(traceId);
	}
	
	public boolean isCallBetweenSingleSystem() {
		return !isCallBetweenMicroService() && !isTraceIdBlank();
	}
	
	
}
