package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

public interface UnusedComponentDetector {

	Map<Long, List<Package>> unusedPackages();
	
	Map<Long, List<ProjectFile>> unusedFiles();
	
}
