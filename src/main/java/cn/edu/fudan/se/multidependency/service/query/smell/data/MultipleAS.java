package cn.edu.fudan.se.multidependency.service.query.smell.data;

public interface MultipleAS {

	boolean isCyclicDependency();
	
	boolean isHubLikeDependency();

	boolean isUnstableDependency();

	boolean isUnstableInterface();
	
	boolean isImplicitCrossModuleDependency();
	
	boolean isUnutilizedAbstraction();
	
	boolean isUnusedInclude();

	default int getSmellCount() {
		return (isCyclicDependency() ? 1 : 0) + (isHubLikeDependency() ? 1 : 0) + (isUnstableDependency() ? 1 : 0) + (isUnstableInterface() ? 1 : 0) + (isImplicitCrossModuleDependency() ? 1 : 0) + (isUnutilizedAbstraction() ? 1 : 0) + (isUnusedInclude() ? 1 : 0);
	}
}
