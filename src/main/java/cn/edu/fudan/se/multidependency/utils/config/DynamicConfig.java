package cn.edu.fudan.se.multidependency.utils.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DynamicConfig {
	private List<String> fileSuffixes = new ArrayList<>();;
	private String logsDirectoryPath;
	private String featuresPath;

	public void addFileSuffix(String fileSuffix) {
		this.fileSuffixes.add(fileSuffix);
	}
}