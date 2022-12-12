package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.Collection;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.data.HistogramWithProjectsSize;

public interface CloneShowService {

	JSONObject clonesGroupsToCytoscape(Collection<CloneGroup> groups, boolean showGroupNode, boolean singleLanguage);
	
	Collection<HistogramWithProjectsSize> withProjectsSizeToHistogram(Collection<CloneGroup> groups, boolean singleLanguage);
	
	JSONArray graphFileClones(Collection<Clone> clones);
	JSONArray graphFileCloneGroups(Collection<CloneGroup> groups);
	
	
	JSONObject crossPackageCloneToCytoscape(Collection<CloneValueForDoubleNodes<Package>> cloneDoublePackages);
}
