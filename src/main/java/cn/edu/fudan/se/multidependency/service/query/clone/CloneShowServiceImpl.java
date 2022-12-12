package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneType;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.DefaultPackageCloneValueCalculator;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.data.Graph;
import cn.edu.fudan.se.multidependency.service.query.data.HistogramWithProjectsSize;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeNode;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;
import lombok.SneakyThrows;

@Service
public class CloneShowServiceImpl implements CloneShowService {
	
	@Autowired
	CloneAnalyseService cloneAnalyseService;
	
	@Autowired
	ContainRelationService containRelationService;
	
    @Autowired
    BasicCloneQueryService basicCloneQueryService;
	
	@Override
	public Collection<HistogramWithProjectsSize> withProjectsSizeToHistogram(Collection<CloneGroup> groups, boolean singleLanguage) {
		Map<Integer, HistogramWithProjectsSize> map = new HashMap<>();
		for(CloneGroup group : groups) {
			int mssSize = 0;
			if(!singleLanguage) {
				Collection<MicroService> mss = cloneAnalyseService.cloneGroupContainMicroServices(group);
				mssSize = mss.size();
			} else {
				Collection<Project> projects = cloneAnalyseService.cloneGroupContainProjects(group);
				mssSize = projects.size();
			}
			if(mssSize == 0) {
				continue;
			}
			HistogramWithProjectsSize histogram = map.getOrDefault(mssSize, new HistogramWithProjectsSize(mssSize));
			histogram.addGroupSize(1);
			histogram.addNodesSize(group.sizeOfNodes());
			map.put(mssSize, histogram);
		}
		return map.values();
	}
	
	public Collection<Clone> removeType1RelatedClone(CloneGroup cloneGroup) {
		Collection<Clone> clones = basicCloneQueryService.findGroupContainCloneRelations(cloneGroup);
		List<Clone> removeClones = new ArrayList<>();
		List<CodeNode> specificNodes = new ArrayList<>();
		List<CodeNode> removeNodes = new ArrayList<>();
		Graph graph = new Graph();
		for(Clone clone : clones) {
			if(CloneType.type_1.toString().equals(clone.getCloneType())) {
				CodeNode node1 = clone.getCodeNode1();
				CodeNode node2 = clone.getCodeNode2();
				graph.addNode(node1);
				graph.addNode(node2);
				graph.addEdge(clone);
			}
		}
		graph.computeConnectedComponents();
		for(List<Node> vs : graph.getConnectedComponents()) {
			specificNodes.add((CodeNode) vs.get(0));
			for(int i = 1; i < vs.size(); i++) {
				removeNodes.add((CodeNode) vs.get(i));
			}
		}
		for(Clone clone : clones) {
			switch(CloneType.valueOf(clone.getCloneType())) {
			case type_1:
				if(!(specificNodes.contains(clone.getCodeNode1()) || specificNodes.contains(clone.getCodeNode2()))) {
					removeClones.add(clone);
				}
				break;
			case type_2:
			case type_3:
				if(removeNodes.contains(clone.getCodeNode1()) || removeNodes.contains(clone.getCodeNode2())) {
					removeClones.add(clone);
				}
				break;
			default:
				break;
			}
		}
		clones.removeAll(removeClones);
		return clones;
	}
	
