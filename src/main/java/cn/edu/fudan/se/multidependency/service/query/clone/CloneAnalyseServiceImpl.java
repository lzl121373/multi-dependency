package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class CloneAnalyseServiceImpl implements CloneAnalyseService {
    @Autowired
    CloneRepository cloneRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    StaticAnalyseService staticAnalyseService;
    
    @Autowired
    CacheService cacheService;
    
    @Autowired
    MicroserviceService msService;
	
    @Autowired
    BasicCloneQueryService basicCloneQueryService;

	@Autowired
	NodeService nodeService;

	@Autowired
	GitAnalyseService gitAnalyseService;

	@Override
	public CloneGroup addNodeAndRelationToCloneGroup(CloneGroup group) {
		Collection<Clone> clones = basicCloneQueryService.findGroupContainCloneRelations(group);
		for(Clone clone : clones) {
			group.addRelation(clone);
			group.addNode(clone.getCodeNode1());
			group.addNode(clone.getCodeNode2());
		}
		return group;
	}
    
	private Map<CloneLevel, Collection<CloneGroup>> typeToGroupCache = new ConcurrentHashMap<>();
    @Override
	public Collection<CloneGroup> group(CloneLevel cloneRelationType, Predicate<CloneGroup> predicate) {
    	Collection<CloneGroup> groups = typeToGroupCache.get(cloneRelationType);
    	if(groups == null) {
    		groups = basicCloneQueryService.findGroupsContainCloneTypeRelation(cloneRelationType);
    		for(CloneGroup group : groups) {
    			Collection<Clone> clones = basicCloneQueryService.findGroupContainCloneRelations(group);
    			for(Clone clone : clones) {
    				group.addRelation(clone);
    				group.addNode(clone.getCodeNode1());
    				group.addNode(clone.getCodeNode2());
    			}
    		}
    		typeToGroupCache.put(cloneRelationType, groups);
    	}
    	List<CloneGroup> list = new LinkedList<>(groups);
    	list.removeIf(predicate);
    	return list;
	}

	private Map<CloneGroup, Collection<Project>> cloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Project> cloneGroupContainProjects(CloneGroup group) {
		if(cloneGroupContainProjectsCache.get(group) != null) {
			return cloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(CodeNode node : group.getNodes()) {
			Project project = containRelationService.findCodeNodeBelongToProject(node);
			result.add(project);
		}
		cloneGroupContainProjectsCache.put(group, result);
		return result;
	}
	
	@Override
	public Collection<MicroService> cloneGroupContainMicroServices(CloneGroup group) {
		Set<MicroService> result = new HashSet<>();
		Collection<Project> projects = cloneGroupContainProjects(group);
		for(Project project : projects) {
			MicroService ms = containRelationService.findProjectBelongToMicroService(project);
			if(ms != null) {
				result.add(ms);
			}
		}
		return result;
	}

	@Override
	public Collection<CloneGroup> findGroupsContainProjects(Collection<CloneGroup> groups, Collection<Project> projects) {
		Set<CloneGroup> result = new HashSet<>();
		for(CloneGroup group : groups) {
			// 克隆组包含的项目
			Collection<Project> containProjects = cloneGroupContainProjects(group);
			boolean contain = true;
			for(Project p : projects) {
				if(!containProjects.contains(p)) {
					contain = false;
					break;
				}
			}
			if(contain) {
				result.add(group);
			}
		}
		return result;
	}
	
	@Override
	public Collection<CloneGroup> findGroupsContainMicroServices(Collection<CloneGroup> groups, Collection<MicroService> mss) {
		Collection<Project> projects = new ArrayList<>();
		for(MicroService ms : mss) {
			projects.addAll(containRelationService.findMicroServiceContainProjects(ms));
		}
		return findGroupsContainProjects(groups, projects);
	}

	@Override
	public Collection<Project> cloneGroupContainProjects(Collection<CloneGroup> groups) {
		Set<Project> result = new HashSet<>();
		for(CloneGroup group : groups) {
			result.addAll(cloneGroupContainProjects(group));
		}
		return result;
	}

	@Override
	public Collection<MicroService> cloneGroupContainMicroServices(Collection<CloneGroup> groups) {
		Set<MicroService> result = new HashSet<>();
		for(CloneGroup group : groups) {
			result.addAll(cloneGroupContainMicroServices(group));
		}
		return result;
	}
	
	@Override
	public Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValues(Collection<CloneGroup> groups) {
		Iterable<Project> allProjects = nodeService.allProjects();
		Map<CloneGroup, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
		for(CloneGroup group : groups) {
			Map<Project, CloneLineValue<Project>> temp = new HashMap<>();
			for(Project project : allProjects) {
				CloneLineValue<Project> projectValue = new CloneLineValue<>(project);
				projectValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
				
				temp.put(project, projectValue);
			}
			result.put(group, temp);
		}
		return result;
	}

	@Override
	public boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2) {
		CloneGroup group1 = containRelationService.findFileBelongToCloneGroup(file1);
		if(group1 == null) {
			return false;
		}
		CloneGroup group2 = containRelationService.findFileBelongToCloneGroup(file2);
		if(group2 == null) {
			return false;
		}
		return group1.equals(group2);
	}
	
	@Override
	public String exportCloneGroup(Collection<? extends Node> projects, Collection<CloneGroup> selectedGroups) {
		final String CSV_COLUMN_SEPARATOR = ",";
		final String CSV_ROW_SEPARATOR = "\r\n";
		StringBuffer buf = new StringBuffer();
		buf.append(" ").append(CSV_COLUMN_SEPARATOR);
		for (Node project : projects) {
			buf.append(project.getName()).append(CSV_COLUMN_SEPARATOR);
		}
		buf.append(CSV_ROW_SEPARATOR);
		for (CloneGroup group : selectedGroups) {
			buf.append(group.getName()).append(CSV_COLUMN_SEPARATOR);
			for (Node project : projects) {
				buf.append("\"");
				for (CodeNode codeNode : group.getNodes()) {
					Project belongedProject = containRelationService.findCodeNodeBelongToProject(codeNode);
					if ((project instanceof Project && belongedProject.getName().equals(project.getName())) ||
						(project instanceof MicroService && containRelationService.findProjectBelongToMicroService(belongedProject).getName().equals(project.getName()))) {
						buf.append(codeNode.getIdentifier()).append(CSV_ROW_SEPARATOR);
					}
				}
				buf.append("\"");
				buf.append(CSV_COLUMN_SEPARATOR);
			}
			buf.append(CSV_ROW_SEPARATOR);
		}
		return buf.toString();
	}

	/*@Override
	public String exportCloneProject(Map<String, Map<Long, CloneLineValue<Project>>> data,
											 Collection<Project> projects, CloneRelationType cloneRelationType) {
		final String CSV_COLUMN_SEPARATOR = ",";
		final String CSV_ROW_SEPARATOR = "\r\n";
		StringBuffer buf = new StringBuffer();
		buf.append(" ").append(CSV_COLUMN_SEPARATOR);
		for (Project project : projects) {
			buf.append(project.getName()).append("(").append(project.getLanguage().toString()).append(")").append(CSV_COLUMN_SEPARATOR);
		}
		buf.append(CSV_ROW_SEPARATOR);
		for (Map.Entry<String, Map<Long, CloneLineValue<Project>>> group : data.entrySet()) {
			buf.append(group.getKey()).append(CSV_COLUMN_SEPARATOR);
			Map<Long, CloneLineValue<Project>> map = group.getValue();
			for (Project project : projects) {
				CloneLineValue<Project> clv = map.get(project.getId());
				boolean hasData = false;
				buf.append("\"");
				if(level == CloneLevel.function) {
					for (Function function : clv.getCloneFunctions()) {
						hasData = true;
						buf.append(function.getName()).append(CSV_ROW_SEPARATOR);
					}
				} else {
					for (ProjectFile projectFile : clv.getCloneFiles()) {
						hasData = true;
						buf.append(projectFile.getPath()).append(CSV_ROW_SEPARATOR);
					}
				}
				if (hasData) {
					int len = buf.length();
					if (len > 2) buf.delete(len-2, len);
				}
				buf.append("\"");
				buf.append(CSV_COLUMN_SEPARATOR);
			}
			buf.append(CSV_ROW_SEPARATOR);
		}
		return buf.toString();
	}*/

	@Override
	public Collection<FileCloneWithCoChange> addCoChangeToFileClones(Collection<Clone> clones) {
		Collection<FileCloneWithCoChange> fileCloneWithCoChanges = new ArrayList<>();
		for(Clone fileClone: clones) {
			if(!(fileClone.getCodeNode1() instanceof ProjectFile)) {
				continue;
			}
			ProjectFile cloneFile1 = (ProjectFile) fileClone.getCodeNode1();
			ProjectFile cloneFile2 = (ProjectFile) fileClone.getCodeNode2();
			CoChange coChange = gitAnalyseService.findCoChangeBetweenTwoFiles(cloneFile1, cloneFile2);
			FileCloneWithCoChange fileCloneWithCoChange = null;
			try {
				fileCloneWithCoChange = new FileCloneWithCoChange(fileClone, coChange);
			} catch (Exception e) {
				e.printStackTrace();
			}
			fileCloneWithCoChanges.add(fileCloneWithCoChange);
		}
		return fileCloneWithCoChanges;
	}
}
