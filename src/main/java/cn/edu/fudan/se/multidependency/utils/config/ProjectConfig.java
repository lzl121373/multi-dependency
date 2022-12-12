package cn.edu.fudan.se.multidependency.utils.config;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

@Data
public class ProjectConfig {
	private String path;
	private String project;
	private Language language;
	private boolean isMicroService;
	private String microserviceName;
	private String serviceGroupName;
	private List<String> excludes = new ArrayList<>();
	private List<String> includeDirs = new ArrayList<>();
	private boolean autoInclude = true;
	private RestfulAPIConfig apiConfig;

	public void addExclude(String exclude) {
		this.excludes.add(exclude);
	}

	public void addIncludeDir(String includeDir) {
		this.includeDirs.add(includeDir);
	}

	public String[] includeDirsArray() {
		String[] result = new String[includeDirs.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = includeDirs.get(i);
		}
		return result;
	}
}
