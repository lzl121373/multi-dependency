package cn.edu.fudan.se.multidependency.service.query.smell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.repository.node.ModuleRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;

@Service
public class ModuleService {
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	public Project findModuleBelongToProject(Module module) {
		if(cache.findNodeBelongToNode(module, NodeLabelType.Project) != null) {
			return (Project) cache.findNodeBelongToNode(module, NodeLabelType.Project);
		}
		Project result = moduleRepository.findModuleBelongToProject(module.getId());
		cache.cacheNodeBelongToNode(module, result);
		return result;
	}
	
	public Module findFileBelongToModule(ProjectFile file) {
		if(cache.findNodeBelongToNode(file, NodeLabelType.Module) != null) {
			return (Module) cache.findNodeBelongToNode(file, NodeLabelType.Module);
		}
		Module result = moduleRepository.findFileBelongToModule(file.getId());
		cache.cacheNodeBelongToNode(file, result);
		return result;
	}
	
	public boolean isInDifferentModule(ProjectFile file1, ProjectFile file2) {
		return !findFileBelongToModule(file1).equals(findFileBelongToModule(file2));
	}
	
	public boolean isInDependence(ProjectFile file1, ProjectFile file2) {
		if(!isInDifferentModule(file1, file2)) {
			return false;
		}
		return !(staticAnalyseService.isDependsOn(file1, file2) || staticAnalyseService.isDependsOn(file2, file1));
	}
	
	public boolean isInDependence(Module m1, Module m2) {
		if(m1.equals(m2)) {
			return false;
		}
		return !(isDependsOn(m1, m2) || isDependsOn(m2, m1));
	}
	
	public boolean isDependsOn(Module m1, Module m2) {
		String key = "IsDependesOn_" + m1.getId() + " " + m2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		boolean result = moduleRepository.isDependsOnBetweenModules(m1.getId(), m2.getId());
		cache.cache(getClass(), key, result);
		return result;
	}
	

}
