package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.AggregationDependsOn;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.AggregationCoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.repository.relation.AggregationDependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.AggregationCoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class HotspotPackagePairDetectorImpl implements HotspotPackagePairDetector {

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private AggregationDependsOnRepository aggregationDependsOnRepository;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private AggregationCloneRepository aggregationCloneRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;

	@Autowired
	private AggregationCoChangeRepository aggregationCoChangeRepository;

	@Autowired
	private ModuleCloneRepository moduleCloneRepository;

	@Autowired
	private CommitUpdateFileRepository commitUpdateFileRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private CacheService cache;

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairs() {
		return detectHotspotPackagePairWithFileClone();
	}

	@Override
	public Map<String, List<DependsOn>> detectHotspotPackagePairWithDependsOn() {
		String key = "hotspotPackagePairWithDependsOn";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<String, List<DependsOn>> result = new HashMap<>();
		Map<Package, Map<Package, List<DependsOn>>> packageDependsOnMap = new HashMap<>();
		List<DependsOn> fileDependsOnList = dependsOnRepository.findFileDepends();
		List<DependsOn> moduleDependsOnList = new ArrayList<>();
		List<DependsOn> aggregationDependsOnList = new ArrayList<>();
		//加载，每个结点都有聚合和非聚合两种依赖
		for(DependsOn fileDependsOn : fileDependsOnList){
			if(fileDependsOn.getDependsOnTypes() != null && !fileDependsOn.getDependsOnTypes().isEmpty()) {
				Package currentPackage1 = dependsOnRepository.findFileBelongPackageByFileId(fileDependsOn.getStartNode().getId());
				Package currentPackage2 = dependsOnRepository.findFileBelongPackageByFileId(fileDependsOn.getEndNode().getId());
				Package childPackage1 = null;
				Package childPackage2 = null;
				boolean isAggregatePackagePair = false;
				while(isParentPackages(currentPackage1, currentPackage2)){
					Map<Package, List<DependsOn>> dependsOnMap = packageDependsOnMap.getOrDefault(currentPackage1, new HashMap<>());
					List<DependsOn> dependsOnList = dependsOnMap.getOrDefault(currentPackage2, new ArrayList<>());
					DependsOn dependsOn = null;
					for(DependsOn d : dependsOnList) {
						if(isAggregatePackagePair == d.isAggregatePackagePair()) {
							dependsOn = new DependsOn(d);
							dependsOnList.remove(d);
							break;
						}
					}
					if(dependsOn != null ){
						int times = dependsOn.getTimes();
						for(Map.Entry<String, Long> entry : fileDependsOn.getDependsOnTypes().entrySet()) {
							String dependsOnKey = entry.getKey();
							Long typeTimes = entry.getValue();
							if(dependsOn.getDependsOnTypes().containsKey(dependsOnKey)) {
								Long currentTypeTimes = dependsOn.getDependsOnTypes().get(dependsOnKey);
								currentTypeTimes += typeTimes;
								dependsOn.getDependsOnTypes().put(dependsOnKey, currentTypeTimes);
							}
							else {
								dependsOn.getDependsOnTypes().put(dependsOnKey, typeTimes);
								String dependsOnType = dependsOn.getDependsOnType();
								dependsOn.setDependsOnType(dependsOnType + "__" + dependsOnKey);
							}
							times += typeTimes.intValue();
						}
						dependsOn.setTimes(times);
					}
					else {
						dependsOn = new DependsOn(currentPackage1, currentPackage2);
						dependsOn.setDependsOnTypes(fileDependsOn.getDependsOnTypes());
						dependsOn.setDependsOnType(fileDependsOn.getDependsOnType());
						dependsOn.setTimes(fileDependsOn.getTimes());
					}
					dependsOn.setAggregatePackagePair(isAggregatePackagePair);
					dependsOnList.add(dependsOn);
					dependsOnMap.put(currentPackage2, dependsOnList);
					packageDependsOnMap.put(currentPackage1, dependsOnMap);
					if(isAggregatePackagePair) {
						childPackage1 = currentPackage1;
						childPackage2 = currentPackage2;
						currentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
						currentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
						//错位
						if(!currentPackage1.getDirectoryPath().equals(currentPackage2.getDirectoryPath()) && currentPackage1.getDirectoryPath().contains(currentPackage2.getDirectoryPath())) {
							currentPackage2 = childPackage2;
						}
						else if(!currentPackage2.getDirectoryPath().equals(currentPackage1.getDirectoryPath()) && currentPackage2.getDirectoryPath().contains(currentPackage1.getDirectoryPath())) {
							currentPackage1 = childPackage1;
						}
					}
					isAggregatePackagePair = true;
				}
			}
		}
		//删除叶子结点的聚合依赖，并计算关系权重
		for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : packageDependsOnMap.entrySet()) {
			Package pck1 = entry.getKey();
			Map<Package, List<DependsOn>> dependsOnMap1 = entry.getValue();
			for(Map.Entry<Package, List<DependsOn>> entryKey1 : dependsOnMap1.entrySet()){
				Package pck2 = entryKey1.getKey();
				List<DependsOn> dependsOnList1 = dependsOnMap1.getOrDefault(pck2, new ArrayList<>());
				if(dependsOnList1.size() == 2) {
					DependsOn dependsOn1_1 = dependsOnList1.get(0);
					DependsOn dependsOn1_2 = dependsOnList1.get(1);
					if(dependsOn1_1.getTimes() == dependsOn1_2.getTimes()) {
						boolean flag = false;
						Map<Package, List<DependsOn>> dependsOnMap2 = packageDependsOnMap.get(pck2);
						if(dependsOnMap2 != null) {
							List<DependsOn> dependsOnList2 = dependsOnMap2.getOrDefault(pck1, new ArrayList<>());
							if(dependsOnList2.size() == 0) {
								flag = true;
							}
							else if(dependsOnList2.size() == 1) {
								if(!dependsOnList2.get(0).isAggregatePackagePair()) {
									flag = true;
								}
							}
							else if(dependsOnList2.size() == 2) {
								DependsOn dependsOn2_1 = dependsOnList2.get(0);
								DependsOn dependsOn2_2 = dependsOnList2.get(1);
								if(dependsOn2_1.getTimes() == dependsOn2_2.getTimes()) {
									flag = true;
								}
							}
						}
						else {
							flag = true;
						}
						if(flag) {
							if(dependsOn1_1.isAggregatePackagePair()) {
								dependsOnList1.remove(dependsOn1_1);
							}
							else {
								dependsOnList1.remove(dependsOn1_2);
							}
						}
					}
				}
				for(DependsOn dependsOn : dependsOnList1) {
					dependsOn.getDependsOnTypes().forEach((type, times) -> {
						Double weight = RelationType.relationWeights.get(RelationType.valueOf(type));
						if(weight != null){
							BigDecimal weightedTimes  =  new BigDecimal(times * weight);
							dependsOn.addWeightedTimes(weightedTimes.setScale(2, RoundingMode.HALF_UP).doubleValue());
						}
					});
					if(!dependsOn.isAggregatePackagePair()) {
						moduleDependsOnList.add(dependsOn);
					}
					else {
						aggregationDependsOnList.add(dependsOn);
					}
				}
			}
		}
		result.put(RelationType.str_DEPENDS_ON, moduleDependsOnList);
		result.put(RelationType.str_AGGREGATION_DEPENDS_ON, aggregationDependsOnList);
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public List<HotspotPackagePair> getHotspotPackagePairWithDependsOn() {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<HotspotPackagePair> allHotspotPackagePair = new ArrayList<>();
		Map<String, Map<Boolean, Integer>> hotspotPackagePairMap = new HashMap<>();
		List<String> keyList = new ArrayList<>();
		List<DependsOn> moduleDependsOnList = dependsOnRepository.findModuleDependsOn();
		List<AggregationDependsOn> aggregationDependsOnList = aggregationDependsOnRepository.findAggregationDependsOn();
		//将DependsOn原始数据转化为HotspotPackagePair
		if(moduleDependsOnList != null && !moduleDependsOnList.isEmpty()) {
			Map<Package, Map<Package, List<DependsOn>>> moduleDependsOnMap = new HashMap<>();
			for(DependsOn moduleDependsOn : moduleDependsOnList){
				Package startNode = (Package) moduleDependsOn.getStartNode();
				Package endNode = (Package) moduleDependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, List<DependsOn>> dependsOnMap = moduleDependsOnMap.getOrDefault(pck1, new HashMap<>());
				List<DependsOn> dependsOnList = dependsOnMap.getOrDefault(pck2, new ArrayList<>());
				dependsOnList.add(moduleDependsOn);
				dependsOnMap.put(pck2, dependsOnList);
				moduleDependsOnMap.put(pck1, dependsOnMap);
			}
			for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : moduleDependsOnMap.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, List<DependsOn>> dependsOnMap = entry.getValue();
				for(Map.Entry<Package, List<DependsOn>> entryKey : dependsOnMap.entrySet()){
					Package pck2 = entryKey.getKey();
					List<DependsOn> dependsOnList = dependsOnMap.getOrDefault(pck2, new ArrayList<>());
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithDependsOn(pck1, pck2, dependsOnList, new ArrayList<>());
					allHotspotPackagePair.add(hotspotPackagePair);
				}
			}
		}
		//将DependsOn聚合数据转化为HotspotPackagePair
		if(aggregationDependsOnList != null && !aggregationDependsOnList.isEmpty()) {
			Map<Package, Map<Package, List<AggregationDependsOn>>> aggregationDependsOnMap = new HashMap<>();
			for(AggregationDependsOn aggregationDependsOn : aggregationDependsOnList){
				Package startNode = (Package) aggregationDependsOn.getStartNode();
				Package endNode = (Package) aggregationDependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, List<AggregationDependsOn>> dependsOnMap = aggregationDependsOnMap.getOrDefault(pck1, new HashMap<>());
				List<AggregationDependsOn> dependsOnList = dependsOnMap.getOrDefault(pck2, new ArrayList<>());
				dependsOnList.add(aggregationDependsOn);
				dependsOnMap.put(pck2, dependsOnList);
				aggregationDependsOnMap.put(pck1, dependsOnMap);
			}
			for(Map.Entry<Package, Map<Package, List<AggregationDependsOn>>> entry : aggregationDependsOnMap.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, List<AggregationDependsOn>> dependsOnMap = entry.getValue();
				for(Map.Entry<Package, List<AggregationDependsOn>> entryKey : dependsOnMap.entrySet()){
					Package pck2 = entryKey.getKey();
					List<AggregationDependsOn> dependsOnList = dependsOnMap.getOrDefault(pck2, new ArrayList<>());
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithDependsOn(pck1, pck2, new ArrayList<>(), dependsOnList);
					allHotspotPackagePair.add(hotspotPackagePair);
				}
			}
		}
		//制作索引
		int index = 0;
		for(HotspotPackagePair hotspotPackagePair : allHotspotPackagePair) {
			Package pck1 = hotspotPackagePair.getPackage1();
			Package pck2 = hotspotPackagePair.getPackage2();
			String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
			Map<Boolean, Integer> booleanMap = hotspotPackagePairMap.getOrDefault(key, new HashMap<>());
			if(hotspotPackagePair.isAggregatePackagePair()) {
				booleanMap.put(true, index);
			}
			else {
				booleanMap.put(false, index);
			}
			hotspotPackagePairMap.put(key, booleanMap);
			index ++;
		}
		//加载父子关系
		for(HotspotPackagePair currentHotspotPackagePair : allHotspotPackagePair) {
			Package currentPackage1 = currentHotspotPackagePair.getPackage1();
			Package currentPackage2 = currentHotspotPackagePair.getPackage2();
			String currentKey = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(keyList.contains(currentKey)) {
				continue;
			}
			HotspotPackagePair hotspotPackagePair = currentHotspotPackagePair;
			Map<Boolean, Integer> booleanMap = hotspotPackagePairMap.get(currentKey);
			if(booleanMap.containsKey(true)) {
				if(booleanMap.containsKey(false)) {
					allHotspotPackagePair.get(booleanMap.get(true)).addHotspotChild(allHotspotPackagePair.get(booleanMap.get(false)));
				}
				hotspotPackagePair = allHotspotPackagePair.get(booleanMap.get(true));
			}
			Package pck1 = containRelationService.findPackageInPackage(currentPackage1);
			Package pck2 = containRelationService.findPackageInPackage(currentPackage2);
			if(!currentPackage1.getDirectoryPath().contains(currentPackage2.getDirectoryPath()) && !currentPackage2.getDirectoryPath().contains(currentPackage1.getDirectoryPath()) && pck1 != null && pck2 != null && !pck1.getId().equals(pck2.getId())) {
				//错位
				if(pck1.getDirectoryPath().contains(pck2.getDirectoryPath())) {
					pck2 = currentPackage2;
				}
				else if(pck2.getDirectoryPath().contains(pck1.getDirectoryPath())) {
					pck1 = currentPackage1;
				}
				Package parentPackage1 = pck1.getId() < pck2.getId() ? pck1 : pck2;
				Package parentPackage2 = pck1.getId() < pck2.getId() ? pck2 : pck1;
				String parentKey = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(hotspotPackagePairMap.containsKey(parentKey)) {
					if (hotspotPackagePairMap.get(parentKey).containsKey(true)) {
						allHotspotPackagePair.get(hotspotPackagePairMap.get(parentKey).get(true)).addHotspotChild(hotspotPackagePair);
					}
				}
			}
			else {
				result.add(hotspotPackagePair);
			}
			keyList.add(currentKey);
		}
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnByProjectId(long projectId) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<DependsOn> projectDependsOn = dependsOnRepository.findPackageDependsInProject(projectId);
		if(projectDependsOn != null && !projectDependsOn.isEmpty()){
			Map<Package, Map<Package, List<DependsOn>>> packageDependsPackage = new HashMap<>();
			for (DependsOn dependsOn : projectDependsOn){
				Package startNode = (Package) dependsOn.getStartNode();
				Package endNode = (Package) dependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, List<DependsOn>> dependsPackage = packageDependsPackage.getOrDefault(pck1, new HashMap<>());
				List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
				dependsOns.add(dependsOn);
				dependsPackage.put(pck2, dependsOns);
				packageDependsPackage.put(pck1, dependsPackage);
			}
			for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : packageDependsPackage.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, List<DependsOn>> dependsPackage = entry.getValue();
				for(Map.Entry<Package, List<DependsOn>> entryKey : dependsPackage.entrySet()){
					Package pck2 = entryKey.getKey();
					List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithDependsOn(pck1, pck2, dependsOns, new ArrayList<>());
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithDependsOnByPackageId(long pck1Id, long pck2Id) {
		List<DependsOn> packageDependsOnList = dependsOnRepository.findPackageDependsByPackageId(pck1Id, pck2Id);
		HotspotPackagePair hotspotPackagePair = null;
		if(packageDependsOnList != null && !packageDependsOnList.isEmpty()){
			Package tmp1 = (Package) packageDependsOnList.get(0).getStartNode();
			Package tmp2 = (Package) packageDependsOnList.get(0).getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;
			hotspotPackagePair = createHotspotPackagePairWithDependsOn(pck1, pck2, packageDependsOnList, new ArrayList<>());
		}
		return hotspotPackagePair;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithFileClone() {
		Map<Long, Integer> directoryIdToAllNodes = new HashMap<>();
		Map<Long, Integer> directoryIdToAllLoc = new HashMap<>();
		Map<String, HotspotPackagePair> directoryPathToHotspotPackagePair = new HashMap<>();
		Map<String, Collection<String>> directoryPathToCloneChildrenPackages = new HashMap<>();
		Map<String, Set<ProjectFile>> directoryPathToAllCloneChildrenPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<BasicDataForDoubleNodes<Node, Relation>> packageCloneList = summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageCloneList) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			Package parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				Collection<String> cloneChildren = new ArrayList<>();
				if(!directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					directoryPathToCloneChildrenPackages.put(parentPackages, cloneChildren);
				}
				cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
				if(cloneChildren.contains(currentPackages)) {
					break;
				}
				cloneChildren.add(currentPackages);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			}
		}

		//检测
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageCloneList) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(isHotspot.containsKey(currentPackages)) {
				continue;
			}

			HotspotPackagePair currentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, currentPackage1, currentPackage2);
			currentHotspotPackagePair.setPackagePairRelationData(packageClone);
			String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
			Set<ProjectFile> cloneChildrenFiles1 = new HashSet<>();
			Set<ProjectFile> cloneChildrenFiles2 = new HashSet<>();
			Set<Node> nodesInNode1 = new HashSet<>(packageClone.getNodesInNode1());
			Set<Node> nodesInNode2 = new HashSet<>(packageClone.getNodesInNode2());
			for(Node cloneNode1 : nodesInNode1) {
				cloneChildrenFiles1.add((ProjectFile) cloneNode1);
			}
			for(Node cloneNode2 : nodesInNode2) {
				cloneChildrenFiles2.add((ProjectFile) cloneNode2);
			}
			directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenFiles1);
			directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenFiles2);
			Collection<String> cloneChildren = new ArrayList<>();
			if(directoryPathToCloneChildrenPackages.containsKey(currentPackages)) {
				cloneChildren = directoryPathToCloneChildrenPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			isHotspot.put(currentPackages, isHotspotPackagePairWithFileClone(aggregator, directoryIdToAllNodes, directoryIdToAllLoc, directoryPathToAllCloneChildrenPackages, currentHotspotPackagePair));
			isChild.put(currentPackages, false);
			String parentPackages;
			HotspotPackagePair parentHotspotPackagePair;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				currentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, currentPackage1, currentPackage2);
				parentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, parentPackage1, parentPackage2);
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(isHotspot.containsKey(parentPackages)) {
					break;
				}
				cloneChildren = new ArrayList<>();
				if(directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					parentHotspotPackagePair.addHotspotChild(currentHotspotPackagePair);
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				isHotspot.put(parentPackages, isHotspotPackagePairWithFileClone(aggregator, directoryIdToAllNodes, directoryIdToAllLoc, directoryPathToAllCloneChildrenPackages, parentHotspotPackagePair));
				if(isHotspot.get(parentPackages)) {
					isChild.put(currentPackages, true);
				}
				isChild.put(parentPackages, false);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			}
		}

		//确定根目录
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackagePair> entry : directoryPathToHotspotPackagePair.entrySet()) {
			String currentPackages = entry.getKey();
			HotspotPackagePair hotspotPackagePair = entry.getValue();
			Package currentPackage1 = hotspotPackagePair.getPackage1();
			Package currentPackage2 = hotspotPackagePair.getPackage2();
			if((!isChild.get(currentPackages) && isHotspot.get(currentPackages))) {
				Package parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
				Package parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
				if(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
					String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
					if((isHotspot.containsKey(parentPackages) && isHotspot.get(parentPackages))) {
						isChild.put(currentPackages, true);
						continue;
					}
				}
				if(!result.contains(hotspotPackagePair)) {
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair getHotspotPackagePairWithFileCloneByPackageId(long pck1Id, long pck2Id, String language) {
		AggregationClone aggregationClone = aggregationCloneRepository.findAggregationCloneByPackageId(pck1Id, pck2Id);
		return setDateHotspotPackagePairWithFileClone(aggregationClone, language);
	}

	@Override
	public List<HotspotPackagePair> getHotspotPackagePairWithFileCloneByParentId(long parent1Id, long parent2Id, String language) {
		List<HotspotPackagePair> result = new ArrayList<>();
		if(language.equals("all")) {
			result.addAll(loadHotspotPackagePairWithFileClone(parent1Id, parent2Id, "java"));
			result.addAll(loadHotspotPackagePairWithFileClone(parent1Id, parent2Id, "cpp"));
		}
		else {
			result.addAll(loadHotspotPackagePairWithFileClone(parent1Id, parent2Id, language));
		}
		return result;
	}

	@Override
	public void exportHotspotPackages(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		setSheetInformation(hwb, "java");
		setSheetInformation(hwb, "cpp");
		try {
			hwb.write(stream);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stream.close();
				hwb.close();
			}
			catch (IOException ignored) {
			}
		}
	}

	private void setSheetInformation(Workbook hwb, String language) {
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Collection<HotspotPackagePair> hotspotPackagePairs = getHotspotPackagePairWithFileCloneByParentId(-1 ,-1, language);
		Sheet sheet = hwb.createSheet(language);
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		CellStyle style = hwb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("目录1");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("目录2");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("克隆文件占比");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("克隆CoChange占比");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("克隆Loc占比");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("克隆相似度");
		cell = row.createCell(6);
		cell.setCellValue("type");
		cell = row.createCell(7);
		cell.setCellValue("克隆文件对数");
		cell.setCellStyle(style);
		for(HotspotPackagePair hotspotPackagePair : hotspotPackagePairs){
			loadHotspotPackageResult(sheet, rowKey, 0, hotspotPackagePair);
		}
	}

	private void loadHotspotPackageResult(Sheet sheet, ThreadLocal<Integer> rowKey, int layer, HotspotPackagePair currentHotspotPackagePair){
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			prefix.append("|---");
		}
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		int clonePairs = currentPackagePairCloneRelationData.getClonePairs();
		int cloneNodesCount1 = currentPackagePairCloneRelationData.getCloneNodesCount1();
		int cloneNodesCount2 = currentPackagePairCloneRelationData.getCloneNodesCount2();
		int allNodesCount1 = currentPackagePairCloneRelationData.getAllNodesCount1();
		int allNodesCount2 = currentPackagePairCloneRelationData.getAllNodesCount2();
		double cloneMatchRate = currentPackagePairCloneRelationData.getCloneMatchRate();
		int cloneNodesLoc1 = currentPackagePairCloneRelationData.getCloneNodesLoc1();
		int cloneNodesLoc2 = currentPackagePairCloneRelationData.getCloneNodesLoc2();
		int allNodesLoc1 = currentPackagePairCloneRelationData.getAllNodesLoc1();
		int allNodesLoc2 = currentPackagePairCloneRelationData.getAllNodesLoc2();
		double cloneLocRate = currentPackagePairCloneRelationData.getCloneLocRate();
		int cloneNodesCoChangeTimes = currentPackagePairCloneRelationData.getCloneNodesCoChangeTimes();
		int allNodesCoChangeTimes = currentPackagePairCloneRelationData.getAllNodesCoChangeTimes();
		double cloneCoChangeRate = currentPackagePairCloneRelationData.getCloneCoChangeRate();
		int cloneType1Count = currentPackagePairCloneRelationData.getCloneType1Count();
		int cloneType2Count = currentPackagePairCloneRelationData.getCloneType2Count();
		int cloneType3Count = currentPackagePairCloneRelationData.getCloneType3Count();
		String cloneType = currentPackagePairCloneRelationData.getCloneType();
		double cloneSimilarityValue = currentPackagePairCloneRelationData.getCloneSimilarityValue();
		double cloneSimilarityRate = currentPackagePairCloneRelationData.getCloneSimilarityRate();

		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + currentHotspotPackagePair.getPackage1().getDirectoryPath());
		row.createCell(1).setCellValue(prefix + currentHotspotPackagePair.getPackage2().getDirectoryPath());
		row.createCell(2).setCellValue("(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + String .format("%.2f", cloneMatchRate));
		row.createCell(3).setCellValue(cloneNodesCoChangeTimes + "/" + allNodesCoChangeTimes + "=" + String .format("%.2f", cloneCoChangeRate));
		row.createCell(4).setCellValue("(" + cloneNodesLoc1 + "+" + cloneNodesLoc2 + ")/(" + allNodesLoc1 + "+" + allNodesLoc2 + ")=" + String .format("%.2f", cloneLocRate));
		row.createCell(5).setCellValue(String .format("%.2f", cloneSimilarityValue) + "/(" + cloneType1Count + "+" + cloneType2Count + "+" + cloneType3Count + ")=" + String .format("%.2f", cloneSimilarityRate));
		row.createCell(6).setCellValue(cloneType);
		row.createCell(7).setCellValue(clonePairs);

		for (HotspotPackagePair childHotspotPackagePair : currentHotspotPackagePair.getChildrenHotspotPackagePairs()){
			loadHotspotPackageResult(sheet, rowKey, layer + 1, childHotspotPackagePair);
		}
		for (Package packageChild1 : currentHotspotPackagePair.getChildrenOtherPackages1()){
			printOtherPackage(sheet, rowKey, -1, layer + 1, packageChild1);
		}
		for (Package packageChild2:currentHotspotPackagePair.getChildrenOtherPackages2()){
			printOtherPackage(sheet, rowKey, 1, layer + 1, packageChild2);
		}
	}
	private void printOtherPackage(Sheet sheet, ThreadLocal<Integer> rowKey, int index, int layer, Package otherPackage){
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			prefix.append("|---");
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		if(index == -1) {
			row.createCell(0).setCellValue(prefix + otherPackage.getDirectoryPath());
			row.createCell(1).setCellValue("0/" + otherPackage.getNof() + "=0.00");
			row.createCell(2).setCellValue("");
			row.createCell(3).setCellValue("");
			row.createCell(4).setCellValue("");
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue("");
		}
		if(index == 1) {
			row.createCell(0).setCellValue("");
			row.createCell(1).setCellValue("");
			row.createCell(2).setCellValue(prefix + otherPackage.getDirectoryPath());
			row.createCell(3).setCellValue("0/" + otherPackage.getNof() + "=0.00");
			row.createCell(4).setCellValue("");
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue("");
		}
	}

	private HotspotPackagePair createHotspotPackagePairWithDependsOn(Package pck1, Package pck2, List<DependsOn> moduleDependsOnList, List<AggregationDependsOn> aggregationDependsOnList) {
		StringBuilder dependsOnStr = new StringBuilder();
		StringBuilder dependsByStr = new StringBuilder();
		int dependsOnTimes = 0;
		int dependsByTimes = 0;
		double dependsOnWeightedTimes = 0.0;
		double dependsByWeightedTimes = 0.0;
		Map<String, Long> dependsOnTypesMap = new HashMap<>();
		Map<String, Long> dependsByTypesMap = new HashMap<>();
		if(!moduleDependsOnList.isEmpty()) {
			for(DependsOn dependsOn : moduleDependsOnList){
				if(dependsOn.getStartNode().getId().equals(pck1.getId())){
					dependsOnStr.append(dependsOn.getDependsOnType());
					dependsOnTimes += dependsOn.getTimes();
					dependsOnWeightedTimes += dependsOn.getWeightedTimes();
					dependsOnTypesMap.putAll(dependsOn.getDependsOnTypes());
				}
				else {
					dependsByStr.append(dependsOn.getDependsOnType());
					dependsByTimes += dependsOn.getTimes();
					dependsByWeightedTimes += dependsOn.getWeightedTimes();
					dependsByTypesMap.putAll(dependsOn.getDependsOnTypes());
				}
			}
		}
		else if(!aggregationDependsOnList.isEmpty()) {
			for(AggregationDependsOn aggregationDependsOn : aggregationDependsOnList){
				if(aggregationDependsOn.getStartNode().getId().equals(pck1.getId())){
					dependsOnStr.append(aggregationDependsOn.getDependsOnType());
					dependsOnTimes += aggregationDependsOn.getTimes();
					dependsOnWeightedTimes += aggregationDependsOn.getWeightedTimes();
					dependsOnTypesMap.putAll(aggregationDependsOn.getDependsOnTypes());
				}
				else {
					dependsByStr.append(aggregationDependsOn.getDependsOnType());
					dependsByTimes += aggregationDependsOn.getTimes();
					dependsByWeightedTimes += aggregationDependsOn.getWeightedTimes();
					dependsByTypesMap.putAll(aggregationDependsOn.getDependsOnTypes());
				}
			}
		}
		DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = new DependsRelationDataForDoubleNodes<>(pck1, pck2);
		dependsRelationDataForDoubleNodes.setDependsOnTypes(dependsOnStr.toString());
		dependsRelationDataForDoubleNodes.setDependsByTypes(dependsByStr.toString());
		dependsRelationDataForDoubleNodes.setDependsOnTimes(dependsOnTimes);
		dependsRelationDataForDoubleNodes.setDependsByTimes(dependsByTimes);
		dependsRelationDataForDoubleNodes.setDependsOnTypesMap(dependsOnTypesMap);
		dependsRelationDataForDoubleNodes.setDependsByTypesMap(dependsByTypesMap);
		dependsRelationDataForDoubleNodes.setDependsOnWeightedTimes(dependsOnWeightedTimes);
		dependsRelationDataForDoubleNodes.setDependsByWeightedTimes(dependsByWeightedTimes);
		dependsRelationDataForDoubleNodes.calDependsOnIntensity();
		dependsRelationDataForDoubleNodes.calDependsByIntensity();
		HotspotPackagePair result = new HotspotPackagePair(dependsRelationDataForDoubleNodes);
		if(!moduleDependsOnList.isEmpty()) {
			result.setAggregatePackagePair(false);
		}
		else if(!aggregationDependsOnList.isEmpty()) {
			result.setAggregatePackagePair(true);
		}
		return result;
	}

	//寻找HotspotPackage
	private HotspotPackagePair findHotspotPackage(Collection<? extends Relation> fileClones, Map<String, HotspotPackagePair> directoryPathToHotspotPackage, Package pck1, Package pck2) {
		String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		if(!directoryPathToHotspotPackage.containsKey(key)) {
			HotspotPackagePair hotspotPackagePair;
			BasicDataForDoubleNodes<Node, Relation> packageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
			if(packageClone != null) {
				hotspotPackagePair = new HotspotPackagePair(packageClone);
				if(hotspotPackagePair.getPackage1().getDirectoryPath().equals(pck2.getDirectoryPath()) && hotspotPackagePair.getPackage2().getDirectoryPath().equals(pck1.getDirectoryPath())) {
					hotspotPackagePair.swapPackages();
				}
			}
			else {
				hotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(pck1, pck2));
			}
			directoryPathToHotspotPackage.put(key, hotspotPackagePair);
		}
		return directoryPathToHotspotPackage.get(key);
	}

	//判断是否为包含文件的分支结点
	private boolean isBranchPackageWithFiles(Package pck) {
		return containRelationService.findPackageContainPackages(pck).size() > 0 && containRelationService.findPackageContainFiles(pck).size() > 0;
	}

	//判断是否为叶子结点
	private boolean isLeafPackage(Package pck) {
		return containRelationService.findPackageContainPackages(pck).size() == 0;
	}

	//包下有文件也有包时，为包下文件创建一个包
	private Package buildPackageForFiles(Package pck) {
		String childDirectoryPath = pck.getDirectoryPath() + "./";
		Package childPackage = new Package();
		childPackage.setId(pck.getId());
		childPackage.setEntityId(pck.getEntityId());
		childPackage.setDirectoryPath(childDirectoryPath);
		childPackage.setName(childDirectoryPath);
		childPackage.setLanguage(pck.getLanguage());
		return childPackage;
	}

	//获取包下文件总数
	private int getAllFilesNum(Map<Long, Integer> directoryIdToAllNodes, Package pck) {
		if(directoryIdToAllNodes.containsKey(pck.getId())) {
			return directoryIdToAllNodes.get(pck.getId());
		}
		int number = containRelationService.findPackageContainFiles(pck).size();
		Collection<Package> childrenPackages = containRelationService.findPackageContainPackages(pck);
		for(Package childPackage : childrenPackages) {
			number += getAllFilesNum(directoryIdToAllNodes, childPackage);
		}
		directoryIdToAllNodes.put(pck.getId(), number);
		return number;
	}

	//获取包下文件代码行
	private int getAllFilesLoc(Map<Long, Integer> directoryIdToAllLoc, Package pck) {
		if(directoryIdToAllLoc.containsKey(pck.getId())) {
			return directoryIdToAllLoc.get(pck.getId());
		}
		int Loc = 0;
		Collection<ProjectFile> allChildrenFiles = new ArrayList<>(containRelationService.findPackageContainFiles(pck));
		for(ProjectFile allChildFile : allChildrenFiles) {
			Loc += allChildFile.getLoc();
		}
		Collection<Package> childrenPackages = containRelationService.findPackageContainPackages(pck);
		for(Package childPackage : childrenPackages) {
			Loc += getAllFilesLoc(directoryIdToAllLoc, childPackage);
		}
		directoryIdToAllLoc.put(pck.getId(), Loc);
		return Loc;
	}

	//判断是否符合聚合条件
	private boolean isHotspotPackagePairWithFileClone(RelationAggregator<Boolean> aggregator, Map<Long, Integer> directoryIdToAllNodes, Map<Long, Integer> directoryIdToAllLoc, Map<String, Set<ProjectFile>> directoryPathToAllCloneChildrenPackages, HotspotPackagePair currentHotspotPackagePair) {
		Package currentPackage1 = currentHotspotPackagePair.getPackage1();
		Package currentPackage2 = currentHotspotPackagePair.getPackage2();
		String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
		String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			return aggregator.aggregate(currentPackagePairCloneRelationData);
		}

		Set<ProjectFile> cloneChildrenFiles1 = new HashSet<>();
		Set<ProjectFile> cloneChildrenFiles2 = new HashSet<>();
		//包下另有文件情况
		if(isBranchPackageWithFiles(currentPackage1) || isBranchPackageWithFiles(currentPackage2)) {
			if(directoryPathToAllCloneChildrenPackages.containsKey(path1)) {
				cloneChildrenFiles1.addAll(directoryPathToAllCloneChildrenPackages.get(path1));
			}
			if(directoryPathToAllCloneChildrenPackages.containsKey(path2)) {
				cloneChildrenFiles2.addAll(directoryPathToAllCloneChildrenPackages.get(path2));
			}
		}
		//遍历包下子包
		Collection<HotspotPackagePair> childrenHotspotPackagePairs = currentHotspotPackagePair.getChildrenHotspotPackagePairs();
		Collection<Package> childrenPackage1 = containRelationService.findPackageContainPackages(currentPackage1);
		Collection<Package> childrenPackage2 = containRelationService.findPackageContainPackages(currentPackage2);
		for(HotspotPackagePair childHotspotPackagePair : childrenHotspotPackagePairs) {
			Package childPackage1 = childHotspotPackagePair.getPackage1();
			Package childPackage2 = childHotspotPackagePair.getPackage2();
			String childPath1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
			String childPath2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
			cloneChildrenFiles1.addAll(directoryPathToAllCloneChildrenPackages.get(childPath1));
			cloneChildrenFiles2.addAll(directoryPathToAllCloneChildrenPackages.get(childPath2));
			childrenPackage1.remove(childPackage1);
			childrenPackage2.remove(childPackage2);
		}
		for(Package otherChild1 : childrenPackage1) {
			currentHotspotPackagePair.addOtherChild1(otherChild1);
		}
		for(Package otherChild2 : childrenPackage2) {
			currentHotspotPackagePair.addOtherChild2(otherChild2);
		}
		int allNodes1 = getAllFilesNum(directoryIdToAllNodes, currentPackage1);
		int allNodes2 = getAllFilesNum(directoryIdToAllNodes, currentPackage2);
		int cloneNodes1 = cloneChildrenFiles1.size();
		int cloneNodes2 = cloneChildrenFiles2.size();
		int cloneNodesLoc1 = 0;
		int cloneNodesLoc2 = 0;
		for(ProjectFile cloneChildFile1 : cloneChildrenFiles1) {
			cloneNodesLoc1 += cloneChildFile1.getLoc();
		}
		for(ProjectFile cloneChildFile2 : cloneChildrenFiles2) {
			cloneNodesLoc2 += cloneChildFile2.getLoc();
		}
		int allNodesLoc1 = getAllFilesLoc(directoryIdToAllLoc, currentPackage1);
		int allNodesLoc2 = getAllFilesLoc(directoryIdToAllLoc, currentPackage2);
		directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenFiles1);
		directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenFiles2);

		//加载数据
		currentPackagePairCloneRelationData.setCloneCountDate(cloneNodes1, cloneNodes2, allNodes1, allNodes2);
		currentPackagePairCloneRelationData.setCloneLocDate(cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2);
		currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
		return aggregator.aggregate(currentPackagePairCloneRelationData);
	}

	//设置HotspotPackage信息++
	public HotspotPackagePair setDateHotspotPackagePairWithFileClone(AggregationClone aggregationClone, String language) {
		Package currentPackage1 = (Package) aggregationClone.getStartNode();
		Package currentPackage2 = (Package) aggregationClone.getEndNode();
		HotspotPackagePair currentHotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(currentPackage1, currentPackage2));
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();
		int cloneNodesCoChangeTimes = 0;
		int allNodesCoChangeTimes = 0;
		ModuleClone packageCloneCoChanges = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
		CoChange packageCoChanges = coChangeRepository.findPackageCoChangeByPackageId(currentPackage1.getId(), currentPackage2.getId());
		if(packageCloneCoChanges != null) {
			cloneNodesCoChangeTimes = packageCloneCoChanges.getCloneNodesCoChangeTimes();
		}
		if(packageCoChanges != null) {
			allNodesCoChangeTimes = packageCoChanges.getTimes();
		}

		//加载数据
		currentPackagePairCloneRelationData.setClonePairs(aggregationClone.getClonePairs());
		currentPackagePairCloneRelationData.setCloneCountDate(aggregationClone.getCloneNodesCount1(), aggregationClone.getCloneNodesCount2(), aggregationClone.getAllNodesCount1(), aggregationClone.getAllNodesCount2());
		currentPackagePairCloneRelationData.setCloneLocDate(aggregationClone.getCloneNodesLoc1(), aggregationClone.getCloneNodesLoc2(), aggregationClone.getAllNodesLoc1(), aggregationClone.getAllNodesLoc2());
		currentPackagePairCloneRelationData.setCoChangeTimesData(cloneNodesCoChangeTimes, allNodesCoChangeTimes);
		currentPackagePairCloneRelationData.setCloneTypeDate(aggregationClone.getCloneType1Count(), aggregationClone.getCloneType2Count(), aggregationClone.getCloneType3Count(), aggregationClone.getCloneSimilarityValue());
		List<HotspotPackagePair> childrenHotspotPackagePairs = new ArrayList<>();

		//根据语言，选择加载内容
		if(language.equals("all")) {
			childrenHotspotPackagePairs.addAll(loadHotspotPackagePairWithFileClone(currentPackage1.getId(), currentPackage2.getId(), "java"));
			childrenHotspotPackagePairs.addAll(loadHotspotPackagePairWithFileClone(currentPackage1.getId(), currentPackage2.getId(), "cpp"));
		}
		else {
			childrenHotspotPackagePairs.addAll(loadHotspotPackagePairWithFileClone(currentPackage1.getId(), currentPackage2.getId(), language));
		}

		//当克隆包下存在基础的克隆子包时才做展示，否则，将克隆包认为是基础的克隆包，不展示包下的非克隆子包
		if(childrenHotspotPackagePairs.size() > 0) {
			Collection<Package> childrenPackages1 = containRelationService.findPackageContainPackages(currentHotspotPackagePair.getPackage1());
			Collection<Package> childrenPackages2 = containRelationService.findPackageContainPackages(currentHotspotPackagePair.getPackage2());

			//将有子包的包下文件打包
			if(isBranchPackageWithFiles(currentPackage1) && isBranchPackageWithFiles(currentPackage2)) {
				ModuleClone moduleClone = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
				Package childPackage1 = buildPackageForFiles(currentPackage1);
				Package childPackage2 = buildPackageForFiles(currentPackage2);
				if(moduleClone != null) {
					HotspotPackagePair childHotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(childPackage1, childPackage2));
					CloneRelationDataForDoubleNodes<Node, Relation> childPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) childHotspotPackagePair.getPackagePairRelationData();
					childPackagePairCloneRelationData.setClonePairs(moduleClone.getClonePairs());
					childPackagePairCloneRelationData.setCloneCountDate(moduleClone.getCloneNodesCount1(), moduleClone.getCloneNodesCount2(), moduleClone.getAllNodesCount1(), moduleClone.getAllNodesCount2());
					childPackagePairCloneRelationData.setCloneLocDate(aggregationClone.getCloneNodesLoc1(), aggregationClone.getCloneNodesLoc2(), aggregationClone.getAllNodesLoc1(), aggregationClone.getAllNodesLoc2());
					childPackagePairCloneRelationData.setCloneTypeDate(aggregationClone.getCloneType1Count(), aggregationClone.getCloneType2Count(), aggregationClone.getCloneType3Count(), aggregationClone.getCloneSimilarityValue());
					childPackagePairCloneRelationData.setCoChangeTimesData(cloneNodesCoChangeTimes, allNodesCoChangeTimes);
					childHotspotPackagePair.setPackagePairRelationData(childPackagePairCloneRelationData);
					childrenHotspotPackagePairs.add(childHotspotPackagePair);
					currentPackagePairCloneRelationData.setClonePairs(0);
				}
				else {
					childPackage1.setNof(containRelationService.findPackageContainFiles(currentPackage1).size());
					childPackage2.setNof(containRelationService.findPackageContainFiles(currentPackage2).size());
					childrenPackages1.add(childPackage1);
					childrenPackages2.add(childPackage2);
				}
			}

			//因为有可能加入了人工制造的子克隆包，所以需要重新进行排序
			resultSortHotspotPackagePairWithFileClone(childrenHotspotPackagePairs);

			//加载子克隆包
			for(HotspotPackagePair childHotspotPackagePair : childrenHotspotPackagePairs) {
				double childSimilarityValue = ((CloneRelationDataForDoubleNodes<Node, Relation>) childHotspotPackagePair.getPackagePairRelationData()).getCloneMatchRate();
				if(childSimilarityValue > 0.5 || childrenPackages1.contains(childHotspotPackagePair.getPackage1()) || childrenPackages1.contains(childHotspotPackagePair.getPackage2())) {
					currentHotspotPackagePair.addHotspotChild(childHotspotPackagePair);
					childrenPackages1.remove(childHotspotPackagePair.getPackage1());
					childrenPackages2.remove(childHotspotPackagePair.getPackage2());
				}
			}

			//加载子非克隆包
			for(Package childPackage1 : childrenPackages1) {
				currentHotspotPackagePair.addOtherChild1(childPackage1);
			}
			for(Package childPackage2 : childrenPackages2) {
				currentHotspotPackagePair.addOtherChild2(childPackage2);
			}
		}

		//加载数据
		currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
		return currentHotspotPackagePair;
	}

	//递归加载聚合结果
	public List<HotspotPackagePair> loadHotspotPackagePairWithFileClone(long parent1Id, long parent2Id, String language) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<AggregationClone> aggregationClones = aggregationCloneRepository.findAggregationCloneByParentId(parent1Id, parent2Id, language);
		for(AggregationClone aggregationClone : aggregationClones) {
			result.add(setDateHotspotPackagePairWithFileClone(aggregationClone, language));
		}

		//聚合排序，每一次均进行排序
		resultSortHotspotPackagePairWithFileClone(result);
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnInAllProjects(){
		List<Project> projects = nodeService.allProjects();
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagePairWithDependsOnByProjectId(project.getId()));
		}
		return result;
	}

	@Override
	public Map<String, List<CoChange>> detectHotspotPackagePairWithCoChange() {
		String key = "hotspotPackagePairWithCoChange";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<String, List<CoChange>> result = new HashMap<>();
		List<CoChange> fileCoChangeList = coChangeRepository.findFileCoChange();
		List<CoChange> moduleCoChangeList = new ArrayList<>();
		List<CoChange> aggregationCoChangeList = new ArrayList<>();
		Map<String, Boolean> isAggregationMap = new HashMap<>();
		if(fileCoChangeList != null && !fileCoChangeList.isEmpty()){
			Map<Package, Map<Package, Set<Commit>>> packageOriginalCommitMap = new HashMap<>();
			Map<Package, Map<Package, Set<Commit>>> packagesAggregateCommitMap = new HashMap<>();
			List<String> keyList = new ArrayList<>();
			//获取发生CoChange包以及其祖先包下的Commit列表
			for(CoChange fileCoChange : fileCoChangeList) {
				Package pck1 = coChangeRepository.findFileBelongPackageByFileId(fileCoChange.getStartNode().getId());
				Package pck2 = coChangeRepository.findFileBelongPackageByFileId(fileCoChange.getEndNode().getId());
				String pathKey = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
				if(!keyList.contains(pathKey) && isParentPackages(pck1, pck2)) {
					keyList.add(pathKey);
					Set<Commit> pck1OriginalCommitSet = new HashSet<>(commitUpdateFileRepository.findCommitInPackageByPackageId(pck1.getId()));
					Set<Commit> pck2OriginalCommitSet = new HashSet<>(commitUpdateFileRepository.findCommitInPackageByPackageId(pck2.getId()));
					Map<Package, Set<Commit>> pck1OriginalCommitMat = packageOriginalCommitMap.getOrDefault(pck1, new HashMap<>());
					Map<Package, Set<Commit>> pck2OriginalCommitMat = packageOriginalCommitMap.getOrDefault(pck2, new HashMap<>());
					pck1OriginalCommitMat.put(pck2, pck1OriginalCommitSet);
					pck2OriginalCommitMat.put(pck1, pck2OriginalCommitSet);
					packageOriginalCommitMap.put(pck1, pck1OriginalCommitMat);
					packageOriginalCommitMap.put(pck2, pck2OriginalCommitMat);
					boolean isAggregation = false;
					while(isParentPackages(pck1, pck2)) {
						Package currentPackage1 = pck1.getId() < pck2.getId() ? pck1 : pck2;
						Package currentPackage2 = pck1.getId() < pck2.getId() ? pck2 : pck1;
						String currentKey = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
						if(!isAggregationMap.containsKey(pathKey) || (isAggregationMap.containsKey(pathKey) && !isAggregationMap.get(pathKey))) {
							isAggregationMap.put(currentKey, isAggregation);
						}
						Map<Package, Set<Commit>> pck1AggregateCommitMat = packagesAggregateCommitMap.getOrDefault(currentPackage1, new HashMap<>());
						Map<Package, Set<Commit>> pck2AggregateCommitMat = packagesAggregateCommitMap.getOrDefault(currentPackage2, new HashMap<>());
						Set<Commit> pck1AggregateCommitSet = pck1AggregateCommitMat.getOrDefault(currentPackage2, new HashSet<>());
						Set<Commit> pck2AggregateCommitSet = pck2AggregateCommitMat.getOrDefault(currentPackage1, new HashSet<>());
						pck1AggregateCommitSet.addAll(pck1OriginalCommitSet);
						pck2AggregateCommitSet.addAll(pck2OriginalCommitSet);
						pck1AggregateCommitMat.put(currentPackage2, pck1AggregateCommitSet);
						pck2AggregateCommitMat.put(currentPackage1, pck2AggregateCommitSet);
						packagesAggregateCommitMap.put(currentPackage1, pck1AggregateCommitMat);
						packagesAggregateCommitMap.put(currentPackage2, pck2AggregateCommitMat);
						pck1 = containRelationService.findPackageInPackage(currentPackage1);
						pck2 = containRelationService.findPackageInPackage(currentPackage2);
						//错位
						if(!pck1.getDirectoryPath().equals(pck2.getDirectoryPath()) && pck1.getDirectoryPath().contains(pck2.getDirectoryPath())) {
							pck2 = currentPackage2;
						}
						else if(!pck2.getDirectoryPath().equals(pck1.getDirectoryPath()) && pck2.getDirectoryPath().contains(pck1.getDirectoryPath())) {
							pck1 = currentPackage1;
						}
						isAggregation = true;
					}
				}
			}
			keyList.clear();
			//基于包的Commit列表，创建CoChange关系
			for(CoChange fileCoChange : fileCoChangeList) {
				Package pck1 = coChangeRepository.findFileBelongPackageByFileId(fileCoChange.getStartNode().getId());
				Package pck2 = coChangeRepository.findFileBelongPackageByFileId(fileCoChange.getEndNode().getId());
				while(isParentPackages(pck1, pck2)) {
					Package currentPackage1 = pck1.getId() < pck2.getId() ? pck1 : pck2;
					Package currentPackage2 = pck1.getId() < pck2.getId() ? pck2 : pck1;
					String pathKey = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
					if(!keyList.contains(pathKey)) {
						keyList.add(pathKey);
						Set<Commit> currentPackage1OriginalCommitSet = packageOriginalCommitMap.getOrDefault(currentPackage1, new HashMap<>()).getOrDefault(currentPackage2, new HashSet<>());
						Set<Commit> currentPackage2OriginalCommitSet = packageOriginalCommitMap.getOrDefault(currentPackage2, new HashMap<>()).getOrDefault(currentPackage1, new HashSet<>());
						Set<Commit> currentPackage1AggregateCommitSet = packagesAggregateCommitMap.getOrDefault(currentPackage1, new HashMap<>()).getOrDefault(currentPackage2, new HashSet<>());
						Set<Commit> currentPackage2AggregateCommitSet = packagesAggregateCommitMap.getOrDefault(currentPackage2, new HashMap<>()).getOrDefault(currentPackage1, new HashSet<>());
						Set<Commit> currentPackagesOriginalCommitSet = new HashSet<>(currentPackage1OriginalCommitSet);
						currentPackagesOriginalCommitSet.retainAll(currentPackage2OriginalCommitSet);
						Set<Commit> currentPackagesAggregateCommitSet = new HashSet<>(currentPackage1AggregateCommitSet);
						currentPackagesAggregateCommitSet.retainAll(currentPackage2AggregateCommitSet);
						if(currentPackagesOriginalCommitSet.size() > 0) {
							CoChange moduleCoChange = new CoChange(currentPackage1, currentPackage2);
							moduleCoChange.setTimes(currentPackagesOriginalCommitSet.size());
							moduleCoChange.setNode1ChangeTimes(currentPackage1AggregateCommitSet.size());
							moduleCoChange.setNode2ChangeTimes(currentPackage2AggregateCommitSet.size());
							moduleCoChangeList.add(moduleCoChange);
						}
						if(isAggregationMap.get(pathKey) && currentPackagesAggregateCommitSet.size() > 0) {
							CoChange aggregationCoChange = new CoChange(currentPackage1, currentPackage2);
							aggregationCoChange.setTimes(currentPackagesAggregateCommitSet.size());
							aggregationCoChange.setNode1ChangeTimes(currentPackage1AggregateCommitSet.size());
							aggregationCoChange.setNode2ChangeTimes(currentPackage2AggregateCommitSet.size());
							aggregationCoChangeList.add(aggregationCoChange);
						}
						pck1 = containRelationService.findPackageInPackage(currentPackage1);
						pck2 = containRelationService.findPackageInPackage(currentPackage2);
						//错位
						if(!pck1.getDirectoryPath().equals(pck2.getDirectoryPath()) && pck1.getDirectoryPath().contains(pck2.getDirectoryPath())) {
							pck2 = currentPackage2;
						}
						else if(!pck2.getDirectoryPath().equals(pck1.getDirectoryPath()) && pck2.getDirectoryPath().contains(pck1.getDirectoryPath())) {
							pck1 = currentPackage1;
						}
					}
					else {
						break;
					}
				}
			}
		}
		result.put(RelationType.str_CO_CHANGE, moduleCoChangeList);
		result.put(RelationType.str_AGGREGATION_CO_CHANGE, aggregationCoChangeList);
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public List<HotspotPackagePair> getHotspotPackagePairWithCoChange() {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<HotspotPackagePair> allHotspotPackagePair = new ArrayList<>();
		List<String> keyList = new ArrayList<>();
		Map<String, Map<Boolean, Integer>> hotspotPackagePairMap = new HashMap<>();
		List<CoChange> moduleCoChangeList = coChangeRepository.findModuleCoChange();
		List<AggregationCoChange> aggregationCoChangeList = aggregationCoChangeRepository.findAggregationCoChange();
		//将CoChange原始数据转化为HotspotPackagePair
		if(moduleCoChangeList != null && !moduleCoChangeList.isEmpty()){
			for(CoChange moduleCoChange : moduleCoChangeList){
				Package startNode = (Package) moduleCoChange.getStartNode();
				Package endNode = (Package) moduleCoChange.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				allHotspotPackagePair.add(createHotspotPackagePairWithCoChange(pck1, pck2, moduleCoChange, null));
			}
		}
		//将CoChange聚合数据转化为HotspotPackagePair
		if(aggregationCoChangeList != null && !aggregationCoChangeList.isEmpty()){
			for(AggregationCoChange aggregationCoChange : aggregationCoChangeList){
				Package startNode = (Package) aggregationCoChange.getStartNode();
				Package endNode = (Package) aggregationCoChange.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				allHotspotPackagePair.add(createHotspotPackagePairWithCoChange(pck1, pck2, null, aggregationCoChange));
			}
		}
		//制作索引
		int index = 0;
		for(HotspotPackagePair hotspotPackagePair : allHotspotPackagePair) {
			Package pck1 = hotspotPackagePair.getPackage1();
			Package pck2 = hotspotPackagePair.getPackage2();
			String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
			Map<Boolean, Integer> booleanMap = hotspotPackagePairMap.getOrDefault(key, new HashMap<>());
			if(hotspotPackagePair.isAggregatePackagePair()) {
				booleanMap.put(true, index);
			}
			else {
				booleanMap.put(false, index);
			}
			hotspotPackagePairMap.put(key, booleanMap);
			index ++;
		}
		//加载父子关系
		for(HotspotPackagePair currentHotspotPackagePair : allHotspotPackagePair) {
			Package currentPackage1 = currentHotspotPackagePair.getPackage1();
			Package currentPackage2 = currentHotspotPackagePair.getPackage2();
			String currentKey = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(keyList.contains(currentKey)) {
				continue;
			}
			HotspotPackagePair hotspotPackagePair = currentHotspotPackagePair;
			Map<Boolean, Integer> booleanMap = hotspotPackagePairMap.get(currentKey);
			if(booleanMap.containsKey(true)) {
				if(booleanMap.containsKey(false)) {
					allHotspotPackagePair.get(booleanMap.get(true)).addHotspotChild(allHotspotPackagePair.get(booleanMap.get(false)));
				}
				hotspotPackagePair = allHotspotPackagePair.get(booleanMap.get(true));
			}
			Package pck1 = containRelationService.findPackageInPackage(currentPackage1);
			Package pck2 = containRelationService.findPackageInPackage(currentPackage2);
			if(!currentPackage1.getDirectoryPath().contains(currentPackage2.getDirectoryPath()) && !currentPackage2.getDirectoryPath().contains(currentPackage1.getDirectoryPath()) && pck1 != null && pck2 != null && !pck1.getId().equals(pck2.getId())) {
				//错位
				if(pck1.getDirectoryPath().contains(pck2.getDirectoryPath())) {
					pck2 = currentPackage2;
				}
				else if(pck2.getDirectoryPath().contains(pck1.getDirectoryPath())) {
					pck1 = currentPackage1;
				}
				Package parentPackage1 = pck1.getId() < pck2.getId() ? pck1 : pck2;
				Package parentPackage2 = pck1.getId() < pck2.getId() ? pck2 : pck1;
				String parentKey = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(hotspotPackagePairMap.containsKey(parentKey)) {
					if (hotspotPackagePairMap.get(parentKey).containsKey(true)) {
						allHotspotPackagePair.get(hotspotPackagePairMap.get(parentKey).get(true)).addHotspotChild(hotspotPackagePair);
					}
				}
			}
			else {
				result.add(hotspotPackagePair);
			}
			keyList.add(currentKey);
		}
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithCoChangeByProjectId(long projectId) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<CoChange> projectCoChanges = coChangeRepository.findPackageCoChangeInProject(projectId);
		if(projectCoChanges != null && !projectCoChanges.isEmpty()){
			Map<Package, Map<Package, CoChange>> packageCoChangePackage = new HashMap<>();
			for (CoChange coChange : projectCoChanges){
				Package startNode = (Package) coChange.getStartNode();
				Package endNode = (Package) coChange.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, CoChange> coChangePackage = packageCoChangePackage.getOrDefault(pck1, new HashMap<>());
				coChangePackage.put(pck2, coChange);
				packageCoChangePackage.put(pck1, coChangePackage);
			}
			for(Map.Entry<Package, Map<Package, CoChange>> entry : packageCoChangePackage.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, CoChange> coChangePackage = entry.getValue();
				for(Map.Entry<Package, CoChange> entryKey : coChangePackage.entrySet()){
					Package pck2 = entryKey.getKey();
					CoChange coChange = entryKey.getValue();
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithCoChange(pck1, pck2, coChange, null);
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithCoChangeByPackageId(long pck1Id, long pck2Id) {
		CoChange packageCoChange = coChangeRepository.findPackageCoChangeByPackageId(pck1Id, pck2Id);
		HotspotPackagePair hotspotPackagePair = null;
		if(packageCoChange != null){
			Package tmp1 = (Package) packageCoChange.getStartNode();
			Package tmp2 = (Package) packageCoChange.getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;
			hotspotPackagePair = createHotspotPackagePairWithCoChange(pck1, pck2, packageCoChange, null);
		}
		return hotspotPackagePair;
	}

	private HotspotPackagePair createHotspotPackagePairWithCoChange(Package pck1, Package pck2, CoChange moduleCoChange, AggregationCoChange aggregationCoChange) {
		CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = new CoChangeRelationDataForDoubleNodes<>(pck1, pck2);
		if(moduleCoChange != null) {
			coChangeRelationDataForDoubleNodes.setCoChangeTimes(moduleCoChange.getTimes());
			coChangeRelationDataForDoubleNodes.setNode1ChangeTimes(moduleCoChange.getNode1ChangeTimes());
			coChangeRelationDataForDoubleNodes.setNode2ChangeTimes(moduleCoChange.getNode2ChangeTimes());
		}
		else if(aggregationCoChange != null) {
			coChangeRelationDataForDoubleNodes.setCoChangeTimes(aggregationCoChange.getTimes());
			coChangeRelationDataForDoubleNodes.setNode1ChangeTimes(aggregationCoChange.getNode1ChangeTimes());
			coChangeRelationDataForDoubleNodes.setNode2ChangeTimes(aggregationCoChange.getNode2ChangeTimes());
		}
		HotspotPackagePair result = new HotspotPackagePair(coChangeRelationDataForDoubleNodes);
		if(moduleCoChange != null) {
			result.setAggregatePackagePair(false);
		}
		else if(aggregationCoChange != null) {
			result.setAggregatePackagePair(true);
		}
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithCoChangeInAllProjects() {
		List<Project> projects = nodeService.allProjects();
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagePairWithCoChangeByProjectId(project.getId()));
		}
		return result;
	}

	private void resultSortHotspotPackagePairWithFileClone(List<HotspotPackagePair> result) {
		result.sort((hotspotPackagePair1, hotspotPackagePair2) -> {
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData1 = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair1.getPackagePairRelationData();
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData2 = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair2.getPackagePairRelationData();
			int allNodes1 = packagePairCloneRelationData1.getAllNodesCount1() + packagePairCloneRelationData1.getAllNodesCount2();
			int cloneNodes1 = packagePairCloneRelationData1.getCloneNodesCount1() + packagePairCloneRelationData1.getCloneNodesCount2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = packagePairCloneRelationData2.getAllNodesCount1() + packagePairCloneRelationData2.getAllNodesCount2();
			int cloneNodes2 = packagePairCloneRelationData2.getCloneNodesCount1() + packagePairCloneRelationData2.getCloneNodesCount2();
			double percentageThreshold2 = (cloneNodes2 + 0.0) / allNodes2;
			if(percentageThreshold1 < percentageThreshold2) {
				return 1;
			}
			else if(percentageThreshold1 > percentageThreshold2) {
				return -1;
			}
			else {
				return Integer.compare(cloneNodes2, cloneNodes1);
			}
		});
	}
	private boolean isParentPackages(Package pck1, Package pck2) {
		if(pck1 == null || pck2 == null) {
			return false;
		}
		else {
			String rootPackageDirectoryPath1 = "/" + pck1.getDirectoryPath().split("/")[1] + "/";
			String rootPackageDirectoryPath2 = "/" + pck2.getDirectoryPath().split("/")[1] + "/";
			return !pck1.getDirectoryPath().equals(rootPackageDirectoryPath1) && !pck2.getDirectoryPath().equals(rootPackageDirectoryPath2) && !pck1.getId().equals(pck2.getId());
		}
	}
}
