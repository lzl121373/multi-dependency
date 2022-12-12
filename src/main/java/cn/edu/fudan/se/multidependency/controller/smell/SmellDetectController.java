package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/detect")
public class SmellDetectController {
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String index(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		return "as/smelldetect";
	}
}
