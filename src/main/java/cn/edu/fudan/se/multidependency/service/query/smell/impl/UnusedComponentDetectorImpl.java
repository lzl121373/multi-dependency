package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class UnusedComponentDetectorImpl implements UnusedComponentDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private CacheService cache;

	@Override
	public Map<Long, List<Package>> unusedPackages() {
		String key = "unusedPackages";
		Map<Long, List<Package>> result = null;
		if(cache.get(this.getClass(), key) != null) {
			return cache.get(this.getClass(), key);
		}
		result = new HashMap<>();
		Collection<Package> pcks = asRepository.unusedPackages();
		for(Package pck : pcks) {
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<Package> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(pck);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<ProjectFile>> unusedFiles() {
		String key = "unusedFiles";
		Map<Long, List<ProjectFile>> result = null;
		if(cache.get(this.getClass(), key) != null) {
			return cache.get(this.getClass(), key);
		}
		result = new HashMap<>();
		Collection<ProjectFile> files = asRepository.unusedFiles();
		for(ProjectFile file : files) {
			Project project = containRelationService.findFileBelongToProject(file);
			List<ProjectFile> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(file);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

}
