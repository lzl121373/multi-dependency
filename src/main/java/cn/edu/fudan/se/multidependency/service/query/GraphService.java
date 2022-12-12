package cn.edu.fudan.se.multidependency.service.query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
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
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.data.Graph;
import cn.edu.fudan.se.multidependency.service.query.data.MatrixContent;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeNode;

@Service
public class GraphService {

	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private DependsOnRepository DependOnRepository;
	
	public Collection<Collection<ProjectFile>> cycleFiles(Project project) {
		List<Collection<ProjectFile>> result = new ArrayList<>();
		Graph graph = new Graph();
		
		List<StructureRelation> relations = staticAnalyseService.findProjectContainStructureRelations(project);
		for(StructureRelation relation : relations) {
			CodeNode startNode = relation.getStartCodeNode();
			CodeNode endNode = relation.getEndCodeNode();
			ProjectFile startFile = containRelationService.findCodeNodeBelongToFile(startNode);
			ProjectFile endFile = containRelationService.findCodeNodeBelongToFile(endNode);
			graph.addNode(startFile);
			graph.addNode(endFile);
			graph.addEdge(new DependsOn(startFile, endFile));
		}
		System.out.println("计算强连通图");
		graph.computeStronglyConnectedComponents();
		
		for(List<Node> vs : graph.getStronglyConnectedComponents()) {
			if(vs.size() > 1) {
				List<ProjectFile> group = new ArrayList<>();
				for(Node v : vs) {
					group.add((ProjectFile) v);
				}
				result.add(group);
			}
		}
		System.out.println(result.size());
		for(Collection<ProjectFile> pcks : result) {
			System.out.println(pcks);
		}
		return result;
	}
	
