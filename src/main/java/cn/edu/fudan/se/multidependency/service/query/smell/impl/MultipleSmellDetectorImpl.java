package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.*;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssueFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class MultipleSmellDetectorImpl implements MultipleSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private HubLikeDependencyDetector hubLikeDependencyDetector;

	@Autowired
	private UnstableDependencyDetectorUsingInstability unstableDependencyDetectorUsingInstability;

	@Autowired
	private UnstableInterfaceDetector unstableInterfaceDetector;
	
	@Autowired
	private ImplicitCrossModuleDependencyDetector implicitCrossModuleDependencyDetector;
	
	@Autowired
	private ContainRelationService containRelationService;
 	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private IssueQueryService issueQueryService;
	
	@Autowired
	private UnutilizedAbstractionDetector unutilizedAbstractionDetector;

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	@Autowired
	private CommitRepository commitRepository;
	
	public int fileCommitsCount(ProjectFile file) {
		String key = "fileCommitsCount_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		int result = asRepository.findCommitsUsingForIssue(file.getId()).size();
		cache.cache(getClass(), key, result);
		return result;
	}
	
	@Override
	public Map<Long, List<CirclePacking>> circlePacking(MultipleAS multipleAS) {
		Map<Long, List<CirclePacking>> result = new HashMap<>();
		Map<Long, List<MultipleASFile>> multipleASFiles = detectMultipleSmellASFile(true);
		Map<Long, List<IssueFile>> issueFilesGroupByProject = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		List<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			List<CirclePacking> circles = new ArrayList<>();
			CirclePacking onlySmellCircle = new CirclePacking(CirclePacking.TYPE_ONLY_SMELL);
			CirclePacking onlyIssueCircle = new CirclePacking(CirclePacking.TYPE_ONLY_ISSUE);
			CirclePacking smellAndIssueCircle = new CirclePacking(CirclePacking.TYPE_SMELL_ISSUE);
			
			List<IssueFile> issueFiles = new ArrayList<>(issueFilesGroupByProject.get(project.getId()));
			List<MultipleASFile> asFiles = multipleASFiles.get(project.getId());
			for(MultipleASFile smellFile : asFiles) {
				if(!smellFile.isSmellFile(multipleAS)) {
					continue;
				}
				IssueFile issueFile = IssueFile.contains(issueFiles, smellFile.getFile());
				if(issueFile != null) {
					// 既是issueFile又是smellFile
					smellAndIssueCircle.addProjectFile(smellFile.getFile(), issueFile.getIssues());
					smellAndIssueCircle.setFileSmellCount(smellFile.getFile(), smellFile.getSmellCount());
					smellAndIssueCircle.setFileCommitsCount(smellFile.getFile(), fileCommitsCount(smellFile.getFile()));
					issueFiles.remove(issueFile);
				} else {
					// 仅是smellFile
					onlySmellCircle.addProjectFile(smellFile.getFile());
					onlySmellCircle.setFileSmellCount(smellFile.getFile(), smellFile.getSmellCount());
					onlySmellCircle.setFileCommitsCount(smellFile.getFile(), fileCommitsCount(smellFile.getFile()));
				}
			}
			for(IssueFile issueFile : issueFiles) {
				// 仅是issueFile
				onlyIssueCircle.addProjectFile(issueFile.getFile(), issueFile.getIssues());
				onlyIssueCircle.setFileSmellCount(issueFile.getFile(), 0);
				onlyIssueCircle.setFileCommitsCount(issueFile.getFile(), fileCommitsCount(issueFile.getFile()));
			}
			
			circles.add(smellAndIssueCircle);
			circles.add(onlySmellCircle);
			circles.add(onlyIssueCircle);
			result.put(project.getId(), circles);
		}
		
		return result;
	}

	@Override
	public Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS) {
		Map<Long, List<MultipleASFile>> multipleASFiles = queryMultipleSmellASFile(true);
		Map<Long, List<IssueFile>> issueFilesGroupByProject = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		Map<Long, PieFilesData> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		
		Set<ProjectFile> isSmellFiles = new HashSet<>();
		Set<ProjectFile> isIssueFiles = new HashSet<>();
		
		for(Project project : projects) {
			List<IssueFile> issueFiles = issueFilesGroupByProject.get(project.getId());
			List<MultipleASFile> asFiles = multipleASFiles.get(project.getId());
			for(IssueFile issueFile : issueFiles) {
				isIssueFiles.add(issueFile.getFile());
			}
			for(MultipleASFile smellFile : asFiles) {
				if(smellFile.isSmellFile(multipleAS)) {
					isSmellFiles.add(smellFile.getFile());
				}
			}
			
			Set<ProjectFile> normalFiles = new HashSet<>();
			Set<ProjectFile> onlyIssueFiles = new HashSet<>();
			Set<ProjectFile> onlySmellFiles = new HashSet<>();
			Set<ProjectFile> issueAndSmellFiles = new HashSet<>();
			
			for(ProjectFile file : containRelationService.findProjectContainAllFiles(project)) {
				if(isSmellFiles.contains(file) && isIssueFiles.contains(file)) {
					issueAndSmellFiles.add(file);
				} else if(isSmellFiles.contains(file)) {
					onlySmellFiles.add(file);
				} else if(isIssueFiles.contains(file)) {
					onlyIssueFiles.add(file);
				} else {
					normalFiles.add(file);
				}
			}
			Set<Issue> allIssues = new HashSet<>(issueQueryService.queryIssues(project));
			Set<Issue> smellIssues = new HashSet<>();
			for(ProjectFile file : onlySmellFiles) {
				smellIssues.addAll(issueQueryService.queryRelatedIssuesOnFile(file));
			}
			for(ProjectFile file : issueAndSmellFiles) {
				smellIssues.addAll(issueQueryService.queryRelatedIssuesOnFile(file));
			}
			
			PieFilesData data = new PieFilesData(project, normalFiles, onlyIssueFiles, onlySmellFiles, issueAndSmellFiles, allIssues, smellIssues);
			result.put(project.getId(), data);
		}
		return result;
	}

	@Override
	public Map<Long, JSONObject> getProjectTotal() {
		String key = "projectTotal";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		for (Project project : projects) {
			JSONObject projectProjectTotalObject = new JSONObject();
			calculateProjectTotalObject(project.getId(), projectProjectTotalObject);
			result.put(project.getId(), projectProjectTotalObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, JSONObject> getFileSmellOverview() {
		String key = "fileSmellOverview";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		List<String> smellTypes = new ArrayList<>();
		smellTypes.add(SmellType.CYCLIC_DEPENDENCY);
		smellTypes.add(SmellType.HUBLIKE_DEPENDENCY);
		smellTypes.add(SmellType.UNSTABLE_DEPENDENCY);
		smellTypes.add(SmellType.UNSTABLE_INTERFACE);
		smellTypes.add(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellTypes.add(SmellType.UNUTILIZED_ABSTRACTION);
		smellTypes.add(SmellType.UNUSED_INCLUDE);
		for (Project project : projects) {
			JSONObject projectFileSmellOverviewObject = new JSONObject();
			JSONArray projectFileSmellArray = new JSONArray();
			for (String smellType : smellTypes) {
				JSONObject projectFileSmellObject = new JSONObject();
				List<Smell> projectFileSmells = new ArrayList<>(smellRepository.findSmells(project.getId(), smellType, SmellLevel.FILE));
				calculateProjectFileSmellObject(project.getId(), smellType, projectFileSmells, projectFileSmellObject);
				projectFileSmellArray.add(projectFileSmellObject);
			}
			projectFileSmellOverviewObject.put("ProjectFileSmell", projectFileSmellArray);
			result.put(project.getId(), projectFileSmellOverviewObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, JSONObject> getPackageSmellOverview() {
		String key = "packageSmellOverview";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		List<String> smellTypes = new ArrayList<>();
		smellTypes.add(SmellType.CYCLIC_DEPENDENCY);
		smellTypes.add(SmellType.HUBLIKE_DEPENDENCY);
		smellTypes.add(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		for (Project project : projects) {
			JSONObject projectPackageSmellOverviewObject = new JSONObject();
			JSONArray projectPackageSmellArray = new JSONArray();
			for (String smellType : smellTypes) {
				JSONObject projectPackageSmellObject = new JSONObject();
				List<Smell> projectPackageSmells = new ArrayList<>(smellRepository.findSmells(project.getId(), smellType, SmellLevel.PACKAGE));
				calculateProjectPackageSmellObject(project.getId(), smellType, projectPackageSmells, projectPackageSmellObject);
				projectPackageSmellArray.add(projectPackageSmellObject);
			}
			projectPackageSmellOverviewObject.put("ProjectPackageSmell", projectPackageSmellArray);
			result.put(project.getId(), projectPackageSmellOverviewObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	private void calculateProjectTotalObject(Long projectId, JSONObject projectTotalObject) {
		projectTotalObject.put("FileCount", metricRepository.getProjectFileCountByProjectId(projectId));
		projectTotalObject.put("IssueCommits", metricRepository.getProjectIssueCommitsByProjectId(projectId));
		projectTotalObject.put("Commits", metricRepository.getProjectCommitsByProjectId(projectId));
		projectTotalObject.put("IssueChangeLines", metricRepository.getProjectIssueChangeLinesByProjectId(projectId));
		projectTotalObject.put("ChangeLines", metricRepository.getProjectChangeLinesByProjectId(projectId));
	}

	private void calculateProjectFileSmellObject(Long projectId, String smellType, List<Smell> projectFileSmells, JSONObject projectFileSmellObject) {
		int smellCount = projectFileSmells.size();
		Integer fileCount = projectFileRepository.calculateFileSmellFileCountByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer issueCommits = commitRepository.calculateFileSmellIssueCommitsByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer commits = commitRepository.calculateFileSmellCommitsByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer issueChangeLines = commitRepository.calculateFileSmellIssueChangeLinesByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer changeLines = commitRepository.calculateFileSmellChangeLinesByProjectId(projectId, smellType, SmellLevel.FILE);
		if (fileCount == null) {
			fileCount = 0;
		}
		if (issueCommits == null) {
			issueCommits = 0;
		}
		if (commits == null) {
			commits = 0;
		}
		if (issueChangeLines == null) {
			issueChangeLines = 0;
		}
		if (changeLines == null) {
			changeLines = 0;
		}
		projectFileSmellObject.put("SmellType", smellType);
		projectFileSmellObject.put("SmellCount", smellCount);
		projectFileSmellObject.put("FileCount", fileCount);
		projectFileSmellObject.put("IssueCommits", issueCommits);
		projectFileSmellObject.put("Commits", commits);
		projectFileSmellObject.put("IssueChangeLines", issueChangeLines);
		projectFileSmellObject.put("ChangeLines", changeLines);
	}

	private void calculateProjectPackageSmellObject(Long projectId, String smellType, List<Smell> projectPackageSmells, JSONObject projectPackageSmellObject) {
		int smellCount = projectPackageSmells.size();
		Integer fileCount = projectFileRepository.calculatePackageSmellFileCountByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer issueCommits = commitRepository.calculatePackageSmellIssueCommitsByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer commits = commitRepository.calculatePackageSmellCommitsByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer issueChangeLines = commitRepository.calculatePackageSmellIssueChangeLinesByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer changeLines = commitRepository.calculatePackageSmellChangeLinesByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		if (fileCount == null) {
			fileCount = 0;
		}
		if (issueCommits == null) {
			issueCommits = 0;
		}
		if (commits == null) {
			commits = 0;
		}
		if (issueChangeLines == null) {
			issueChangeLines = 0;
		}
		if (changeLines == null) {
			changeLines = 0;
		}
		projectPackageSmellObject.put("SmellType", smellType);
		projectPackageSmellObject.put("SmellCount", smellCount);
		projectPackageSmellObject.put("FileCount", fileCount);
		projectPackageSmellObject.put("IssueCommits", issueCommits);
		projectPackageSmellObject.put("Commits", commits);
		projectPackageSmellObject.put("IssueChangeLines", issueChangeLines);
		projectPackageSmellObject.put("ChangeLines", changeLines);
	}

	@Override
	public Map<Long, List<MultipleASFile>> queryMultipleSmellASFile(boolean removeNoASFile) {
		String key = "multipleSmellASFile";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<MultipleASFile>> result;
		Map<Long, List<Cycle<ProjectFile>>> fileCyclicDependencyMap = cyclicDependencyDetector.queryFileCyclicDependency();
		Map<Long, List<FileHubLike>> fileHubLikeDependencyMap = hubLikeDependencyDetector.queryFileHubLikeDependency();
		Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> fileUnstableDependencyMap = unstableDependencyDetectorUsingInstability.queryFileUnstableDependency();
		Map<Long, List<UnstableInterface>> fileUnstableInterfaceMap = unstableInterfaceDetector.queryFileUnstableInterface();
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> fileImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.queryFileImplicitCrossModuleDependency();
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizedAbstractionMap = unutilizedAbstractionDetector.queryFileUnutilizedAbstraction();
		Map<Long, List<UnusedInclude>> fileUnusedIncludeMap = unusedIncludeDetector.queryFileUnusedInclude();
		result = calculateMultipleSmellASFile(removeNoASFile, fileCyclicDependencyMap, fileHubLikeDependencyMap, fileUnstableDependencyMap, fileUnstableInterfaceMap, fileImplicitCrossModuleDependencyMap, fileUnutilizedAbstractionMap, fileUnusedIncludeMap);
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<MultipleASPackage>> queryMultipleSmellASPackage(boolean removeNoASPackage) {
		String key = "multipleSmellASPackage";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<MultipleASPackage>> result;
		Map<Long, List<Cycle<Package>>> packageCyclicDependencyMap = cyclicDependencyDetector.queryPackageCyclicDependency();
		Map<Long, List<PackageHubLike>> packageHubLikeDependencyMap = hubLikeDependencyDetector.queryPackageHubLikeDependency();
		Map<Long, List<LogicCouplingComponents<Package>>> packageImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.queryPackageImplicitCrossModuleDependency();
		result = calculateMultipleSmellASPackage(removeNoASPackage, packageCyclicDependencyMap, packageHubLikeDependencyMap, packageImplicitCrossModuleDependencyMap);
		cache.cache(getClass(), key, result);
		return result;
	}
	
	@Override
	public Map<Long, List<MultipleASFile>> detectMultipleSmellASFile(boolean removeNoASFile) {
		Map<Long, List<MultipleASFile>> result;
		Map<Long, List<Cycle<ProjectFile>>> fileCyclicDependencyMap = cyclicDependencyDetector.detectFileCyclicDependency();
		Map<Long, List<FileHubLike>> fileHubLikeDependencyMap = hubLikeDependencyDetector.detectFileHubLikeDependency();
		Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> fileUnstableDependencyMap = unstableDependencyDetectorUsingInstability.detectFileUnstableDependency();
		Map<Long, List<UnstableInterface>> fileUnstableInterfaceMap = unstableInterfaceDetector.detectFileUnstableInterface();
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> fileImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.detectFileImplicitCrossModuleDependency();
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizedAbstractionMap = unutilizedAbstractionDetector.detectFileUnutilizedAbstraction();
		Map<Long, List<UnusedInclude>> fileUnusedIncludeMap = unusedIncludeDetector.detectFileUnusedInclude();
		result = calculateMultipleSmellASFile(removeNoASFile, fileCyclicDependencyMap, fileHubLikeDependencyMap, fileUnstableDependencyMap, fileUnstableInterfaceMap, fileImplicitCrossModuleDependencyMap, fileUnutilizedAbstractionMap, fileUnusedIncludeMap);
		return result;
	}

	@Override
	public Map<Long, List<MultipleASPackage>> detectMultipleSmellASPackage(boolean removeNoASPackage) {
		Map<Long, List<MultipleASPackage>> result;
		Map<Long, List<Cycle<Package>>> packageCyclicDependencyMap = cyclicDependencyDetector.detectPackageCyclicDependency();
		Map<Long, List<PackageHubLike>> packageHubLikeDependencyMap = hubLikeDependencyDetector.detectPackageHubLikeDependency();
		Map<Long, List<LogicCouplingComponents<Package>>> packageImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.detectPackageImplicitCrossModuleDependency();
		result = calculateMultipleSmellASPackage(removeNoASPackage, packageCyclicDependencyMap, packageHubLikeDependencyMap, packageImplicitCrossModuleDependencyMap);
		return result;
	}

	private Map<Long, List<MultipleASFile>> calculateMultipleSmellASFile(boolean removeNoASFile,
																		 Map<Long, List<Cycle<ProjectFile>>> fileCyclicDependencyMap,
																		 Map<Long, List<FileHubLike>> fileHubLikeDependencyMap,
																		 Map<Long, List<UnstableDependencyByInstability<ProjectFile>>> fileUnstableDependencyMap,
																		 Map<Long, List<UnstableInterface>> fileUnstableInterfaceMap,
																		 Map<Long, List<LogicCouplingComponents<ProjectFile>>> fileImplicitCrossModuleDependencyMap,
																		 Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizedAbstractionMap,
																		 Map<Long, List<UnusedInclude>> fileUnusedIncludeMap) {
		Map<ProjectFile, MultipleASFile> map = new HashMap<>();
		List<ProjectFile> allFiles = new ArrayList<>(nodeService.queryAllFiles());
		for (List<Cycle<ProjectFile>> fileCyclicDependency : fileCyclicDependencyMap.values()) {
			for (Cycle<ProjectFile> files : fileCyclicDependency) {
				for (ProjectFile file : files.getComponents()) {
					MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
					mas.setCyclicDependency(true);
					map.put(file, mas);
					allFiles.remove(file);
				}
			}
		}

		for (List<FileHubLike> fileHubLikeDependency : fileHubLikeDependencyMap.values()) {
			for (FileHubLike file : fileHubLikeDependency) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setHubLikeDependency(true);
				map.put(file.getFile(), mas);
				allFiles.remove(file.getFile());
			}
		}

		for (List<UnstableDependencyByInstability<ProjectFile>> fileUnstableDependency : fileUnstableDependencyMap.values()) {
			for (UnstableDependencyByInstability<ProjectFile> file : fileUnstableDependency) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnstableDependency(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}

		for (List<UnstableInterface> fileUnstableInterface : fileUnstableInterfaceMap.values()) {
			for (UnstableInterface file : fileUnstableInterface) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnstableInterface(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}

		for (List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependency : fileImplicitCrossModuleDependencyMap.values()) {
			for (LogicCouplingComponents<ProjectFile> files : fileImplicitCrossModuleDependency) {
				MultipleASFile mas = map.getOrDefault(files.getNode1(), new MultipleASFile(files.getNode1()));
				mas.setImplicitCrossModuleDependency(true);
				map.put(files.getNode1(), mas);
				mas = map.getOrDefault(files.getNode2(), new MultipleASFile(files.getNode2()));
				mas.setImplicitCrossModuleDependency(true);
				map.put(files.getNode2(), mas);
				allFiles.remove(files.getNode1());
				allFiles.remove(files.getNode2());
			}
		}

		for(List<UnutilizedAbstraction<ProjectFile>> fileUnutilizedAbstraction : fileUnutilizedAbstractionMap.values()) {
			for(UnutilizedAbstraction<ProjectFile> file : fileUnutilizedAbstraction) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnutilizedAbstraction(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}

		for (List<UnusedInclude> fileUnusedIncludeList : fileUnusedIncludeMap.values()) {
			for (UnusedInclude fileUnusedInclude : fileUnusedIncludeList) {
				ProjectFile file = fileUnusedInclude.getCoreFile();
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setUnusedInclude(true);
				map.put(file, mas);
				allFiles.remove(file);
			}
		}

		//没有异味的文件
		if(!removeNoASFile) {
			for(ProjectFile file : allFiles) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				map.put(file, mas);
			}
		}

		Map<Long, List<MultipleASFile>> result = new HashMap<>();
		for(Map.Entry<ProjectFile, MultipleASFile> entry : map.entrySet()) {
			ProjectFile file = entry.getKey();
			MultipleASFile value = entry.getValue();
			Project project = containRelationService.findFileBelongToProject(file);
			value.setProject(project);
			List<MultipleASFile> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(value);
			result.put(project.getId(), temp);
		}

		for(Map.Entry<Long, List<MultipleASFile>> entry : result.entrySet()) {
			entry.getValue().sort((m1, m2) -> m2.getSmellCount() - m1.getSmellCount());
		}
		return result;
	}

	private Map<Long, List<MultipleASPackage>> calculateMultipleSmellASPackage(boolean removeNoASPackage,
																		 Map<Long, List<Cycle<Package>>> packageCyclicDependencyMap,
																		 Map<Long, List<PackageHubLike>> packageHubLikeDependencyMap,
																		 Map<Long, List<LogicCouplingComponents<Package>>> packageImplicitCrossModuleDependencyMap) {
		Map<Package, MultipleASPackage> map = new HashMap<>();
		List<Package> allPackages = new ArrayList<>(nodeService.queryAllPackages());
		for (List<Cycle<Package>> packageCyclicDependency : packageCyclicDependencyMap.values()) {
			for (Cycle<Package> packageCycle : packageCyclicDependency) {
				for (Package pck : packageCycle.getComponents()) {
					MultipleASPackage mas = map.getOrDefault(pck, new MultipleASPackage(pck));
					mas.setCyclicDependency(true);
					map.put(pck, mas);
					allPackages.remove(pck);
				}
			}
		}

		for (List<PackageHubLike> packageHubLikeDependency : packageHubLikeDependencyMap.values()) {
			for (PackageHubLike packageHubLike : packageHubLikeDependency) {
				MultipleASPackage mas = map.getOrDefault(packageHubLike.getPck(), new MultipleASPackage(packageHubLike.getPck()));
				mas.setHubLikeDependency(true);
				map.put(packageHubLike.getPck(), mas);
				allPackages.remove(packageHubLike.getPck());
			}
		}

		for (List<LogicCouplingComponents<Package>> fileImplicitCrossModuleDependency : packageImplicitCrossModuleDependencyMap.values()) {
			for (LogicCouplingComponents<Package> packageImplicitCrossModule : fileImplicitCrossModuleDependency) {
				MultipleASPackage mas = map.getOrDefault(packageImplicitCrossModule.getNode1(), new MultipleASPackage(packageImplicitCrossModule.getNode1()));
				mas.setImplicitCrossModuleDependency(true);
				map.put(packageImplicitCrossModule.getNode1(), mas);
				mas = map.getOrDefault(packageImplicitCrossModule.getNode2(), new MultipleASPackage(packageImplicitCrossModule.getNode2()));
				mas.setImplicitCrossModuleDependency(true);
				map.put(packageImplicitCrossModule.getNode2(), mas);
				allPackages.remove(packageImplicitCrossModule.getNode1());
				allPackages.remove(packageImplicitCrossModule.getNode2());
			}
		}

		//没有异味的文件
		if(!removeNoASPackage) {
			for(Package pck : allPackages) {
				MultipleASPackage mas = map.getOrDefault(pck, new MultipleASPackage(pck));
				map.put(pck, mas);
			}
		}

		Map<Long, List<MultipleASPackage>> result = new HashMap<>();
		for(Map.Entry<Package, MultipleASPackage> entry : map.entrySet()) {
			Package pck = entry.getKey();
			MultipleASPackage value = entry.getValue();
			Project project = containRelationService.findPackageBelongToProject(pck);
			value.setProject(project);
			List<MultipleASPackage> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(value);
			result.put(project.getId(), temp);
		}

		for(Map.Entry<Long, List<MultipleASPackage>> entry : result.entrySet()) {
			entry.getValue().sort((m1, m2) -> m2.getSmellCount() - m1.getSmellCount());
		}
		return result;
	}

	@Override
	public Map<Long, HistogramAS> projectHistogramOnVersion() {
		Map<Long, HistogramAS> result = new HashMap<>();
		Map<Long, List<MultipleASFile>> multipleASFiles = queryMultipleSmellASFile(true);
		Map<Long, List<IssueFile>> issueFiles = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		List<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			HistogramAS data = new HistogramAS(project);
			data.setAllFilesCount(containRelationService.findProjectContainAllFiles(project).size());
			data.setIssueFilesCount(issueFiles.get(project.getId()).size());
			data.setSmellFilesCount(multipleASFiles.get(project.getId()).size());
			result.put(project.getId(), data);
		}
		
		return result;
	}
}
