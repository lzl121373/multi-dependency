package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.CodeUnit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_IMPLEMENTS_C)
@EqualsAndHashCode
public class ImplementsC implements StructureRelation {

	private static final long serialVersionUID = -2276258703466523731L;

	@Id
    @GeneratedValue
    private Long id;

	public ImplementsC(CodeUnit codeUnit, CodeUnit implementsCodeUnit) {
		this.startNode = codeUnit;
		this.endNode = implementsCodeUnit;
	}
	
	/**
	 * Function
	 */
	@StartNode
	private CodeUnit startNode;
	
	/**
	 * Function
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
		return RelationType.IMPLEMENTS_C;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
	
}
