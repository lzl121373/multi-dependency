package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;

@Controller
@RequestMapping("/as/implicitcrossmoduledependency")
public class ImplicitCrossModuleDependencyController {

	@Autowired
	private ImplicitCrossModuleDependencyDetector implicitCrossModuleDependencyDetector;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectRepository projectRepository;
	
	@GetMapping("/query")
	public String queryImplicitCrossModuleDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.queryFileImplicitCrossModuleDependency());
					request.setAttribute("packageImplicitCrossModuleDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", null);
					request.setAttribute("packageImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.queryPackageImplicitCrossModuleDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.queryFileImplicitCrossModuleDependency());
					request.setAttribute("packageImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.queryPackageImplicitCrossModuleDependency());
					break;
			}
		}
		return "as/implicitcrossmoduledependency";
	}

	@GetMapping("/detect")
	public String detectImplicitCrossModuleDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.detectFileImplicitCrossModuleDependency());
					request.setAttribute("packageImplicitCrossModuleDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", null);
					request.setAttribute("packageImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.detectPackageImplicitCrossModuleDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.detectFileImplicitCrossModuleDependency());
					request.setAttribute("packageImplicitCrossModuleDependencyMap", implicitCrossModuleDependencyDetector.detectPackageImplicitCrossModuleDependency());
					break;
			}
		}
		return "as/implicitcrossmoduledependency";
	}
	
	@GetMapping("/cochange/{projectId}")
	@ResponseBody
	public Integer[] getProjectMinCoChange(@PathVariable("projectId") Long projectId) {
		Integer[] result = new Integer[2];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			result[0] = implicitCrossModuleDependencyDetector.getFileMinCoChange(projectId);
			result[1] = implicitCrossModuleDependencyDetector.getPackageMinCoChange(projectId);
		}
		return result;
	}
	
	@PostMapping("/cochange/{projectId}")
	@ResponseBody
	public boolean setProjectMinCoChange(@PathVariable("projectId") Long projectId, @RequestParam("minFileCoChange") int minFileCoChange, @RequestParam("minPackageCoChange") int minPackageCoChange) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			implicitCrossModuleDependencyDetector.setProjectFileMinCoChange(projectId, minFileCoChange);
			implicitCrossModuleDependencyDetector.setProjectPackageMinCoChange(projectId, minPackageCoChange);
			return true;
		}
		return false;
	}
}
