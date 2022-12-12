package cn.edu.fudan.se.multidependency.service.query.smell.data;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@QueryResult
public class ModuleHubLike {
	
	private Module module;
	
	private long fanOut;
	
	private long fanIn;
	
}
