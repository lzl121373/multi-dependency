package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/unusedinclude")
public class UnusedIncludeController {

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectRepository projectRepository;

	@GetMapping("/query")
	public String queryUnusedInclude(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.queryFileUnusedInclude());
					break;
				case SmellLevel.PACKAGE:
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.queryFileUnusedInclude());
					break;
			}
		}
		return "as/unusedinclude";
	}

	@GetMapping("/detect")
	public String detectUnusedInclude(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.detectFileUnusedInclude());
					break;
				case SmellLevel.PACKAGE:
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.detectFileUnusedInclude());
					break;
			}
		}
		return "as/unusedinclude";
	}

	@PostMapping("/export")
	@ResponseBody
	public boolean setMinFanIO(@RequestParam("projectid") long projectId) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			unusedIncludeDetector.exportUnusedInclude(project);
			return true;
		}
		return false;
	}
}
