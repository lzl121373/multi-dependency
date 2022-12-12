package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_LOCAL_VARIABLE)
@EqualsAndHashCode
public class LocalVariable implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = -4473341521680611765L;

	@StartNode
	private CodeNode startCodeNode;

	@EndNode
	private CodeNode endCodeNode;

	private int times = 1;

	public LocalVariable(CodeNode startCodeNode, CodeNode endCodeNode) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 1;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public CodeNode getStartCodeNode() {
		return startCodeNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return endCodeNode;
	}
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.LOCAL_VARIABLE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
}
