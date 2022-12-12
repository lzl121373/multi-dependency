package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.GodComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/god")
public class GodComponentController {
	
	@Autowired
	private GodComponentDetector godComponentDetector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String godComponent(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", godComponentDetector.fileGodComponents());
		request.setAttribute("packages", godComponentDetector.packageGodComponents());
		return "godcomponents";
	}

	@GetMapping("/threshold/{projectId}")
	@ResponseBody
	public int[] getComponentThreshold(@PathVariable("projectId") long projectId) {
		int[] result = new int[2];
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			return result;
		}
		int minFileLoc = godComponentDetector.getProjectMinFileLoc(project);
		int minFileCountInPackage = godComponentDetector.getProjectMinFileCountInPackage(project);
		result[0] = minFileLoc;
		result[1] = minFileCountInPackage;
		return result;
	}
	
	@PostMapping("/threshold/{projectId}")
	@ResponseBody
	public boolean setComponentThreshold(@PathVariable("projectId") long projectId, 
			@RequestParam("minFileLoc") int minFileLoc, @RequestParam("minFileCountInPackage") int minFileCountInPackage) {
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			return false;
		}
		godComponentDetector.setProjectMinFileLoc(project, minFileLoc);
		godComponentDetector.setProjectMinFileCountInPackage(project, minFileCountInPackage);
		return true;
	}
	
}
