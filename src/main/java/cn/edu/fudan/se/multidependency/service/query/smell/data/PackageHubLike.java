package cn.edu.fudan.se.multidependency.service.query.smell.data;

import org.springframework.data.neo4j.annotation.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import cn.edu.fudan.se.multidependency.model.node.Package;

@Data
@AllArgsConstructor
@QueryResult
public class PackageHubLike {
	
	private Package pck;
	
	private long fanOut;
	
	private long fanIn;
	
}
