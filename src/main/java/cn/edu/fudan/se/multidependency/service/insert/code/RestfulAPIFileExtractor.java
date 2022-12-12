package cn.edu.fudan.se.multidependency.service.insert.code;

import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;

public interface RestfulAPIFileExtractor {
	
	Iterable<RestfulAPI> extract();
	
}
