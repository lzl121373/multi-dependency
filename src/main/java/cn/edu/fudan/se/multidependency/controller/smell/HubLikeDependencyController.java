package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.HubLikeDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/hublikedependency")
public class HubLikeDependencyController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private HubLikeDependencyDetector hubLikeDependencyDetector;

	@Autowired
	private ProjectRepository projectRepository;
	
	@GetMapping("/query")
	public String queryHubLikeDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileHubLikeDependencyMap", hubLikeDependencyDetector.queryFileHubLikeDependency());
					request.setAttribute("packageHubLikeDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileHubLikeDependencyMap", null);
					request.setAttribute("packageHubLikeDependencyMap", hubLikeDependencyDetector.queryPackageHubLikeDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileHubLikeDependencyMap", hubLikeDependencyDetector.queryFileHubLikeDependency());
					request.setAttribute("packageHubLikeDependencyMap", hubLikeDependencyDetector.queryPackageHubLikeDependency());
					break;
			}
		}
		return "as/hublikedependency";
	}

	@GetMapping("/detect")
	public String detectHubLikeDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileHubLikeDependencyMap", hubLikeDependencyDetector.detectFileHubLikeDependency());
					request.setAttribute("packageHubLikeDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileHubLikeDependencyMap", null);
					request.setAttribute("packageHubLikeDependencyMap", hubLikeDependencyDetector.detectPackageHubLikeDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileHubLikeDependencyMap", hubLikeDependencyDetector.detectFileHubLikeDependency());
					request.setAttribute("packageHubLikeDependencyMap", hubLikeDependencyDetector.detectPackageHubLikeDependency());
					break;
			}
		}
		return "as/hublikedependency";
	}
	
	@GetMapping("/fanio/{projectId}")
	@ResponseBody
	public Integer[] getMinFanIO(@PathVariable("projectId") long projectId) {
		Integer[] result = new Integer[4];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			Integer[] minFileIO = hubLikeDependencyDetector.getProjectMinFileFanIO(project.getId());
			Integer[] minPackageIO = hubLikeDependencyDetector.getProjectMinPackageFanIO(project.getId());
			result[0] = minFileIO[0];
			result[1] = minFileIO[1];
			result[2] = minPackageIO[0];
			result[3] = minPackageIO[1];
		}
		return result;
	}

	@PostMapping("/fanio/{projectId}")
	@ResponseBody
	public boolean setMinFanIO(@PathVariable("projectId") long projectId,
								@RequestParam("minFileFanIn") int minFileFanIn, @RequestParam("minFileFanOut") int minFileFanOut,
								@RequestParam("minPackageFanIn") int minPackageFanIn, @RequestParam("minPackageFanOut") int minPackageFanOut) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			hubLikeDependencyDetector.setProjectMinFileFanIO(project.getId(), minFileFanIn, minFileFanOut);
			hubLikeDependencyDetector.setProjectMinPackageFanIO(project.getId(), minPackageFanIn, minPackageFanOut);
			return true;
		}
		return false;
	}
}
