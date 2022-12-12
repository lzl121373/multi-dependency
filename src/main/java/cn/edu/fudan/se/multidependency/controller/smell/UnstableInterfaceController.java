package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableInterfaceDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/unstableinterface")
public class UnstableInterfaceController {
	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private UnstableInterfaceDetector unstableInterfaceDetector;
	
	@GetMapping("/query")
	public String queryUnstableInterface(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnstableInterfaceMap", unstableInterfaceDetector.queryFileUnstableInterface());
					request.setAttribute("packageUnstableInterfaceMap", null);
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnstableInterfaceMap", unstableInterfaceDetector.queryFileUnstableInterface());
					request.setAttribute("packageUnstableInterfaceMap", null);
					break;
			}
		}
		return "as/unstableinterface";
	}
	@GetMapping("/detect")
	public String detectUnstableInterface(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnstableInterfaceMap", unstableInterfaceDetector.detectFileUnstableInterface());
					request.setAttribute("packageUnstableInterfaceMap", null);
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnstableInterfaceMap", unstableInterfaceDetector.detectFileUnstableInterface());
					request.setAttribute("packageUnstableInterfaceMap", null);
					break;
			}
		}
		return "as/unstableinterface";
	}
	
	@GetMapping("/threshold/{projectId}")
	@ResponseBody
	public Double[] getThreshold(@PathVariable("projectId") long projectId) {
		Double[] result = new Double[3];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			result[0] = Double.valueOf(unstableInterfaceDetector.getProjectMinFileFanIn(projectId));
			result[1] = Double.valueOf(unstableInterfaceDetector.getProjectFileMinCoChange(projectId));
			result[2] = unstableInterfaceDetector.getProjectMinRatio(projectId);
		}
		return result;
	}
	
	@PostMapping("/threshold/{projectId}")
	@ResponseBody
	public boolean setThreshold(@PathVariable("projectId") Long projectId,
												 @RequestParam("minFileFanIn") Integer minFileFanIn,
								                 @RequestParam("coChangeTimes") Integer coChangeTimes,
												 @RequestParam("minRatio") Double minRatio) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			unstableInterfaceDetector.setProjectMinFileFanIn(project.getId(), minFileFanIn);
			unstableInterfaceDetector.setProjectFileMinCoChange(project.getId(), coChangeTimes);
			unstableInterfaceDetector.setProjectMinRatio(project.getId(), minRatio);
			return true;
		}
		return false;
	}

}
