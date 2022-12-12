package cn.edu.fudan.se.multidependency.utils.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RestfulAPIConfig {
	private String framework;
	private List<String> excludeTags = new ArrayList<>();
	private String path;
	public static final String FRAMEWORK_SWAGGER = "swagger";

	public void addExcludeTag(String excludeTag) {
		this.excludeTags.add(excludeTag);
	}
}

