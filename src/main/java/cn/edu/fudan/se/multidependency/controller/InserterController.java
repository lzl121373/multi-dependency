package cn.edu.fudan.se.multidependency.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/insert")
public class InserterController {
	private static final Logger LOGGER = LoggerFactory.getLogger(InserterController.class);
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("/")
	public String index() {
		return "insert/insert";
	}
	
}
