package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableInterface;

import java.util.List;
import java.util.Map;

public interface UnstableInterfaceDetector {

	Map<Long, List<UnstableInterface>> queryFileUnstableInterface();

	Map<Long, List<UnstableInterface>> detectFileUnstableInterface();

	void setProjectMinFileFanIn(Long projectId, Integer minFileFanIn);
	
	void setProjectFileMinCoChange(Long projectId, Integer minFileCoChange);
	
	void setProjectCoChangeFile(Long projectId, Integer coChangeFile);

	void setProjectMinRatio(Long projectId, Double minRatio);

	Integer getProjectMinFileFanIn(Long projectId);

	Integer getProjectFileMinCoChange(Long projectId);

	Integer getProjectCoChangeFile(Long projectId);

	Double getProjectMinRatio(Long projectId);
}
