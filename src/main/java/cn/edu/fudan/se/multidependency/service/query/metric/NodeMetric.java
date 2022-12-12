package cn.edu.fudan.se.multidependency.service.query.metric;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@QueryResult
public class NodeMetric<T extends Node> {
	
	private T node;
	
	private Metric metric;
}
