package cn.edu.fudan.se.multidependency.service.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.Language;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

@Service
public class CacheService {

	private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();
	
	public void remove(Class<?> cls, String key) {
		if(cache.get(className(cls)) != null) {
			cache.get(className(cls)).remove(key);
		}
	}
	
	public void remove(Class<?> cls) {
		cache.remove(className(cls));
	}
	
	public void cache(Class<?> cls, String key, Object data) {
		Map<String, Object> temp = cache.getOrDefault(className(cls), new ConcurrentHashMap<>());
		temp.put(key, data);
		cache.put(className(cls), temp);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<?> cls, String key) {
		Map<String, Object> temp = cache.getOrDefault(className(cls), new ConcurrentHashMap<>());
		return (T) (temp.get(key));
	}
	
	private String className(Class<?> cls) {
		return cls.getName();
	}
	
	private final Map<String, ProjectFile> pathToFile = new ConcurrentHashMap<>();
	private final Map<String, Map<String, Package>> directoryToPackage = new ConcurrentHashMap<>();
	private final Map<Long, Node> idToNodeCache = new ConcurrentHashMap<>();
	private final Map<Node, Map<NodeLabelType, Node>> nodeBelongToNodeCache = new ConcurrentHashMap<>();
	
    public void clearCache() {
    	idToNodeCache.clear();
    	nodeBelongToNodeCache.clear();
    	pathToFile.clear();
    }
    
    public void cacheNodeBelongToNode(Node node, Node belongToNode) {
    	Map<NodeLabelType, Node> belongToNodes = nodeBelongToNodeCache.getOrDefault(node, new ConcurrentHashMap<>());
    	if(belongToNode == null) {
    		return ;
    	}
    	belongToNodes.put(belongToNode.getNodeType(), belongToNode);
    	this.nodeBelongToNodeCache.put(node, belongToNodes);
    	cacheNodeById(node);
    	cacheNodeById(belongToNode);
    }
    
    public Node findNodeBelongToNode(Node node, NodeLabelType label) {
    	cacheNodeById(node);
    	Map<NodeLabelType, Node> belongToNodes = nodeBelongToNodeCache.get(node);
    	return belongToNodes == null ? null : belongToNodes.get(label);
    }
	
	public Node findNodeById(long id) {
		return idToNodeCache.get(id);
	}
	
	public Node cacheNodeById(Node node) {
		if(node == null) {
			return null;
		}
		idToNodeCache.put(node.getId(), node);
		if(node instanceof ProjectFile) {
//			this.pathToFile.put(((ProjectFile) node).getPath(), (ProjectFile) node);
			cacheFileByPath(((ProjectFile) node).getPath(), (ProjectFile) node);
		} else if(node instanceof Package) {
			String language = ((Package) node).getLanguage();
			if (language == null){
				language = Language.java.name();
			}
			cachePackageByDirectory(((Package) node).getDirectoryPath(), language, (Package) node);
		}
		return node;
	}
	
	public void cacheFileByPath(String path, ProjectFile file) {
		this.pathToFile.put(path, file);
	}
	
	public ProjectFile findFileByPath(String path) {
		return this.pathToFile.get(path);
	}
	
	public void cachePackageByDirectory(String directory, String language, Package pck) {
		Map<String, Package> packageMap = this.directoryToPackage.getOrDefault(language, new ConcurrentHashMap<>());
		packageMap.put(directory, pck);
		this.directoryToPackage.put(language, packageMap);
	}

	@Deprecated
	public Package findPackageByDirectoryPath(String directoryPath, String language) {
		return this.directoryToPackage.get(language).get(directoryPath);
	}
	
}
