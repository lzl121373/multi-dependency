package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.clone.data.*;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CloneValueServiceImpl implements CloneValueService {
	
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
	GitAnalyseService gitAnalyseService;

	@Autowired
	NodeService nodeService;

	@Override
	public Collection<CloneValueForDoubleNodes<Package>> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones) {
		Collection<CloneValueForDoubleNodes<Package>> cache = removeSameNodeToCloneValuePackages;
		if(cache == null) {
			queryPackageCloneFromFileClone(fileClones);
			cache = removeSameNodeToCloneValuePackages;
		}
		List<CloneValueForDoubleNodes<Package>> result = new ArrayList<>(cache);
		result.sort((v1, v2) -> {
			return v2.getChildren().size() - v1.getChildren().size();
		});
		return result;
	}

	@Override
	public PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<Clone> fileClones, Package pck1, Package pck2) throws Exception {
		CloneValueForDoubleNodes<Package> temp = queryPackageCloneFromFileCloneSort(fileClones, pck1, pck2);
		if(temp == null) {
			Package OriginalPck1 = nodeService.queryPackage(pck1.getId());
			Package OriginalPck2 = nodeService.queryPackage(pck2.getId());
			temp = queryPackageCloneFromFileCloneSort(fileClones, OriginalPck1, OriginalPck2);
		}
		PackageCloneValueWithFileCoChange result = new PackageCloneValueWithFileCoChange();
		result.setPck1(temp.getNode1());
		result.setPck2(temp.getNode2());
		result.addFile1(containRelationService.findPackageContainFiles(pck1));
		result.addFile2(containRelationService.findPackageContainFiles(pck2));
		List<Clone> children = temp.getChildren();
		for(Clone clone : children) {
			ProjectFile file1 = (ProjectFile) clone.getCodeNode1();
			ProjectFile file2 = (ProjectFile) clone.getCodeNode2();
			if(containRelationService.findFileBelongToPackage(file1).equals(pck1)) {
				result.addCloneFile1(file1);
				result.addCloneFile2(file2);
			} else {
				result.addCloneFile1(file2);
				result.addCloneFile2(file1);
			}
			CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file1, file2);
			result.addChild(new FileCloneWithCoChange(clone, cochange));
		}

		Set<ProjectFile> cloneFiles1 = result.getCloneFiles1();
		Set<ProjectFile> cloneFiles2 = result.getCloneFiles2();
		Set<ProjectFile> allFiles1 = result.getAllFiles1();
		Set<ProjectFile> allFiles2 = result.getAllFiles2();

		Set<ProjectFile> noneCloneFiles1 = new HashSet();
		Set<ProjectFile> noneCloneFiles2 = new HashSet();

		for(ProjectFile projectFile: allFiles1){
			noneCloneFiles1.add(projectFile);
		}

		for(ProjectFile projectFile: allFiles2){
			noneCloneFiles2.add(projectFile);
		}

		noneCloneFiles1.removeAll(cloneFiles1);
		noneCloneFiles2.removeAll(cloneFiles2);

		result.addNoneCloneFiles1(noneCloneFiles1);
		result.addNoneCloneFiles2(noneCloneFiles2);

