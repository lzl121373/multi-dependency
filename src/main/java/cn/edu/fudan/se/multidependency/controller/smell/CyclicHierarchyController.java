package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.smell.CyclicHierarchyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/cylic/hierarchy")
public class CyclicHierarchyController {
	
	@Autowired
	private CyclicHierarchyDetector cyclicHierarchyDetector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String cyclicHierarchy(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("types", cyclicHierarchyDetector.cyclicHierarchies());
		return "as/hierarchy";
	}

}