	public Collection<Collection<Package>> cyclePackages(Project project) {
		List<Collection<Package>> result = new ArrayList<>();
		Graph graph = new Graph();
		
		List<StructureRelation> relations = staticAnalyseService.findProjectContainStructureRelations(project);
		for(StructureRelation relation : relations) {
			CodeNode startNode = relation.getStartCodeNode();
			CodeNode endNode = relation.getEndCodeNode();
			Package startPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(startNode));
			Package endPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(endNode));
			graph.addNode(startPackage);
			graph.addNode(endPackage);
			graph.addEdge(new DependsOn(startPackage, endPackage));
		}
		graph.computeStronglyConnectedComponents();
		
		for(List<Node> vs : graph.getStronglyConnectedComponents()) {
			if(vs.size() > 1) {
				List<Package> group = new ArrayList<>();
				for(Node v : vs) {
					group.add((Package) v);
				}
				result.add(group);
			}
		}
		System.out.println(result.size());
		for(Collection<Package> pcks : result) {
			System.out.println(pcks);
		}
		return result;
	}
	
	@Deprecated
	public JSONObject packageToCytoscape(Project project) {
		JSONObject data = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();

		Set<Package> packages = new HashSet<>();
		Map<Package, Set<Package>> packageRelations = new HashMap<>();
		
		List<StructureRelation> relations = staticAnalyseService.findProjectContainStructureRelations(project);
		for(StructureRelation relation : relations) {
			CodeNode startNode = relation.getStartCodeNode();
			CodeNode endNode = relation.getEndCodeNode();
			Package startPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(startNode));
			Package endPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(endNode));
			if(startPackage.equals(endPackage)) {
				continue;
			}
			if(!packages.contains(startPackage)) {
				nodes.add(new CytoscapeNode(startPackage.getId(), startPackage.getDirectoryPath(), "Package").toJSON());
				packages.add(startPackage);
			}
			if(!packages.contains(endPackage)) {
				nodes.add(new CytoscapeNode(endPackage.getId(), endPackage.getDirectoryPath(), "Package").toJSON());
				packages.add(endPackage);
			}
			Set<Package> callerDependesPackages = packageRelations.getOrDefault(startPackage, new HashSet<>());
			if(!callerDependesPackages.contains(endPackage)) {
				callerDependesPackages.add(endPackage);
				edges.add(new CytoscapeEdge(startPackage, endPackage, "Depends").toJSON());
			}
		}
		data.put("nodes", nodes);
		data.put("edges", edges);
		return data;
	}
	
	public MatrixContent<Package, Package>[][] cloneMatrix(Project project) {
		List<Package> packages = new ArrayList<>(containRelationService.findProjectContainPackages(project));
		packages.sort((p1, p2) -> {
			return p1.getDirectoryPath().compareTo(p2.getDirectoryPath());
		});
		MatrixContent<Package, Package>[][] result = (MatrixContent<Package, Package>[][]) Array.newInstance(MatrixContent.class, packages.size(), packages.size());
		Map<Package, Integer> pcksToIndex = new HashMap<>();
		for(int i = 0; i < packages.size(); i++) {
			for(int j = 0; j < packages.size(); j++) {
				MatrixContent<Package, Package> defaultContent = new MatrixContent<>(packages.get(i), packages.get(j), i, j);
				result[i][j] = defaultContent;
			}
			pcksToIndex.put(packages.get(i), i);
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Collection<Clone> clones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		for(Clone clone : clones) {
			CodeNode node1 = clone.getCodeNode1();
			CodeNode node2 = clone.getCodeNode2();
			Package startPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(node1));
			Package endPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(node2));
			if(!packages.contains(startPackage) || !packages.contains(endPackage)) {
				continue;
			}
			int i = pcksToIndex.get(startPackage);
			int j = pcksToIndex.get(endPackage);
			MatrixContent<Package, Package> content = result[i][j];
			content.setIntegerValue(content.getIntegerValue() + 1);
			content.addContent(clone.getRelationType());
			
			j = pcksToIndex.get(startPackage);
			i = pcksToIndex.get(endPackage);
			content = result[j][i];
			content.setIntegerValue(content.getIntegerValue() + 1);
			content.addContent(clone.getRelationType());
			
			min = Math.min(min, content.getIntegerValue());
			max = Math.max(max, content.getIntegerValue());
		}
		
		return result;
	}
	
	public MatrixContent<Package, Package>[][] strcutureMatrix(Project project) {
		List<Package> packages = new ArrayList<>(containRelationService.findProjectContainPackages(project));
		packages.sort((p1, p2) -> {
			return p1.getDirectoryPath().compareTo(p2.getDirectoryPath());
		});
		Map<Package, Integer> pcksToIndex = new HashMap<>();
		for(int i = 0; i < packages.size(); i++) {
			pcksToIndex.put(packages.get(i), i);
		}
		MatrixContent<Package, Package>[][] result = (MatrixContent<Package, Package>[][]) Array.newInstance(MatrixContent.class, packages.size(), packages.size());
		
		List<StructureRelation> relations = staticAnalyseService.findProjectContainStructureRelations(project);
		for(StructureRelation relation : relations) {
			CodeNode startNode = relation.getStartCodeNode();
			CodeNode endNode = relation.getEndCodeNode();
			Package startPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(startNode));
			Package endPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(endNode));
			int i = pcksToIndex.get(startPackage);
			int j = pcksToIndex.get(endPackage);
			MatrixContent<Package, Package> content = result[i][j];
			if(content == null) {
				content = new MatrixContent<>();
				content.setStart(startPackage);
				content.setEnd(endPackage);
				content.setI(i);
				content.setJ(j);
				result[i][j] = content;
			}
			content.addContent(relation.getRelationType());
		}		
		
		return result;
	}

	
	public JSONObject staticGraphDependency(Project project) {
		JSONObject result = new JSONObject();
		
		result.put("type", "force");
		JSONArray packagesArray = new JSONArray();
		JSONArray lengendsArray = new JSONArray();
		
		Collection<Package> packages = containRelationService.findProjectContainPackages(project);
		System.out.println("size: " + packages.size());
		lengendsArray.add("package");
		for(Package pck : packages) {
			JSONObject pckJson = new JSONObject();
			pckJson.put("name", pck.getDirectoryPath());
			pckJson.put("id", pck.getId().toString());
			packagesArray.add(pckJson);
		}
		result.put("nodes", packagesArray);
		result.put("legend", lengendsArray);
		
		JSONArray linksArray = new JSONArray();
		Map<Package, Set<Package>> packageRelations = new HashMap<>();
		
		List<StructureRelation> relations = staticAnalyseService.findProjectContainStructureRelations(project);
		for(StructureRelation relation : relations) {
			CodeNode startNode = relation.getStartCodeNode();
			CodeNode endNode = relation.getEndCodeNode();
			Package startPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(startNode));
			Package endPackage = containRelationService.findFileBelongToPackage(containRelationService.findCodeNodeBelongToFile(endNode));
			if(startPackage.equals(endPackage)) {
				continue;
			}
			Set<Package> callerDependesPackages = packageRelations.getOrDefault(startPackage, new HashSet<>());
			if(!callerDependesPackages.contains(endPackage)) {
				callerDependesPackages.add(endPackage);
				JSONObject linkJson = new JSONObject();
				linkJson.put("source", startPackage.getId().toString());
				linkJson.put("target", endPackage.getId().toString());
				linksArray.add(linkJson);
			}
		}
		
		result.put("links", linksArray);
		
		return result;
	}
}
