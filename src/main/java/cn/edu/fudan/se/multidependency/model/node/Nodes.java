package cn.edu.fudan.se.multidependency.model.node;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.StringUtils;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.git.Branch;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class Nodes implements Serializable {

    private static final long serialVersionUID = 164758520858214494L;

    private List<Node> allNodes = new ArrayList<>();

    private Map<NodeLabelType, List<Node>> nodeTypeToNodes = new ConcurrentHashMap<>();

    /**
     * 每个项目内的节点的entityId是唯一的
     */
    private Map<Project, Map<NodeLabelType, Map<Long, Node>>> projectToNodes = new ConcurrentHashMap<>();

    private List<Project> projects = new ArrayList<>();

    private Map<String, ProjectFile> filePathToFile = new ConcurrentHashMap<>();
    
    private Map<Project, Map<String, Package>> directoryPathToPackageInProject = new ConcurrentHashMap<>();
    
    private Map<String, Map<Integer, CodeNode>> nodeInFileByEndLine = new ConcurrentHashMap<>();
    
    private Map<String, Map<Integer, List<CodeNode>>> fileContainsNodesSortByLineCache = new ConcurrentHashMap<>();
    
    public synchronized void putNodeToFileByEndLine(ProjectFile file, CodeNode node) {
    	Map<Integer, CodeNode> functions = nodeInFileByEndLine.getOrDefault(file.getPath(), new ConcurrentHashMap<>());
    	if(node.getEndLine() > 0) {
    		functions.put(node.getEndLine(), node);
    	}
    	nodeInFileByEndLine.put(file.getPath(), functions);
    	fileContainsNodesSortByLineCache.clear();
    }
    
    public static final int FILE_NODES_WITH_LINE_PAGE = 300;
    
    /**
     * 按节点开始行数的顺序获取文件的所有节点（Type、Function）
     * 每300行存放一个Node的集合，加速查询
     * @param file
     * @return
     */
    public Map<Integer, List<CodeNode>> fileContainsNodesSortByLine(ProjectFile file) {
    	if(fileContainsNodesSortByLineCache.get(file.getPath()) != null) {
    		return fileContainsNodesSortByLineCache.get(file.getPath());
    	}
    	// 获取该文件下的所有节点，按节点所在的开始行数排序
    	Map<Integer, CodeNode> nodesMap = nodeInFileByEndLine.getOrDefault(file.getPath(), new ConcurrentHashMap<>());
    	List<CodeNode> nodes = new ArrayList<>(nodesMap.values());
    	nodes.sort((node1, node2) ->
                (int)(node1.getEndLine() - node2.getEndLine())
        );
    	Map<Integer, List<CodeNode>> result = new ConcurrentHashMap<>();

//        nodes.forEach( codeNode -> {
//            result.put(codeNode.getEndLine(),codeNode);
//                }
//        );
//
    	for(CodeNode node : nodes) {
    		int endLine = node.getEndLine();
    		int page = endLine / FILE_NODES_WITH_LINE_PAGE;
            List<CodeNode> nodesByPage = null;

    		if(result.size() <= page) {
    		    for (int i =0; i < page - result.size(); i++){
                    nodesByPage = new ArrayList<>();
                    result.put(page-1-i,nodesByPage);
                }
    			nodesByPage = new ArrayList<>();
    			result.put(page,nodesByPage);
    		} else {
    			nodesByPage = result.get(page);
    		}
    		nodesByPage.add(node);
    	}
    	fileContainsNodesSortByLineCache.put(file.getPath(), result);
    	return result;
    }
    
    public CodeNode findNodeByEndLineInFile(ProjectFile file, int endLine) {
    	Map<Integer, CodeNode> nodes = nodeInFileByEndLine.get(file.getPath());
    	if(nodes == null) {
    		return null;
    	}
    	return nodes.get(endLine);
    }

    public CodeNode findFatherNodeByLineInFile(ProjectFile file, int startLine, int endLine) {
        Map<Integer, List<CodeNode>> nodes = fileContainsNodesSortByLine(file);
        if(nodes == null) {
            return null;
        }
        int pageKey = endLine / FILE_NODES_WITH_LINE_PAGE;

        CodeNode codeNode = null;
        boolean isFound = false;
        while (!isFound && pageKey <= nodes.size()-1) {
            List<CodeNode> codeNodes = nodes.get(pageKey);
            if(codeNodes == null || codeNodes.isEmpty()){
                pageKey++;
                continue;
            }
            for (CodeNode node : codeNodes) {
                int line1 = node.getStartLine();
                int line2 = node.getEndLine();
                if (startLine >= line1 && endLine <= line2) {
                    codeNode = node;
                    isFound = true;
                    break;
                }
            }
            pageKey++;
        }
        return codeNode;
    }
	
	/*private Map<String, CodeNode> identifierToCodeNode = new ConcurrentHashMap<>();
	
	public void addCodeNode(CodeNode node) {
		this.identifierToCodeNode.put(node.getIdentifier(), node);
	}*/
	
	/*public CodeNode findCodeNodeByIdentifier(String identifier) {
		return identifierToCodeNode.get(identifier);
	}*/

    public ProjectFile findFileByPathRecursion(String path) {
        ProjectFile result = filePathToFile.get(path);
        String newPath = path;
        while (result == null) {
            newPath = FileUtil.extractNextPath(newPath);
            if (StringUtils.isBlank(newPath)) {
                return null;
            }
            result = filePathToFile.get(newPath);
        }
        return result;
    }

    public List<Project> findAllProjects() {
        return new ArrayList<>(projects);
    }

    public synchronized Project findProject(String name, Language language) {
        if (language == null) {
            return null;
        }
        for (Project project : projects) {
            if (project.getName().equals(name) && project.getLanguage().equals(language.toString())) {
                return project;
            }
        }
        return null;
    }

    public synchronized List<Project> findProject(String name) {
        List<Project> pjs = new ArrayList<>();
        for (Project project : projects) {
            if (project.getName().equals(name) ) {
                pjs.add(project);
            }
        }
        return pjs;
    }

    private void clearCache() {
        this.allLibrariesCache.clear();
    	this.fileContainsNodesSortByLineCache.clear();
    }

    public void clear() {
        allNodes.clear();
        nodeTypeToNodes.clear();
        projectToNodes.clear();
        projects.clear();
        librariesWithAPI.clear();
        filePathToFile.clear();
        directoryPathToPackageInProject.clear();
    }

    public Map<NodeLabelType, List<Node>> getAllNodes() {
        return new ConcurrentHashMap<>(nodeTypeToNodes);
    }

    public int size() {
        return allNodes.size();
    }

    /**
     * 表示该节点属于哪个Project，Project可以为null
     *
     * @param node
     * @param inProject
     * @return
     */
    public synchronized void addNode(Node node, Project inProject) {
        clearCache();

        allNodes.add(node);
        if (node instanceof Project) {
            projects.add((Project) node);
        }
        if(node instanceof Package) {
        	Map<String, Package> directoryPathToPackage = directoryPathToPackageInProject.getOrDefault(inProject, new ConcurrentHashMap<>());
        	directoryPathToPackage.put(((Package) node).getDirectoryPath(), (Package) node);
        	this.directoryPathToPackageInProject.put(inProject, directoryPathToPackage);
        }
        if (node instanceof ProjectFile) {
            this.filePathToFile.put(((ProjectFile) node).getPath(), (ProjectFile) node);
        }
        List<Node> nodes = nodeTypeToNodes.getOrDefault(node.getNodeType(), new ArrayList<>());
        nodes.add(node);
        nodeTypeToNodes.put(node.getNodeType(), nodes);

        if (inProject != null && projects.contains(inProject)) {
            Map<NodeLabelType, Map<Long, Node>> projectHasNodes = projectToNodes.getOrDefault(inProject, new ConcurrentHashMap<>());
            Map<Long, Node> entityIdToNode = projectHasNodes.getOrDefault(node.getNodeType(), new ConcurrentHashMap<>());
            entityIdToNode.put(node.getEntityId(), node);
            projectHasNodes.put(node.getNodeType(), entityIdToNode);
            projectToNodes.put(inProject, projectHasNodes);
        }
    }

    /**
     * 尚需测试
     * @TODO
     * @param node
     * @param inProject
     */
    public synchronized void deleteNode(Node node, Project inProject) {
        clearCache();

        allNodes.remove(node);
        if (node instanceof Project) {
            projects.remove((Project) node);
        }
        if(node instanceof Package) {
            Map<String, Package> directoryPathToPackage = directoryPathToPackageInProject.getOrDefault(inProject, new ConcurrentHashMap<>());
            if(directoryPathToPackage.containsKey(((Package) node).getDirectoryPath())){
                directoryPathToPackage.remove(((Package) node).getDirectoryPath(), (Package) node);
                this.directoryPathToPackageInProject.put(inProject, directoryPathToPackage);
            }
        }
        if (node instanceof ProjectFile ) {
            this.filePathToFile.remove(((ProjectFile) node).getPath(), (ProjectFile) node);
        }
        List<Node> nodes = nodeTypeToNodes.getOrDefault(node.getNodeType(), new ArrayList<>());
        if (nodes.contains(node)){
            nodes.remove(node);
            nodeTypeToNodes.put(node.getNodeType(), nodes);
        }

        if (inProject != null && projects.contains(inProject)) {
            Map<NodeLabelType, Map<Long, Node>> projectHasNodes = projectToNodes.getOrDefault(inProject, new ConcurrentHashMap<>());
            Map<Long, Node> entityIdToNode = projectHasNodes.getOrDefault(node.getNodeType(), new ConcurrentHashMap<>());
            if(entityIdToNode.containsKey(node.getEntityId())){
                entityIdToNode.remove(node.getEntityId(), node);
                projectHasNodes.put(node.getNodeType(), entityIdToNode);
                projectToNodes.put(inProject, projectHasNodes);
            }
        }
    }

    private Map<Library, Map<String, LibraryAPI>> librariesWithAPI = new ConcurrentHashMap<>();

    public synchronized void addLibraryAPINode(LibraryAPI api, Library belongToLibrary) {
        Library lib = findLibrary(belongToLibrary.getGroupId(), belongToLibrary.getName(), belongToLibrary.getVersion());
        if (lib == null) {
            return;
        }
        Map<String, LibraryAPI> apis = librariesWithAPI.getOrDefault(lib, new ConcurrentHashMap<>());
        apis.put(api.getName(), api);
        librariesWithAPI.put(lib, apis);
        this.addNode(api, null);
    }

    public List<? extends Node> findNodesByNodeType(NodeLabelType nodeType) {
        return nodeTypeToNodes.getOrDefault(nodeType, new ArrayList<>());
    }

    public Node findNodeByEntityIdInProject(NodeLabelType nodeType, long entityId, Project inProject) {
        Map<Long, ? extends Node> nodes = findNodesByNodeTypeInProject(nodeType, inProject);
        if (nodes.get(entityId) != null) {
            return nodes.get(entityId).getNodeType() == nodeType ? nodes.get(entityId) : null;
        }
        return null;
    }

    public Node findNodeByEntityIdInProject(long entityId, Project inProject) {
        Map<NodeLabelType, Map<Long, Node>> typeToNodes = projectToNodes.get(inProject);
        if (typeToNodes == null) {
            return null;
        }
        for (Map<Long, Node> entityIdToNode : typeToNodes.values()) {
            if (entityIdToNode.get(entityId) != null) {
                return entityIdToNode.get(entityId);
            }
        }
        return null;
    }
    
    public Map<String, Package> findPackagesInProject(Project project) {
    	return this.directoryPathToPackageInProject.getOrDefault(project, new HashMap<>());
    }

    /**
     * 在给定project中查找指定节点类型的所有节点
     * entity : node
     * 不建议使用该方法获取Project包含的Package节点，应使用 Map<String, Package> findPackagesInProject(Project project)
     *
     * @param nodeType
     * @param inProject
     * @return
     */
    public Map<Long, ? extends Node> findNodesByNodeTypeInProject(NodeLabelType nodeType, Project inProject) {
        Map<NodeLabelType, Map<Long, Node>> projectHasNodes = projectToNodes.getOrDefault(inProject, new ConcurrentHashMap<>());
        return projectHasNodes.getOrDefault(nodeType, new ConcurrentHashMap<>());
    }

    /**
     * 某节点是否存在
     *
     * @param node
     * @return
     */
    public boolean existNode(Node node) {
        return allNodes.contains(node);
    }

    public Map<String, List<Function>> findFunctionsInProject(Project project) {
        Map<String, List<Function>> result = new ConcurrentHashMap<>();
        if (project == null) {
            return result;
        }
        @SuppressWarnings("unchecked")
        Map<Long, Function> functions = (Map<Long, Function>) findNodesByNodeTypeInProject(NodeLabelType.Function, project);
        functions.values().forEach(function -> {
            String functionName = function.getName();
            List<Function> fs = result.getOrDefault(functionName, new ArrayList<>());
            fs.add(function);
            result.put(functionName, fs);
        });

        return result;
    }

    public Package findPackageByDirectoryPath(String directoryPath, Project project) {
        /*@SuppressWarnings("unchecked")
        Map<Long, Package> packages = (Map<Long, Package>) findNodesByNodeTypeInProject(NodeLabelType.Package, project);
        for (Package pck : packages.values()) {
            if (pck.getDirectoryPath().equals(directoryPath)) {
                return pck;
            }
        }
        return null;*/
    	return this.directoryPathToPackageInProject.getOrDefault(project, new HashMap<>()).get(directoryPath);
    }

    public Map<Integer, Scenario> findScenarios() {
        Map<Integer, Scenario> scenarios = new ConcurrentHashMap<>();
        findNodesByNodeType(NodeLabelType.Scenario).forEach(node -> {
            Scenario scenario = (Scenario) node;
            scenarios.put(scenario.getScenarioId(), scenario);
        });
        return scenarios;
    }

    public Map<Integer, TestCase> findTestCases() {
        Map<Integer, TestCase> features = new ConcurrentHashMap<>();
        findNodesByNodeType(NodeLabelType.TestCase).forEach(node -> {
            TestCase testcase = (TestCase) node;
            features.put(testcase.getTestCaseId(), testcase);
        });
        return features;
    }

    /**
     * featureId to feature
     *
     * @return
     */
    public Map<Integer, Feature> findFeatures() {
        Map<Integer, Feature> features = new ConcurrentHashMap<>();
        findNodesByNodeType(NodeLabelType.Feature).forEach(node -> {
            Feature feature = (Feature) node;
            features.put(feature.getFeatureId(), feature);
        });
        return features;
    }

    /**
     * traceId to trace
     *
     * @return
     */
    public Map<String, Trace> findTraces() {
        Map<String, Trace> traces = new ConcurrentHashMap<>();
        findNodesByNodeType(NodeLabelType.Trace).forEach(node -> {
            Trace trace = (Trace) node;
            traces.put(trace.getTraceId(), trace);
        });
        return traces;
    }

    public MicroService findMicroServiceByName(String name) {
        synchronized (this) {
            for (Node node : findNodesByNodeType(NodeLabelType.MicroService)) {
                MicroService temp = (MicroService) node;
                if (name.equals(temp.getName())) {
                    return temp;
                }
            }
            return null;
        }
    }

    public RestfulAPI findRestfulAPIByProjectAndSimpleFunctionName(Project project, String simpleFunctionName) {
        Map<Long, ? extends Node> nodes = findNodesByNodeTypeInProject(NodeLabelType.RestfulAPI, project);
        for (Node node : nodes.values()) {
            RestfulAPI api = (RestfulAPI) node;
            if (simpleFunctionName.equals(api.getApiFunctionSimpleName())) {
                return api;
            }
        }
        return null;
    }

    public RestfulAPI findMicroServiceAPIByAPIFunction(String apiFunctionName) {
        for (Node node : findNodesByNodeType(NodeLabelType.RestfulAPI)) {
            RestfulAPI api = (RestfulAPI) node;
            if (apiFunctionName.equals(api.getApiFunctionName())) {
                return api;
            }
        }
        return null;
    }

    public Span findSpanBySpanId(String spanId) {
        for (Node node : findNodesByNodeType(NodeLabelType.Span)) {
            Span span = (Span) node;
            if (spanId.equals(span.getSpanId())) {
                return span;
            }
        }
        return null;
    }

    public synchronized Commit findCommitByCommitId(String commitId) {
        for (Node node : findNodesByNodeType(NodeLabelType.Commit)) {
            Commit commit = (Commit) node;
            if (commitId.equals(commit.getCommitId())) {
                return commit;
            }
        }
        return null;
    }

    public synchronized Branch findBranchByBranchId(String branchId) {
        for (Node node : findNodesByNodeType(NodeLabelType.Branch)) {
            Branch branch = (Branch) node;
            if (branchId.equals(branch.getBranchId())) {
                return branch;
            }
        }
        return null;
    }

    public synchronized Developer findDeveloperByName(String name) {
        for (Node node : findNodesByNodeType(NodeLabelType.Developer)) {
            Developer developer = (Developer) node;
            if (name.equals(developer.getName())) {
                return developer;
            }
        }
        return null;
    }

	/*public ProjectFile findFileByPath(String path){
		for(Node node : findNodesByNodeType(NodeLabelType.ProjectFile)){
			ProjectFile file = (ProjectFile) node;
			if(file.getPath().endsWith(path)) {
				return file;
			}
		}
		return null;
	}*/

    private Map<String, List<Library>> allLibrariesCache = new ConcurrentHashMap<>();

    public Map<String, List<Library>> findAllLibraries() {
        if (allLibrariesCache.isEmpty()) {
            for (Node node : findNodesByNodeType(NodeLabelType.Library)) {
                Library library = (Library) node;
                List<Library> libraries = allLibrariesCache.getOrDefault(library.getGroupId(), new ArrayList<>());
                libraries.add(library);
                allLibrariesCache.put(library.getGroupId(), libraries);
            }
        }
        return new ConcurrentHashMap<>(allLibrariesCache);
    }

    public Library findLibrary(String group, String name, String version) {
        Iterable<Library> libraries = findAllLibraries().getOrDefault(group, new ArrayList<>());
        for (Library library : libraries) {
            if (library.getName().equals(name) && library.getVersion().equals(version)) {
                return library;
            }
        }
        return null;
    }

    public LibraryAPI findLibraryAPIInLibraryByAPIName(String apiName, Library belongToLibrary) {
        return librariesWithAPI.getOrDefault(belongToLibrary, new ConcurrentHashMap<>()).get(apiName);
    }

}
