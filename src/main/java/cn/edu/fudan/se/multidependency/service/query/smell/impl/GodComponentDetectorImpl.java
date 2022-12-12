package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.GodComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.FileGod;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PackageGod;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetric;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class GodComponentDetectorImpl implements GodComponentDetector {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MetricCalculatorService metricCalculatorService;
	
	@Autowired
	private CacheService cache;

	@Override
	public Map<Long, List<FileGod>> fileGodComponents() {
		String key = "fileGodComponents";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<FileGod>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), fileGods(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<PackageGod>> packageGodComponents() {
		String key = "packageGodComponents";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<PackageGod>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), packageGod(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	private Map<Project, Integer> projectToMinFileLoc = new ConcurrentHashMap<>();
	
	private Map<Project, Integer> projectToMinFileCountInPackage = new ConcurrentHashMap<>();
	
	private List<FileGod> fileGods(Project project) {
		Collection<FileMetric> metrics = metricCalculatorService.calculateFileMetrics(project);
		List<FileGod> result = new ArrayList<>();
		for(FileMetric metric : metrics) {
			if(isFileGod(project, metric)) {
				result.add(new FileGod(metric.getFile(), metric));
			}
		}
		result.sort((f1, f2) -> {
			return f2.getMetrics().getStructureMetric().getLoc() - f1.getMetrics().getStructureMetric().getLoc();
		});
		return result;
	}
	
	protected boolean isFileGod(Project project, FileMetric metrics) {
		return metrics.getStructureMetric().getLoc() >= getProjectMinFileLoc(project);
	}
	
	private List<PackageGod> packageGod(Project project) {
		Collection<PackageMetric> metrics = metricCalculatorService.calculateProjectPackageMetrics(project);
		List<PackageGod> result = new ArrayList<>();
		for(PackageMetric metric : metrics) {
			if(isPackageGod(project, metric)) {
				result.add(new PackageGod(metric.getPck(), metric));
			}
		}
		result.sort((p1, p2) -> {
			return p2.getPckMetrics().getNof() - p1.getPckMetrics().getNof();
		});
		return result;
	}
	
	protected boolean isPackageGod(Project project, PackageMetric metrics) {
		return metrics.getNof() >= getProjectMinFileCountInPackage(project);
	}
	
	private int defaultProjectMinFileLoc(Project project) {
		return 1000;
	}
	
	private int defaultProjectMinFileCountInPackage(Project project) {
		return 30;
	}

	@Override
	public int getProjectMinFileLoc(Project project) {
		if(projectToMinFileLoc.get(project) == null) {
			projectToMinFileLoc.put(project, defaultProjectMinFileLoc(project));
		}
		return projectToMinFileLoc.get(project);
	}

	@Override
	public void setProjectMinFileLoc(Project project, int minFileLoc) {
		projectToMinFileLoc.put(project, minFileLoc);
		cache.remove(getClass());
	}

	@Override
	public int getProjectMinFileCountInPackage(Project project) {
		if(projectToMinFileCountInPackage.get(project) == null) {
			projectToMinFileCountInPackage.put(project, defaultProjectMinFileCountInPackage(project));
		}
		return projectToMinFileCountInPackage.get(project);
	}

	@Override
	public void setProjectMinFileCountInPackage(Project project, int minFileCountInPackage) {
		projectToMinFileCountInPackage.put(project, minFileCountInPackage);
		cache.remove(getClass());
	}
	
}
