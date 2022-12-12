package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.CodeUnit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_IMPLEMENTS)
@EqualsAndHashCode
public class Implements implements StructureRelation {
	
	private static final long serialVersionUID = 7582417525375943056L;

	@Id
    @GeneratedValue
    private Long id;
	
	public Implements(CodeUnit codeUnit, CodeUnit implementsCodeUnit) {
		this.startNode = codeUnit;
		this.endNode = implementsCodeUnit;
	}
	
	/**
	 * Type
	 */
	@StartNode
	private CodeUnit startNode;
	
	/**
	 * Type
	 */
	@EndNode
	private CodeUnit endNode;

	@Override
	public CodeNode getStartCodeNode() {
		return startNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return endNode;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.IMPLEMENTS;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
	
}
