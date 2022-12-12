package cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_MICRO_SERVICE_CREATE_SPAN)
public class MicroServiceCreateSpan implements Relation {

	private static final long serialVersionUID = -7559932764276563718L;
	
	public MicroServiceCreateSpan(MicroService microService, Span span) {
		this.microservice = microService;
		this.span = span;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private MicroService microservice;
	
	@EndNode
	private Span span;

	@Override
	public Node getStartNode() {
		return microservice;
	}

	@Override
	public Node getEndNode() {
		return span;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.MICRO_SERVICE_CREATE_SPAN;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
