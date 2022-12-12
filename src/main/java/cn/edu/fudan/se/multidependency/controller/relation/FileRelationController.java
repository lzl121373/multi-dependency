package cn.edu.fudan.se.multidependency.controller.relation;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.service.query.metric.MetricShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.CommitQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.relation.FileRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/relation/file/{fileId}")
public class FileRelationController {

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private MetricShowService metricShowService;
	
	@Autowired
	private FileRelationService fileRelationService;
	
	@Autowired
	private IssueQueryService issueService;
	
	@Autowired
	private CommitQueryService commitService;

	@GetMapping("")
	public String index(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		request.setAttribute("file", file);
		request.setAttribute("pck", containRelationService.findFileBelongToPackage(file));
		request.setAttribute("project", containRelationService.findFileBelongToProject(file));
		return "relation/file";
	}
	
	@GetMapping("/metric")
	@ResponseBody
	public Object metric(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return metricShowService.getFileMetric(file);
	}
	
	@GetMapping("/contain/type")
	@ResponseBody
	public Object containType(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return containRelationService.findFileDirectlyContainTypes(file);
	}

	@GetMapping("/contain/namespace")
	@ResponseBody
	public Object containNamespace(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return containRelationService.findFileContainNamespaces(file);
	}

	@GetMapping("/contain/variable")
	@ResponseBody
	public Object containVariable(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return containRelationService.findFileDirectlyContainVariables(file);
	}

	@GetMapping("/contain/function")
	@ResponseBody
	public Object containFunction(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return containRelationService.findFileDirectlyContainFunctions(file);
	}
	
	@GetMapping("/import/type")
	@ResponseBody
	public Object importType(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportTypeRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/import/function")
	@ResponseBody
	public Object importFunction(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportFunctionRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/import/variable")
	@ResponseBody
	public Object importVariable(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportVariableRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/dependsOn")
	@ResponseBody
	public Object dependsOn(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findFileDependsOn(file);
	}
	
	@GetMapping("/dependedBy")
	@ResponseBody
	public Object dependedOn(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findFileDependedOnBy(file);
	}
	
	@GetMapping("/dependsOn/cytoscape")
	@ResponseBody
	public Object dependsOnCytoscape(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return fileRelationService.dependsOnCytoscape(file);
	}
	
	@GetMapping("/cochange")
	@ResponseBody
	public Object cochange(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return gitAnalyseService.cochangesWithFile(file);
	}
	
	@GetMapping("/commit")
	@ResponseBody
	public Object commit(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return commitService.queryUpdatedByCommits(file);
	}
	
	@GetMapping("/commit/matrix")
	@ResponseBody
	public Object commitMatrix(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return commitService.queryUpdatedFilesByCommitsInFile(file);
	}
	
	@GetMapping("/issue")
	@ResponseBody
	public Object issue(@PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return issueService.queryRelatedIssuesOnFile(file);
	}
}
