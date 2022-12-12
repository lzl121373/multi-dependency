package cn.edu.fudan.se.multidependency.utils.config;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

@Data
public class LibConfig {
	private Language language;
	private String path;
}
