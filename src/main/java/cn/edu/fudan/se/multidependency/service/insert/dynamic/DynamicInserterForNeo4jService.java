package cn.edu.fudan.se.multidependency.service.insert.dynamic;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.DynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.query.DynamicUtil;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	private File[] dynamicFunctionCallFiles;
	
	protected abstract void extractNodesAndRelations() throws Exception;
	
	protected Map<Language, Map<String, List<DynamicFunctionExecution>>> executionsGroupByLanguageAndProject;
	
	public DynamicInserterForNeo4jService(File[] dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}
	
	@Override
	public void addNodesAndRelations() throws Exception {
		if(dynamicFunctionCallFiles == null) {
			throw new Exception("动态运行日志dynamicFunctionCallFiles不能为null！");
		}
		executionsGroupByLanguageAndProject = DynamicUtil.readDynamicLogs(dynamicFunctionCallFiles);
		extractNodesAndRelations();
	}

	public void setDynamicFunctionCallFiles(File... dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}
	
}
