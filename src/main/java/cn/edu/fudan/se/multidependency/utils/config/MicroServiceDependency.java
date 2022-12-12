package cn.edu.fudan.se.multidependency.utils.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MicroServiceDependency {
	private String microService;
	private List<String> dependencies = new ArrayList<>();

	public void addDependency(String dependency) {
		this.dependencies.add(dependency);
	}
}