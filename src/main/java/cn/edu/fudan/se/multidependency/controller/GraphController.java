package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.GraphService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueServiceImpl;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileGraph;
import cn.edu.fudan.se.multidependency.service.query.data.PackageGraph;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/graph")
public class GraphController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private CloneValueServiceImpl cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private GraphService graphService;
	
	@RequestMapping("/file")
	@ResponseBody
	public JSONObject fileClones() {
		JSONObject result = new JSONObject();
		FileGraph fileGraph = new FileGraph();
//		fileGraph.setFiles(nodeService.queryAllFiles());
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		fileGraph.setFileClones(fileClones);
		fileGraph.setContainRelationService(containRelationService);
//		return fileGraph.matrix();
		return fileGraph.matrixForClone();
	}
	
	@RequestMapping("/file/cycle/{projectId}")
	@ResponseBody
	public JSONObject fileCycle(@PathVariable("projectId") long projectId) {
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception();
			}
			result.put("data", graphService.cycleFiles(project));
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@RequestMapping("/package/cycle/{projectId}")
	@ResponseBody
	public JSONObject packageCycle(@PathVariable("projectId") long projectId) {
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception();
			}
			result.put("data", graphService.cyclePackages(project));
		} catch (Exception e) {
			
		}
		return result;
	}
	@RequestMapping("/package/cytoscape/{projectId}")
	@ResponseBody
	public JSONObject packageStructureCytoscapeGraph(@PathVariable("projectId") long projectId) {
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception();
			}
			System.out.println(project);
			result.put("data", graphService.packageToCytoscape(project));
		} catch (Exception e) {
			
		}
		return result;
	}
	@RequestMapping("/package/clone/{projectId}")
	@ResponseBody
	public JSONObject packageCloneGraph(@PathVariable("projectId") long projectId) {
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception();
			}
			System.out.println(project);
			result.put("matrix", graphService.cloneMatrix(project));
		} catch (Exception e) {
			
		}
		return result;
	}
	@RequestMapping("/package/structure/{projectId}")
	@ResponseBody
	public JSONObject packageStructureGraph(@PathVariable("projectId") long projectId) {
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception();
			}
			System.out.println(project);
			result.put("data", graphService.staticGraphDependency(project));
			result.put("matrix", graphService.strcutureMatrix(project));
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@RequestMapping("/package")
	@ResponseBody
	public JSONObject packageGraph() {
		JSONObject result = new JSONObject();
		try {
			PackageGraph graph = new PackageGraph();
			List<Package> packages = new ArrayList<>(nodeService.queryAllPackages());
			graph.setPackages(packages);
			for(Package pck : packages) {
				graph.add(pck, containRelationService.findPackageBelongToProject(pck));
			}
			Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
			graph.setPackageCloneValues(cloneValueService.queryPackageCloneFromFileClone(fileClones));
			result.put("result", "success");
			result.put("data", graph.changeToEChartsGraph());
		} catch (Exception e) {
			
		}
		return result;
	}
	
}
