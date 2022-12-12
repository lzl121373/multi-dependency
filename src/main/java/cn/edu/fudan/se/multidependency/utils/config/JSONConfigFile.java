package cn.edu.fudan.se.multidependency.utils.config;

import java.util.Collection;

import lombok.Data;

@Data
public class JSONConfigFile {
	private Collection<ProjectConfig> projectsConfig;
	private Collection<MicroServiceDependency> microServiceDependencies;
	private DynamicConfig dynamicsConfig;
	private Collection<GitConfig> gitsConfig;
	private Collection<LibConfig> libsConfig;
	private Collection<CloneConfig> clonesConfig;
}