package cn.edu.fudan.se.multidependency.service.insert.code;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SwaggerJSON {
	
	private String path;
	
	private List<String> excludeTags = new ArrayList<>();
	
	public void addExcludeTag(String tag) {
		this.excludeTags.add(tag);
	}
}
