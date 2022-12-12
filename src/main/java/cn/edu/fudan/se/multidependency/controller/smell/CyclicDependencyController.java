package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/as/cyclicdependency")
public class CyclicDependencyController {

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	SmellDetectorService smellDetectorService;

	@GetMapping("/query")
	public String queryCyclicDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("typeCyclicDependencyMap", null);
					request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.queryFileCyclicDependency());
					request.setAttribute("packageCyclicDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("typeCyclicDependencyMap", null);
					request.setAttribute("fileCyclicDependencyMap", null);
					request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.queryPackageCyclicDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("typeCyclicDependencyMap", cyclicDependencyDetector.queryTypeCyclicDependency());
					request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.queryFileCyclicDependency());
					request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.queryPackageCyclicDependency());
					break;
			}
		}
		return "as/cyclicdependency";
	}

	@GetMapping("/detect")
	public String detectCyclicDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("typeCyclicDependencyMap", null);
					request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.detectFileCyclicDependency());
					request.setAttribute("packageCyclicDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("typeCyclicDependencyMap", null);
					request.setAttribute("fileCyclicDependencyMap", null);
					request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.detectPackageCyclicDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("typeCyclicDependencyMap", cyclicDependencyDetector.detectTypeCyclicDependency());
					request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.detectFileCyclicDependency());
					request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.detectPackageCyclicDependency());
					break;
			}
		}
		return "as/cyclicdependency";
	}
}