	@SneakyThrows
	@Override
	public JSONObject clonesGroupsToCytoscape(Collection<CloneGroup> groups, boolean showGroupNode, boolean singleLanguage) {
		Map<Node, ZTreeNode> nodeToZTreeNode = new HashMap<>();
		Map<Project, ZTreeNode> projectToZTreeNode = new HashMap<>();
		Map<MicroService, ZTreeNode> msToZTreeNode = new HashMap<>();
		Set<ZTreeNode> groupZTreeNodes = new HashSet<>();
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		List<CytoscapeNode> groupNodes = new ArrayList<>();
		List<CytoscapeEdge> groupEdges = new ArrayList<>();
		Map<Node, Boolean> isNodeToCytoscapeNode = new HashMap<>();
		Map<String, Boolean> isIdToCytoscapeEdge = new HashMap<>();
		List<Clone> allClones = new ArrayList<>();
		for(CloneGroup cloneGroup : groups) {
			CytoscapeNode groupNode = new CytoscapeNode(cloneGroup.getId(), cloneGroup.getName(), "CloneGroup");
			groupNodes.add(groupNode);
			groupZTreeNodes.add(new ZTreeNode(cloneGroup.getId(), cloneGroup.getName(), false, "CloneGroup", false));
			Collection<Clone> clones = cloneGroup.getRelations();
//			Collection<Clone> clones = removeType1RelatedClone(cloneGroup);
			allClones.addAll(clones);
			for(Clone cloneRelation : clones) {
				CodeNode node1 = cloneRelation.getCodeNode1();
				CodeNode node2 = cloneRelation.getCodeNode2();
				ProjectFile file1 = null;
				ProjectFile file2 = null;
				if(node1 instanceof ProjectFile && node2 instanceof ProjectFile) {
					file1 = (ProjectFile) node1;
					file2 = (ProjectFile) node2;
					if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
						isNodeToCytoscapeNode.put(file1, true);
						CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLines() + ")", "File");
						file1CytoscapeNode.setValue(file1.getPath());
						nodes.add(file1CytoscapeNode);
						nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLines() + ")", false, "File", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
						isNodeToCytoscapeNode.put(file2, true);
						CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLines() + ")", "File");
						file2CytoscapeNode.setValue(file2.getPath());
						nodes.add(file2CytoscapeNode);
						nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLines() + ")", false, "File", true));
					}
					edges.add(new CytoscapeEdge(file1, file2, "Clone", new StringBuilder().append(cloneRelation.getValue()).append(" : ").append(cloneRelation.getCloneType()).toString()));
				} else {
					file1 = containRelationService.findCodeNodeBelongToFile(node1);
					file2 = containRelationService.findCodeNodeBelongToFile(node2);
					if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
						isNodeToCytoscapeNode.put(file1, true);
						CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLines() + ")", "File");
						file1CytoscapeNode.setValue(file1.getPath());
						nodes.add(file1CytoscapeNode);
						nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLines() + ")", false, "File", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
						isNodeToCytoscapeNode.put(file2, true);
						CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLines() + ")", "File");
						file2CytoscapeNode.setValue(file2.getPath());
						nodes.add(file2CytoscapeNode);
						nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLines() + ")", false, "File", true));
					}
					if(node1 instanceof Type) {
						Type type1 = (Type) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(type1, false)) {
							isNodeToCytoscapeNode.put(type1, true);
							CytoscapeNode type1CytoscapeNode = new CytoscapeNode(type1.getId(), type1.getName() + "\n(" + type1.getStartLine() + " , " + type1.getEndLine() + ")", "Type");
							type1CytoscapeNode.setValue(type1.getIdentifierSuffix());
							nodes.add(type1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(type1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(type1, new ZTreeNode(type1.getId(), type1.getIdentifier() + "(" + type1.getStartLine() + " , " + type1.getEndLine() + ")", false, "Type", false));
						}
						String file1ContainType1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(type1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainType1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainType1Id, true);
							edges.add(new CytoscapeEdge(file1, type1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(type1));
						}
					} else if(node1 instanceof Function) {
						Function function1 = (Function) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(function1, false)) {
							isNodeToCytoscapeNode.put(function1, true);
							CytoscapeNode function1CytoscapeNode = new CytoscapeNode(function1.getId(), function1.getName() + "\n(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", "Function");
							function1CytoscapeNode.setValue(function1.getFunctionIdentifier());
							nodes.add(function1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(function1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(function1, new ZTreeNode(function1.getId(), function1.getFunctionIdentifier() + "(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", false, "Function", false));
						}
						String file1ContainFunction1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(function1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainFunction1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainFunction1Id, true);
							edges.add(new CytoscapeEdge(file1, function1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(function1));
						}
					} else if(node1 instanceof Snippet) {
						Snippet snippet1 = (Snippet) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(snippet1, false)) {
							isNodeToCytoscapeNode.put(snippet1, true);
							CytoscapeNode snippet1CytoscapeNode = new CytoscapeNode(snippet1.getId(), snippet1.getName(), "Snippet");
							snippet1CytoscapeNode.setValue(snippet1.getIdentifier());
							nodes.add(snippet1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(snippet1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(snippet1, new ZTreeNode(snippet1.getId(), snippet1.getIdentifier(), false, "Snippet", false));
						}
						String file1ContainSnippet1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(snippet1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainSnippet1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainSnippet1Id, true);
							edges.add(new CytoscapeEdge(file1, snippet1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(snippet1));
						}
					}
					if(node2 instanceof Type) {
						Type type2 = (Type) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(type2, false)) {
							isNodeToCytoscapeNode.put(type2, true);
							CytoscapeNode type1CytoscapeNode = new CytoscapeNode(type2.getId(), type2.getName() + "\n(" + type2.getStartLine() + " , " + type2.getEndLine() + ")", "Type");
							type1CytoscapeNode.setValue(type2.getIdentifierSuffix());
							nodes.add(type1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(type2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(type2, new ZTreeNode(type2.getId(), type2.getIdentifier() + "(" + type2.getStartLine() + " , " + type2.getEndLine() + ")", false, "Type", false));
						}
						String file2ContainType2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(type2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainType2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainType2Id, true);
							edges.add(new CytoscapeEdge(file2, type2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(type2));
						}
					} else if(node2 instanceof Function) {
						Function function2 = (Function) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(function2, false)) {
							isNodeToCytoscapeNode.put(function2, true);
							CytoscapeNode function2CytoscapeNode = new CytoscapeNode(function2.getId(), function2.getName() + "\n(" + function2.getStartLine() + " , " + function2.getEndLine() + ")", "Function");
							function2CytoscapeNode.setValue(function2.getFunctionIdentifier());
							nodes.add(function2CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(function2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(function2, new ZTreeNode(function2.getId(), function2.getFunctionIdentifier() + "(" + function2.getStartLine() + " , " + function2.getEndLine() + ")", false, "Function", false));
						}
						String file2ContainFunction2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(function2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainFunction2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainFunction2Id, true);
							edges.add(new CytoscapeEdge(file2, function2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(function2));
						}
					} else if(node2 instanceof Snippet) {
						Snippet snippet2 = (Snippet) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(snippet2, false)) {
							isNodeToCytoscapeNode.put(snippet2, true);
							CytoscapeNode snippet2CytoscapeNode = new CytoscapeNode(snippet2.getId(), snippet2.getName(), "Snippet");
							snippet2CytoscapeNode.setValue(snippet2.getIdentifier());
							nodes.add(snippet2CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(snippet2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(snippet2, new ZTreeNode(snippet2.getId(), snippet2.getIdentifier(), false, "Snippet", false));
						}
						String file2ContainSnippet2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(snippet2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainSnippet2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainSnippet2Id, true);
							edges.add(new CytoscapeEdge(file2, snippet2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(snippet2));
						}
					}
					
//					edges.add(new CytoscapeEdge(node1, node2, "Clone", String.valueOf(cloneRelation.getValue())));
					edges.add(new CytoscapeEdge(node1, node2, "Clone", new StringBuilder().append(cloneRelation.getValue()).append(" : ").append(cloneRelation.getCloneType()).toString()));
				}

				Package package1 = containRelationService.findFileBelongToPackage(file1);
				Project project1 = containRelationService.findPackageBelongToProject(package1);
				if(!singleLanguage) {
					MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
					if(ms1 != null) {
						if(!isNodeToCytoscapeNode.getOrDefault(ms1, false)) {
							isNodeToCytoscapeNode.put(ms1, true);
							nodes.add(new CytoscapeNode(ms1.getId(), ms1.getName(), "MicroService"));
							msToZTreeNode.put(ms1, new ZTreeNode(ms1.getId(), ms1.getName(), false, "MicroService", true));
						}
						if(!isNodeToCytoscapeNode.getOrDefault(package1, false)) {
							isNodeToCytoscapeNode.put(package1, true);
							nodes.add(new CytoscapeNode(package1.getId(), package1.getDirectoryPath(), "Package"));
							nodeToZTreeNode.put(package1, new ZTreeNode(package1.getId(), package1.getDirectoryPath(), false, "Package", true));
						}
						String ms1ContainPackageId = String.join("_", String.valueOf(ms1.getId()), String.valueOf(package1.getId()));

						if(!isIdToCytoscapeEdge.getOrDefault(ms1ContainPackageId, false)) {
							isIdToCytoscapeEdge.put(ms1ContainPackageId, true);
							edges.add(new CytoscapeEdge(ms1, package1, "Contain"));
							msToZTreeNode.get(ms1).addChild(nodeToZTreeNode.get(package1));
						}
						String package1ContainFile1Id = String.join("_", String.valueOf(package1.getId()), String.valueOf(file1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(package1ContainFile1Id, false)) {
							isIdToCytoscapeEdge.put(package1ContainFile1Id, true);
							edges.add(new CytoscapeEdge(package1, file1, "Contain"));
							nodeToZTreeNode.get(package1).addChild(nodeToZTreeNode.get(file1));
						}
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(project1, false)) {
						isNodeToCytoscapeNode.put(project1, true);
						nodes.add(new CytoscapeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project1, new ZTreeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage() + ")", false, "Project", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(package1, false)) {
						isNodeToCytoscapeNode.put(package1, true);
						nodes.add(new CytoscapeNode(package1.getId(), package1.getDirectoryPath(), "Package"));
						nodeToZTreeNode.put(package1, new ZTreeNode(package1.getId(), package1.getDirectoryPath(), false, "Package", true));
					}
					String project1ContainPackageId = String.join("_", String.valueOf(project1.getId()), String.valueOf(package1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project1ContainPackageId, false)) {
						isIdToCytoscapeEdge.put(project1ContainPackageId, true);
						edges.add(new CytoscapeEdge(project1, package1, "Contain"));
						projectToZTreeNode.get(project1).addChild(nodeToZTreeNode.get(package1));
					}
					String package1ContainFile1Id = String.join("_", String.valueOf(package1.getId()), String.valueOf(file1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(package1ContainFile1Id, false)) {
						isIdToCytoscapeEdge.put(package1ContainFile1Id, true);
						edges.add(new CytoscapeEdge(package1, file1, "Contain"));
						nodeToZTreeNode.get(package1).addChild(nodeToZTreeNode.get(file1));
					}
				}
				Package package2 = containRelationService.findFileBelongToPackage(file2);
				Project project2 = containRelationService.findPackageBelongToProject(package2);
				if(!singleLanguage) {
					MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
					if(ms2 != null) {
						if(!isNodeToCytoscapeNode.getOrDefault(ms2, false)) {
							isNodeToCytoscapeNode.put(ms2, true);
							nodes.add(new CytoscapeNode(ms2.getId(), ms2.getName(), "MicroService"));
							msToZTreeNode.put(ms2, new ZTreeNode(ms2.getId(), ms2.getName(), false, "MicroService", true));
						}
						if(!isNodeToCytoscapeNode.getOrDefault(package2, false)) {
							isNodeToCytoscapeNode.put(package2, true);
							nodes.add(new CytoscapeNode(package2.getId(), package2.getDirectoryPath(), "Package"));
							nodeToZTreeNode.put(package2, new ZTreeNode(package2.getId(), package2.getDirectoryPath(), false, "Package", true));
						}
						String ms2ContainPackageId = String.join("_", String.valueOf(ms2.getId()), String.valueOf(package2.getId()));

						if(!isIdToCytoscapeEdge.getOrDefault(ms2ContainPackageId, false)) {
							isIdToCytoscapeEdge.put(ms2ContainPackageId, true);
							edges.add(new CytoscapeEdge(ms2, package2, "Contain"));
							msToZTreeNode.get(ms2).addChild(nodeToZTreeNode.get(package2));
						}
						String package2ContainFile2Id = String.join("_", String.valueOf(package2.getId()), String.valueOf(file2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(package2ContainFile2Id, false)) {
							isIdToCytoscapeEdge.put(package2ContainFile2Id, true);
							edges.add(new CytoscapeEdge(package2, file2, "Contain"));
							nodeToZTreeNode.get(package2).addChild(nodeToZTreeNode.get(file2));
						}
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(project2, false)) {
						isNodeToCytoscapeNode.put(project2, true);
						nodes.add(new CytoscapeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project2, new ZTreeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage() + ")", false, "Project", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(package2, false)) {
						isNodeToCytoscapeNode.put(package2, true);
						nodes.add(new CytoscapeNode(package2.getId(), package2.getDirectoryPath(), "Package"));
						nodeToZTreeNode.put(package2, new ZTreeNode(package2.getId(), package2.getDirectoryPath(), false, "Package", true));
					}
					String project2ContainPackageId = String.join("_", String.valueOf(project2.getId()), String.valueOf(package2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project2ContainPackageId, false)) {
						isIdToCytoscapeEdge.put(project2ContainPackageId, true);
						edges.add(new CytoscapeEdge(project2, package2, "Contain"));
						projectToZTreeNode.get(project2).addChild(nodeToZTreeNode.get(package2));
					}
					String package2ContainFile2Id = String.join("_", String.valueOf(package2.getId()), String.valueOf(file2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(package2ContainFile2Id, false)) {
						isIdToCytoscapeEdge.put(package2ContainFile2Id, true);
						edges.add(new CytoscapeEdge(package2, file2, "Contain"));
						nodeToZTreeNode.get(package2).addChild(nodeToZTreeNode.get(file2));
					}
				}
			}
		}
		
		JSONArray ztreeResult = new JSONArray();
		for(ZTreeNode node : projectToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		for(ZTreeNode node : msToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		if(showGroupNode) {
			nodes.addAll(groupNodes);
			edges.addAll(groupEdges);
			ZTreeNode groupAllNodes = new ZTreeNode(-1, "所有组", true, "NodeGroup", true);
			for(ZTreeNode node : groupZTreeNodes) {
				groupAllNodes.addChild(node);
			}
			ztreeResult.add(groupAllNodes);
		}
		result.put("ztree", ztreeResult);
		result.put("nodes", CytoscapeUtil.toNodes(nodes));
		result.put("edges", CytoscapeUtil.toEdges(edges));
		List<FileCloneWithCoChange> allClonesWithCoChange = new ArrayList<>(cloneAnalyseService.addCoChangeToFileClones(allClones));
		result.put("clonesWithCoChange", allClonesWithCoChange);
		//根据cochange次数降序排列
		allClonesWithCoChange.sort(new Comparator<FileCloneWithCoChange>() {
			@Override
			public int compare(FileCloneWithCoChange f1, FileCloneWithCoChange f2) {
				return Integer.compare(f2.getCochangeTimes(), f1.getCochangeTimes());
			}
		});
		return result;
	}


	/*
	*跨包克隆关系
	* */
	@Override
	public JSONArray graphFileClones(Collection<Clone> clones) {
		Map<CodeNode, Map<CodeNode, Clone>> values = new HashMap<>();
		Set<CodeNode> nodes = new HashSet<>();
		for(Clone clone : clones) {
			CodeNode node1 = clone.getCodeNode1();
			CodeNode node2 = clone.getCodeNode2();
			Map<CodeNode, Clone> temp = values.get(node1);
			if(temp == null) {
				temp = values.get(node2);
				if(temp == null) {
					temp = new HashMap<>();
				}
				temp.put(node1, clone);
				values.put(node2, temp);
			} else {
				temp.put(node2, clone);
				values.put(node1, temp);
			}
			nodes.add(node1);
			nodes.add(node2);
		}
		JSONArray result = new JSONArray();
		for(Map.Entry<CodeNode, Map<CodeNode, Clone>> entryNode1 : values.entrySet()) {
			CodeNode node1 = entryNode1.getKey();
			JSONObject nodeJSON = new JSONObject();
			nodeJSON.put("name", node1.getIdentifier());
			nodeJSON.put("id", node1.getId());
			JSONArray imports = new JSONArray();
			for(Map.Entry<CodeNode, Clone> entryNode2 : entryNode1.getValue().entrySet()) {
				CodeNode node2 = entryNode2.getKey();
				imports.add(node2.getIdentifier());
			}
			nodeJSON.put("imports", imports);
			result.add(nodeJSON);
			nodes.remove(node1);
		}
		for(CodeNode node : nodes) {
			JSONObject nodeJSON = new JSONObject();
			nodeJSON.put("name", node.getIdentifier());
			nodeJSON.put("id", node.getId());
			JSONArray imports = new JSONArray();
			nodeJSON.put("imports", imports);
			result.add(nodeJSON);
		}
		return result;
	}


	/*
	 *克隆组克隆关系
	 * */
	@Override
	public JSONArray graphFileCloneGroups(Collection<CloneGroup> groups) {
		JSONArray result = new JSONArray();
		Map<CodeNode, Map<CodeNode, Clone>> values = new HashMap<>();
		Set<CodeNode> nodes = new HashSet<>();
		for(CloneGroup cloneGroup : groups) {
			for(Clone cloneRelation : cloneGroup.getRelations()) {
				CodeNode node1 = cloneRelation.getCodeNode1();
				CodeNode node2 = cloneRelation.getCodeNode2();
				Map<CodeNode, Clone> temp = values.get(node1);
				if(temp == null) {
					temp = values.get(node2);
					if(temp == null) {
						temp = new HashMap<>();
					}
					temp.put(node1, cloneRelation);
					values.put(node2, temp);
				} else {
					temp.put(node2, cloneRelation);
					values.put(node1, temp);
				}
				nodes.add(node1);
				nodes.add(node2);
			}
		}
		for(Map.Entry<CodeNode, Map<CodeNode, Clone>> entryNode1 : values.entrySet()) {
			CodeNode node1 = entryNode1.getKey();
			JSONObject nodeJSON = new JSONObject();
			nodeJSON.put("name", node1.getIdentifier());
			nodeJSON.put("id", node1.getId());
			JSONArray imports = new JSONArray();
			for(Map.Entry<CodeNode, Clone> entryNode2 : entryNode1.getValue().entrySet()) {
				CodeNode node2 = entryNode2.getKey();
				imports.add(node2.getIdentifier());
			}
			nodeJSON.put("imports", imports);
			result.add(nodeJSON);
			nodes.remove(node1);
		}
		for(CodeNode node : nodes) {
			JSONObject nodeJSON = new JSONObject();
			nodeJSON.put("name", node.getIdentifier());
			nodeJSON.put("id", node.getId());
			JSONArray imports = new JSONArray();
			nodeJSON.put("imports", imports);
			result.add(nodeJSON);
		}
		return result;
	}

	@Override
	public JSONObject crossPackageCloneToCytoscape(Collection<CloneValueForDoubleNodes<Package>> cloneDoublePackages) {
		JSONObject result = new JSONObject();
		Set<Package> pcks = new HashSet<>();
		Map<String, CytoscapeNode> packageNodes = new HashMap<>();
		Map<String, CytoscapeEdge> cloneEdges = new HashMap<>();
		Map<String, CytoscapeNode> parentNodes = new HashMap<>();
		Map<String, CytoscapeEdge> containEdges = new HashMap<>();
		
		for(CloneValueForDoubleNodes<Package> clone : cloneDoublePackages) {
			boolean isSimilar = (boolean) clone.calculateValue(DefaultPackageCloneValueCalculator.getInstance());
			if(!isSimilar) {
				continue;
			}
			Package pck1 = clone.getNode1();
			Package pck2 = clone.getNode2();
			pcks.add(pck1);
			pcks.add(pck2);
			if(packageNodes.get(pck1.getId().toString()) == null) {
				packageNodes.put(pck1.getId().toString(), new CytoscapeNode(pck1.getId(), pck1.getDirectoryPath(), "Package"));
			}
			if(packageNodes.get(pck2.getId().toString()) == null) {
				packageNodes.put(pck2.getId().toString(), new CytoscapeNode(pck2.getId(), pck2.getDirectoryPath(), "Package"));
			}
			String edgeId = String.join("_", pck1.getId().toString(), pck2.getId().toString());
			if(cloneEdges.get(edgeId) == null) {
				cloneEdges.put(edgeId, new CytoscapeEdge(pck1.getId().toString(), pck2.getId().toString(), "Clone", String.valueOf(clone.getValue())));
			}
			
			
		}
		
		result.put("nodes", CytoscapeUtil.toNodes(packageNodes.values()));
		result.put("edges", CytoscapeUtil.toEdges(cloneEdges.values()));
		return result;
	}
}