package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
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
public class HotspotPackageDetectorImpl<ps> implements HotspotPackageDetector {

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private AggregationCloneRepository aggregationCloneRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;

	@Autowired
	private ModuleCloneRepository moduleCloneRepository;

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private NodeService nodeService;

	private ThreadLocal<Integer> rowKey = new ThreadLocal<>();

	@Override
	public List<HotspotPackage> detectHotspotPackages() {
		return detectHotspotPackagesByFileClone();
		//return detectHotspotPackagesByFileCloneLoc();
		//return detectHotspotPackagesByFileCoChange();
		//return detectHotspotPackagesByFileCoChangeTimes();
	}

	private List<HotspotPackage> aggregatePackageRelation(Collection<? extends Relation> subNodeRelations,
																Collection<RelationDataForDoubleNodes<Node, Relation>> packageRelations,
																RelationAggregator<Boolean> aggregator) {

		Map<String, Package> directoryPathToParentPackage = new HashMap<>();
		Map<String, HotspotPackage> idToPackageRelation = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		for (RelationDataForDoubleNodes<Node, Relation> packageRelation : packageRelations) {
			boolean isSimilar = (boolean) packageRelation.aggregateValue(aggregator);
			if (!isSimilar) {
				continue;
			}
			if (idToPackageRelation.get(packageRelation.getId()) != null) {
				continue;
			}
			HotspotPackage temp = new HotspotPackage(packageRelation);
			idToPackageRelation.put(temp.getId(), temp);
			isChild.put(temp.getId(), false);
			String id = temp.getId();
			Package currentPackage1 = (Package)packageRelation.getNode1();
			Package currentPackage2 = (Package)packageRelation.getNode2();
			Package parentPackage1 = findParentPackage(directoryPathToParentPackage, currentPackage1);
			Package parentPackage2 = findParentPackage(directoryPathToParentPackage, currentPackage2);
			while (parentPackage1 != null && parentPackage2 != null) {
//				RelationDataForDoubleNodes<Node, Relation> parentPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(subNodeRelations, parentPackage1, parentPackage2);
				RelationDataForDoubleNodes<Node, Relation> parentPackageClone = null;
				if (parentPackageClone == null) {
					break;
				}
				if (!(boolean) parentPackageClone.aggregateValue(aggregator)) {
					break;
				}
				HotspotPackage parentHotspotPackage = idToPackageRelation.getOrDefault(parentPackageClone.getId(), new HotspotPackage(parentPackageClone));
				idToPackageRelation.put(parentHotspotPackage.getId(), parentHotspotPackage);
				isChild.put(id, true);
				isChild.put(parentHotspotPackage.getId(), false);
				parentHotspotPackage.addHotspotChild(idToPackageRelation.get(id));
				id = parentHotspotPackage.getId();
				parentPackage1 = containRelationService.findPackageInPackage(parentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(parentPackage2);
			}
		}

		Map<String, HotspotPackage> parentSimilarPackages = new HashMap<>();
		for (Map.Entry<String, HotspotPackage> entry : idToPackageRelation.entrySet()) {
			String id = entry.getKey();
			if (!isChild.get(id)) {
				HotspotPackage value = entry.getValue();
				Package pck1 = value.getPackage1();
				Package pck2 = value.getPackage2();
				Package parentPck1 = containRelationService.findPackageInPackage(pck1);
				Package parentPck2 = containRelationService.findPackageInPackage(pck2);
				if (parentPck1 == null && parentPck2 == null) {
					String parentPck1Path = pck1.lastPackageDirectoryPath();
					String parentPck2Path = pck2.lastPackageDirectoryPath();
					String parentId = String.join("_", parentPck1Path, parentPck2Path);
					HotspotPackage parentSimilar = parentSimilarPackages.get(parentId);
					if (parentSimilar == null) {
						parentPck1 = new Package();
						parentPck1.setId(-1L);
						parentPck1.setEntityId(-1L);
						parentPck1.setDirectoryPath(parentPck1Path);
						parentPck1.setName(parentPck1Path);
						parentPck1.setLanguage(pck1.getLanguage());
						parentPck2 = new Package();
						parentPck2.setId(-1L);
						parentPck2.setEntityId(-1L);
						parentPck2.setDirectoryPath(parentPck2Path);
						parentPck2.setName(parentPck2Path);
						parentPck1.setLanguage(pck2.getLanguage());
						parentSimilar = new HotspotPackage(new RelationDataForDoubleNodes(parentPck1, parentPck2, parentId));
						parentSimilarPackages.put(parentSimilar.getId(), parentSimilar);
					}
					parentSimilar.addHotspotChild(value);
					isChild.put(id, true);
				}
			}
		}

		List<HotspotPackage> result = new ArrayList<>();

		for (HotspotPackage parentHotspotPackage : parentSimilarPackages.values()) {
			result.add(parentHotspotPackage);
		}

		for (Map.Entry<String, HotspotPackage> entry : idToPackageRelation.entrySet()) {
			String id = entry.getKey();
			if (!isChild.get(id)) {
				result.add(entry.getValue());
			}
		}
		result.sort((d1, d2) -> {
			return d1.getPackage1().getDirectoryPath().compareTo(d2.getPackage1().getDirectoryPath());
		});
		return result;
	}

	//第三版本的HotspotPackage检测代码，通过克隆文件数进行聚合，保留了多对多的克隆信息
	@Override
	public List<HotspotPackage> detectHotspotPackagesByFileClone() {
		Map<Long, Integer> directoryIdToAllNodes = new HashMap<>();
		Map<String, HotspotPackage> directoryPathToHotspotPackage = new HashMap<>();
		Map<String, Collection<String>> directoryPathToCloneChildrenPackages = new HashMap<>();
		Map<String, Set<CodeNode>> directoryPathToAllCloneChildrenPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		//List<RelationDataForDoubleNodes<Node, Relation>> packageClones = (List<RelationDataForDoubleNodes<Node, Relation>>) summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		List<RelationDataForDoubleNodes<Node, Relation>> packageClones = null;
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
			directoryPathToAllCloneChildrenPackages.put(path1, packageClone.getNodesInNode1());
			directoryPathToAllCloneChildrenPackages.put(path2, packageClone.getNodesInNode2());
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
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			HotspotPackage hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
			hotspotPackage.setData(packageClone.getAllNodesInNode1().size(), packageClone.getAllNodesInNode2().size(), packageClone.getNodesInNode1().size(), packageClone.getNodesInNode2().size());
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(isHotspot.containsKey(currentPackages)) {
				continue;
			}
			Collection<String> cloneChildren = new ArrayList<>();
			if(directoryPathToCloneChildrenPackages.containsKey(currentPackages)) {
				cloneChildren = directoryPathToCloneChildrenPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			isHotspot.put(currentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, hotspotPackage));
			isChild.put(currentPackages, false);
			String parentPackages;
			HotspotPackage parentHotspotPackage;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
				parentHotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, parentPackage1, parentPackage2);
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(isHotspot.containsKey(parentPackages)) {
					break;
				}
				cloneChildren = new ArrayList<>();
				if(directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					parentHotspotPackage.addHotspotChild(hotspotPackage);
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				isHotspot.put(parentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, parentHotspotPackage));
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
		List<HotspotPackage> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackage> entry : directoryPathToHotspotPackage.entrySet()) {
			String currentPackages = entry.getKey();
			HotspotPackage hotspotPackage = entry.getValue();
			Package currentPackage1 = hotspotPackage.getPackage1();
			Package currentPackage2 = hotspotPackage.getPackage2();
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
				if(!result.contains(hotspotPackage)) {
					result.add(hotspotPackage);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackage detectHotspotPackagesByPackageId(long pck1Id, long pck2Id, String language) {
		AggregationClone aggregationClone = aggregationCloneRepository.findAggregationCloneByPackageId(pck1Id, pck2Id);
		return setHotspotPackageData(aggregationClone, pck1Id, pck2Id, language);
	}

	@Override
	public List<HotspotPackage> detectHotspotPackagesByParentId(long parent1Id, long parent2Id, String language) {
		List<HotspotPackage> result = new ArrayList<>();
		if(language.equals("all")) {
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, "java"));
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, "cpp"));
		}
		else {
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, language));
		}
		return result;
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCloneLoc() {
		return null;
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCoChange() {
		Map<String, HotspotPackage> idToPackageRelation = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<CoChange> fileCoChanges = gitAnalyseService.calCntOfFileCoChange();
//		Collection<RelationDataForDoubleNodes<Node, Relation>> packageRelations = summaryAggregationDataService.queryPackageCoChangeFromFileCoChangeSort(fileCoChanges);
		List<RelationDataForDoubleNodes<Node, Relation>> packageRelations = null;
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByCoChange.getInstance();
		return aggregatePackageRelation(fileCoChanges,packageRelations,aggregator);
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCoChangeTimes() {
		return null;
	}

	@Override
	public List<HotspotPackage> detectHotspotPackagesByDependsOnInProject(long projectId) {
		List<HotspotPackage> result = new ArrayList<>();
		List<DependsOn> projectDependsOn = dependsOnRepository.findPackageDependsInProject(projectId);
		if(projectDependsOn != null && !projectDependsOn.isEmpty()){
			Map<Package, List<Package>> packageDependsPackage = new HashMap<>();
			for (DependsOn dependsOn : projectDependsOn){
				Package startNode = (Package) dependsOn.getStartNode();
				Package endNode = (Package) dependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				List<Package> dependsPackage = packageDependsPackage.getOrDefault(pck1, new ArrayList<>());
				dependsPackage.add(pck2);
				packageDependsPackage.put(pck1, dependsPackage);
			}
			for(Map.Entry<Package, List<Package>> entry : packageDependsPackage.entrySet()){
				Package pck1 = entry.getKey();
				List<Package> dependsPackage = entry.getValue();
				dependsPackage.forEach(pck2 ->{
					HotspotPackage hotspotPackage = detectHotspotPackagesWithDependsOnByPackageId(pck1.getId(), pck2.getId());
					result.add(hotspotPackage);
				});
			}
		}
		return result;
	}

	@Override
	public List<HotspotPackage> detectHotspotPackagesByDependsOnInAllProjects(){
		List<Project> projects = nodeService.allProjects();
		List<HotspotPackage> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagesByDependsOnInProject(project.getId()));
		}
		return result;
	}

	@Override
	public HotspotPackage detectHotspotPackagesWithDependsOnByPackageId(long pck1Id, long pck2Id) {
		List<DependsOn> packageDependsOnList = dependsOnRepository.findPackageDependsByPackageId(pck1Id, pck2Id);
		HotspotPackage hotspotPackage = null;
		if(packageDependsOnList != null && !packageDependsOnList.isEmpty()){
			Package tmp1 = (Package) packageDependsOnList.get(0).getStartNode();
			Package tmp2 = (Package) packageDependsOnList.get(0).getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;

			String dependsOnStr = "DependsOn: ";
			String dependsByStr = "DependsOnBy: ";
			int dependsOnTimes = 0;
			int dependsByTimes = 0;
			for (DependsOn dependsOn : packageDependsOnList){
				if(dependsOn.getStartNode().getId() == pck1Id){
					dependsOnStr += dependsOn.getDependsOnType();
					dependsOnTimes += dependsOn.getTimes();
				}else {
					dependsByStr += dependsOn.getDependsOnType();
					dependsByTimes += dependsOn.getTimes();
				}
			}
			RelationDataForDoubleNodes<Node, Relation> dependsOnRelationDataForDoubleNodes = new RelationDataForDoubleNodes(pck1, pck2, dependsOnStr, dependsByStr);
			dependsOnRelationDataForDoubleNodes.setDependsOnTimes(dependsOnTimes);
			dependsOnRelationDataForDoubleNodes.setDependsByTimes(dependsByTimes);

			hotspotPackage = new HotspotPackage(dependsOnRelationDataForDoubleNodes);
		}

		return hotspotPackage;
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
		rowKey.set(0);
		Collection<HotspotPackage> hotspotPackages = detectHotspotPackagesByParentId(-1 ,-1, language);
		Sheet sheet = hwb.createSheet(new StringBuilder().append(language).toString());
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		CellStyle style = hwb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("目录1");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("目录1克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("目录2");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("目录2克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("总克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("包克隆CoChange占比");
		cell = row.createCell(6);
		cell.setCellValue("克隆文件对数");
		cell.setCellStyle(style);
		for(HotspotPackage hotspotPackage : hotspotPackages){
			loadHotspotPackageResult(sheet, 0, hotspotPackage);
		}
	}

	private void loadHotspotPackageResult(Sheet sheet, int layer, HotspotPackage hotspotPackage){
		String prefix = "";
		for(int i = 0; i < layer; i++) {
			prefix += "|---";
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + hotspotPackage.getPackage1().getDirectoryPath());
		BigDecimal clonePercent1 = BigDecimal.valueOf((hotspotPackage.getRelationNodes1() + 0.0) / hotspotPackage.getAllNodes1());
		clonePercent1 = clonePercent1.setScale(2, RoundingMode.HALF_UP);
		row.createCell(1).setCellValue(hotspotPackage.getRelationNodes1() + "/" + hotspotPackage.getAllNodes1() + "=" + clonePercent1.toString());
		row.createCell(2).setCellValue(prefix + hotspotPackage.getPackage2().getDirectoryPath());
		BigDecimal clonePercent2 = BigDecimal.valueOf((hotspotPackage.getRelationNodes2() + 0.0) / hotspotPackage.getAllNodes2());
		clonePercent2 = clonePercent2.setScale(2, RoundingMode.HALF_UP);
		row.createCell(3).setCellValue(hotspotPackage.getRelationNodes2() + "/" + hotspotPackage.getAllNodes2() + "=" + clonePercent2.toString());
		BigDecimal clonePercent = BigDecimal.valueOf((hotspotPackage.getRelationNodes1() + hotspotPackage.getRelationNodes2() + 0.0) / (hotspotPackage.getAllNodes1() + hotspotPackage.getAllNodes2()));
		clonePercent = clonePercent.setScale(2, RoundingMode.HALF_UP);
		row.createCell(4).setCellValue("(" + hotspotPackage.getRelationNodes1() + "+" + hotspotPackage.getRelationNodes2() + ")/(" + hotspotPackage.getAllNodes1() + "+" + hotspotPackage.getAllNodes2() + ")=" + clonePercent.toString());
		BigDecimal cochangePercent = BigDecimal.valueOf(0);
		if(hotspotPackage.getPackageCochangeTimes() != 0) {
			cochangePercent = BigDecimal.valueOf((hotspotPackage.getPackageCloneCochangeTimes() + 0.0) / hotspotPackage.getPackageCochangeTimes());
		}
		cochangePercent = cochangePercent.setScale(2, RoundingMode.HALF_UP);
		row.createCell(5).setCellValue(hotspotPackage.getPackageCloneCochangeTimes() + "/" + hotspotPackage.getPackageCochangeTimes() + "=" + cochangePercent.toString());
		row.createCell(6).setCellValue(hotspotPackage.getClonePairs());
		for (HotspotPackage hotspotPackageChild : hotspotPackage.getChildrenHotspotPackages()){
			loadHotspotPackageResult(sheet, layer + 1, hotspotPackageChild);
		}
		for (Package packageChild1 : hotspotPackage.getChildrenOtherPackages1()){
			printOtherPackage(sheet, -1, layer + 1, packageChild1);
		}
		for (Package packageChild2:hotspotPackage.getChildrenOtherPackages2()){
			printOtherPackage(sheet, 1, layer + 1, packageChild2);
		}
	}
	private void printOtherPackage(Sheet sheet, int index, int layer, Package otherPackage){
		String prefix = "";
		for(int i = 0; i < layer; i++) {
			prefix += "|---";
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

	//寻找HotspotPackage
	private HotspotPackage findHotspotPackage(Collection<? extends Relation> fileClones, Map<String, HotspotPackage> directoryPathToHotspotPackage, Package pck1, Package pck2) {
		String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		if(!directoryPathToHotspotPackage.containsKey(key)) {
			HotspotPackage hotspotPackage;
//			RelationDataForDoubleNodes<Node, Relation> packageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
			RelationDataForDoubleNodes<Node, Relation> packageClone = null;
			if(packageClone != null) {
				hotspotPackage = new HotspotPackage(packageClone);
				if(hotspotPackage.getPackage1().getDirectoryPath().equals(pck2.getDirectoryPath()) && hotspotPackage.getPackage2().getDirectoryPath().equals(pck1.getDirectoryPath())) {
					hotspotPackage.swapPackages();
				}
			}
			else {
				hotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(pck1, pck2));
			}
			directoryPathToHotspotPackage.put(key, hotspotPackage);
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

	//寻找父包
	private Package findParentPackage(Map<String, Package> directoryPathToParentPackage, Package pck) {
		directoryPathToParentPackage.put(pck.getDirectoryPath(), pck);
		String parentDirectoryPath = pck.lastPackageDirectoryPath();
		if (FileUtil.SLASH_LINUX.equals(parentDirectoryPath)) {
			return null;
		}
		Package parent = directoryPathToParentPackage.get(parentDirectoryPath);
		if (parent == null) {
			parent = containRelationService.findPackageInPackage(pck);
			if (parent != null) {
				directoryPathToParentPackage.put(parentDirectoryPath, parent);
			}
		}
		return parent;
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

	//判断是否符合聚合条件
	private boolean isHotspotPackage(RelationAggregator<Boolean> aggregator, Map<Long, Integer> directoryIdToAllNodes, Map<String, Set<CodeNode>> directoryPathToAllCloneChildrenPackages, HotspotPackage hotspotPackage) {
		Package currentPackage1 = hotspotPackage.getPackage1();
		Package currentPackage2 = hotspotPackage.getPackage2();
		String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
		String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
		RelationDataForDoubleNodes<Node, Relation> packageClone = new RelationDataForDoubleNodes<Node, Relation>(currentPackage1, currentPackage2);

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			packageClone.setDate(hotspotPackage.getAllNodes1(), hotspotPackage.getAllNodes2(), hotspotPackage.getRelationNodes1(), hotspotPackage.getRelationNodes2());
			return aggregator.aggregate(packageClone);
		}

		Set<CodeNode> cloneChildrenPackages1 = new HashSet<>();
		Set<CodeNode> cloneChildrenPackages2 = new HashSet<>();
		//包下另有文件情况
		if(isBranchPackageWithFiles(currentPackage1) || isBranchPackageWithFiles(currentPackage2)) {
			if(directoryPathToAllCloneChildrenPackages.containsKey(path1)) {
				cloneChildrenPackages1.addAll(directoryPathToAllCloneChildrenPackages.get(path1));
			}
			if(directoryPathToAllCloneChildrenPackages.containsKey(path2)) {
				cloneChildrenPackages2.addAll(directoryPathToAllCloneChildrenPackages.get(path2));
			}
		}
		//遍历包下子包
		Collection<HotspotPackage> childrenHotspotPackages = hotspotPackage.getChildrenHotspotPackages();
		Collection<Package> childrenPackage1 = containRelationService.findPackageContainPackages(currentPackage1);
		Collection<Package> childrenPackage2 = containRelationService.findPackageContainPackages(currentPackage2);
		for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
			Package childPackage1 = childHotspotPackage.getPackage1();
			Package childPackage2 = childHotspotPackage.getPackage2();
			String childPath1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
			String childPath2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
			cloneChildrenPackages1.addAll(directoryPathToAllCloneChildrenPackages.get(childPath1));
			cloneChildrenPackages2.addAll(directoryPathToAllCloneChildrenPackages.get(childPath2));
			childrenPackage1.remove(childPackage1);
			childrenPackage2.remove(childPackage2);
		}
		for(Package otherChild1 : childrenPackage1) {
			hotspotPackage.addOtherChild1(otherChild1);
		}
		for(Package otherChild2 : childrenPackage2) {
			hotspotPackage.addOtherChild2(otherChild2);
		}
		int allNodes1 = getAllFilesNum(directoryIdToAllNodes, currentPackage1);
		int allNodes2 = getAllFilesNum(directoryIdToAllNodes, currentPackage2);
		int cloneNodes1 = cloneChildrenPackages1.size();
		int cloneNodes2 = cloneChildrenPackages2.size();
		directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenPackages1);
		directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenPackages2);
		hotspotPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		packageClone.setDate(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		return aggregator.aggregate(packageClone);
	}

	//设置HotspotPackage信息
	public HotspotPackage setHotspotPackageData(AggregationClone aggregationClone, long parent1Id, long parent2Id, String language) {
		Package currentPackage1 = (Package) aggregationClone.getStartNode();
		Package currentPackage2 = (Package) aggregationClone.getEndNode();
		CoChange packageCoChanges = null;
		ModuleClone packageCloneCoChanges = null;
		if(parent1Id > -1 && parent2Id > -1){
			packageCoChanges = coChangeRepository.findPackageCoChangeByPackageId(currentPackage1.getId(), currentPackage2.getId());
			packageCloneCoChanges = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
		}
		RelationDataForDoubleNodes<Node, Relation> relationDataForDoubleNodes = new RelationDataForDoubleNodes<Node, Relation>(currentPackage1, currentPackage2);
		HotspotPackage hotspotPackage = new HotspotPackage(relationDataForDoubleNodes);
		hotspotPackage.setClonePairs(aggregationClone.getClonePairs());
		hotspotPackage.setData(aggregationClone.getAllNodesCount1(), aggregationClone.getAllNodesCount2(), aggregationClone.getCloneNodesCount1(), aggregationClone.getCloneNodesCount2());
		if(packageCoChanges != null){
			hotspotPackage.setPackageCochangeTimes(packageCoChanges.getTimes());
		}
		else {
			hotspotPackage.setPackageCochangeTimes(0);
		}
		if(packageCloneCoChanges != null){
			hotspotPackage.setPackageCloneCochangeTimes(packageCloneCoChanges.getCloneNodesCoChangeTimes());
		}
		else {
			hotspotPackage.setPackageCloneCochangeTimes(0);
		}
		List<HotspotPackage> childrenHotspotPackages = new ArrayList<>();
		if(language.equals("all")) {
			childrenHotspotPackages.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), "java"));
			childrenHotspotPackages.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), "cpp"));
		}
		else {
			childrenHotspotPackages.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), language));
		}
		if(childrenHotspotPackages.size() > 0) {
			//将有子包的包下文件打包
			if(isBranchPackageWithFiles(currentPackage1) && isBranchPackageWithFiles(currentPackage2)) {
				ModuleClone moduleClone = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
				Package childPackage1 = buildPackageForFiles(currentPackage1);
				Package childPackage2 = buildPackageForFiles(currentPackage2);
				if(moduleClone != null) {
					HotspotPackage childHotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage2));
					childHotspotPackage.setData(moduleClone.getAllNodesCount1(), moduleClone.getAllNodesCount2(), moduleClone.getCloneNodesCount1(), moduleClone.getCloneNodesCount2());
					childHotspotPackage.setClonePairs(moduleClone.getClonePairs());
					if(packageCoChanges != null){
						childHotspotPackage.setPackageCochangeTimes(packageCoChanges.getTimes());
					}
					else {
						childHotspotPackage.setPackageCochangeTimes(0);
					}
					if(packageCloneCoChanges != null){
						childHotspotPackage.setPackageCloneCochangeTimes(packageCloneCoChanges.getCloneNodesCoChangeTimes());
					}
					else {
						childHotspotPackage.setPackageCloneCochangeTimes(0);
					}
					hotspotPackage.setPackageCochangeTimes(0);
					hotspotPackage.setPackageCloneCochangeTimes(0);
					hotspotPackage.setClonePairs(0);
					hotspotPackage.addHotspotChild(childHotspotPackage);
				}
				else {
					childPackage1.setNof(containRelationService.findPackageContainFiles(currentPackage1).size());
					childPackage2.setNof(containRelationService.findPackageContainFiles(currentPackage2).size());
					hotspotPackage.addOtherChild1(childPackage1);
					hotspotPackage.addOtherChild2(childPackage2);
				}
			}

			Collection<Package> childrenPackages1 = containRelationService.findPackageContainPackages(hotspotPackage.getPackage1());
			Collection<Package> childrenPackages2 = containRelationService.findPackageContainPackages(hotspotPackage.getPackage2());
			for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
				if(childHotspotPackage.getSimilarityValue() > 0.5 || childrenPackages1.contains(childHotspotPackage.getPackage1()) || childrenPackages1.contains(childHotspotPackage.getPackage2())) {
					hotspotPackage.addHotspotChild(childHotspotPackage);
					childrenPackages1.remove(childHotspotPackage.getPackage1());
					childrenPackages2.remove(childHotspotPackage.getPackage2());
				}
			}
			for(Package childPackage1 : childrenPackages1) {
				hotspotPackage.addOtherChild1(childPackage1);
			}
			for(Package childPackage2 : childrenPackages2) {
				hotspotPackage.addOtherChild2(childPackage2);
			}
		}
		return hotspotPackage;
	}

	//递归加载聚合结果
	public List<HotspotPackage> loadHotspotPackagesByParentId(long parent1Id, long parent2Id, String language) {
		List<HotspotPackage> result = new ArrayList<>();
		List<AggregationClone> aggregationClones = aggregationCloneRepository.findAggregationCloneByParentId(parent1Id, parent2Id, language);
		for(AggregationClone aggregationClone : aggregationClones) {
			result.add(setHotspotPackageData(aggregationClone, parent1Id, parent2Id, language));
		}
		//聚合排序，每一次均进行排序
		result.sort((d1, d2) -> {
			int allNodes1 = d1.getAllNodes1() + d1.getAllNodes2();
			int cloneNodes1 = d1.getRelationNodes1() + d1.getRelationNodes2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = d2.getAllNodes1() + d2.getAllNodes2();
			int cloneNodes2 = d2.getRelationNodes1() + d2.getRelationNodes2();
			double percentageThreshold2 = (cloneNodes2 + 0.0) / allNodes2;
			if(percentageThreshold1 < percentageThreshold2) {
				return 1;
			}
			else if(percentageThreshold1 == percentageThreshold2) {
				return Integer.compare(cloneNodes2, cloneNodes1);
			}
			else {
				return -1;
			}
		});
		return result;
	}
}
