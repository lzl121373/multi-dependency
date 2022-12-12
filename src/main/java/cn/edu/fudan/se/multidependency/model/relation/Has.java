package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_HAS)
public class Has implements Relation {

	private static final long serialVersionUID = -483821275966593371L;

	@Id
    @GeneratedValue
    private Long id;
	
	public Has(Node start, Node end) {
		super();
		this.start = start;
		this.end = end;
	}

	@StartNode
	private Node start;
	
	@EndNode
	private Node end;

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
		return RelationType.HAS;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
