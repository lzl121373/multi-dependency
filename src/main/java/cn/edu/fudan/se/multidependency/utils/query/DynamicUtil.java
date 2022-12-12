package cn.edu.fudan.se.multidependency.utils.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.exception.DynamicLogSentenceErrorException;
import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.CppDynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.DynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.JavaDynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.Language;

public class DynamicUtil {
	
	/**
	 * 语言，项目名，函数调用
	 * @param files
	 * @return
	 */
	public static Map<Language, Map<String, List<DynamicFunctionExecution>>> readDynamicLogs(File... files) {
		Map<Language, Map<String, List<DynamicFunctionExecution>>> result = new HashMap<>();
		for(File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					DynamicFunctionExecution dynamicFunction = null;
					try {
						dynamicFunction = extractDynamicFunctionExecution(line);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					Language language = dynamicFunction.getLanguage();
					Map<String, List<DynamicFunctionExecution>> projectExecutions = result.getOrDefault(language, new HashMap<>());
					String project = dynamicFunction.getProject();
					List<DynamicFunctionExecution> executions = projectExecutions.getOrDefault(project, new ArrayList<>());
					executions.add(dynamicFunction);
					projectExecutions.put(project, executions);
					result.put(language, projectExecutions);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static DynamicFunctionExecution extractDynamicFunctionExecution(String sentence) throws DynamicLogSentenceErrorException, Exception {
		try {
			JSONObject json = JSONObject.parseObject(sentence);
			Language language = Language.valueOf(json.getString("language"));
			switch(language) {
			case java:
				JavaDynamicFunctionExecution functionExecution = new JavaDynamicFunctionExecution();
				functionExecution.setSentence(sentence);
				functionExecution.setTime(json.getString("time"));
				functionExecution.setProject(json.getString("project"));
				String file = json.getString("inFile");
				file = file == null ? json.getString("file") : file;
				functionExecution.setInFile(file);
				String function = json.getString("function");
				String functionName = function.substring(0, function.indexOf("("));
				functionExecution.setFunctionName(functionName);
				String parametersStr = function.substring(function.indexOf("(") + 1, function.length() - 1);
				if(!StringUtils.isBlank(parametersStr)) {
					String[] parameters = parametersStr.split(",");
					for(String parameter : parameters) {
						functionExecution.addParameter(parameter.trim());
					}
				}
				functionExecution.setOrder(Long.parseLong(json.getString("order")));
				functionExecution.setDepth(Long.parseLong(json.getString("depth")));
				functionExecution.setRemarks(json.getJSONObject("remarks"));
				functionExecution.setTraceId(json.getString("traceId"));
				functionExecution.setSpanId(json.getString("spanId"));
				functionExecution.setParentSpanId(json.getString("parentSpanId"));
				functionExecution.setThreadId(json.getLong("currentThreadId"));
				functionExecution.setThreadName(json.getString("currentThreadName"));
				String callForm = json.getString("callMethod");
				if(callForm == null) {
					callForm = json.getString("callForm");
				}
				functionExecution.setCallForm(callForm);
				Boolean isConstructor = json.getBoolean("isConstructor");
				if(isConstructor != null) {
					functionExecution.setConstructor(isConstructor);
				}
				return functionExecution;
			case cpp:
				return new CppDynamicFunctionExecution();
			default:
				throw new LanguageErrorException(json.getString("language"));
			}
		} catch (LanguageErrorException e) {
			throw new LanguageErrorException(e.getErrorLanguage());
		} catch (Exception e) {
			throw new DynamicLogSentenceErrorException(sentence);
		}
	}
	
	/**
	 * 从一组数据中找出最大的小于num的数，并返回下标
	 * list为排序数组，否则后果自负
	 * num不在list中，否则返回-1
	 * @param num
	 * @param list
	 * @return
	 */
	public static int find(Long num, List<Long> list) {
		if(list == null || list.size() == 0 || list.contains(num) || num < list.get(0)) {
			return -1;
		}
		if(num > list.get(list.size() - 1)) {
			return list.size() - 1;
		}
		int lessIndex = 0;
		int moreIndex = list.size() - 1;
		while(lessIndex < moreIndex) {
			int midIndex = (moreIndex + lessIndex) / 2;
			if(list.get(midIndex) > num) {
				moreIndex = midIndex;
			} else {
				lessIndex = midIndex;
			}
			if(moreIndex - lessIndex == 1) {
				break;
			}
		}
		return lessIndex;
	}
	
}
