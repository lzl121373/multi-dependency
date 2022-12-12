package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_EXTENDS)
public class Extends implements StructureRelation {
	
	private static final long serialVersionUID = 3740594031088738257L;

	@Id
    @GeneratedValue
    private Long id;
	
	public Extends(Type start, Type end) {
		super();
		this.start = start;
		this.end = end;
	}

	@StartNode
	private Type start;
	
	@EndNode
	private Type end;

	@Override
	public CodeNode getStartCodeNode() {
		return start;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return end;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.EXTENDS;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
}
