package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.CyclicHierarchy;

public interface CyclicHierarchyDetector {

	Map<Long, List<CyclicHierarchy>> cyclicHierarchies();
	
}
