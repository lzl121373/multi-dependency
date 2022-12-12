package cn.edu.fudan.se.multidependency.service.insert.code;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

/**
 * 调用depends的API提取代码entity
 * @author fan
 *
 */
public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo() throws Exception;
	
	void setLanguage(Language language);
	
	void setProjectPath(String projectPath);
	
	void setExcludes(List<String> excludes);

	int getEntityCount();
	
	void setAutoInclude(boolean autoInclude);
	
	void setIncludeDirs(String[] includeDirs);
}
