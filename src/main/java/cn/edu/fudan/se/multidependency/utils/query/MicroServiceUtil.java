package cn.edu.fudan.se.multidependency.utils.query;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

public class MicroServiceUtil {

	public static boolean isMicroServiceCall(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls) {
		return msCalls == null ? false : msCalls.getOrDefault(start, new HashMap<>()).get(end) != null;
	}

	public static boolean isMicroServiceDependOn(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns) {
		return msDependOns == null ? false : msDependOns.getOrDefault(start, new HashMap<>()).get(end) != null;
	}
}
