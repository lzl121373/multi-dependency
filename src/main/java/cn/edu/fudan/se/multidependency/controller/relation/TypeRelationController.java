package cn.edu.fudan.se.multidependency.controller.relation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/relation/type/{typeId}")
public class TypeRelationController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@GetMapping("")
	public String index(HttpServletRequest request, @PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		ProjectFile file = containRelationService.findTypeBelongToFile(type);
		request.setAttribute("type", type);
		request.setAttribute("file", file);
		request.setAttribute("pck", containRelationService.findFileBelongToPackage(file));
		request.setAttribute("project", containRelationService.findFileBelongToProject(file));
		return "relation/type";
	}
	
	@GetMapping("/contain/field")
	@ResponseBody
	public Object contain(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return containRelationService.findTypeDirectlyContainFields(type);
	}
	
	@GetMapping("/contain/function")
	@ResponseBody
	public Object containFunction(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return containRelationService.findTypeDirectlyContainFunctions(type);
	}
	
	@GetMapping("/extends/super")
	@ResponseBody
	public Object extendsRelation(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return staticAnalyseService.queryExtendsSuperTypes(type);
	}
	
	@GetMapping("/extends/sub")
	@ResponseBody
	public Object extendedRelation(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return staticAnalyseService.queryExtendsSubTypes(type);
	}
	
	@GetMapping("/implements/super")
	@ResponseBody
	public Object implementsRelation(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return staticAnalyseService.queryImplementsSuperTypes(type);
	}
	
	@GetMapping("/implements/sub")
	@ResponseBody
	public Object implementedRelation(@PathVariable("typeId") long id) {
		Type type = nodeService.queryType(id);
		return staticAnalyseService.queryImplementsSubTypes(type);
	}
}
