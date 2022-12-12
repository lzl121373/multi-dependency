package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class MultipleASFile implements MultipleAS {

	private Project project;

	private ProjectFile file;
	
	private boolean cyclicDependency;
	
	private boolean hubLikeDependency;

	private boolean unstableDependency;

	private boolean unstableInterface;
	
	private boolean implicitCrossModuleDependency;

	private boolean unutilizedAbstraction;
	
	private boolean unusedInclude;

	public MultipleASFile(ProjectFile file) {
		this.file = file;
	}
	
	public boolean isSmellFile(MultipleAS smell) {
		if (smell.isCyclicDependency() && isCyclicDependency()) {
			return true;
		}
		if (smell.isHubLikeDependency() && isHubLikeDependency()) {
			return true;
		}
		if (smell.isUnstableDependency() && isUnstableDependency()) {
			return true;
		}
		if (smell.isImplicitCrossModuleDependency() && isImplicitCrossModuleDependency()) {
			return true;
		}
		if (smell.isUnutilizedAbstraction() && isUnutilizedAbstraction()) {
			return true;
		}
		if (smell.isUnusedInclude() && isUnusedInclude()) {
			return true;
		}
		return false;
	}
}
