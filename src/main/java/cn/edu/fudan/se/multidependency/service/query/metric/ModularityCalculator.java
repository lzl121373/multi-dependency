package cn.edu.fudan.se.multidependency.service.query.metric;

import cn.edu.fudan.se.multidependency.model.node.Project;

public interface ModularityCalculator {
	
	Modularity calculate(Project project);
	
}
