package cn.edu.fudan.se.multidependency.service.query.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import lombok.Getter;
import lombok.Setter;

public class PackageGraph {

	@Setter
	@Getter
	private List<Package> packages = new ArrayList<>();
	
	@Setter
	@Getter
	private Map<Package, Project> pckBelongToProject = new HashMap<>();
	
	public void add(Package pck, Project project) {
		pckBelongToProject.put(pck, project);
	}

	@Setter
	@Getter
	private Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> packageCloneValues = new HashMap<>();
	
	public JSONObject changeToEChartsGraph() {
		JSONObject result = new JSONObject();
		result.put("type", "force");
		JSONArray packagesArray = new JSONArray();
		JSONArray lengendsArray = new JSONArray();
		lengendsArray.add("package");
		for(Package pck : packages) {
			JSONObject pckJson = new JSONObject();
			pckJson.put("name", pck.getDirectoryPath());
			pckJson.put("id", pck.getId().toString());
			packagesArray.add(pckJson);
		}
		result.put("nodes", packagesArray);
		result.put("legend", lengendsArray);
		JSONArray linksArray = new JSONArray();
		for(Map.Entry<Package, Map<Package, CloneValueForDoubleNodes<Package>>> entry1 : packageCloneValues.entrySet()) {
			Package pck1 = entry1.getKey();
			for(Map.Entry<Package, CloneValueForDoubleNodes<Package>> entry2 : entry1.getValue().entrySet()) {
				Package pck2 = entry2.getKey();
				CloneValueForDoubleNodes<Package> cloneValue = entry2.getValue();
				JSONObject linkJson = new JSONObject();
				linkJson.put("source", pck1.getId().toString());
				linkJson.put("target", pck2.getId().toString());
				linksArray.add(linkJson);
			}
		}
		result.put("links", linksArray);
		return result;
	}
	
	public JSONObject changeToEChartsGraph1() {
		JSONObject result = new JSONObject();
		result.put("type", "force");
		JSONArray packagesArray = new JSONArray();
		JSONArray categoriesArray = new JSONArray();
		JSONArray lengendsArray = new JSONArray();
		
		Map<Project, Integer> projectContain = new HashMap<>();
		int i = 0;
		for(Package pck : packages) {
			Project project = pckBelongToProject.get(pck);
			Integer index = projectContain.get(project);
			if(index == null) {
				JSONObject projectCategory = new JSONObject();
				String projectName = new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString();
				projectCategory.put("name", projectName);
				projectCategory.put("keyword", new JSONObject());
				projectCategory.put("vase", projectName);
				categoriesArray.add(projectCategory);
				lengendsArray.add(projectName);
			}
			index = i++;
			projectContain.put(project, index);
			
			JSONObject pckJson = new JSONObject();
			pckJson.put("name", pck.getDirectoryPath());
			pckJson.put("id", pck.getId().toString());
			pckJson.put("category", index);
			packagesArray.add(pckJson);
		}
		result.put("nodes", packagesArray);
		result.put("legend", lengendsArray);
		result.put("categories", categoriesArray);
		JSONArray linksArray = new JSONArray();
		for(Map.Entry<Package, Map<Package, CloneValueForDoubleNodes<Package>>> entry1 : packageCloneValues.entrySet()) {
			Package pck1 = entry1.getKey();
			for(Map.Entry<Package, CloneValueForDoubleNodes<Package>> entry2 : entry1.getValue().entrySet()) {
				Package pck2 = entry2.getKey();
				CloneValueForDoubleNodes<Package> cloneValue = entry2.getValue();
				JSONObject linkJson = new JSONObject();
				linkJson.put("source", pck1.getId().toString());
				linkJson.put("target", pck2.getId().toString());
				linksArray.add(linkJson);
			}
		}
		result.put("links", linksArray);
		return result;
	}
	
	
}
