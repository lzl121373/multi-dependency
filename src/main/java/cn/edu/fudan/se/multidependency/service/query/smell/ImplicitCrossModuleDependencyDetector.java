package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.LogicCouplingComponents;

public interface ImplicitCrossModuleDependencyDetector {

	Map<Long, List<LogicCouplingComponents<ProjectFile>>> queryFileImplicitCrossModuleDependency();

	Map<Long, List<LogicCouplingComponents<Package>>> queryPackageImplicitCrossModuleDependency();

	Map<Long, List<LogicCouplingComponents<ProjectFile>>> detectFileImplicitCrossModuleDependency();

	Map<Long, List<LogicCouplingComponents<Package>>> detectPackageImplicitCrossModuleDependency();

	void setProjectFileMinCoChange(Long projectId, int minFileCoChange);

	void setProjectPackageMinCoChange(Long projectId, int minPackageCoChange);
	
	Integer getFileMinCoChange(Long projectId);

	Integer getPackageMinCoChange(Long projectId);

}
