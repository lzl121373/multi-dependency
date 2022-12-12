package cn.edu.fudan.se.multidependency.utils.config;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

@Data
public class CloneConfig {
	private Language language;
	private Granularity granularity;
	private String namePath;
	private String resultPath;
	private String groupPath;
}