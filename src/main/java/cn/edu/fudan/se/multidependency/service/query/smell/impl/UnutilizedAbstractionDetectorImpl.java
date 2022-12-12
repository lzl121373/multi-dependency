package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnutilizedAbstractionDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnutilizedAbstraction;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnutilizedAbstractionDetectorImpl implements UnutilizedAbstractionDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private SmellRepository smellRepository;

	@Override
	public Map<Long, List<UnutilizedAbstraction<Type>>> queryTypeUnutilizedAbstraction() {
		String key = "typeUnutilizedAbstraction";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnutilizedAbstraction<Type>>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnutilizedAbstraction<ProjectFile>>> queryFileUnutilizedAbstraction() {
		String key = "fileUnutilizedAbstraction";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNUTILIZED_ABSTRACTION));
		SmellUtils.sortSmellByName(smells);
		List<UnutilizedAbstraction<ProjectFile>> fileUnutilizedAbstractions = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			if (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				fileUnutilizedAbstractions.add(new UnutilizedAbstraction<>(component));
			}
		}
		for(UnutilizedAbstraction<ProjectFile> fileUnutilizedAbstraction : fileUnutilizedAbstractions) {
			Project project = containRelationService.findFileBelongToProject(fileUnutilizedAbstraction.getComponent());
			if (project != null) {
				List<UnutilizedAbstraction<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileUnutilizedAbstraction);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnutilizedAbstraction<Type>>> detectTypeUnutilizedAbstraction() {
		Map<Long, List<UnutilizedAbstraction<Type>>> result = new HashMap<>();
		List<Type> types = asRepository.unutilizedTypes();
		List<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			result.put(project.getId(), new ArrayList<>());
		}
		
		for(Type type : types) {
			try {
				Project project = containRelationService.findCodeNodeBelongToProject(type);
				result.get(project.getId()).add(new UnutilizedAbstraction<>(type));
			}
			catch (Exception e) {
				System.out.println("project: " + containRelationService.findCodeNodeBelongToProject(type));
				System.out.println("type: " + type);
			}
		}
		return result;
	}

	@Override
	public Map<Long, List<UnutilizedAbstraction<ProjectFile>>> detectFileUnutilizedAbstraction() {
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> result = new HashMap<>();
		Map<Long, List<UnutilizedAbstraction<Type>>> types = detectTypeUnutilizedAbstraction();
		for(Map.Entry<Long, List<UnutilizedAbstraction<Type>>> entry : types.entrySet()) {
			long projectId = entry.getKey();
			Set<ProjectFile> files = new HashSet<>();
			for(UnutilizedAbstraction<Type> type : entry.getValue()) {
				ProjectFile file = containRelationService.findTypeBelongToFile(type.getComponent());
				files.add(file);
			}
			List<UnutilizedAbstraction<ProjectFile>> unutilizedFiles = new ArrayList<>();
			for(ProjectFile file : files) {
				unutilizedFiles.add(new UnutilizedAbstraction<>(file));
			}
			sortFileUnutilizedAbstractionByPath(unutilizedFiles);
			result.put(projectId, unutilizedFiles);
		}
		return result;
	}

	private void sortFileUnutilizedAbstractionByPath(List<UnutilizedAbstraction<ProjectFile>> fileUnutilizedAbstractionList) {
		fileUnutilizedAbstractionList.sort(Comparator.comparing(fileUnutilizedAbstraction -> fileUnutilizedAbstraction.getComponent().getPath()));
	}
}
