package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.FileGod;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PackageGod;

public interface GodComponentDetector {
	
	Map<Long, List<FileGod>> fileGodComponents();
	
	Map<Long, List<PackageGod>> packageGodComponents();
	
	int getProjectMinFileLoc(Project project);
	
	void setProjectMinFileLoc(Project project, int minFileLoc);
	
	int getProjectMinFileCountInPackage(Project project);
	
	void setProjectMinFileCountInPackage(Project project, int minFileCountInPackage);

}
