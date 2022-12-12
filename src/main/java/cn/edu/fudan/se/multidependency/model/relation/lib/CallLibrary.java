package cn.edu.fudan.se.multidependency.model.relation.lib;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import lombok.Data;

/**
 * 
 * @author fan
 * 一个节点调用了哪些三方库，分别调用了三方库的什么API
 */
@Data
public class CallLibrary<T extends Node> implements Serializable {

	private static final long serialVersionUID = -8204021236638982630L;

	private T caller;
	
	private Set<Library> callLibraries = new HashSet<>();
	
	private Map<Library, Set<LibraryAPI>> callLibraryToAPIs = new HashMap<>();

	private Map<LibraryAPI, Integer> callAPITimes = new HashMap<>();
	
	public int timesOfCallAPI(LibraryAPI api) {
		return callAPITimes.getOrDefault(api, 0);
	}
	
	public int timesOfCallLib(Library lib) {
		int result = 0;
		Set<LibraryAPI> apis = callLibraryToAPIs.getOrDefault(lib, new HashSet<>());
		for(LibraryAPI api : apis) {
			result += timesOfCallAPI(api);
		}
		return result;
	}
	
	public void addLibraryAPI(LibraryAPI api, Library belongToLibrary, int times) {
		callLibraries.add(belongToLibrary);
		
		Set<LibraryAPI> apis = callLibraryToAPIs.getOrDefault(api, new HashSet<>());
		apis.add(api);
		this.callLibraryToAPIs.put(belongToLibrary, apis);
		
		Integer previousTimes = callAPITimes.getOrDefault(apis, 0);
		callAPITimes.put(api, previousTimes + times);
	}
	
	public NodeLabelType getCallerType() {
		return caller.getNodeType();
	}
	
}
