package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByHistory;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingHistory;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingHistoryImpl implements UnstableDependencyDetectorUsingHistory {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private StaticAnalyseService staticAnalyseService;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private CacheService cache;

	private final Map<Long, Integer> projectToFanOutThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeTimesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeFilesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Double> projectToMinRatioMap = new ConcurrentHashMap<>();

	public static final int DEFAULT_THRESHOLD_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_CO_CHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_CO_CHANGE_FILES = 5;
	public static final double DEFAULT_MIN_RATIO = 0.5;

	@Override
	public void setProjectMinFileFanOut(Long projectId, Integer minFileFanOut) {
		this.projectToFanOutThresholdMap.put(projectId, minFileFanOut);
		cache.remove(getClass());
	}

	@Override
	public void setProjectFileMinCoChange(Long projectId, Integer minFileCoChange) {
		this.projectToCoChangeTimesThresholdMap.put(projectId, minFileCoChange);
		cache.remove(getClass());
	}

	@Override
	public void setProjectCoChangeFile(Long projectId, Integer coChangeFile) {
		this.projectToCoChangeFilesThresholdMap.put(projectId, coChangeFile);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinRatio(Long projectId, Double minRatio) {
		this.projectToMinRatioMap.put(projectId, minRatio);
		cache.remove(getClass());
	}

	@Override
	public Integer getProjectMinFileFanOut(Long projectId) {
		if (!projectToFanOutThresholdMap.containsKey(projectId)) {
			Integer medFileFanOut = metricRepository.getMedFileFanInByProjectId(projectId);
			if (medFileFanOut != null) {
				projectToFanOutThresholdMap.put(projectId, medFileFanOut);
			}
			else {
				projectToFanOutThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_OUT);
			}
		}
		return projectToFanOutThresholdMap.get(projectId);
	}

	@Override
	public Integer getProjectFileMinCoChange(Long projectId) {
		if (!projectToCoChangeTimesThresholdMap.containsKey(projectId)) {
			Integer medFileCoChangeTimes = metricRepository.getMedFileCoChangeByProjectId(projectId);
			if (medFileCoChangeTimes != null) {
				projectToCoChangeTimesThresholdMap.put(projectId, medFileCoChangeTimes);
			}
			else {
				projectToCoChangeTimesThresholdMap.put(projectId, DEFAULT_THRESHOLD_CO_CHANGE_TIMES);
			}
		}
		return projectToCoChangeTimesThresholdMap.get(projectId);
	}

	@Override
	public Integer getProjectCoChangeFile(Long projectId) {
		if (!projectToCoChangeFilesThresholdMap.containsKey(projectId)) {
			projectToCoChangeFilesThresholdMap.put(projectId, DEFAULT_THRESHOLD_CO_CHANGE_FILES);
		}
		return projectToCoChangeFilesThresholdMap.get(projectId);
	}

	@Override
	public Double getProjectMinRatio(Long projectId) {
		if (!projectToMinRatioMap.containsKey(projectId)) {
			projectToMinRatioMap.put(projectId, DEFAULT_MIN_RATIO);
		}
		return projectToMinRatioMap.get(projectId);
	}

	@Override
	public Map<Long, List<UnstableDependencyByHistory>> queryFileUnstableDependency() {
		String key = "queryFileUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableDependencyByHistory>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			if (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				Project project = containRelationService.findFileBelongToProject(component);
				UnstableDependencyByHistory unstableDependencyByHistory = new UnstableDependencyByHistory();
				unstableDependencyByHistory.setComponent(component);
				Collection<DependsOn> dependsOns = staticAnalyseService.findFileDependsOn(component);
				unstableDependencyByHistory.addAllFanOutDependencies(dependsOns);
				unstableDependencyByHistory.setFanOut(dependsOns.size());
				List<CoChange> allCoChanges = new ArrayList<>();
				for(DependsOn dependsOn : dependsOns) {
					ProjectFile fanOutFile = (ProjectFile) dependsOn.getEndNode();
					List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(component,fanOutFile);
					if(coChanges != null && !coChanges.isEmpty()) {
						int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
						if(times >= DEFAULT_THRESHOLD_CO_CHANGE_TIMES){
							allCoChanges.addAll(coChanges);
						}
					}
				}
				unstableDependencyByHistory.addAllCoChanges(allCoChanges);

				List<UnstableDependencyByHistory> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(unstableDependencyByHistory);
				result.put(project.getId(), temp);
			}
		}

		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByHistory>> detectFileUnstableDependency() {
		Map<Long, List<UnstableDependencyByHistory>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		if(projects != null && !projects.isEmpty()){
			for(Project project : projects) {
				List<UnstableDependencyByHistory> unstableDependencies = new ArrayList<>();
				List<ProjectFile> fileList = nodeService.queryAllFilesByProject(project.getId());
				for(ProjectFile file : fileList) {
					UnstableDependencyByHistory unstableInterfaceInFileLevel = isUnstableDependencyInFileLevel(project.getId(), file);
					if(unstableInterfaceInFileLevel != null) {
						unstableDependencies.add(unstableInterfaceInFileLevel);
					}
				}
				if(!unstableDependencies.isEmpty()){
					sortFileUnstableDependencyByRadioAndFanOut(unstableDependencies);
					result.put(project.getId(), unstableDependencies);
				}
			}
		}
		return result;
	}

	private UnstableDependencyByHistory isUnstableDependencyInFileLevel(Long projectId, ProjectFile file) {
		UnstableDependencyByHistory result = null;
		Integer fanOutThreshold = getProjectMinFileFanOut(projectId);
		Double minRatio = getProjectMinRatio(projectId);
		Collection<DependsOn> fanOutDependencies = staticAnalyseService.findFileDependsOn(file);
		if(fanOutDependencies != null && fanOutDependencies.size() > fanOutThreshold) {
			int coChangeFilesCount = 0;
			List<CoChange> allCoChanges = new ArrayList<>();
			for(DependsOn dependedOn : fanOutDependencies) {
				// 遍历每个依赖File的文件，搜索协同修改次数
				ProjectFile fanInFile = (ProjectFile)dependedOn.getEndNode();
				List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,file);
				if(coChanges != null && !coChanges.isEmpty()) {
					int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
					if(times >= DEFAULT_THRESHOLD_CO_CHANGE_TIMES){
						coChangeFilesCount++;
					}
					allCoChanges.addAll(coChanges);
				}
			}
			if((coChangeFilesCount*1.0) / fanOutDependencies.size() >= minRatio) {
				result = new UnstableDependencyByHistory();
				result.setComponent(file);
				result.addAllFanOutDependencies(fanOutDependencies);
				result.setFanOut(fanOutDependencies.size() );
				result.addAllCoChanges(allCoChanges);
			}
		}
		return result;
	}

	private void sortFileUnstableDependencyByRadioAndFanOut(List<UnstableDependencyByHistory> fileUnstableDependencyList) {
		fileUnstableDependencyList.sort((fileUnstableDependency1, fileUnstableDependency2) -> {
			float radio1 = (float) ((fileUnstableDependency1.getFanOut() + 0.0) / fileUnstableDependency1.getCoChangeFiles().size());
			float radio2 = (float) ((fileUnstableDependency2.getFanOut() + 0.0) / fileUnstableDependency2.getCoChangeFiles().size());
			int radioCompare = Float.compare(radio1, radio2);
			if (radioCompare == 0) {
				return Integer.compare(fileUnstableDependency2.getFanOut(), fileUnstableDependency1.getFanOut());
			}
			return radioCompare;
		});
	}
}
