package cn.edu.fudan.se.multidependency.model.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_RELATE_TO)
public class RelateTo implements Relation {

	private static final long serialVersionUID = 8915775225187198834L;

	@Id
    @GeneratedValue
    private Long id;

	public RelateTo(Node start, Node end) {
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
		return RelationType.RELATE_TO;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
