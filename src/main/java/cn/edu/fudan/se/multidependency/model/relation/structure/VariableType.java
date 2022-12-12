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
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_VARIABLE_TYPE)
@Data
@NoArgsConstructor
public class VariableType implements StructureRelation {
	private static final long serialVersionUID = 1767344862220786333L;
	@Id
    @GeneratedValue
    private Long id;

	public VariableType(Variable variable, Type type) {
		super();
		this.variable = variable;
		this.type = type;
	}

	@StartNode
	private Variable variable;
	
	@EndNode
	private Type type;

	@Override
	public CodeNode getStartCodeNode() {
		return variable;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return type;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.VARIABLE_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
