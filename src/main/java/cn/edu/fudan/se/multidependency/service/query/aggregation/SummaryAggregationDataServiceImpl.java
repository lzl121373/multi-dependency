package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CloneRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CoChangeRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChangeMatrix;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SummaryAggregationDataServiceImpl implements SummaryAggregationDataService {
    
    @Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	private List<BasicDataForDoubleNodes<Node, Relation>> removeSameNodeToCloneValuePackages = null;
    private Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileCloneCache = null;
	public Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileClone(Collection<? extends Relation> fileClones) {
		Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> pckToPackageClones = queryPackageCloneFromFileCloneCache;
		if(pckToPackageClones == null) {
			List<BasicDataForDoubleNodes<Node, Relation>> cache = new ArrayList<>();
			pckToPackageClones = new HashMap<>();
			
			for(Relation clone : fileClones) {
				CodeNode node1 = (CodeNode)clone.getStartNode();
				CodeNode node2 = (CodeNode)clone.getEndNode();
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
				BasicDataForDoubleNodes<Node, Relation> cloneValue = getSuperNodeRelationWithSubNodeRelation(pckToPackageClones, pck1, pck2);
				if(cloneValue == null) {
					cloneValue = new CloneRelationDataForDoubleNodes(pck1, pck2);
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
				cloneValue.setAllNodesInNode1(new HashSet<>(containRelationService.findPackageContainFiles((Package)cloneValue.getNode1())));
				cloneValue.setAllNodesInNode2(new HashSet<>(containRelationService.findPackageContainFiles((Package)cloneValue.getNode2())));
				
				Map<Node, BasicDataForDoubleNodes<Node, Relation>> pck1ToClones = pckToPackageClones.getOrDefault(pck1, new HashMap<>());
				pck1ToClones.put(pck2, cloneValue);
				pckToPackageClones.put(pck1, pck1ToClones);
			}
			removeSameNodeToCloneValuePackages = cache;
			queryPackageCloneFromFileCloneCache = pckToPackageClones;
		}
		return pckToPackageClones;
	}

	@Override
	public List<BasicDataForDoubleNodes<Node, Relation>> queryPackageCloneFromFileCloneSort(Collection<? extends Relation> fileClones) {
		List<BasicDataForDoubleNodes<Node, Relation>> cache = removeSameNodeToCloneValuePackages;
		if(cache == null) {
			queryPackageCloneFromFileClone(fileClones);
			cache = removeSameNodeToCloneValuePackages;
		}
		List<BasicDataForDoubleNodes<Node, Relation>> result = new ArrayList<>(cache);
		setCloneRelationDataForDoubleNodes(result);
		result.sort((v1, v2) -> {
			return v2.getChildren().size() - v1.getChildren().size();
		});
		return result;
	}

	private void setCloneRelationDataForDoubleNodes(List<BasicDataForDoubleNodes<Node, Relation>> packageClones) {
		Map<Long, Integer> directoryIdToAllLoc = new HashMap<>();
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			int clonePairs = packageClone.sizeOfChildren();
			int cloneNodesCount1 = packageClone.getNodesInNode1().size();
			int cloneNodesCount2 = packageClone.getNodesInNode2().size();
			int allNodesCount1 = packageClone.getAllNodesInNode1().size();
			int allNodesCount2 = packageClone.getAllNodesInNode2().size();
			int cloneNodesLoc1 = 0;
			int cloneNodesLoc2 = 0;
			int allNodesLoc1 = getAllFilesLoc(directoryIdToAllLoc, currentPackage1);
			int allNodesLoc2 = getAllFilesLoc(directoryIdToAllLoc, currentPackage2);
			int cloneType1Count = 0;
			int cloneType2Count = 0;
			int cloneType3Count = 0;
			double cloneSimilarityValue = 0.00;
			List<FileCloneWithCoChange> childrenClonePairs;
			try {
				childrenClonePairs = queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), currentPackage1, currentPackage2).getChildren();
				childrenClonePairs.sort((d1, d2) -> {
					double value1 = d1.getFileClone().getValue();
					double value2 = d2.getFileClone().getValue();
					if(value1 != value2) {
						return Double.compare(value2, value1);
					}
					else {
						String cloneType1 = d1.getFileClone().getCloneType();
						String cloneType2 = d2.getFileClone().getCloneType();
						return cloneType1.compareTo(cloneType2);
					}
				});
				Set<ProjectFile> cloneChildrenFiles1 = new HashSet<>();
				Set<ProjectFile> cloneChildrenFiles2 = new HashSet<>();
				for(FileCloneWithCoChange childrenClonePair : childrenClonePairs) {
					String cloneType = childrenClonePair.getFileClone().getCloneType();
					switch (cloneType){
						case "type_1":
							cloneType1Count++;
							break;
						case "type_2":
							cloneType2Count++;
							break;
						case "type_3":
							cloneType3Count++;
							break;
					}
					cloneSimilarityValue += childrenClonePair.getFileClone().getValue();
					cloneChildrenFiles1.add(childrenClonePair.getFile1());
					cloneChildrenFiles2.add(childrenClonePair.getFile2());
				}
				for(ProjectFile cloneChildFile1 : cloneChildrenFiles1) {
					cloneNodesLoc1 += cloneChildFile1.getLoc();
				}
				for(ProjectFile cloneChildFile2 : cloneChildrenFiles2) {
					cloneNodesLoc2 += cloneChildFile2.getLoc();
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			//存储数据
			CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) packageClone;
			currentPackagePairCloneRelationData.setClonePairs(clonePairs);
			currentPackagePairCloneRelationData.setCloneCountDate(cloneNodesCount1, cloneNodesCount2, allNodesCount1, allNodesCount2);
			currentPackagePairCloneRelationData.setCloneTypeDate(cloneType1Count, cloneType2Count, cloneType3Count, cloneSimilarityValue);
			currentPackagePairCloneRelationData.setCloneLocDate(cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2);
		}
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

	@Override
	public PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<? extends Relation> fileClones, Package pck1, Package pck2) throws Exception {
		BasicDataForDoubleNodes<Node, Relation> temp = querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
		PackageCloneValueWithFileCoChange result = new PackageCloneValueWithFileCoChange();
		result.setPck1((Package)temp.getNode1());
		result.setPck2((Package)temp.getNode2());
		result.addFile1(containRelationService.findPackageContainFiles(pck1));
		result.addFile2(containRelationService.findPackageContainFiles(pck2));
		List<Relation> children = new ArrayList<>(temp.getChildren());
		for(Relation clone : children) {
			ProjectFile file1 = (ProjectFile) clone.getStartNode();
			ProjectFile file2 = (ProjectFile) clone.getEndNode();
			if(containRelationService.findFileBelongToPackage(file1).equals(pck1)) {
				result.addCloneFile1(file1);
				result.addCloneFile2(file2);
			} else {
				result.addCloneFile1(file2);
				result.addCloneFile2(file1);
			}
			CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file1, file2);
			result.addChild(new FileCloneWithCoChange((Clone)clone, cochange));
		}
		result.calculateNoneClone();
		result.sortChildren();
		return result;
	}

	private List<BasicDataForDoubleNodes<Node, Relation>> removeSameNodeToCoChangeValuePackages = null;
	private Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChangeCache = null;
	public Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChange(Collection<? extends Relation> fileCoChanges) {
		Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> pckToPackageCoChanges = queryPackageCoChangeFromFileCoChangeCache;
		if(pckToPackageCoChanges == null) {
			List<BasicDataForDoubleNodes<Node, Relation>> cache = new ArrayList<>();
			pckToPackageCoChanges = new HashMap<>();

			for(Relation coChange : fileCoChanges) {
				CodeNode node1 = (CodeNode)coChange.getStartNode();
				CodeNode node2 = (CodeNode)coChange.getEndNode();
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
				BasicDataForDoubleNodes<Node, Relation> coChangeTimes = getSuperNodeRelationWithSubNodeRelation(pckToPackageCoChanges, pck1, pck2);
				if(coChangeTimes == null) {
					coChangeTimes = new CoChangeRelationDataForDoubleNodes(pck1, pck2);
					cache.add(coChangeTimes);
				}
				coChangeTimes.addChild(coChange);
				if(pck1.equals(coChangeTimes.getNode1())) {
					coChangeTimes.addCodeNodeToNode1(file1);
					coChangeTimes.addCodeNodeToNode2(file2);
				} else {
					coChangeTimes.addCodeNodeToNode2(file1);
					coChangeTimes.addCodeNodeToNode1(file2);
				}
				coChangeTimes.setAllNodesInNode1(new HashSet<>(containRelationService.findPackageContainFiles((Package)coChangeTimes.getNode1())));
				coChangeTimes.setAllNodesInNode2(new HashSet<>(containRelationService.findPackageContainFiles((Package)coChangeTimes.getNode2())));

				Map<Node, BasicDataForDoubleNodes<Node, Relation>> pck1ToCoChanges = pckToPackageCoChanges.getOrDefault(pck1, new HashMap<>());
				pck1ToCoChanges.put(pck2, coChangeTimes);
				pckToPackageCoChanges.put(pck1, pck1ToCoChanges);
			}
			removeSameNodeToCoChangeValuePackages = cache;
			queryPackageCoChangeFromFileCoChangeCache = pckToPackageCoChanges;
		}
		return pckToPackageCoChanges;
	}

	@Override
	public Collection<BasicDataForDoubleNodes<Node, Relation>> queryPackageCoChangeFromFileCoChangeSort(Collection<? extends Relation> fileCoChanges) {
		Collection<BasicDataForDoubleNodes<Node, Relation>> cache = removeSameNodeToCoChangeValuePackages;
		if(cache == null) {
			queryPackageCoChangeFromFileCoChange(fileCoChanges);
			cache = removeSameNodeToCoChangeValuePackages;
		}
		List<BasicDataForDoubleNodes<Node, Relation>> result = new ArrayList<>(cache);
		result.sort((v1, v2) -> {
			return v2.getChildren().size() - v1.getChildren().size();
		});
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
}
