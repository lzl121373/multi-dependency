package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableInterfaceDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableInterface;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UnstableInterfaceDetectorImpl implements UnstableInterfaceDetector {

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

	private final Map<Long, Integer> projectToFanInThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeTimesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeFilesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Double> projectToMinRatioMap = new ConcurrentHashMap<>();
	
	public static final int DEFAULT_THRESHOLD_FAN_IN = 10;
	public static final int DEFAULT_THRESHOLD_CO_CHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_CO_CHANGE_FILES = 5;
	public static final double DEFAULT_MIN_RATIO = 0.5;

	@Override
	public void setProjectMinFileFanIn(Long projectId, Integer minFileFanIn) {
		this.projectToFanInThresholdMap.put(projectId, minFileFanIn);
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
	public Integer getProjectMinFileFanIn(Long projectId) {
		if (!projectToFanInThresholdMap.containsKey(projectId)) {
			Integer medFileFanIn = metricRepository.getMedFileFanInByProjectId(projectId);
			if (medFileFanIn != null) {
				projectToFanInThresholdMap.put(projectId, medFileFanIn);
			}
			else {
				projectToFanInThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_IN);
			}
		}
		return projectToFanInThresholdMap.get(projectId);
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
	public Map<Long, List<UnstableInterface>> queryFileUnstableInterface() {
		String key = "queryFileUnstableInterface";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableInterface>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_INTERFACE));
		SmellUtils.sortSmellByName(smells);
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			if (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				Project project = containRelationService.findFileBelongToProject(component);
				UnstableInterface unstableInterface = new UnstableInterface();
				unstableInterface.setComponent(component);
				Collection<DependsOn> dependsOnBys = staticAnalyseService.findFileDependedOnBy(component);
				unstableInterface.addAllFanInDependencies(dependsOnBys);
				unstableInterface.setFanIn(dependsOnBys.size());
				List<CoChange> allCoChanges = new ArrayList<>();
				for(DependsOn dependsOn : dependsOnBys) {
					ProjectFile fanInFile = (ProjectFile) dependsOn.getStartNode();
					List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,component);
					if(coChanges != null && !coChanges.isEmpty()) {
						int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
						if(times >= DEFAULT_THRESHOLD_CO_CHANGE_TIMES){
							allCoChanges.addAll(coChanges);
						}
//						if(times >= getCoChangeTimesThreshold(project.getId())){
//							allCoChanges.addAll(coChanges);
//						}
					}
				}
				unstableInterface.addAllCoChanges(allCoChanges);

				List<UnstableInterface> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(unstableInterface);
				result.put(project.getId(), temp);
			}
		}

		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableInterface>> detectFileUnstableInterface() {
		Map<Long, List<UnstableInterface>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		if(projects != null && !projects.isEmpty()){
			for(Project project : projects) {
				List<UnstableInterface> unstableInterfaces = new ArrayList<>();
				List<ProjectFile> fileList = nodeService.queryAllFilesByProject(project.getId());
				for(ProjectFile file : fileList) {
					UnstableInterface unstableFile = isUnstableInterfaceInFileLevel(project.getId(), file);
					if(unstableFile != null) {
						unstableInterfaces.add(unstableFile);
					}
				}
				if(!unstableInterfaces.isEmpty()){
					sortFileUnstableInterfaceByRadioAndFanIn(unstableInterfaces);
					result.put(project.getId(), unstableInterfaces);
				}
			}
		}
		return result;
	}
	
	private UnstableInterface isUnstableInterfaceInFileLevel(Long projectId, ProjectFile file) {
		UnstableInterface result = null;
		Integer fanInThreshold = getProjectMinFileFanIn(projectId);
		Double minRatio = getProjectMinRatio(projectId);
		Collection<DependsOn> fanInDependencies = staticAnalyseService.findFileDependedOnBy(file);
		if(fanInDependencies != null && fanInDependencies.size() > fanInThreshold) {
			int coChangeFilesCount = 0;
			List<CoChange> allCoChanges = new ArrayList<>();
			for(DependsOn dependedOnBy : fanInDependencies) {
				// 遍历每个依赖File的文件，搜索协同修改次数
				ProjectFile fanInFile = (ProjectFile)dependedOnBy.getStartNode();
				List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,file);
				if(coChanges != null && !coChanges.isEmpty()) {
					int times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
//					if(times >= getCoChangeTimesThreshold(projectId)){
//						coChangeFilesCount++;
//					    allCoChanges.addAll(coChanges);
//					}
					if(times >= DEFAULT_THRESHOLD_CO_CHANGE_TIMES){
						coChangeFilesCount++;
						allCoChanges.addAll(coChanges);
					}
				}
			}
			if((coChangeFilesCount*1.0) / fanInDependencies.size() >= minRatio) {
				result = new UnstableInterface();
				result.setComponent(file);
				result.addAllFanInDependencies(fanInDependencies);
				result.setFanIn(fanInDependencies.size() );
				result.addAllCoChanges(allCoChanges);
			}
		}
		return result;
	}

	private void sortFileUnstableInterfaceByRadioAndFanIn(List<UnstableInterface> fileUnstableInterfaceList) {
		fileUnstableInterfaceList.sort((fileUnstableInterface1, fileUnstableInterface2) -> {
			float radio1 = (float) ((fileUnstableInterface1.getFanIn() + 0.0) / fileUnstableInterface1.getCoChangeFiles().size());
			float radio2 = (float) ((fileUnstableInterface2.getFanIn() + 0.0) / fileUnstableInterface2.getCoChangeFiles().size());
			int radioCompare = Float.compare(radio1, radio2);
			if (radioCompare == 0) {
				return Integer.compare(fileUnstableInterface2.getFanIn(), fileUnstableInterface1.getFanIn());
			}
			return radioCompare;
		});
	}
}
