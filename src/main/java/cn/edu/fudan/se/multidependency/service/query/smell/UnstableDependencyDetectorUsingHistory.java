package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByHistory;

public interface UnstableDependencyDetectorUsingHistory {

	Map<Long, List<UnstableDependencyByHistory>> queryFileUnstableDependency();

	Map<Long, List<UnstableDependencyByHistory>> detectFileUnstableDependency();

	void setProjectMinFileFanOut(Long projectId, Integer minFileFanOut);

	void setProjectFileMinCoChange(Long projectId, Integer minFileCoChange);

	void setProjectCoChangeFile(Long projectId, Integer coChangeFile);

	void setProjectMinRatio(Long projectId, Double minRatio);

	Integer getProjectMinFileFanOut(Long projectId);

	Integer getProjectFileMinCoChange(Long projectId);

	Integer getProjectCoChangeFile(Long projectId);

	Double getProjectMinRatio(Long projectId);
}
