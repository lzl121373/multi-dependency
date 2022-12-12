package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.LogicCouplingComponents;

@Service
public class ImplicitCrossModuleDependencyDetectorImpl implements ImplicitCrossModuleDependencyDetector {
	
	@Autowired
	private CoChangeRepository cochangeRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ModuleService moduleService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private MetricRepository metricRepository;

	private static final int DEFAULT_MIN_FILE_CO_CHANGE = 10;
	private static final int DEFAULT_MIN_PACKAGE_CO_CHANGE = 10;

	private final Map<Long, Integer> projectToMinFileCoChangeMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToMinPackageCoChangeMap = new ConcurrentHashMap<>();

	@Override
	public Map<Long, List<LogicCouplingComponents<ProjectFile>>> queryFileImplicitCrossModuleDependency() {
		String key = "fileImplicitCrossModuleDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<LogicCouplingComponents<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			ProjectFile file1 = null;
			ProjectFile file2 = null;
			if (iterator.hasNext()) {
				file1 = (ProjectFile) iterator.next();
			}
			if (iterator.hasNext()) {
				file2 = (ProjectFile) iterator.next();
			}
			if (file1 != null && file2 != null) {
				List<CoChange> coChanges = cochangeRepository.findCoChangesBetweenTwoFilesWithoutDirection(file1.getId(), file2.getId());
				if (coChanges != null && !coChanges.isEmpty()) {
					int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
					LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency = new LogicCouplingComponents<>(file1, file2, times);
					fileImplicitCrossModuleDependencyList.add(fileImplicitCrossModuleDependency);
				}
			}
		}
		for (LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency : fileImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<Package>>> queryPackageImplicitCrossModuleDependency() {
		String key = "packageImplicitCrossModuleDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<LogicCouplingComponents<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<LogicCouplingComponents<Package>> packageImplicitCrossModuleDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			Package package1 = null;
			Package package2 = null;
			if (iterator.hasNext()) {
				package1 = (Package) iterator.next();
			}
			if (iterator.hasNext()) {
				package2 = (Package) iterator.next();
			}
			if (package1 != null && package2 != null) {
				List<CoChange> coChanges = cochangeRepository.findCoChangesBetweenTwoPackagesWithoutDirection(package1.getId(), package2.getId());
				if (coChanges != null && !coChanges.isEmpty()) {
					int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
					LogicCouplingComponents<Package> fileImplicitCrossModuleDependency = new LogicCouplingComponents<>(package1, package2, times);
					packageImplicitCrossModuleDependencyList.add(fileImplicitCrossModuleDependency);
				}
			}
		}
		for (LogicCouplingComponents<Package> fileImplicitCrossModuleDependency : packageImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findPackageBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findPackageBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<Package>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<ProjectFile>>> detectFileImplicitCrossModuleDependency() {
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> result = new HashMap<>();
		List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependencyList = new ArrayList<>();
		List<Project> projects = nodeService.allProjects();
		for (Project project : projects) {
			List<CoChange> fileCoChangeList = cochangeRepository.findProjectFileCoChangeGreaterThanCount(project.getId(), getFileMinCoChange(project.getId()));
			for(CoChange fileCoChange : fileCoChangeList) {
				// 两个文件在不同的模块，并且两个文件之间没有依赖关系
				if(moduleService.isInDependence((ProjectFile) fileCoChange.getNode1(), (ProjectFile) fileCoChange.getNode2())) {
					fileImplicitCrossModuleDependencyList.add(new LogicCouplingComponents<>((ProjectFile) fileCoChange.getNode1(), (ProjectFile) fileCoChange.getNode2(), fileCoChange.getTimes()));
				}
			}
		}
		sortFileImplicitCrossModuleDependencyByTimesAndPath(fileImplicitCrossModuleDependencyList);
		for (LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency : fileImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<Package>>> detectPackageImplicitCrossModuleDependency() {
		Map<Long, List<LogicCouplingComponents<Package>>> result = new HashMap<>();
		List<LogicCouplingComponents<Package>> packageImplicitCrossModuleDependencyList = new ArrayList<>();
		List<Project> projects = projectRepository.queryAllProjects();
		for (Project project : projects) {
			Collection<CoChange> packageCoChangeList = cochangeRepository.findProjectPackageCoChangeGreaterThanCount(project.getId(), getPackageMinCoChange(project.getId()));
			for (CoChange packageCoChange : packageCoChangeList) {
				long pckId1 = packageCoChange.getNode1().getId();
				long pckId2 = packageCoChange.getNode2().getId();
				int fileNumber1 = projectFileRepository.getFilesNumberByPackageId(pckId1);
				int fileNumber2 = projectFileRepository.getFilesNumberByPackageId(pckId2);
				int fileCoChangeNumber = packageRepository.getCoChangeFileNumberByPackagesId(pckId1, pckId2);
				int fileDependOnNumber = packageRepository.getDependOnFileNumberByPackagesId(pckId1, pckId2);
				if (packageCoChange.getTimes() >= getPackageMinCoChange(project.getId()) && fileCoChangeNumber / (fileNumber1 + fileNumber2 + 0.0) > 0.5 && packageCoChange.getTimes() / (fileNumber1 + fileNumber2 + 0.0) >= 1.5 && fileDependOnNumber == 0) {
					packageImplicitCrossModuleDependencyList.add(new LogicCouplingComponents<>((Package) packageCoChange.getNode1(), (Package) packageCoChange.getNode2(), packageCoChange.getTimes()));
				}
			}
		}
		sortPackageImplicitCrossModuleDependencyByTimesAndPath(packageImplicitCrossModuleDependencyList);
		for (LogicCouplingComponents<Package> packageImplicitCrossModuleDependency : packageImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findPackageBelongToProject(packageImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findPackageBelongToProject(packageImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<Package>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(packageImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		return result;
	}

	@Override
	public void setProjectFileMinCoChange(Long projectId, int minFileCoChange) {
		projectToMinFileCoChangeMap.put(projectId, minFileCoChange);
	}

	@Override
	public void setProjectPackageMinCoChange(Long projectId, int minPackageCoChange) {
		projectToMinPackageCoChangeMap.put(projectId, minPackageCoChange);
	}
	
	@Override
	public Integer getFileMinCoChange(Long projectId) {
		if (!projectToMinFileCoChangeMap.containsKey(projectId)) {
			Integer medFileCoChange = metricRepository.getMedFileCoChangeByProjectId(projectId);
			if (medFileCoChange != null) {
				projectToMinFileCoChangeMap.put(projectId, medFileCoChange);
			}
			else {
				projectToMinFileCoChangeMap.put(projectId, DEFAULT_MIN_FILE_CO_CHANGE);
			}
			if (projectToMinFileCoChangeMap.get(projectId) < DEFAULT_MIN_FILE_CO_CHANGE) {
				projectToMinFileCoChangeMap.put(projectId, DEFAULT_MIN_FILE_CO_CHANGE);
			}
		}
		return projectToMinFileCoChangeMap.get(projectId);
	}

	@Override
	public Integer getPackageMinCoChange(Long projectId) {
		if (!projectToMinPackageCoChangeMap.containsKey(projectId)) {
			Integer medPackageCoChange = metricRepository.getMedPackageCoChangeByProjectId(projectId);
			if (medPackageCoChange != null) {
				projectToMinPackageCoChangeMap.put(projectId, medPackageCoChange);
			}
			else {
				projectToMinPackageCoChangeMap.put(projectId, DEFAULT_MIN_PACKAGE_CO_CHANGE);
			}
			if (projectToMinPackageCoChangeMap.get(projectId) < DEFAULT_MIN_PACKAGE_CO_CHANGE) {
				projectToMinPackageCoChangeMap.put(projectId, DEFAULT_MIN_PACKAGE_CO_CHANGE);
			}
		}
		return projectToMinPackageCoChangeMap.get(projectId);

	}

	private void sortFileImplicitCrossModuleDependencyByTimesAndPath(List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependencyList) {
		fileImplicitCrossModuleDependencyList.sort((fileImplicitCrossModuleDependency1, fileImplicitCrossModuleDependency2) -> {
			int timesCompare = Integer.compare(fileImplicitCrossModuleDependency2.getCochangeTimes(), fileImplicitCrossModuleDependency1.getCochangeTimes());
			if (timesCompare == 0) {
				return fileImplicitCrossModuleDependency1.getNode1().getPath().compareTo(fileImplicitCrossModuleDependency2.getNode1().getPath());
			}
			return timesCompare;
		});
	}

	private void sortPackageImplicitCrossModuleDependencyByTimesAndPath(List<LogicCouplingComponents<Package>> packageImplicitCrossModuleDependencyList) {
		packageImplicitCrossModuleDependencyList.sort((packageImplicitCrossModuleDependency1, packageImplicitCrossModuleDependency2) -> {
			int timesCompare = Integer.compare(packageImplicitCrossModuleDependency2.getCochangeTimes(), packageImplicitCrossModuleDependency1.getCochangeTimes());
			if (timesCompare == 0) {
				return packageImplicitCrossModuleDependency1.getNode1().getDirectoryPath().compareTo(packageImplicitCrossModuleDependency2.getNode1().getDirectoryPath());
			}
			return timesCompare;
		});
	}
}
