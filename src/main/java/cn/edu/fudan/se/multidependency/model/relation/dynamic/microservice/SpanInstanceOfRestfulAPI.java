package cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SPAN_INSTANCE_OF_RESTFUL_API)
public class SpanInstanceOfRestfulAPI implements Relation {
	private static final long serialVersionUID = 5928117416854260539L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Span span;
	
	@EndNode
	private RestfulAPI api;
	
//	private Integer testCaseId;

	@Override
	public Node getStartNode() {
		return span;
	}

	@Override
	public Node getEndNode() {
		return api;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SPAN_INSTANCE_OF_RESTFUL_API;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
//		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		return properties;
	}

}