//		result.addNoneCloneFiles1(addNoneCloneFiles(cloneFiles1,allFiles1));
//		result.addNoneCloneFiles1(addNoneCloneFiles(cloneFiles2,allFiles2));

		result.sortChildren();
		return result;
	}

	@Override
	public PackageCloneValueWithFileCoChangeMatrix queryPackageCloneWithFileCoChangeMatrix(Collection<Clone> fileClones, Package pck1, Package pck2) {
		try {
			PackageCloneValueWithFileCoChange packageCloneValueWithFileCoChange = queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
			Map<String, FileCloneWithCoChange> fileClone = new HashMap<>();
			List<FileCloneWithCoChange> children = new ArrayList<>(packageCloneValueWithFileCoChange.getChildren());
			LinkedHashSet<ProjectFile> cloneFiles1 = new LinkedHashSet<>();
			LinkedHashSet<ProjectFile> cloneFiles2 = new LinkedHashSet<>();
			Set<ProjectFile> noneCloneFiles1 = new HashSet<>(packageCloneValueWithFileCoChange.getNoneCloneFiles1());
			Set<ProjectFile> noneCloneFiles2 = new HashSet<>(packageCloneValueWithFileCoChange.getNoneCloneFiles2());
			Map<Long, Integer> map = new HashMap<>();
			int row = 0;
			int col = 0;
			for(FileCloneWithCoChange child : children) {
				if(!map.containsKey(child.getFile1().getId())) {
					map.put(child.getFile1().getId(), row);
					cloneFiles1.add(child.getFile1());
					row ++;
				}
				if(!map.containsKey(child.getFile2().getId())) {
					map.put(child.getFile2().getId(), col);
					cloneFiles2.add(child.getFile2());
					col ++;
				}
			}
			boolean[][] matrix = new boolean[row][col];
			for(int i = 0; i < row; i ++) {
				for(int j = 0; j < col; j ++) {
					matrix[i][j] = false;
				}
			}
			for(FileCloneWithCoChange child : children) {
				int i = map.get(child.getFile1().getId());
				int j = map.get(child.getFile2().getId());
				matrix[i][j] = true;
				String key = String.join("_", child.getFile1().getName(), child.getFile2().getName());
				fileClone.put(key, child);
			}
			return new PackageCloneValueWithFileCoChangeMatrix(fileClone, cloneFiles1, cloneFiles2, noneCloneFiles1, noneCloneFiles2, matrix);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

//	private Set<ProjectFile> addNoneCloneFiles(Set<ProjectFile> cloneFiles, Set<ProjectFile> allFiles){
//		for(ProjectFile projectFile: cloneFiles){
//			if(allFiles.contains(projectFile)){
//				allFiles.remove(projectFile);
//			}
//		}
//		return allFiles;
//	}
	/**
	 * 两个包之间的文件级克隆的聚合，两个包之间不分先后顺序
	 * @param fileClones
	 * @param pck1
	 * @param pck2
	 * @return
	 */
	@Override
	public CloneValueForDoubleNodes<Package> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, Package pck1, Package pck2) {
		Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> packageClones = queryPackageCloneFromFileClone(fileClones);
		Map<Package, CloneValueForDoubleNodes<Package>> map = packageClones.getOrDefault(pck1, new HashMap<>());
		CloneValueForDoubleNodes<Package> result = map.get(pck2);
		if(result == null) {
			map = packageClones.getOrDefault(pck2, new HashMap<>());
			result = map.get(pck1);
		}
		if(result != null) {
			result.sortChildren();
		}
		return result;
	}

    private Collection<CloneValueForDoubleNodes<Package>> removeSameNodeToCloneValuePackages = null;
    private Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> queryPackageCloneFromFileCloneCache = null;
	public Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> queryPackageCloneFromFileClone(Collection<Clone> fileClones) {
		Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> pckToPackageClones = queryPackageCloneFromFileCloneCache;
		if(pckToPackageClones == null) {
			Collection<CloneValueForDoubleNodes<Package>> cache = new ArrayList<>();
			pckToPackageClones = new HashMap<>();
			
			for(Clone clone : fileClones) {
				CodeNode node1 = clone.getCodeNode1();
				CodeNode node2 = clone.getCodeNode2();
				if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
					continue;
				}
				ProjectFile file1 = (ProjectFile) node1;
				ProjectFile file2 = (ProjectFile) node2;
				if(file1.equals(file2)) {
					continue;
				}
				Package pck1 = containRelationService.findFileBelongToPackage(file1);
				Package pck2 = containRelationService.findFileBelongToPackage(file2);
				if(pck1.equals(pck2)) {
					continue;
				}
				CloneValueForDoubleNodes<Package> cloneValue = hasFileCloneInPackage(pckToPackageClones, pck1, pck2);
				if(cloneValue == null) {
					cloneValue = new CloneValueForDoubleNodes<Package>(pck1, pck2);
					cache.add(cloneValue);
				}
				cloneValue.addChild(clone);
				if(pck1.equals(cloneValue.getNode1())) {
					cloneValue.addCodeNodeToNode1(file1);
					cloneValue.addCodeNodeToNode2(file2);
				} else {
					cloneValue.addCodeNodeToNode2(file1);
					cloneValue.addCodeNodeToNode1(file2);
				}
				cloneValue.setAllNodesInNode1(new HashSet<>(containRelationService.findPackageContainFiles(cloneValue.getNode1())));
				cloneValue.setAllNodesInNode2(new HashSet<>(containRelationService.findPackageContainFiles(cloneValue.getNode2())));
				
				Map<Package, CloneValueForDoubleNodes<Package>> pck1ToClones = pckToPackageClones.getOrDefault(pck1, new HashMap<>());
				pck1ToClones.put(pck2, cloneValue);
				pckToPackageClones.put(pck1, pck1ToClones);
			}
			removeSameNodeToCloneValuePackages = cache;
			queryPackageCloneFromFileCloneCache = pckToPackageClones;
		}
		return pckToPackageClones;
	}
	
	private CloneValueForDoubleNodes<Package> hasFileCloneInPackage(
			Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> pckToPackageClones, 
			Package pck1, Package pck2) {
		Map<Package, CloneValueForDoubleNodes<Package>> pck1ToClones = pckToPackageClones.getOrDefault(pck1, new HashMap<>());
		CloneValueForDoubleNodes<Package> clone = pck1ToClones.get(pck2);
		if(clone != null) {
			return clone;
		}
		Map<Package, CloneValueForDoubleNodes<Package>> pck2ToClones = pckToPackageClones.getOrDefault(pck2, new HashMap<>());
		clone = pck2ToClones.get(pck1);
		return clone;
	}

	@Override
	public Collection<CloneValueForDoubleNodes<Project>> queryProjectCloneFromFileClone(Collection<Clone> fileClones) {
		List<CloneValueForDoubleNodes<Project>> result = new ArrayList<>();
		Map<Project, Map<Project, CloneValueForDoubleNodes<Project>>> projectToProjectClones = new HashMap<>();
		for(Clone clone : fileClones) {
			CodeNode node1 = clone.getCodeNode1();
			CodeNode node2 = clone.getCodeNode2();
			if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
				continue;
			}
			ProjectFile file1 = (ProjectFile) node1;
			ProjectFile file2 = (ProjectFile) node2;
			if(file1.equals(file2)) {
				continue;
			}
			Project project1 = containRelationService.findFileBelongToProject(file1);
			Project project2 = containRelationService.findFileBelongToProject(file2);
			if(project1.equals(project2)) {
				continue;
			}
			CloneValueForDoubleNodes<Project> cloneValue = hasFileCloneInProject(projectToProjectClones, project1, project2);
			if(cloneValue == null) {
				cloneValue = new CloneValueForDoubleNodes<Project>(project1, project2);
				result.add(cloneValue);
			}
			cloneValue.addChild(clone);
			
			Map<Project, CloneValueForDoubleNodes<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, cloneValue);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private CloneValueForDoubleNodes<Project> hasFunctionCloneInProject(
			Map<Project, Map<Project, CloneValueForDoubleNodes<Project>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, CloneValueForDoubleNodes<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
		CloneValueForDoubleNodes<Project> cloneValue = project1ToClones.get(project2);
		if(cloneValue != null) {
			return cloneValue;
		}
		Map<Project, CloneValueForDoubleNodes<Project>> project2ToClones = projectToProjectClones.getOrDefault(project2, new HashMap<>());
		cloneValue = project2ToClones.get(project1);
		return cloneValue;
	}
	
	private CloneValueForDoubleNodes<Project> hasFileCloneInProject(
			Map<Project, Map<Project, CloneValueForDoubleNodes<Project>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, CloneValueForDoubleNodes<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
		CloneValueForDoubleNodes<Project> clone = project1ToClones.get(project2);
		if(clone != null) {
			return clone;
		}
		Map<Project, CloneValueForDoubleNodes<Project>> project2ToClones = projectToProjectClones.getOrDefault(project2, new HashMap<>());
		clone = project2ToClones.get(project1);
		return clone;
	}

	@Override
	public Collection<CloneValueForDoubleNodes<Project>> findProjectCloneFromFunctionClone(Collection<Clone> functionClones) {
		List<CloneValueForDoubleNodes<Project>> result = new ArrayList<>();
		Map<Project, Map<Project, CloneValueForDoubleNodes<Project>>> projectToProjectClones = new HashMap<>();
		for(Clone functionCloneFunction : functionClones) {
			CodeNode node1 = functionCloneFunction.getCodeNode1();
			CodeNode node2 = functionCloneFunction.getCodeNode2();
			if(!(node1 instanceof Function) || !(node2 instanceof Function)) {
				continue;
			}
			Function function1 = (Function) node1;
			Function function2 = (Function) node2;
			if(function1.equals(function2)) {
				continue;
			}
			Project project1 = containRelationService.findFunctionBelongToProject(function1);
			Project project2 = containRelationService.findFunctionBelongToProject(function2);
			if(project1.equals(project2)) {
				continue;
			}
			CloneValueForDoubleNodes<Project> clone = hasFunctionCloneInProject(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new CloneValueForDoubleNodes<Project>(project1, project2);
				result.add(clone);
			}
			// 函数间的克隆作为Children
			clone.addChild(functionCloneFunction);
			
			Map<Project, CloneValueForDoubleNodes<Project>> project1ToClones 
				= projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private CloneValueForDoubleNodes<MicroService> hasCloneInFunctionClones(
			Map<MicroService, Map<MicroService, CloneValueForDoubleNodes<MicroService>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		CloneValueForDoubleNodes<MicroService> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}
	
	private CloneValueForDoubleNodes<MicroService> hasCloneInFileClones(
			Map<MicroService, Map<MicroService, CloneValueForDoubleNodes<MicroService>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		CloneValueForDoubleNodes<MicroService> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}

	@Override
	public Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFunctionClone(Collection<Clone> functionClones) {
		Collection<CloneValueForDoubleNodes<Project>> projectClones = findProjectCloneFromFunctionClone(functionClones);
		List<CloneValueForDoubleNodes<MicroService>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, CloneValueForDoubleNodes<MicroService>>> msToMsClones = new HashMap<>();
		for(CloneValueForDoubleNodes<Project> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null || ms1.equals(ms2)) {
				continue;
			}
			CloneValueForDoubleNodes<MicroService> clone = hasCloneInFunctionClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new CloneValueForDoubleNodes<MicroService>(ms1, ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}

	@Override
	public Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFileClone(
			Collection<Clone> fileClones) {
		Iterable<CloneValueForDoubleNodes<Project>> projectClones = queryProjectCloneFromFileClone(fileClones);
		List<CloneValueForDoubleNodes<MicroService>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, CloneValueForDoubleNodes<MicroService>>> msToMsClones = new HashMap<>();
		for(CloneValueForDoubleNodes<Project> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(ms1.equals(ms2)) {
				continue;
			}
			CloneValueForDoubleNodes<MicroService> clone = hasCloneInFileClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new CloneValueForDoubleNodes<MicroService>(ms1, ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, CloneValueForDoubleNodes<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}
}
