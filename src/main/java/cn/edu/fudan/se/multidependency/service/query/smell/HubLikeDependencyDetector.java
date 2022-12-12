package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.FileHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.ModuleHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PackageHubLike;

public interface HubLikeDependencyDetector {
	
	Map<Long, List<FileHubLike>> queryFileHubLikeDependency();

	Map<Long, List<PackageHubLike>> queryPackageHubLikeDependency();

	Map<Long, List<ModuleHubLike>> queryModuleHubLikeDependency();

	Map<Long, List<FileHubLike>> detectFileHubLikeDependency();

	Map<Long, List<PackageHubLike>> detectPackageHubLikeDependency();

	Map<Long, List<ModuleHubLike>> detectModuleHubLikeDependency();

	void setProjectMinFileFanIO(Long projectId, Integer minFileFanIn, Integer minFileFanOut);

	void setProjectMinPackageFanIO(Long projectId, Integer minPackageFanIn, Integer minPackageFanOut);

	void setProjectMinModuleFanIO(Long projectId, Integer minModuleFanIn, Integer minModuleFanOut);

	Integer[] getProjectMinFileFanIO(Long projectId);

	Integer[] getProjectMinPackageFanIO(Long projectId);

	Integer[] getProjectMinModuleFanIO(Long projectId);
}
