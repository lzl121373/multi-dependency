package cn.edu.fudan.se.multidependency.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.service.query.*;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

@Controller
@RequestMapping("/project")
public class ProjectController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private DependencyOrganizationService dependencyOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private MultipleService multipleService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private HotspotPackageDetector hotspotPackageDetector;

	private JSONArray nodesInPackagesForEcharts = new JSONArray();
	private JSONArray linksForEcharts = new JSONArray();

	@GetMapping("/graph")
	public String index() {
		return "dependency_graph/projectgraph";
	}

	@GetMapping("/treemap")
	public String treemap() {
		return "dependency_graph/treemap";
	}

	@GetMapping("/combo_chart")
	public String combo(@RequestParam(defaultValue = "-1",required = false) String projectId, HttpServletRequest request) {
		request.setAttribute("projectId", projectId);
		return "dependency_graph/combo_chart";
	}

	@GetMapping("/coupling_chart")
	public String coupling() {
		return "dependency_graph/coupling_chart";
	}

	@GetMapping("/tree")
	public String tree() {
		return "projecttree";
	}

	@GetMapping("/echarts")
	public String echartsHtml() {
		return "projectecharts";
	}

	@GetMapping("/all")
	@ResponseBody
	public Map<Long, Project> allProjects() {
		List<Project> projects = nodeService.allProjects();
		Map<Long, Project> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), project);
		}
		return result;
	}

	@GetMapping("/all/name")
	@ResponseBody
	public JSONArray allProjectsNames() {
		JSONArray result = new JSONArray();
		List<Project> projects = nodeService.allProjects();

		projects.sort(Comparator.comparing(Project::getName));

		for(Project project : projects) {
			JSONObject temp_project = new JSONObject();
			temp_project.put("id", project.getId().toString());
			temp_project.put("name", project.getName() + "(" + project.getLanguage() + ")");
			result.add(temp_project);
		}
		return result;
	}

	@GetMapping("/all/id")
	@ResponseBody
	public JSONObject allProjectsIds() {
		JSONObject result = new JSONObject();
		JSONArray pjlist = new JSONArray();
		List<Project> projects = nodeService.allProjects();

		projects.sort(Comparator.comparing(Project::getName));

		for(Project project : projects) {
			JSONObject temp_project = new JSONObject();
			temp_project.put("id", project.getId().toString());
			pjlist.add(temp_project);
		}

		result.put("projectIds", pjlist);
		return result;
	}
	
	@GetMapping(value = "/fanIO/file/{projectId}")
	@ResponseBody
	public Collection<Fan_IO<ProjectFile>> calculateFanIOs(@PathVariable("projectId") long id) {
		Project project = nodeService.queryProject(id);
		List<Fan_IO<ProjectFile>> result = staticAnalyseService.queryAllFileFanIOs(project);
		int minSize = 35;
		result = result.subList(0, result.size() < minSize ? result.size() : minSize);
		return result;
	}
	
	@GetMapping(value = "/all/{page}")
	@ResponseBody
	public List<Project> allProjectsByPage(@PathVariable("page") int page) {
		List<Project> result = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
		return result;
	}
	
	@GetMapping(value = "/pages/count")
	@ResponseBody
	public long queryMicroServicePagesCount() {
		long count = nodeService.allProjects().size();
		long pageCount = count % Constant.SIZE_OF_PAGE == 0 ? 
				count / Constant.SIZE_OF_PAGE : count / Constant.SIZE_OF_PAGE + 1;
		return pageCount;
	}

    private static final Executor executor = Executors.newCachedThreadPool();
    
    @GetMapping(value = "/ztree/function/variable")
    @ResponseBody
    public JSONObject functionContainNodesToZTree(@RequestParam("functionId") long id) {
    	JSONObject result = new JSONObject();
		try {
			Function function = nodeService.queryFunction(id);
			if(function == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			Collection<Variable> variables = containRelationService.findFunctionDirectlyContainVariables(function);
			if(!variables.isEmpty()) {
				ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
				for(Variable variable : variables) {
					ZTreeNode node = new ZTreeNode(variable, false);
					variableNodes.addChild(node);
				}
				values.add(variableNodes.toJSON());
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/type/{childrenType}")
    @ResponseBody
    public JSONObject typeContainNodesToZTree(@RequestParam("typeId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			Type type = nodeService.queryType(id);
			if(type == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "function":
				Collection<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function.getId(), function.getName() + function.getParametersIdentifies(), false, function.getNodeType().toString(), true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findTypeDirectlyContainFields(type);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("属性 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, false);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/namespace/{childrenType}")
    @ResponseBody
    public JSONObject namespaceContainNodesToZTree(@RequestParam("namespaceId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			Namespace namespace = nodeService.queryNamespace(id);
			if(namespace == null) {
				throw new Exception("namespace is null, namespaceId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "type":
				Collection<Type> types = containRelationService.findNamespaceDirectlyContainTypes(namespace);
				if(!types.isEmpty()) {
					ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + ")", true);
					for(Type type : types) {
						ZTreeNode node = new ZTreeNode(type, true);
						typeNodes.addChild(node);
					}
					values.add(typeNodes.toJSON());
				}
				break;
			case "function":
				Collection<Function> functions = containRelationService.findNamespaceDirectlyContainFunctions(namespace);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function.getId(), function.getName() + function.getParametersIdentifies(), false, function.getNodeType().toString(), true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findNamespaceDirectlyContainVariables(namespace);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, false);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/file/{childrenType}")
    @ResponseBody
    public JSONObject fileContainNodesToZTree(@RequestParam("fileId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			ProjectFile file = nodeService.queryFile(id);
			if(file == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "namespace":
				Collection<Namespace> namespaces = containRelationService.findFileContainNamespaces(file);
				if(!namespaces.isEmpty()) {
					ZTreeNode filesNodes = new ZTreeNode("命名空间 (" + namespaces.size() + ")", true);
					for(Namespace namespace : namespaces) {
						ZTreeNode node = new ZTreeNode(namespace, true);
						filesNodes.addChild(node);
					}
					values.add(filesNodes.toJSON());
				}
				break;
			case "type":
				Collection<Type> types = containRelationService.findFileDirectlyContainTypes(file);
				if(!types.isEmpty()) {
					ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + ")", true);
					for(Type type : types) {
						ZTreeNode node = new ZTreeNode(type, true);
						typeNodes.addChild(node);
					}
					values.add(typeNodes.toJSON());
				}
				break;
			case "function":
				Collection<Function> functions = containRelationService.findFileDirectlyContainFunctions(file);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function, true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findFileDirectlyContainVariables(file);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, true);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/file")
    @ResponseBody
    public JSONObject packageContainFilesToZTree(@RequestParam("packageId") long id) {
    	JSONObject result = new JSONObject();
		try {
			Package pck = nodeService.queryPackage(id);
			if(pck == null) {
				throw new Exception("package is null, pckId: " + id);
			}
			Collection<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			ZTreeNode filesNodes = new ZTreeNode(-1, "文件 (" + files.size() + ")", false, "FileList", true);
			for(ProjectFile file : files) {
				ZTreeNode node = new ZTreeNode(file, true);
				node.setNocheck(true);
				filesNodes.addChild(node);
			}
			List<Package> pcks = containRelationService.findPackageContainPackages(pck);
			ZTreeNode pcksnodes = new ZTreeNode(-1, "包 (" + pcks.size() + ")", false, "PckList", true);
			for(Package pck1 : pcks) {
				ZTreeNode node = new ZTreeNode(pck1.getId(), pck1.getDirectoryPath(), false, pck1.getNodeType().toString(), true);
				pcksnodes.addChild(node);
			}
			JSONArray values = new JSONArray();
			values.add(filesNodes.toJSON());
			values.add(pcksnodes.toJSON());
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/package")
    @ResponseBody
    public JSONObject allProjectContainPackagesByPageToZTree(@RequestParam("projectId") long projectId) {
    	JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception("project is null, projectId: " + projectId);
			}
			Collection<Package> rootPackage = containRelationService.findProjectRootPackages(project);
			JSONArray values = new JSONArray();
			for(Package pck : rootPackage){
				String name = pck.getName().equals(pck.getDirectoryPath()) ? pck.getDirectoryPath() : String.join(":", pck.getName(), pck.getDirectoryPath());
				ZTreeNode pckNodes = new ZTreeNode(pck.getId(), name, false, pck.getNodeType().toString(), true);
				values.add(pckNodes.toJSON());
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }

	@GetMapping(value = "/all/ztree/project/{page}")
	@ResponseBody
	public JSONObject allProjectsByPageToZtree(@PathVariable("page") int page) {
		JSONObject result = new JSONObject();
		try {
			Collection<Project> projects = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
			JSONArray values = new JSONArray();
			for(Project project : projects) {
				ZTreeNode node = new ZTreeNode(project, true);
				values.add(node.toJSON());
			}
			result.put("result", "success");
			result.put("values", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@PostMapping(value = "/all/ztree/project")
	@ResponseBody
	public JSONObject selectProjectsToZtree(@RequestBody String[] params) {
		JSONObject result = new JSONObject();
		try {
			Collection values = new ArrayList();
			int len = params.length;
			long projectId;
			int i;
			for(i = 0; i < len; i ++) {
				projectId = Long.parseLong(params[i]);
				Project project = nodeService.queryProject(projectId);
				ZTreeNode node = new ZTreeNode(project, true);
				node.setNocheck(true);
				ztreeNodeInitializer(node);
				values.add(node);
			}
			result.put("result", "success");
			result.put("values", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}

    private void ztreeNodeInitializer(ZTreeNode node){
		long nodeId = node.getId();
    	switch(node.getType()){
			case "Package":
				Package pck = nodeService.queryPackage(nodeId);
				Collection<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
				ZTreeNode filesNodes = new ZTreeNode(-1, "文件 (" + files.size() + ")", false, "FileList", true);
				for(ProjectFile file : files) {
					ZTreeNode fileNode = new ZTreeNode(file, false);
					fileNode.setNocheck(true);
					filesNodes.addChild(fileNode);
				}
				List<Package> pcks = containRelationService.findPackageContainPackages(pck);
				ZTreeNode pcksnodes = new ZTreeNode(-1, "包 (" + pcks.size() + ")", false, "PckList", true);
				for(Package pck1 : pcks) {
					ZTreeNode pckNode = new ZTreeNode(pck1.getId(), pck1.getDirectoryPath(), false, pck1.getNodeType().toString(), true);
					ztreeNodeInitializer(pckNode);
					pcksnodes.addChild(pckNode);
				}
				node.addChild(filesNodes);
				node.addChild(pcksnodes);
				break;
			case "Project":
				Project project = nodeService.queryProject(nodeId);
				Collection<Package> rootPackage = containRelationService.findProjectRootPackages(project);
				for(Package pck2 : rootPackage){
					String name = pck2.getName().equals(pck2.getDirectoryPath()) ? pck2.getDirectoryPath() : String.join(":", pck2.getName(), pck2.getDirectoryPath());
					ZTreeNode pckNodes = new ZTreeNode(pck2.getId(), name, false, pck2.getNodeType().toString(), true);
					ztreeNodeInitializer(pckNodes);
					node.addChild(pckNodes);
				}
    	}
    	return ;
	}
    
	@GetMapping(value = "/all/ztree/structure/{page}")
	@ResponseBody
	public JSONObject allProjectsContainStructureByPage(@PathVariable("page") int page) {
		JSONObject result = new JSONObject();
		try {
			Collection<Project> projects = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
			List<ZTreeNode> nodes = new ArrayList<>();
			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			LOGGER.info("开始时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
			CountDownLatch latch = new CountDownLatch(projects.size());
			List<FutureTask<ZTreeNode>> list = new ArrayList<>();
			for(Project project : projects) {
				FutureTask<ZTreeNode> s = new FutureTask<>(new ProjectStructureExtractor(project, multipleService, latch));
				list.add(s);
				executor.execute(s);
			}
			latch.await();
			for(FutureTask<ZTreeNode> t : list) {
				nodes.add(t.get());
			}
			LOGGER.info("结束时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
			JSONArray values = new JSONArray();
			for(ZTreeNode node : nodes) {
				values.add(node.toJSON());
			}
			result.put("result", "success");
			result.put("values", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request, @RequestParam(required=true, value="id") long id) {
		Project project = nodeService.queryProject(id);
		if(project == null) {
			request.setAttribute("error", "没有找到id为 " + id + " 的Project");
			return "error";
		} else {
			request.setAttribute("project", project);
			return "project";
		}
	}
	
	@GetMapping("/dynamiccall")
	@ResponseBody
	public void dynamicCall() {
		System.out.println("/project/dynamiccall");
	}
	
	@PostMapping("/absolute")
	@ResponseBody
	public JSONObject setAbsolutePath(@RequestBody Map<String, Object>[] params) {
    	int len = params.length;
		long projectId;
		String absolutePath;
		JSONObject result = new JSONObject();
		int i;
    	for(i = 0; i < len; i ++) {
			projectId = Long.parseLong(params[i].get("id").toString());
			absolutePath = (String) params[i].get("path");
			absolutePath = absolutePath.replace("\\", "/");
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				result.put("result", "fail");
				result.put("msg", "没找到Project");
				break;
			}
			projectService.setAbsolutePath(project, absolutePath);
			result.put("project", project);
			result.put("path", absolutePath);
		}
    	if(i == len) {
			result.put("result", "success");
		}
		return result;
	}
	
	@GetMapping("/absolute")
	@ResponseBody
	public JSONObject queryAbsolutePath(@RequestParam("id") long projectId) {
		JSONObject result = new JSONObject();
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			result.put("result", "fail");
			result.put("msg", "没找到Project");
			return result;
		}
		result.put("result", "success");
		result.put("project", project);
		result.put("path", projectService.getAbsolutePath(project));
		return result;
	}

	@PostMapping("/pckfilter")
	@ResponseBody
	public JSONObject setFilterPck(@RequestBody Map<String, Object>[] ids) {
		int len = ids.length;
		JSONObject result = new JSONObject();
		Map<String, Boolean> filterList = new HashMap<>();
		for(int i = 0; i < len; i ++) {
			long id = Long.parseLong(ids[i].get("id").toString());
			if(ids[i].get("type").equals("pck")){
				Package pck = nodeService.queryPackage(id);
				filterList.put(pck.getDirectoryPath(), true);
			}else if(ids[i].get("type").equals("FileList")){
				Package pck = nodeService.queryPackage(id);
				filterList.put(pck.getDirectoryPath(), false);
			}else{
				Package pck = nodeService.queryPackage(id);
				List<Package> subPckList = containRelationService.findPackageContainPackages(pck);
				for(Package subPck : subPckList){
					filterList.put(subPck.getDirectoryPath(), true);
				}
			}
		}
		projectService.setSelectedPcks(filterList);
		if(filterList.keySet().size() >0){
			result.put("result", "success");
			result.put("length", filterList.keySet().size());
			result.put("path", filterList.keySet());
		}
		return result;
	}

	@GetMapping("/clearfilter")
	@ResponseBody
	public JSONObject clearfilter() {
		JSONObject result = new JSONObject();
		projectService.clearSelectedPcks();
		if(projectService.getSelectedPcks().size() == 0){
			result.put("result", "success");
		}else{
			result.put("result", "fail");
		}
		return result;
	}
	
	/*@GetMapping("/absolute")
	@ResponseBody
	public JSONObject setAbsolutePath(@RequestParam("id") long projectId, @RequestParam("path") String absolutePath) {
//		long projectId = (long) params.get("id");
//		String absolutePath = (String) params.get("path");
		JSONObject result = new JSONObject();
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			result.put("result", "fail");
			result.put("msg", "没找到Project");
			return result;
		}
		projectService.setAbsolutePath(project, absolutePath);
		result.put("result", "success");
		result.put("project", project);
		result.put("path", absolutePath);
		return result;
	}*/
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject cytoscape(
			@RequestParam("projectId") Long projectId,
			@RequestParam("dependency") String dependency,
			@RequestParam("level") String level) {
		System.out.println("/project/cytoscape");
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception("没有找到id为 " + projectId + " 的项目");
			}
			if("dynamic".equals(dependency)) {
				List<DynamicCall> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				dependencyOrganizationService.dynamicCallDependency(calls);
				if("file".equals(level)) {
					result.put("value", dependencyOrganizationService.fileCallToCytoscape());
				} else if("directory".equals(level)) {
					result.put("value", dependencyOrganizationService.directoryCallToCytoscape());
				}
			}
			if("static".equals(dependency)) {
				result.put("value", dependencyOrganizationService.projectToCytoscape(project));
			}
			if("all".equals(dependency)) {
				List<DynamicCall> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				System.out.println("start to find clones");
//				Iterable<Clone> clones = basicCloneQueryService.queryProjectContainFunctionCloneFunctions(project);
//				result.put("value", dependencyOrganizationService.projectStaticAndDynamicToCytoscape(project, calls));
				System.out.println("end finding clones");
//				result.put("value", dependencyOrganizationService.projectToCytoscape(project, calls, clones));
				result.put("value", dependencyOrganizationService.projectToCytoscape(project));
				result.put("ztreenode", multipleService.projectToZTree(project));
			}
			System.out.println(result.get("value"));
			if(result.get("value") == null) {
				throw new Exception("结果暂无");
			}
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}

	@PostMapping("/has")
	@ResponseBody
	public JSONArray projectHas(@RequestBody JSONObject requestBody) {
		return projectService.getMultipleProjectsGraphJson(requestBody, Constant.PROJECT_STRUCTURE_CIRCLE_PACKING);
	}

	@PostMapping("/has/treemap")
	@ResponseBody
	public JSONArray projectHasTreeMap(@RequestBody JSONObject requestBody) {
		return projectService.getMultipleProjectsGraphJson(requestBody, Constant.PROJECT_STRUCTURE_TREEMAP);
	}

	@PostMapping("/has/combo")
	@ResponseBody
	public JSONArray projectHasCombo(@RequestBody JSONObject requestBody) {
		return projectService.getMultipleProjectsGraphJson(requestBody, Constant.PROJECT_STRUCTURE_COMBO);
	}

	/**
	 * 递归遍历项目中所有package的包含关系
	 */
	public void getHasJsonForEcharts(List<PackageStructure> childrenPackages){
		for(PackageStructure pckstru :childrenPackages){
			List<PackageStructure> pckList = pckstru.getChildrenPackages();
//			List<ProjectFile> fileList = pckstru.getChildrenFiles();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name",pckstru.getPck().getName());
			jsonObject.put("id", pckstru.getPck().getId().toString());
			JSONObject modularity_class = new JSONObject();
			modularity_class.put("modularity_class", 1);
			jsonObject.put("attributes", modularity_class);
			nodesInPackagesForEcharts.add(jsonObject);
//			if(fileList.size() > 0){
//				for(ProjectFile profile : fileList){
//					JSONObject linkBetweenPackageAndFile = new JSONObject();
//					linkBetweenPackageAndFile.put("source", pckstru.getPck().getId().toString());
//					linkBetweenPackageAndFile.put("target", profile.getId().toString());
//					linksForEcharts.add(linkBetweenPackageAndFile);
//					JSONObject jsonObject2 = new JSONObject();
//					jsonObject2.put("long_name",profile.getPath());
//					jsonObject2.put("name",profile.getName());
//					jsonObject2.put("id",profile.getId().toString());
//					JSONObject modularity_class_file = new JSONObject();
//					modularity_class.put("modularity_class", 2);
//					jsonObject2.put("attributes", modularity_class);
//					nodesInPackagesForEcharts.add(jsonObject2);
//				}
//			}

			if(pckList.size()>0){//如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
				for(PackageStructure pckstru_children : pckList){
					JSONObject linkBetweenPackageAndPackage = new JSONObject();
					linkBetweenPackageAndPackage.put("source", pckstru.getPck().getId().toString());
					linkBetweenPackageAndPackage.put("target", pckstru_children.getPck().getId().toString());
					linksForEcharts.add(linkBetweenPackageAndPackage);
				}
				getHasJsonForEcharts(pckList);
			}
//			System.out.println(pckList.size());
		}
	}
}
