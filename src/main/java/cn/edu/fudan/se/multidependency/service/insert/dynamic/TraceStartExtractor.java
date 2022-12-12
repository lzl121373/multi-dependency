package cn.edu.fudan.se.multidependency.service.insert.dynamic;

import java.io.File;
import java.sql.Timestamp;
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
import cn.edu.fudan.se.multidependency.utils.TimeUtil;
import lombok.Data;
import lombok.ToString;

/**
 * 不做插入数据库操作，只从日志文件中找出parentSpanId为-1的trace
 * 与语言无关
 * @author fan
 *
 */
public class TraceStartExtractor extends DynamicInserterForNeo4jService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TraceStartExtractor.class);
	
	public TraceStartExtractor(File[] dynamicFunctionCallFiles) {
		super(dynamicFunctionCallFiles);
	}

	@Data
	@ToString
	public static class TraceStartWithProject {
		String project;
		Language language;
		String traceId;
		String time;
		String functionName;
		
		public TraceStartWithProject(DynamicFunctionExecution execution) {
			this.project = execution.getProject();
			this.language = execution.getLanguage();
			this.traceId = execution.getTraceId();
			this.time = execution.getTime();
			this.functionName = execution.getFunctionName();
		}
	}
	
	private Map<String, TraceStartWithProject> traceStartProjects = new HashMap<>();	
	
	public List<TraceStartWithProject> getTraceStartProjects() {
		List<TraceStartWithProject> result = new ArrayList<>();
		for(String traceId : traceStartProjects.keySet()) {
			result.add(traceStartProjects.get(traceId));
		}
		result.sort(new Comparator<TraceStartWithProject>() {
			@Override
			public int compare(TraceStartWithProject o1, TraceStartWithProject o2) {
				Timestamp o1time = new Timestamp(TimeUtil.changeTimeStrToLong(o1.getTime()));
				Timestamp o2time = new Timestamp(TimeUtil.changeTimeStrToLong(o2.getTime()));
				
//				return o1.getProject().compareTo(o2.getProject());
				return o1time.compareTo(o2time);
			}
		});
		return result;
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		for(Language language : this.executionsGroupByLanguageAndProject.keySet()) {
			for(String projectName : this.executionsGroupByLanguageAndProject.get(language).keySet()) {
				List<DynamicFunctionExecution> executions = this.executionsGroupByLanguageAndProject.get(language).get(projectName);
				for(DynamicFunctionExecution execution : executions) {
					if((execution.isCallBetweenMicroService() 
							&& JavaDynamicFunctionExecution.TRACE_START_PARENT_SPAN_ID.equals(execution.getParentSpanId())) || execution.isCallBetweenSingleSystem()) {
						addTraceStartProjects(execution);
					}
				}
			}
		}
		for(TraceStartWithProject project : getTraceStartProjects()) {
			LOGGER.info(project.toString());
		}
	}
	private void addTraceStartProjects(DynamicFunctionExecution execution) {
		String traceId = execution.getTraceId();
		if(traceStartProjects.get(traceId) == null) {
			TraceStartWithProject project = new TraceStartWithProject(execution);
			traceStartProjects.put(traceId, project);
		}
	}

}
