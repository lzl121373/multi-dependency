package cn.edu.fudan.se.multidependency.controller.smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.smell.impl.FilesCommitsService;
import cn.edu.fudan.se.multidependency.service.query.history.data.CommitsInFileMatrix;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssuesInFileMatrix;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/matrix")
public class MatrixController {

	@Autowired
	private FilesCommitsService service;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private NodeService nodeService;
	
	@RequestMapping("")
	public String matrix(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		String allFilesIds = params.get("allFiles")[0];
		String specifiedFilesIds = params.get("specifiedFiles")[0];
		List<ProjectFile> allFiles = new ArrayList<>();
		List<ProjectFile> specifiedFiles = new ArrayList<>();
		int minCount = Integer.valueOf(params.get("minCount")[0]);
		for(String idStr : allFilesIds.split(",")) {
			ProjectFile file = nodeService.queryFile(Long.valueOf(idStr));
			allFiles.add(file);
		}
		if(!StringUtils.isBlank(specifiedFilesIds)) {
			for(String idStr : specifiedFilesIds.split(",")) {
				ProjectFile file = nodeService.queryFile(Long.valueOf(idStr));
				specifiedFiles.add(file);
			}
		}
		Project project = containRelationService.findFileBelongToProject(allFiles.get(0));
		CommitsInFileMatrix matrixForCommits = service.findFilesCommits(allFiles, specifiedFiles, minCount, project);
		IssuesInFileMatrix matrixForIssues = service.findFilesIssues(allFiles, specifiedFiles, minCount, project);
		request.setAttribute("matrixForCommits", matrixForCommits);
		request.setAttribute("matrixForIssues", matrixForIssues);
		return "as/matrix";
	}
	
}
