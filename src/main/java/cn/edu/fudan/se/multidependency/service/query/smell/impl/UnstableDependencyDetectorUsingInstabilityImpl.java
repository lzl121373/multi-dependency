package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.smell.UnstableASRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingInstability;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByInstability;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingInstabilityImpl implements UnstableDependencyDetectorUsingInstability {

	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private UnstableASRepository asRepository;
	
	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private DependsOnRepository dependsOnRepository;
	
	public static final int DEFAULT_MIN_FILE_FAN_OUT = 10;
	public static final int DEFAULT_MIN_PACKAGE_FAN_OUT = 10;
	public static final int DEFAULT_MIN_MODULE_FAN_OUT = 10;
	public static final double DEFAULT_MIN_RATIO = 0.3;
	
	private final Map<Long, Integer> projectToMinFileFanOutMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToMinPackageFanOutMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToMinModuleFanOutMap = new ConcurrentHashMap<>();
	private final Map<Long, Double> projectToMinRatioMap = new ConcurrentHashMap<>();

	@Override
	public Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> queryFileUnstableDependency() {
		String key = "fileUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<UnstableDependencyByInstability<ProjectFile>> fileUnstableDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			if (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				UnstableDependencyByInstability<ProjectFile> fileUnstableDependency = new UnstableDependencyByInstability<>();
				fileUnstableDependency.setComponent(component);
				List<DependsOn> dependsOns = dependsOnRepository.findFileDependsOn(component.getId());
				fileUnstableDependency.addAllTotalDependencies(dependsOns);
				for(DependsOn dependsOn : dependsOns) {
					ProjectFile dependsOnFile = (ProjectFile) dependsOn.getEndNode();
					if(component.getInstability() < dependsOnFile.getInstability()) {
						fileUnstableDependency.addBadDependency(dependsOn);
					}
				}
				fileUnstableDependency.setAllDependencies();
				fileUnstableDependency.setBadDependencies();
				fileUnstableDependencyList.add(fileUnstableDependency);
			}
		}
		for (UnstableDependencyByInstability<ProjectFile> fileUnstableDependency : fileUnstableDependencyList) {
			Project project = containRelationService.findFileBelongToProject(fileUnstableDependency.getComponent());
			if (project != null) {
				List<UnstableDependencyByInstability<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileUnstableDependency);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByInstability<Package>>> queryPackageUnstableDependency() {
		String key = "packageUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableDependencyByInstability<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.UNSTABLE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<UnstableDependencyByInstability<Package>> fileUnstableDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			if (iterator.hasNext()) {
				Package component = (Package) iterator.next();
				UnstableDependencyByInstability<Package> packageUnstableDependency = new UnstableDependencyByInstability<>();
				packageUnstableDependency.setComponent(component);
				List<DependsOn> dependsOns = dependsOnRepository.findModuleDependsOn(component.getId());
				packageUnstableDependency.addAllTotalDependencies(dependsOns);
				for(DependsOn dependsOn : dependsOns) {
					Package dependsOnPackage = (Package) dependsOn.getEndNode();
					if(component.getInstability() < dependsOnPackage.getInstability()) {
						packageUnstableDependency.addBadDependency(dependsOn);
					}
				}
				packageUnstableDependency.setAllDependencies();
				packageUnstableDependency.setBadDependencies();
				fileUnstableDependencyList.add(packageUnstableDependency);
			}
		}
		for (UnstableDependencyByInstability<Package> fileUnstableDependency : fileUnstableDependencyList) {
			Project project = containRelationService.findPackageBelongToProject(fileUnstableDependency.getComponent());
			if (project != null) {
				List<UnstableDependencyByInstability<Package>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileUnstableDependency);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByInstability<Module>>> queryModuleUnstableDependency() {
		String key = "moduleUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableDependencyByInstability<Module>>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> detectFileUnstableDependency() {
		Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableDependencyByInstability<ProjectFile>> temp = asRepository.unstableFilesByInstability(project.getId(), getProjectMinFileFanOut(project.getId()), getProjectMinRatio(project.getId()));
			result.put(project.getId(), temp);
		}
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByInstability<Package>>> detectPackageUnstableDependency() {
		Map<Long, List<UnstableDependencyByInstability<Package>>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableDependencyByInstability<Package>> temp = asRepository.unstablePackagesByInstability(project.getId(), getProjectMinPackageFanOut(project.getId()), getProjectMinRatio(project.getId()));
			result.put(project.getId(), temp);
		}
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByInstability<Module>>> detectModuleUnstableDependency() {
		Map<Long, List<UnstableDependencyByInstability<Module>>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableDependencyByInstability<Module>> temp = asRepository.unstableModulesByInstability(project.getId(), getProjectMinModuleFanOut(project.getId()), getProjectMinRatio(project.getId()));
			result.put(project.getId(), temp);
		}
		return result;
	}

	@Override
	public void setProjectMinFileFanOut(Long projectId, Integer minFileFanOut) {
		this.projectToMinFileFanOutMap.put(projectId, minFileFanOut);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinPackageFanOut(Long projectId, Integer minPackageFanOut) {
		this.projectToMinPackageFanOutMap.put(projectId, minPackageFanOut);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinModuleFanOut(Long projectId, Integer minModuleFanOut) {
		this.projectToMinModuleFanOutMap.put(projectId, minModuleFanOut);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinRatio(Long projectId, Double minRatio) {
		this.projectToMinRatioMap.put(projectId, minRatio);
		cache.remove(getClass());
	}

	@Override
	public Integer getProjectMinFileFanOut(Long projectId) {
		if (!projectToMinFileFanOutMap.containsKey(projectId)) {
			Integer medFileFanOut = metricRepository.getMedFileFanOutByProjectId(projectId);
			if (medFileFanOut != null) {
				projectToMinFileFanOutMap.put(projectId, medFileFanOut);
			}
			else {
				projectToMinFileFanOutMap.put(projectId, DEFAULT_MIN_FILE_FAN_OUT);
			}
		}
		return projectToMinFileFanOutMap.get(projectId);
	}

	@Override
	public Integer getProjectMinPackageFanOut(Long projectId) {
		if (!projectToMinPackageFanOutMap.containsKey(projectId)) {
			Integer medPackageFanOut = metricRepository.getMedPackageFanOutByProjectId(projectId);
			if (medPackageFanOut != null) {
				projectToMinPackageFanOutMap.put(projectId, medPackageFanOut);
			}
			else {
				projectToMinPackageFanOutMap.put(projectId, DEFAULT_MIN_PACKAGE_FAN_OUT);
			}
		}
		return projectToMinPackageFanOutMap.get(projectId);
	}

	@Override
	public Integer getProjectMinModuleFanOut(Long projectId) {
		if (!projectToMinModuleFanOutMap.containsKey(projectId)) {
			Integer medModuleFanOut = metricRepository.getMedPackageFanOutByProjectId(projectId);
			if (medModuleFanOut != null) {
				projectToMinModuleFanOutMap.put(projectId, medModuleFanOut);
			}
			else {
				projectToMinModuleFanOutMap.put(projectId, DEFAULT_MIN_MODULE_FAN_OUT);
			}
		}
		return projectToMinModuleFanOutMap.get(projectId);
	}

	@Override
	public Double getProjectMinRatio(Long projectId) {
		if (!projectToMinRatioMap.containsKey(projectId)) {
			projectToMinRatioMap.put(projectId, DEFAULT_MIN_RATIO);
		}
		return projectToMinRatioMap.get(projectId);
	}
}
