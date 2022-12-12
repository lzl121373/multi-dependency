package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_IMPLLINK)
@EqualsAndHashCode
public class ImplLink implements StructureRelation {
	private static final long serialVersionUID = -5543957038003318L;

	@Id
    @GeneratedValue
    private Long id;
	
	public ImplLink(CodeNode function, Function impllinkFunction) {
		this.function = function;
		this.impllinkFunction = impllinkFunction;
	}
	
	@StartNode
	private CodeNode function;
	
	@EndNode
	private Function impllinkFunction;

	@Override
	public CodeNode getStartCodeNode() {
		return function;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return impllinkFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.IMPLLINK;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
