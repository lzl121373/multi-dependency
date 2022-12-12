package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@RequestMapping("/package")
@Controller
public class PackageController {
	
	@Autowired
	NodeService nodeService;
	
	@Autowired
	ContainRelationService containRelationService;

	@GetMapping("/subpackages/{pckId}")
	@ResponseBody
	public Collection<Package> subPackages(@PathVariable("pckId") long packageId) {
		Package pck = nodeService.queryPackage(packageId);
		if(pck == null) {
			return new ArrayList<>();
		}
//		pck = new Package();
//		pck.setLanguage(Language.java.name());
//		pck.setDirectoryPath("/guava/src/com/google/common/");
		return containRelationService.findPackageContainPackages(pck);
	}
	
}
