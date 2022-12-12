package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import org.springframework.web.bind.annotation.RequestParam;

public interface ProjectService {
	String getAbsolutePath(Project project);

	void setAbsolutePath(Project project, String path);

	JSONArray getPackageContainJson(List<PackageStructure> childrenPackages,String type);

	JSONArray getPackageContainJsonCombo(List<PackageStructure> childrenPackages);

	JSONArray getMultipleProjectsGraphJson(JSONObject dataList, String type);

//	JSONObject cloneGraphAndTableOfChildrenPackages(long package1Id, long package2Id);

	JSONObject getAllProjectsLinks();

	JSONArray getAllProjectsLinksCombo(JSONArray projectIds);

	Map<String, Boolean> getSelectedPcks();

	void setSelectedPcks(Map<String, Boolean> pcks);

	boolean clearSelectedPcks();
}