package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByInstability;

public interface UnstableDependencyDetectorUsingInstability {

	Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> queryFileUnstableDependency();

	Map<Long, List<UnstableDependencyByInstability<Package>>> queryPackageUnstableDependency();
	
	Map<Long, List<UnstableDependencyByInstability<Module>>> queryModuleUnstableDependency();

	Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> detectFileUnstableDependency();

	Map<Long, List<UnstableDependencyByInstability<Package>>> detectPackageUnstableDependency();

	Map<Long, List<UnstableDependencyByInstability<Module>>> detectModuleUnstableDependency();
	
	void setProjectMinFileFanOut(Long projectId, Integer minFileFanOut);
	
	void setProjectMinPackageFanOut(Long projectId, Integer minPackageFanOut);

	void setProjectMinModuleFanOut(Long projectId, Integer minModuleFanOut);

	void setProjectMinRatio(Long projectId, Double minRatio);
	
	Integer getProjectMinFileFanOut(Long projectId);

	Integer getProjectMinPackageFanOut(Long projectId);

	Integer getProjectMinModuleFanOut(Long projectId);
	
	Double getProjectMinRatio(Long projectId);
}
