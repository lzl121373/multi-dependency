package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
public class MDController {

	@Autowired
	private NodeService nodeService;
	
	@GetMapping(value= {"/",})
	public String index(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		return "overview";
	}

	@GetMapping(value= {"/index"})
	public String index2(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		return "index";
	}

	@GetMapping(value= {"/overview"})
	public String overview() {
		return "overview";
	}
	
}
