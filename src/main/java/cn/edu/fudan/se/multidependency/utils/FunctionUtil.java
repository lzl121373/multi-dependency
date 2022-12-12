package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;

public class FunctionUtil {
	
	public static void main(String[] args) throws Exception {
//		System.out.println(extractFunctionNameAndParameters("depends.format.json.JsonFormatDependencyDumper.toJson(JDepObject, String)"));
		String functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.putMacros(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.MacroEhcacheRepo(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		functionFullName = "com.google.common.cache.CacheTesting.checkRecency(LoadingCache<Integer, Integer>,int,Receiver<ReferenceEntry<Integer, Integer)";
//		while(functionFullName.contains("<")) {
//			functionFullName = functionFullName.replaceAll("<[^<>]*>", "");
//		}
		System.out.println(extractFunctionNameAndParameters(functionFullName));
		
	/*	StringBuilder builder = new StringBuilder();
		builder.append("configure");
		builder.append("(");
		for(int i = 0; i < 5; i++) {
			if(i != 0) {
				builder.append(",");
			}
			builder.append("Map<String, Map<String,String>>");
		}
		builder.append(")");
		functionFullName = builder.toString();
		System.out.println(extractFunctionNameAndParameters(functionFullName));*/
	}
	
	public static List<String> extractFunctionNameAndParameters(String functionFullName) throws Exception {
		List<String> result = new ArrayList<>();
		int index = functionFullName.indexOf("(");
		String functionName = functionFullName.substring(0, index);
		if(functionName.contains(".")) {
			String[] functionNameSplit = functionName.split("\\.");
			boolean isContrustor = false;
			if(functionNameSplit.length >= 2) {
				isContrustor = functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]);
			}
			if(isContrustor) {
				functionName = functionName.substring(0, functionName.lastIndexOf("."));
			}
		}
		result.add(functionName);
		String parametersStr = functionFullName.substring(index + 1, functionFullName.length() - 1);
		result.add(parametersStr.replace(" ", ""));
		/*String[] parameters = parametersStr.split(",");
		Queue<String> queue = new LinkedList<>();
		int middleBracket = 0;
		for(String parameter : parameters) {
			if(StringUtils.isBlank(parameter)) {
				continue;
			}
			queue.offer(parameter);
			if(!parameter.contains("<") && !parameter.contains(">") && middleBracket == 0) {
				result.add(queue.poll());
			} else if(parameter.contains("<")) {
				middleBracket++;
			} else if(parameter.contains(">")) {
				middleBracket--;
			}
		}*/
		return result;
	}
	
}
