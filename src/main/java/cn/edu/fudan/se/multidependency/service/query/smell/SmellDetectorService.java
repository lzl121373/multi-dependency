package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelateTo;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.RelateToRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import cn.edu.fudan.se.multidependency.service.query.smell.impl.GodComponentDetectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmellDetectorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmellDetectorService.class);

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainRepository containRepository;

	@Autowired
	private RelateToRepository relateToRepository;

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private HubLikeDependencyDetector hubLikeDependencyDetector;

	@Autowired
	private UnstableDependencyDetectorUsingHistory unstableDependencyDetectorUsingHistory;

	@Autowired
	private UnstableInterfaceDetector unstableInterfaceDetector;

	@Autowired
	private ImplicitCrossModuleDependencyDetector implicitCrossModuleDependencyDetector;

	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;

	@Autowired
	private GodComponentDetectorImpl godComponentDetector;

	@Autowired
	private UnutilizedAbstractionDetector unutilizedAbstractionDetector;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@Autowired
	private StaticAnalyseService staticAnalyseService;

	public void createCloneSmells(boolean isRecreate){
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.CLONE);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Clone Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
        smellRepository.deleteSmellContainRelations(SmellType.CLONE);
        smellRepository.deleteSmellMetric(SmellType.CLONE);
        smellRepository.deleteSmells(SmellType.CLONE);

		smellRepository.createCloneSmells();
		smellRepository.createCloneSmellContains();
		smellRepository.setCloneSmellProject();
		LOGGER.info("创建Clone Smell节点关系完成");
	}

	public void createCycleDependencySmells(boolean isRecreate){
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.CYCLIC_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Cyclic Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmellMetric(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.CYCLIC_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<Cycle<Type>>> typeCyclicDependencyMap = new HashMap<>(cyclicDependencyDetector.detectTypeCyclicDependency());
		String typeSmellName = SmellLevel.TYPE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
		for (Map.Entry<Long, List<Cycle<Type>>> entry : typeCyclicDependencyMap.entrySet()){
			int typeSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (Cycle<Type> typeCyclicDependency : entry.getValue()){
				List<Type> types = typeCyclicDependency.getComponents();
				Smell smell = new Smell();
				smell.setName(typeSmellName + typeSmellIndex);
				smell.setSize(types.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.TYPE);
				smells.add(smell);
				for (Type type : types) {
					Contain contain = new Contain(smell, type);
					smellContains.add(contain);
				}
				typeSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<Cycle<ProjectFile>>> fileCyclicDependencyMap = new HashMap<>(cyclicDependencyDetector.detectFileCyclicDependency());
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
		for (Map.Entry<Long, List<Cycle<ProjectFile>>> entry : fileCyclicDependencyMap.entrySet()){
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (Cycle<ProjectFile> fileCyclicDependency : entry.getValue()){
				List<ProjectFile> files = fileCyclicDependency.getComponents();
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(files.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				for (ProjectFile file : files) {
					Contain contain = new Contain(smell, file);
					smellContains.add(contain);
				}
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<Cycle<Package>>> packageCyclicDependencyMap = new HashMap<>(cyclicDependencyDetector.detectPackageCyclicDependency());
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
		for (Map.Entry<Long, List<Cycle<Package>>> entry : packageCyclicDependencyMap.entrySet()){
			int packageSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (Cycle<Package> packageCyclicDependency : entry.getValue()){
				List<Package> packages = packageCyclicDependency.getComponents();
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageSmellIndex);
				smell.setSize(packages.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				for (Package pck : packages) {
					Contain contain = new Contain(smell, pck);
					smellContains.add(contain);
				}
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Cyclic Dependency Smell节点关系完成");
	}

	public void createHubLikeDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.HUBLIKE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Hub-Like Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmellRelateToRelations(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmellMetric(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.HUBLIKE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		List<RelateTo> smellRelateTos = new ArrayList<>();

		Map<Long, List<FileHubLike>> fileHubLikeDependencyMap = hubLikeDependencyDetector.detectFileHubLikeDependency();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.HUBLIKE_DEPENDENCY + "_";
		for (Map.Entry<Long, List<FileHubLike>> entry : fileHubLikeDependencyMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (FileHubLike fileHubLikeDependency : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.HUBLIKE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				int smellSize = 0;
				Collection<DependsOn> fileDependsOns = staticAnalyseService.findFileDependsOn(fileHubLikeDependency.getFile());
				if(fileDependsOns != null && !fileDependsOns.isEmpty()){
					fileDependsOns.forEach(fileDpOn->{
						ProjectFile fileOn = (ProjectFile)fileDpOn.getEndNode();
						RelateTo relateTo = new RelateTo(smell,fileOn);
						smellRelateTos.add(relateTo);
					});
					smellSize += fileDependsOns.size();
				}
				Collection<DependsOn> fileDependsOnBys = staticAnalyseService.findFileDependedOnBy(fileHubLikeDependency.getFile());
				if(fileDependsOnBys != null && !fileDependsOnBys.isEmpty()){
					fileDependsOnBys.forEach(fileDpOnBy->{
						ProjectFile fileBy = (ProjectFile)fileDpOnBy.getStartNode();
						RelateTo relateTo = new RelateTo(smell,fileBy);
						smellRelateTos.add(relateTo);
					});
					smellSize += fileDependsOnBys.size();
				}
				smell.setSize(smellSize);
				smells.add(smell);
				Contain contain = new Contain(smell, fileHubLikeDependency.getFile());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		relateToRepository.saveAll(smellRelateTos);

		smells.clear();
		smellContains.clear();
		smellRelateTos.clear();
		Map<Long, List<PackageHubLike>> packageHubLikeDependencyMap = hubLikeDependencyDetector.detectPackageHubLikeDependency();
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.HUBLIKE_DEPENDENCY + "_";
		for (Map.Entry<Long, List<PackageHubLike>> entry : packageHubLikeDependencyMap.entrySet()) {
			int packageSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (PackageHubLike packageHubLikeDependency : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.HUBLIKE_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Collection<DependsOn> pakDependsOns = staticAnalyseService.findPackageDependsOn(packageHubLikeDependency.getPck());
				if(pakDependsOns != null && !pakDependsOns.isEmpty()){
					pakDependsOns.forEach(pckDpOn->{
						Package pckOn = (Package)pckDpOn.getEndNode();
						RelateTo relateTo = new RelateTo(smell,pckOn);
						smellRelateTos.add(relateTo);
					});
				}
				Collection<DependsOn> pckDependsOnBys = staticAnalyseService.findPackageDependedOnBy(packageHubLikeDependency.getPck());
				if(pckDependsOnBys != null && !pckDependsOnBys.isEmpty()){
					pckDependsOnBys.forEach(pckDpOnBy->{
						Package pckBy = (Package)pckDpOnBy.getStartNode();
						RelateTo relateTo = new RelateTo(smell,pckBy);
						smellRelateTos.add(relateTo);
					});
				}
				Contain contain = new Contain(smell, packageHubLikeDependency.getPck());
				smellContains.add(contain);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		relateToRepository.saveAll(smellRelateTos);
		LOGGER.info("创建Hub-Like Dependency Smell节点关系完成");
	}

	public void createUnstableDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNSTABLE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unstable Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmellMetric(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmellRelateToRelations(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.UNSTABLE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		List<RelateTo> smellRelateTos = new ArrayList<>();

		Map<Long, List<UnstableDependencyByHistory>> fileUnstableComponentMap = unstableDependencyDetectorUsingHistory.detectFileUnstableDependency();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNSTABLE_DEPENDENCY + "_";
		for (Map.Entry<Long, List<UnstableDependencyByHistory>> entry : fileUnstableComponentMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (UnstableDependencyByHistory fileUnstableComponent : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNSTABLE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Collection<DependsOn> fileDependsOns = staticAnalyseService.findFileDependsOn(fileUnstableComponent.getComponent());
				if(fileDependsOns != null && !fileDependsOns.isEmpty()){
					fileDependsOns.forEach(fileDpOnBy->{
						ProjectFile fileBy = (ProjectFile)fileDpOnBy.getEndNode();
						RelateTo relateTo = new RelateTo(smell,fileBy);
						smellRelateTos.add(relateTo);
					});
				}
				Contain contain = new Contain(smell, fileUnstableComponent.getComponent());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		relateToRepository.saveAll(smellRelateTos);

//		smells.clear();
//		smellContains.clear();
//		smellRelateTos.clear();
//		Map<Long, List<UnstableDependencyByInstability<Package>>> packageUnstableComponentMap = unstableDependencyDetectorUsingInstability.detectPackageUnstableDependency();
//		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.UNSTABLE_DEPENDENCY + "_";
//		for (Map.Entry<Long, List<UnstableDependencyByInstability<Package>>> entry : packageUnstableComponentMap.entrySet()) {
//			int packageSmellIndex = 1;
//			long projectId = entry.getKey();
//			Project project = (Project) projectRepository.queryNodeById(projectId);
//			for (UnstableDependencyByInstability<Package> packageUnstableComponent : entry.getValue()) {
//				Smell smell = new Smell();
//				smell.setName(packageSmellName + packageSmellIndex);
//				smell.setSize(1);
//				smell.setLanguage(project.getLanguage());
//				smell.setProjectId(projectId);
//				smell.setProjectName(project.getName());
//				smell.setType(SmellType.UNSTABLE_DEPENDENCY);
//				smell.setLevel(SmellLevel.PACKAGE);
//				smells.add(smell);
//				Collection<DependsOn> pckDependsOns = staticAnalyseService.findPackageDependsOn(packageUnstableComponent.getComponent());
//				if(pckDependsOns != null && !pckDependsOns.isEmpty()){
//					pckDependsOns.forEach(pckDpOnBy->{
//						Package pckBy = (Package)pckDpOnBy.getEndNode();
//						RelateTo relateTo = new RelateTo(smell,pckBy);
//						smellRelateTos.add(relateTo);
//					});
//				}
//				Contain contain = new Contain(smell, packageUnstableComponent.getComponent());
//				smellContains.add(contain);
//				packageSmellIndex ++;
//			}
//		}
//		smellRepository.saveAll(smells);
//		containRepository.saveAll(smellContains);
//		relateToRepository.saveAll(smellRelateTos);
		LOGGER.info("创建Unstable Dependency Smell节点关系完成");
	}

	public void createUnstableInterfaceSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNSTABLE_INTERFACE);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unstable Interface Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNSTABLE_INTERFACE);
		smellRepository.deleteSmellMetric(SmellType.UNSTABLE_INTERFACE);
		smellRepository.deleteSmellRelateToRelations(SmellType.UNSTABLE_INTERFACE);
		smellRepository.deleteSmells(SmellType.UNSTABLE_INTERFACE);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		List<RelateTo> smellRelateTos = new ArrayList<>();

		Map<Long, List<UnstableInterface>> fileUnstableInterfaceMap = unstableInterfaceDetector.detectFileUnstableInterface();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNSTABLE_INTERFACE + "_";
		for (Map.Entry<Long, List<UnstableInterface>> entry : fileUnstableInterfaceMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (UnstableInterface fileUnstableInterface : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNSTABLE_INTERFACE);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Collection<DependsOn> fileDependsOnBys = staticAnalyseService.findFileDependedOnBy(fileUnstableInterface.getComponent());
				if(fileDependsOnBys != null && !fileDependsOnBys.isEmpty()){
					fileDependsOnBys.forEach(fileDpOnBy->{
						ProjectFile fileBy = (ProjectFile)fileDpOnBy.getStartNode();
						RelateTo relateTo = new RelateTo(smell,fileBy);
						smellRelateTos.add(relateTo);
					});
				}
				Contain contain = new Contain(smell, fileUnstableInterface.getComponent());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		relateToRepository.saveAll(smellRelateTos);

		LOGGER.info("创建Unstable Interface Smell节点关系完成");
	}

	public void createSimilarComponentsSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.SIMILAR_COMPONENTS);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Similar Components Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.SIMILAR_COMPONENTS);
		smellRepository.deleteSmellMetric(SmellType.SIMILAR_COMPONENTS);
		smellRepository.deleteSmells(SmellType.SIMILAR_COMPONENTS);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<SimilarComponents<ProjectFile>>> fileSimilarComponentsMap = similarComponentsDetector.detectFileSimilarComponents();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.SIMILAR_COMPONENTS + "_";
		for (Map.Entry<Long, List<SimilarComponents<ProjectFile>>> entry : fileSimilarComponentsMap.entrySet()) {
			int fileSmellIndex = 1;
			for (SimilarComponents<ProjectFile> fileSimilarComponents : entry.getValue()) {
				Smell smell = new Smell();
				Package pck1 = containRepository.findFileBelongToPackage(fileSimilarComponents.getNode1().getId());
				Package pck2 = containRepository.findFileBelongToPackage(fileSimilarComponents.getNode2().getId());
				Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
				Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(2);
				if (project1.getId().equals(project2.getId())) {
					smell.setLanguage(project1.getLanguage());
					smell.setProjectId(project1.getId());
					smell.setProjectName(project1.getName());
				}
				else {
					smell.setLanguage(project1.getLanguage());
					smell.setProjectId(project1.getId());
					smell.setProjectName(project1.getName() + "+" + project2.getName());
				}
				smell.setType(SmellType.SIMILAR_COMPONENTS);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain1 = new Contain(smell, fileSimilarComponents.getNode1());
				Contain contain2 = new Contain(smell, fileSimilarComponents.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<SimilarComponents<Package>>> packageSimilarComponentsMap = similarComponentsDetector.detectPackageSimilarComponents();
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.SIMILAR_COMPONENTS + "_";
		for (Map.Entry<Long, List<SimilarComponents<Package>>> entry : packageSimilarComponentsMap.entrySet()) {
			int packageSmellIndex = 1;
			for (SimilarComponents<Package> packageSimilarComponents : entry.getValue()) {
				Package pck1 = packageSimilarComponents.getNode1();
				Package pck2 = packageSimilarComponents.getNode2();
				Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
				Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageSmellIndex);
				smell.setSize(2);
				if (project1.getId().equals(project2.getId())) {
					smell.setLanguage(project1.getLanguage());
					smell.setProjectId(project1.getId());
					smell.setProjectName(project1.getName());
				}
				else {
					smell.setLanguage(project1.getLanguage());
					smell.setProjectId(project1.getId());
					smell.setProjectName(project1.getName() + "+" + project2.getName());
				}
				smell.setType(SmellType.SIMILAR_COMPONENTS);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Contain contain1 = new Contain(smell, packageSimilarComponents.getNode1());
				Contain contain2 = new Contain(smell, packageSimilarComponents.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Similar Components Smell节点关系完成");
	}

	public void createImplicitCrossModuleDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Implicit Cross Module Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellRepository.deleteSmellMetric(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<LogicCouplingComponents<ProjectFile>>> fileImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.detectFileImplicitCrossModuleDependency();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY + "_";
		for (Map.Entry<Long, List<LogicCouplingComponents<ProjectFile>>> entry : fileImplicitCrossModuleDependencyMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(2);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain1 = new Contain(smell, fileImplicitCrossModuleDependency.getNode1());
				Contain contain2 = new Contain(smell, fileImplicitCrossModuleDependency.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<LogicCouplingComponents<Package>>> packageImplicitCrossModuleDependencyMap = implicitCrossModuleDependencyDetector.detectPackageImplicitCrossModuleDependency();
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY + "_";
		for (Map.Entry<Long, List<LogicCouplingComponents<Package>>> entry : packageImplicitCrossModuleDependencyMap.entrySet()) {
			int packageSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (LogicCouplingComponents<Package> packageImplicitCrossModuleDependency : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageSmellIndex);
				smell.setSize(2);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Contain contain1 = new Contain(smell, packageImplicitCrossModuleDependency.getNode1());
				Contain contain2 = new Contain(smell, packageImplicitCrossModuleDependency.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Implicit CrossModule Dependency Smell节点关系完成");
	}

	public void createGodComponentSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.GOD_COMPONENT);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在God Component Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.GOD_COMPONENT);
		smellRepository.deleteSmellMetric(SmellType.GOD_COMPONENT);
		smellRepository.deleteSmells(SmellType.GOD_COMPONENT);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

  		Map<Long, List<FileGod>> fileGodComponentMap = godComponentDetector.fileGodComponents();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.GOD_COMPONENT + "_";
		for (Map.Entry<Long, List<FileGod>> entry : fileGodComponentMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<FileGod> files = entry.getValue();
			for (FileGod fileGodComponent : files) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.GOD_COMPONENT);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, fileGodComponent.getFile());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建God Component Smell节点关系完成");
	}

	public void createUnutilizedAbstractionSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNUTILIZED_ABSTRACTION);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unutilized Abstraction Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNUTILIZED_ABSTRACTION);
		smellRepository.deleteSmellMetric(SmellType.UNUTILIZED_ABSTRACTION);
		smellRepository.deleteSmells(SmellType.UNUTILIZED_ABSTRACTION);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizedAbstractionMap = unutilizedAbstractionDetector.detectFileUnutilizedAbstraction();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNUTILIZED_ABSTRACTION + "_";
		for (Map.Entry<Long, List<UnutilizedAbstraction<ProjectFile>>> entry : fileUnutilizedAbstractionMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (UnutilizedAbstraction<ProjectFile> fileUnutilizedAbstraction : entry.getValue()) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNUTILIZED_ABSTRACTION);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, fileUnutilizedAbstraction.getComponent());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Unutilized Abstraction Smell节点关系完成");
	}

	public void createUnusedIncludeSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNUSED_INCLUDE);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unused Include Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNUSED_INCLUDE);
		smellRepository.deleteSmellRelateToRelations(SmellType.UNUSED_INCLUDE);
		smellRepository.deleteSmellMetric(SmellType.UNUSED_INCLUDE);
		smellRepository.deleteSmells(SmellType.UNUSED_INCLUDE);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		List<RelateTo> smellRelateTos = new ArrayList<>();

		Map<Long, List<UnusedInclude>> fileUnusedIncludeMap = unusedIncludeDetector.detectFileUnusedInclude();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNUSED_INCLUDE + "_";
		for (Map.Entry<Long, List<UnusedInclude>> entry : fileUnusedIncludeMap.entrySet()) {
			int fileSmellIndex = 1;
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			for (UnusedInclude fileUnusedInclude : entry.getValue()) {
				Set<ProjectFile> unusedIncludeFiles = fileUnusedInclude.getUnusedIncludeFiles();
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(unusedIncludeFiles.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNUSED_INCLUDE);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				for (ProjectFile unusedIncludeFile : unusedIncludeFiles) {
					RelateTo relateTo = new RelateTo(smell, unusedIncludeFile);
					smellRelateTos.add(relateTo);
				}
				Contain contain = new Contain(smell, fileUnusedInclude.getCoreFile());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		relateToRepository.saveAll(smellRelateTos);
		LOGGER.info("创建Unused Include Smell节点关系完成");
	}
}
