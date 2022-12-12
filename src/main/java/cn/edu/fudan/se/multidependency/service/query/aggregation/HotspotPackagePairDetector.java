package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface HotspotPackagePairDetector {

	List<HotspotPackagePair> detectHotspotPackagePairs();

	Map<String, List<DependsOn>> detectHotspotPackagePairWithDependsOn();

	List<HotspotPackagePair> getHotspotPackagePairWithDependsOn();

	List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnByProjectId(long projectId);

	HotspotPackagePair detectHotspotPackagePairWithDependsOnByPackageId(long pck1Id, long pck2Id);

	List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnInAllProjects();

	Map<String, List<CoChange>> detectHotspotPackagePairWithCoChange();

	List<HotspotPackagePair> getHotspotPackagePairWithCoChange();

	List<HotspotPackagePair> detectHotspotPackagePairWithCoChangeByProjectId(long projectId);

	HotspotPackagePair detectHotspotPackagePairWithCoChangeByPackageId(long pck1Id, long pck2Id);

	List<HotspotPackagePair> detectHotspotPackagePairWithCoChangeInAllProjects();

	List<HotspotPackagePair> detectHotspotPackagePairWithFileClone();

	HotspotPackagePair getHotspotPackagePairWithFileCloneByPackageId(long pck1Id, long pck2Id, String language);

	List<HotspotPackagePair> getHotspotPackagePairWithFileCloneByParentId(long parent1Id, long parent2Id, String language);

	void exportHotspotPackages(OutputStream stream);
}
