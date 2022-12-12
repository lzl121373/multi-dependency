package cn.edu.fudan.se.multidependency.model.relation.structure.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_MICROSERVICE_DEPEND_ON_MICROSERVICE)
public class MicroServiceDependOnMicroService implements Relation {
	private static final long serialVersionUID = 823195437213702237L;

	public MicroServiceDependOnMicroService(MicroService start, MicroService end) {
		this.start = start;
		this.end = end;
	}
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private MicroService start;
	
	@EndNode
	private MicroService end;
	
	@Override
	public Node getStartNode() {
		return start;
	}

	@Override
	public Node getEndNode() {
		return end;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.MICROSERVICE_DEPEND_ON_MICROSERVICE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
