package cn.edu.fudan.se.multidependency.service.query.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeNode;

@Service
public class FileRelationService {

	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	public JSONObject dependsOnCytoscape(ProjectFile file) {
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		
		Collection<DependsOn> dependsOn = staticAnalyseService.findFileDependsOn(file);
		Collection<DependsOn> dependedOnBy = staticAnalyseService.findFileDependedOnBy(file);
		String mainFileId = "main" + file.getId();
		nodes.add(new CytoscapeNode(mainFileId, file.getName(), "MainFile", file.getPath()));
		
		for(DependsOn depends : dependsOn) {
			ProjectFile f = (ProjectFile) depends.getEndNode();
			String id = "depends" + f.getId();
			nodes.add(new CytoscapeNode(id, f.getName(), "DependsOnFile", f.getPath()));
			edges.add(new CytoscapeEdge(mainFileId, id, "DependsOn", String.valueOf(depends.getTimes())));
		}
		
		for(DependsOn depends : dependedOnBy) {
			ProjectFile f = (ProjectFile) depends.getStartNode();
			String id = "depended" + f.getId();
			nodes.add(new CytoscapeNode(id, f.getName(), "DependedOnFile", f.getPath()));
			edges.add(new CytoscapeEdge(id, mainFileId, "DependsOn", String.valueOf(depends.getTimes())));
		}
		
		result.put("nodes", CytoscapeUtil.toNodes(nodes));
		result.put("edges", CytoscapeUtil.toEdges(edges));
		return result;
	}
	
}
